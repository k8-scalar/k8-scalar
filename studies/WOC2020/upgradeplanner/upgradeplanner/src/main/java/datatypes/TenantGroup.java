package datatypes;

import datamanagement.DatabaseDeployment;
import upgradeplanner.ActivationController;
import upgradeplanner.UpgradePlanner;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* This class represents a group of tenant configurations that will upgrade in group.
 * Tenant configs are added to a group if they have the same currVersion, UpgradeVersion and requests.
 * */

public class TenantGroup {
    Deployment currDep, upgDep;
    List<TenantConfig> tenantList = new ArrayList<>();
    String appName, slaClass, currVersion, upgradeVersion;
    Integer maxGroupSize = null;
    LocalTime deadline;
    ActivationController subscriber = ActivationController.getInstance();
    UpgradePlanner planner = UpgradePlanner.getInstance();
    ScheduledExecutorService executorService;
    Status status = Status.getInstance();

    public TenantGroup(TenantConfig tenantConf) {
        tenantList.add(tenantConf);
        currVersion = tenantConf.getCurrVersion();
        upgradeVersion = tenantConf.getUpgVersion();
        slaClass = tenantConf.getSlaClass();
        appName = tenantConf.getAppName();
        deadline = tenantConf.getDeadline();

        // Get the rest of the data from the database
        upgDep = DatabaseDeployment.getInstance().getData(appName, upgradeVersion, slaClass); // Looks up the requests
        // Get the current deployment from the database
        currDep = DatabaseDeployment.getInstance().getData(appName, currVersion, slaClass); // Looks up the current deployment name
        if (upgDep == null || currDep == null) {
            stopScheduler();
            planner.removeGroup(this);
        }

        System.out.println(this.toString());
    }

    public boolean isMaxSize() {
        return maxGroupSize == tenantList.size();
    }

    public void updateGroupDeployments() {
        upgDep = DatabaseDeployment.getInstance().getData(appName, upgradeVersion, slaClass);
        currDep = DatabaseDeployment.getInstance().getData(appName, currVersion, slaClass);
    }

    public void startScheduler() {
        // Schedule checkConstraints every 1s
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::checkConstraints, 0, 4, TimeUnit.SECONDS);
    }

    public void stopScheduler() {
        executorService.shutdown();
    }

    /*
    This function will be ran at a set period. It will check if the constraints regarding size or deadline are met.
    If they are the ActivationController will be called and send this group as parameter.
    This group will also be removed from the planner, so that no new TenantConfigs can be added.
    */
    public void checkConstraints() {
        try {
            LocalTime currentTime = LocalTime.now();
            LocalTime prevPeriod = currentTime.minus(20, ChronoUnit.SECONDS);
            //LocalTime diffServerWorld = deadline.minus(currentTime.getHour(), ChronoUnit.HOURS); // DIT ZOU TIMEZONE DEPENDANT MOETEN ZIJN EN TEVEEL WERK
            LocalTime deadlineInServerTime;

            boolean isBefore = LocalTime.now().isBefore(deadline.plus(20, ChronoUnit.MINUTES));
            boolean isAfter = LocalTime.now().isAfter(deadline.minus(20, ChronoUnit.MINUTES));
            if (isBefore && isAfter) {
                deadlineInServerTime = deadline;
            } else {
                deadlineInServerTime = deadline.minus(2, ChronoUnit.HOURS);
            }
            System.out.println("Current time = " + currentTime + " and period time " + prevPeriod + " and formated deadline = " + deadlineInServerTime);

            if (tenantList.size() == maxGroupSize
                    || (deadlineInServerTime.isAfter(prevPeriod)) && deadlineInServerTime.isBefore(currentTime)) {
                planner.addToActivation(this);
            }

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    // This method adds a configuration if it's not a duplicate.
    public void addTenantConfig(TenantConfig tenConf) {
        // If it's a duplicate, then update the configuration with the new one
        int index = tenantList.indexOf(tenConf);
        if (index == -1) {
            tenantList.add(tenConf);
        } else {
            tenantList.remove(tenConf);
            tenantList.add(tenConf);
        }


        System.out.println("Tenant Group");
        // Earliest Deadline First
        LocalTime edf = null;
        for (TenantConfig conf : tenantList) {
            if (edf == null) {
                edf = conf.getDeadline();
            }
            if (conf.getDeadline().isBefore(edf)) {
                edf = conf.getDeadline();
            }
            System.out.println(conf);
        }
        setDeadline(edf);
    }

    public List<TenantConfig> getTenantList() {
        return tenantList;
    }


    public String getAppName() {
        return appName;
    }

    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setMaxGroupSize(int grpSize) {
        int diff = tenantList.size() - grpSize;
        if (this.maxGroupSize == null || grpSize > 0) {
            this.maxGroupSize = grpSize;
        } else if (grpSize <= 0) {
            status.addStatus("Group size is smaller than 0 and makes upgrading impossible");
            planner.removeGroup(this);
        } else if (diff > 0) {
            // Add all these extra configs to the list
            List<TenantConfig> configsList = new ArrayList<>();
            planner.removeGroup(this);
            for (int i = 0; i <= diff; i++) {
                TenantConfig conf = tenantList.remove(tenantList.size() - 1);
                configsList.add(conf);
            }
            // add al these removed configurations to the UpgradePlanner
            for (TenantConfig c : configsList) {
                String newDeadline = c.getDeadline().plus(5, ChronoUnit.MINUTES).toString();
                c.setDeadline(newDeadline);
                planner.addToPlanning(c);
            }
        }
    }

    public String getSlaClass() {
        return slaClass;
    }

    public String getCurrVersion() {
        return currVersion;
    }

    public String getUpgradeVersion() {
        return upgradeVersion;
    }

    public Deployment getCurrDep() {
        return currDep;
    }

    public Deployment getUpgDep() {
        return upgDep;
    }

    public void setDeadline(LocalTime deadline) {
        this.deadline = deadline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantGroup that = (TenantGroup) o;
        return deadline.equals(that.deadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline);
    }

    @Override
    public String toString() {
        return "TenantGroup{" +
                "currDep=" + currDep +
                ", upgDep=" + upgDep +
                ", tenantList=" + tenantList +
                ", appName='" + appName + '\'' +
                ", currVersion='" + currVersion + '\'' +
                ", upgradeVersion='" + upgradeVersion + '\'' +
                ", maxGroupSize=" + maxGroupSize +
                ", subscriber=" + subscriber +
                '}';
    }
}
