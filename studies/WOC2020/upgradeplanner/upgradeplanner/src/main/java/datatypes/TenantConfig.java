package datatypes;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/* Is the configuration send by a Tenant to represent themselves in the database.
   If the upgVersion and deadline != null then it will be used for upgrading the tenant.
 */
public class TenantConfig implements Serializable {

    int tenantId;
    String appName;
    String slaClass;
    String currVersion;
    String upgVersion;
    LocalTime deadline;

    public TenantConfig(int tenantId, String appName, String slaClass, String currVersion, String upgVersion, LocalTime deadline) {
        this.tenantId = tenantId;
        this.appName = appName;
        this.slaClass = slaClass;
        this.currVersion = currVersion;
        this.upgVersion = upgVersion;
        this.deadline = deadline;
        System.out.println(this.toString());
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getAppName() {
        return appName;
    }

    public String getSlaClass() {
        return slaClass;
    }

    public String getCurrVersion() {
        return currVersion;
    }

    public String getUpgVersion() {
        return upgVersion;
    }

    public LocalTime getDeadline() {
        return deadline;
    }


    public LocalTime setDeadline(String dline) {
        if (dline == null) {
            return null;
        } else {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(dline, fmt);
            boolean isBefore = LocalTime.now().isBefore(time.plus(20, ChronoUnit.MINUTES));
            boolean isAfter = LocalTime.now().isAfter(time.minus(20, ChronoUnit.MINUTES));
            System.out.println("LocalTime.now() is before " + time + " +20mins ? " + isBefore);
            System.out.println("LocalTime.now() is after " + time + " +20mins ? " + isAfter);
            if (isBefore && isAfter) {
                // do nothing
            } else {
                time = time.minus(2, ChronoUnit.HOURS);
            }
            System.out.println("LocalTime = " + time);
            return time;
        }
    }

    @Override
    public String toString() {
        return "TenantConfig{" +
                "tenantId=" + tenantId +
                ", app='" + appName + '\'' +
                ", slaClass='" + slaClass + '\'' +
                ", currVersion='" + currVersion + '\'' +
                ", newVersion='" + upgVersion + '\'' +
                ", deadLine=" + deadline +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantConfig that = (TenantConfig) o;
        return tenantId == that.tenantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId);
    }
}
