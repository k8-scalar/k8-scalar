package upgradeplanner;

import datamanagement.DatabaseDeployment;
import datamanagement.DatabaseTenantConfiguration;
import datatypes.Deployment;
import datatypes.Status;
import datatypes.TenantConfig;
import datatypes.TenantGroup;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1beta2Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class ActivationController {
    public static ActivationController actController;

    boolean readyCheck = true;
    Status status = Status.getInstance();

    private ActivationController() {

    }

    public static ActivationController getInstance() {
        if (actController == null) {
            actController = new ActivationController();
        }
        return actController;
    }

    //region Activation of Upgrades
    /*
    This function starts the activation of the Upgrade.
     */
    public void update(TenantGroup grp) {
        try {
            UpgradePlanner upgradePlanner = UpgradePlanner.getInstance();
            activateControllers(grp);
            readyCheck = true;
            upgradePlanner.startActivation();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    public boolean isReady() {
        return readyCheck;
    }

    public void setReady(boolean bool) {
        readyCheck = bool;
    }

    /*
    This function will activate and manage the upgrade.
    It does this by first upgrading the new version and waiting for it to finish
    Then downgrading the old version and waiting for it to finish
    Once this is all done it will update the DBs to be consistent with current state
    and it will call the upgradePlanner to update the group sizes.
     */
    private boolean activateControllers(TenantGroup grp) {
        // TODO hieronder verplaatsen naar extra functie die lijst overloopt wanneer die niet leeg is.
        // Blijkbaar volgt dat JSON patch http://jsonpatch.com/ standard dus dit zouden we kunnen gebruiken om shit aan te passen.
        // https://github.com/kubernetes/kubernetes/issues/68861 Voorbeeld hier lager in
        try {
            status.addStatus(LocalTime.now() + "- START OF UPGRADE PROCESS FOR " + grp.getTenantList() + " from " + grp.getCurrVersion() + " to " + grp.getUpgradeVersion());
            int grpSize = grp.getTenantList().size();
            TenantConfig tmpConf = grp.getTenantList().get(0);

            System.out.println("Group size: " + grpSize + " and the stack :" + grp.getTenantList());
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("+++++++ " + tmpConf.getAppName() + " to version " +
                    tmpConf.getUpgVersion() + " from version " + tmpConf.getCurrVersion() + " +++++++");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");

            // Deployments from the TenantGroup
            grp.updateGroupDeployments(); // make sure the deployments are completely up to date.
            Deployment currDep = grp.getCurrDep();
            System.out.println("CurrDep: " + currDep.toString());
            // Deployment currDep = deploymentDB.getData(grp.getAppName(), grp.getCurrVersion(), grp.getSlaClass()); // Make sure that latest Deployment data is used
            String currDeploymentName = currDep.getDeploymentName();
            Deployment upgDep = grp.getUpgDep();
            System.out.println("UpgDep: " + upgDep.toString());
            // Deployment upgDep = deploymentDB.getData(grp.getAppName(), grp.getUpgradeVersion(), grp.getSlaClass()); // Make sure that latest Deployment data is used
            String upgDeploymentName = upgDep.getDeploymentName();

            // Kubernetes API setup
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
            AppsV1beta2Api api = new AppsV1beta2Api();

            // Upgrade the new version and patch amount of replica's
            status.addStatus(LocalTime.now() + "- START: Upgrade to new version " + grp.getUpgradeVersion() + " for " + grp.getTenantList());
            upgradeDeployment(upgDep, upgDeploymentName, grpSize, api);
            TimeUnit.SECONDS.sleep(2);
            podsReady("UpdateV2", grp);
            status.addStatus(LocalTime.now() + "- COMPLETION: Upgrade to new version " + grp.getUpgradeVersion() + " for " + grp.getTenantList());

            // IDLE IN BETWEEN FASE: Notify the application that tenants are switching versions
            System.out.println("Start call to MT-API");
            callMtApi(grp);
            TimeUnit.SECONDS.sleep(5);

            // Downgrade the currVersion
            status.addStatus(LocalTime.now() + "- START: Downgrade of old version " + grp.getCurrVersion() + " for " + grp.getTenantList());
            downgradeDeployment(currDep, currDeploymentName, grpSize, api);
            TimeUnit.SECONDS.sleep(2);
            podsReady("UpdateV1", grp);
            status.addStatus(LocalTime.now() + "- COMPLETION: Downgrade of old version " + grp.getCurrVersion() + " for " + grp.getTenantList());
            TimeUnit.SECONDS.sleep(1);
            // region Update DBs
            // Update the tenantDB and the Deployment DB
            updateDeploymentDB(currDeploymentName, upgDeploymentName);

            // Voor elke tenant in de group moet dit gebeuren.
            updateTenantDB(grp);

            //endregion
            status.addStatus(LocalTime.now() + "- END OF UPGRADE PROCESS FOR " + grp.getTenantList());
            return true;
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return false;
        }
    }

    private void callMtApi(TenantGroup grp) {
        final String uri = "http://bsv1:80/admin/tenants/version";
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("tenant-id", "admin");
        JSONObject tenantJson = new JSONObject();
        JSONArray tenantArray = new JSONArray();
        for (TenantConfig cfg : grp.getTenantList()) {
            tenantArray.add(cfg.getTenantId());
        }
        System.out.println(tenantArray);
        tenantJson.put("names", tenantArray);
        tenantJson.put("version", "v" + grp.getUpgradeVersion());
        HttpEntity<String> entity = new HttpEntity<String>(tenantJson.toJSONString(), headers);
        System.out.println(tenantJson.toJSONString());
        System.out.println("httpEntity: " + entity);
        System.out.println("Sending the Object");
        String response = template.postForObject(uri, entity, String.class);
        System.out.println("Response: " + response);
    }
    //endregion

    //region Upgrade process
    public void podsReady(String phase, TenantGroup grp) {
        // Variables that are important for function flow
        boolean readyFlag = false;
        Boolean deletionFlag = false;
        int correctReady = 0;

        while (!readyFlag) {
            System.out.println("In ready check loop");
            try {
                // API configuration
                ApiClient client = ClientBuilder.cluster().build();
                Configuration.setDefaultApiClient(client);
                CoreV1Api coreApi = new CoreV1Api();
                V1PodList defPodList = coreApi.listNamespacedPod("default", null, null, null, null, null, null, null, null, null);

                // Every second this resets
                int podNotReadyCounter = 0;

                // For every Pod in the "default" namespace check this
                for (V1Pod pod : defPodList.getItems()) {
                    // is the Ready Condition present and is it set to true, this indicates that Kubernetes API thinks that it can handle traffic
                    boolean readyPresent = pod.getStatus().getConditions().stream().filter(o -> o.getType().equals("Ready") && o.getStatus().equals("True")).findFirst().isPresent();
                    // is a deletion timestamp present? this indicates that termination has started
                    boolean deletionStamp = pod.getMetadata().getDeletionTimestamp() != null;

                    // If the Pod is not ready add to notReadyCounter
                    if (!readyPresent) {
                        podNotReadyCounter++;
                    }
                    // If a deletionTimestamp is set then add to notReadyCounter and set deletionFlag to true
                    if (deletionStamp) {
                        podNotReadyCounter++;
                        // If deletionFlag is either true or false then change it. This is done make sure that the callMtApi is only executed once
                        if (deletionFlag != null) {
                            deletionFlag = true;
                        }
                    }
                }

                // If deletionFlag is set then termination has begun. This means that the switch of tenants is possible
                // TODO kan verplaatst worden naar juist onder deze functie om het toch op basis
                if (deletionFlag != null && phase.equals("UpdateV2")) {
                    deletionFlag = null;
                    status.addStatus(LocalTime.now() + "- TERMINATION has started for " + phase + " - Tenant " + grp.getTenantList() + " will be swapped to the new version");
                    TimeUnit.SECONDS.sleep(2);
                    callMtApi(grp);
                }

                // If the PodNotReadyCounter is greater than zero it means that (a) Ready != True or (b) Termination is still going on
                if (podNotReadyCounter > 0) {
                    readyFlag = false;
                    correctReady = 0;
                } else {
                    correctReady++;
                }

                // If correctReady is >= 2 than Pods are Ready, this means that twice in a row all the checks passed
                if (correctReady >= 2) {
                    readyFlag = true;
                    correctReady = 0;
                    System.out.println("Pods ready for " + phase);
                }
                TimeUnit.SECONDS.sleep(1);

            } catch (ApiException e) {
                System.err.println("PodsReady - Exception when calling AppsV1beta2Api#listDeploymentForAllNamespaces");
                System.err.println("Status code: " + e.getCode());
                System.err.println("Reason: " + e.getResponseBody());
                System.err.println("Response headers: " + e.getResponseHeaders());
                System.err.println("Full message: " + e.getMessage());
                e.printStackTrace();
            } catch (NullPointerException | IOException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Pods are ready, on to the next");
    }

    public void upgradeDeployment(Deployment upgDep, String upgDeploymentName, int grpSize, AppsV1beta2Api api) {
        try {
            //region Upgrade UpgDeployment
            // Requests and Limits per tenant for the UpgradedVersion
            int upgRequests = upgDep.getPodRequests() + (upgDep.getTenantRequests() * grpSize);
            int upgLimits = upgDep.getPodLimits() + (upgDep.getTenantLimits() * grpSize);

            CoreV1Api coreApi = new CoreV1Api();
            V1NodeList nodeList = coreApi.listNode(null, null, null, null, "deployable=true", null, null, null, null);

            // Adjust the amount of replicas to spread around the amount of nodes matching the label
            int numberNodes = nodeList.getItems().size();

            V1Patch patchLimitsRequests = new V1Patch(
                    "[{\"op\":\"replace\",\"path\":\"/spec/template/spec/containers/0/resources/requests/cpu\",\"value\":\"" + upgRequests + "m\"}" +
                            ", {\"op\":\"replace\",\"path\":\"/spec/template/spec/containers/0/resources/limits/cpu\",\"value\":\"" + upgLimits + "m\"}" +
                            ", {\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":" + numberNodes + "}]");

            System.out.println("------");
            System.out.println("Updating the Upgrade deployment " + upgDeploymentName + "\nTO request = " + upgRequests + " and limits = " +
                    upgLimits + " \nFROM original value request = " + upgDep.getPodRequests() + " and limits = " +
                    upgDep.getPodLimits());
            System.out.println("------");

            api.patchNamespacedDeployment(
                    upgDeploymentName, "default", patchLimitsRequests, null, null, null, null);

            // Sleep the amount of time it takes to upgrade
            System.out.println("Upgrade Time is : " + upgDep.getUpgradeTime() + " from " + LocalTime.now());
            //endregion
        } catch (ApiException e) {
            System.err.println("upgradeDeployment - Exception when calling AppsV1beta2Api#listDeploymentForAllNamespaces");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Full message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public void downgradeDeployment(Deployment currDep, String currDeploymentName, int grpSize, AppsV1beta2Api api) {
        try {
            // region Downgrade currDeployment
            // Requests and Limits per tenant for the CurrentVersion
            int currRequests = currDep.getPodRequests() - (currDep.getTenantRequests() * grpSize);
            int currLimits = currDep.getPodLimits() - (currDep.getTenantLimits() * grpSize);

            V1Patch patchLimitsRequests = new V1Patch(
                    "[{\"op\":\"replace\",\"path\":\"/spec/template/spec/containers/0/resources/requests/cpu\",\"value\":\""
                            + currRequests + "m\"}, {\"op\":\"replace\",\"path\":\"/spec/template/spec/containers/0/resources/limits/cpu\",\"value\":\""
                            + currLimits + "m\"}]");

            System.out.println("------");
            System.out.println("Updating the current deployment " + currDeploymentName + " \nTO request = " + currRequests + " and limits = " +
                    currLimits + " \nFROM original value request = " + currDep.getPodRequests() + " and limits = " +
                    currDep.getPodLimits());
            System.out.println("------");
            // Requests first because downgrading
            api.patchNamespacedDeployment(
                    currDeploymentName, "default", patchLimitsRequests, null, null, null, null);

            System.out.println("Downgrade Time is : " + currDep.getUpgradeTime() + " from " + LocalTime.now());
            //endregion
        } catch (ApiException e) {
            System.err.println("Exception when calling AppsV1beta2Api#listDeploymentForAllNamespaces");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Full message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
    //endregion

    //region DB consistency
    public void updateDeploymentDB(String currDepName, String upgDepName) {
        DatabaseDeployment deploymentDB = DatabaseDeployment.getInstance();
        deploymentDB.addData(currDepName);
        deploymentDB.addData(upgDepName);
    }

    public void updateTenantDB(TenantGroup grp) {
        DatabaseTenantConfiguration tenantDB = DatabaseTenantConfiguration.getInstance();
        for (TenantConfig cfg : grp.getTenantList()) {
            tenantDB.updateData(new TenantConfig(cfg.getTenantId(), cfg.getAppName(), cfg.getSlaClass(), cfg.getUpgVersion(), null, null));
        }
    }
    //endregion
}
