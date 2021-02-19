package upgradeplanner;

import datamanagement.DatabaseTenantConfiguration;
import datatypes.Deployment;
import datatypes.Status;
import datatypes.TenantConfig;
import datatypes.TenantGroup;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpgradePlanner {
    // Class will control groups of tenants.
    // Activate on earliest deadline or max size
    // Need to check this activation using Observer? Deadline observing or max size observing?

    public static UpgradePlanner upgradePlanner;
    public List<TenantGroup> upgradeList = new ArrayList<>();
    Status status = Status.getInstance();
    ActivationController activationController = ActivationController.getInstance();
    private List<TenantGroup> groups = new ArrayList<>();

    public UpgradePlanner() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::startActivation, 0, 2, TimeUnit.SECONDS);
    }

    public static UpgradePlanner getInstance() {
        if (upgradePlanner == null) {
            upgradePlanner = new UpgradePlanner();
        }
        return upgradePlanner;
    }

    //region Group management
    /*
    This function adds a TenantConfig to either (a) new group if none match or (b) a matching group.
    A group matches when TenantRequests, CurrVersion and UpgVersion are equal.
    The MaxSize of a group is decided here by calling the sizeAlgorithm() and is set for the group.
    This also done when a new TenantConfig is added to be sure that it is as recent as possible.
    When a group is created for the first time the scheduler that checks the constraints is also started.
     */
    public void addToPlanning(TenantConfig conf) {
        System.out.println("---------------------------------------------------");
        TenantGroup tmpGrp = new TenantGroup(conf); // Maak een temporary TenantGroup om te vergelijken.
        int index = groups.indexOf(tmpGrp);
        if (index == -1) {
            System.out.println("No matching groups are found. Add this group to the List");
            status.addStatus("Tenant " + conf.getTenantId() + " created a new group");
            addNewGroup(tmpGrp);
        } else {
            System.out.println("Matching group is found");
            matchingGroup(tmpGrp, conf);
        }
        System.out.println("---------------------------------------------------");
    }

    public TenantGroup matchingGroup(TenantGroup tmpGrp, TenantConfig conf) {
        List<TenantGroup> filteredGrp = getPredicateGroup(tmpGrp);
        boolean noGroup = true;
        for (TenantGroup foundGrp : filteredGrp) {
            if (foundGrp.isMaxSize()) {
                System.out.println("Group is at max size");
            } else {
                noGroup = false;
                status.addStatus("Tenant " + conf.getTenantId() + " was added to a group");
                foundGrp.addTenantConfig(conf);
            }
        }
        if (noGroup) {
            status.addStatus("Matching group(s) at max size");
            addNewGroup(tmpGrp);
            status.addStatus("Tenant " + conf.getTenantId() + " created a new group");
        }
        return null;
    }

    private List<TenantGroup> getPredicateGroup(TenantGroup tmpGrp) {
        List<TenantGroup> l = new ArrayList<>();
        for (TenantGroup grp : groups) {
            if (grp.equals(tmpGrp)) {
                l.add(grp);
            }
        }
        // Return the null
        return l;
    }

    public void addNewGroup(TenantGroup grp) {
        int grpSize = sizeAlgorithm(grp);
        grp.setMaxGroupSize(grpSize);
        grp.startScheduler();
        groups.add(grp);
    }

    public void updateGroups() {
        try {
            for (TenantGroup grp : groups) {
                int grpSize = sizeAlgorithm(grp);
                grp.setMaxGroupSize(grpSize);
                grp.updateGroupDeployments();
            }
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

    }

    public void removeGroup(TenantGroup grp) {
        System.out.println("RemoveGroup called");
        grp.stopScheduler();
        groups.remove(grp);
        System.out.println("RemoveGroup finished: " + groups.toString());
    }

    //endregion

    //region Activation Management
    public void addToActivation(TenantGroup grp) {
        // Stop the scheduler of the group that is ready to activate
        // Add group to an updateList
        grp.stopScheduler(); //want the group to still be accessible before upgrade
        upgradeList.add(grp);
    }

    public void startActivation() {
        // If the ActivationController has no current job && the upgradeList is nonempty
        if (activationController.isReady() && !upgradeList.isEmpty()) {
            // Make sure that no other group gets planned by setting the controller to busy
            activationController.setReady(false);
            // Update the groups to be sure that the Deployments are up-to-date
            updateGroups();
            // Get the first group
            TenantGroup grp = upgradeList.get(0);
            // Remove from both groups
            groups.remove(grp);
            upgradeList.remove(grp);
            // Start the update
            activationController.update(grp);
        }
    }
    //endregion

    /* Algorithm decides the optimal group size*/
    public int sizeAlgorithm(TenantGroup grp) {
        // Get the current version, upgradeVersion and requests from the TenantGroup.
        String currV = grp.getCurrVersion();
        String upgradeV = grp.getUpgradeVersion();
        String app = grp.getAppName();
        double currReqs = grp.getCurrDep().getTenantRequests();
        double upgReqs = grp.getUpgDep().getTenantRequests(); // Leave this double because the formule divides in double values

        // Get the highest free value for the resource. This is the allocatable space for the Pods.
        System.out.println("------");
        int maxNode = getFreeResource("cpu", grp.getCurrDep(), grp.getUpgDep());
        ;
        System.out.println("++ The value according to the calculations is = " + maxNode);
        System.out.println("------");

        // Determine the amount of tenants per version
        DatabaseTenantConfiguration db = DatabaseTenantConfiguration.getInstance();
        int tenCurrV = db.getNumTenantsVersion(currV, app);
        int tenUpgV = db.getNumTenantsVersion(upgradeV, app);
        System.out.println("------");
        System.out.println("- Calculate the amount of tenants per group per phase");

        /*
        Calculate the amount of tenants that can be fitted according to fase 1 of the upgrade proces, this fase corresponds
        with upgrading version p+i.
            (Rvp * Tvp) + (Rvpi * Tvpi) + (Rvpi * (Tvpi + x) <= maxNode
            Rvpi*x <= maxNode - (Rvp * Tvp) - (Rvpi * Tvpi) - (Rvpi * Tvpi)
            x <= (maxNode - (Rvp * Tvp) - (Rvpi * Tvpi) - (Rvpi * Tvpi)) / Rvpi
        */
        double sizePhase1 = (maxNode - (currReqs * tenCurrV) - (upgReqs * tenUpgV) - (upgReqs * tenUpgV)) / upgReqs;
        System.out.println("Size for phase1 = " + sizePhase1 + " with UpgRequest = " + upgReqs + " and CurrReqs = " + currReqs + ", maxNode = " + maxNode +
                ", #TOldV = " + tenCurrV + " and #TNewV = " + tenUpgV);

        /*
        Calculate the amount of tenants that can be fitted according to fase 2 of the upgrade proces, this face corresponds
        with upgrading version p
        if (Rvp == Rvpi)
            (Rvp * Tvp) + (Rvpi * (Tvpi + x)) + (Rvp * (Tvp - x)) <= maxNode
             Rvpi*x - Rvp*x <= maxNode - (Rvp * Tvp) - (Rvpi * Tvpi) - (Rvp * Tvp)
             0 <= maxNode - (Rvp * Tvp) - (Rvpi * Tvpi) - (Rvp * Tvp)
         else
            (Rvp * Tvp) + (Rvpi * (Tvpi + x)) + (Rvp * (Tvp - x)) <= maxNode
            Rvpi*x - Rvp*x <= maxNode - (Rvp * Tvp) - (Rvpi * Tvpi) - (Rvp * Tvp)
            x <= (maxNode - (Rvp * Tvp) - (Rvpi * Tvpi) - (Rvp * Tvp)) / (Rvpi - Rvp)
         */
        double sizePhase2 = 0;
        double basis = maxNode - (currReqs * tenCurrV) - (upgReqs * tenUpgV) - (currReqs * tenCurrV);
        if (upgReqs != currReqs) {
            /*
            If (upgReqs != currReqs) the sizePhase will return an amount of tenants that satisfy the preconditions
            The min(sizePhase1, sizePhase2) indicates the amount of tenants that fit in a group
             */
            sizePhase2 = basis / (upgReqs - currReqs);
            System.out.println("Size for phase2 = " + sizePhase2 + " with UpgRequest = " + upgReqs + " and CurrReqs = " + currReqs + ", maxNode = " + maxNode +
                    ", #TOldV = " + tenCurrV + " and #TNewV = " + tenUpgV);
            System.out.println("------");
            Double[] valueList = {sizePhase1, sizePhase2};
            Double max = null;
            for (Double i : valueList) {
                if (max == null | i.compareTo(max) > 0) {
                    max = i;
                }
            }
            return max.intValue();
        } else {
            /*
            If (upgReqs == currReqs) then this phase is independent from the amount of tenants. Meaning that it will hold a constant value
            As long as this constant value is > 0 the upgrade can happen with maxSize == sizePhase1
             */
            sizePhase2 = basis;
            System.out.println("UpgRequest = " + upgReqs + " is equal to currReqs = " + currReqs + " for sizePhase2 and it's value is = " + sizePhase2 +
                    " , the amount of currVtenants = " + tenCurrV + " and upgVtenants = " + tenUpgV);
            System.out.println("------");
            if (sizePhase2 > 0) {
                return (int) sizePhase1;
            } else {
                return 0;
            }
        }
    }

    //region Resource calculation
    private Integer getFreeResource(String resource, Deployment oldDep, Deployment newDep) {
        System.out.println("- Maximum Allocatable Resources per Node");
        Map<String, Integer> allocMap = maxAllocResources(resource); // Returns a list of <node-name, alloc-value> and will be passed onto Used resources to give difference.
        System.out.println("- Requested resources per Node, not these 2 deployments");
        Map<String, Integer> requestMap = requestsPerNode(resource, allocMap, oldDep, newDep);
        // Determine the diff between the maps
        Map<String, Integer> diffMap = Stream.of(allocMap, requestMap)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue, (v1, v2) -> v1 - v2)
                );

        System.out.println("- Difference Allocatable and Requested per Node");
        System.out.println(diffMap.toString());

        // Find the max value. This should be the same in our setup but this is sort of future proof.
        Integer max = null;
        for (Integer i : diffMap.values()) {
            if (max == null || i.compareTo(max) > 0) {
                max = i;
            }
        }
        return max;
    }

    private Map<String, Integer> maxAllocResources(String resource) {
        try {
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            V1NodeList nodeList = api.listNode(null, null, null, null, "deployable=true", null, null, null, null);
            Map<String, Integer> nodeMap = new HashMap<>();
            for (V1Node node : nodeList.getItems()) {
                // Check if the node is online and ready
                List<V1NodeCondition> nodeConds = node.getStatus().getConditions();
                String ready = "";
                for (V1NodeCondition cond : nodeConds) {
                    if (cond.getType().equals("Ready")) {
                        ready = cond.getStatus();
                    }
                }
                // If ready then it can be used to calculate space
                if (ready.equals("True")) {
                    Map<String, Quantity> alloc = node.getStatus().getAllocatable();
                    double value = alloc.get(resource).getNumber().doubleValue() * 1000; // set to millicore
                    nodeMap.put(node.getMetadata().getName(), (int) value);
                }
            }
            System.out.println(nodeMap.toString());
            // Sorteren of hoogste waarde bepalen hiervan. Technisch gezien is dit altijd consistent maar hier kan het altijd geplaatst worden.
            return nodeMap;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return null;
        }
    }

    private Map<String, Integer> requestsPerNode(String resource, Map<String, Integer> allocMap, Deployment oldDep, Deployment newDep) {
        // We pullen eerst alle Pods dan filteren we deze met de allocMap node names tot we per Node een lijst van Pods hebben
        // Hierna tellen we dus per Pod alle requests op.
        try {
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
            // Initialize the HashMap with nodeNames
            Map<String, List<V1Pod>> nodePodMap = new HashMap<>();
            for (String node : allocMap.keySet()) {
                nodePodMap.put(node, new ArrayList<>());
            }

            // Fill the HashMap with Pods
            for (V1Pod item : list.getItems()) {
                String nodeName = item.getSpec().getNodeName();
                if (nodePodMap.containsKey(nodeName)) {
                    List<V1Pod> podList = nodePodMap.get(nodeName);
                    podList.add(item);
                    nodePodMap.put(nodeName, podList);
                }
            }

            // Calculate sum of requests and store in Map<String,Integer>
            Map<String, Integer> requestsPerNode = new HashMap<>();
            for (String nodeName : nodePodMap.keySet()) {
                List<V1Pod> pods = nodePodMap.get(nodeName);
                double requestSum = 0;
                for (V1Pod p : pods) {
                    List<V1Container> containersList = p.getSpec().getContainers();
                    Map<String, String> labels = p.getMetadata().getLabels();
                    System.out.println(labels);
                    try {
                        if (labels.get("app").equals(oldDep.getAppName()) && labels.get("version").equals(oldDep.getVersion())) {
                            System.out.println("Skipped this one 1");
                            continue;
                        }
                        if (labels.get("app").equals(newDep.getAppName()) && labels.get("version").equals(newDep.getVersion())) {
                            System.out.println("Skipped this one 2");
                            continue;
                        }
                    } catch (NullPointerException e) {
                        // do nothing
                    }

                    for (V1Container cont : containersList) {
                        double val = (cont.getResources().getRequests() == null) ?
                                0 : cont.getResources().getRequests().get(resource).getNumber().doubleValue() * 1000; // millicores
                        requestSum += val;
                    }
                }
                requestsPerNode.put(nodeName, (int) requestSum);
            }
            System.out.println(requestsPerNode.toString());
            return requestsPerNode;
        } catch (ApiException e) {
            System.err.println("Exception when calling CoreV1API");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Exception when calling CoreV1API but not ApiException");
            System.err.println("Reason: " + e.getCause());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
