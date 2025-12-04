package datatypes;

/* Represents a Kubernetes Deployment with a few mandatory labels:
 * - app, represents the application group that the Deployment belongs to
 * - version, represents the version of the application running in the Deployment
 * - requests, represents the amount of Requests needed per extra tenant
 * - limits, represents the amount of Limits needed per extra tenant
 * - upgradeTime, represents the time needed to upgrade this Deployment (startup of new Pod)
 * - downgradeTime, represents the time needed to downgrade this deployment (startup of new Pod)
 * */
// TODO ja dus upgrade en downgrade time zijn mogelijk wel gewoon exact hetzelfde, dus mogelijk hernoemen naar startupTime.
public class Deployment {
    String deploymentName, appName, version, slaClass;
    int podCpuRequests, podCpuLimits, tenantRequests, tenantLimits, upgradeTime, podMemRequests, podMemLimits;

    public Deployment(String deploymentName, String appName, String version, String slaClass, int podCpuRequests,
            int podCpuLimits, int podMemRequests, int podMemLimits, int tenantRequests, int tenantLimits,
            int upgradeTime) {
        this.deploymentName = deploymentName;
        this.appName = appName;
        this.version = version;
        this.slaClass = slaClass;
        this.podCpuRequests = podCpuRequests;
        this.podCpuLimits = podCpuLimits;
        this.podMemLimits = podMemLimits;
        this.podMemRequests = podMemRequests;
        this.tenantRequests = tenantRequests;
        this.tenantLimits = tenantLimits;
        this.upgradeTime = upgradeTime;
        System.out.println(this.toString());
    }

    public String getVersion() {
        return version;
    }

    public String getAppName() {
        return appName;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public String getSlaClass() {
        return slaClass;
    }

    public int getTenantRequests() {
        return tenantRequests;
    }

    public int getTenantLimits() {
        return tenantLimits;
    }

    public int getPodCpuRequests() {
        return podCpuRequests;
    }

    public int getPodCpuLimits() {
        return podCpuLimits;
    }

    public int getUpgradeTime() {
        return upgradeTime;
    }

    public int getPodMemLimits() {
        return podMemLimits;
    }

    public int getPodMemRequests() {
        return podMemRequests;
    }

    @Override
    public String toString() {
        return "Deployment{" +
                "deploymentName='" + deploymentName + '\'' +
                ", appName='" + appName + '\'' +
                ", version='" + version + '\'' +
                ", podCpuRequests=" + podCpuRequests +
                ", podCpuLimits=" + podCpuLimits +
                ", podMemRequests=" + podMemRequests +
                ", podMemLimits=" + podMemLimits +
                ", tenantRequests=" + tenantRequests +
                ", tenantLimits=" + tenantLimits +
                ", upgradeTime=" + upgradeTime +
                '}';
    }
}
