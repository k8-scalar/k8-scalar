package upgradeendpoint;

import datamanagement.DatabaseTenantConfiguration;
import datatypes.Status;
import datatypes.TenantConfig;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import upgradeplanner.UpgradePlanner;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TenantConfigurationController {
    DatabaseTenantConfiguration dbtenant = DatabaseTenantConfiguration.getInstance();
    UpgradePlanner planner = UpgradePlanner.getInstance();
    Status status = Status.getInstance();

    // TENANT STUFF

    /* Returns a specific tenant configuration from the database. */
    @GetMapping(path = "/tenantconfig")
    @ResponseStatus(HttpStatus.OK)
    public TenantConfig getTenantConfig(@RequestParam(value = "tenantID") int id) {
        return dbtenant.getData(id);
    }


    @PostMapping(path = "/tenantconfig", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<TenantConfig> pushTenantConfigs(@RequestBody List<TenantConfig> upgConfigs) {
        for (TenantConfig cfg : upgConfigs) {
            if (dbtenant.checkIfValid(cfg)) {
                dbtenant.addData(cfg);
                if (cfg.getDeadline() == null || cfg.getUpgVersion() == null) {
                    status.addStatus("Configuration from tenant " + cfg.getTenantId() + " is invalid because either deadline or upgVersion is null");
                } else {
                    planner.addToPlanning(cfg);
                }
            }
        }
        return upgConfigs;

    }

    /* Is used to initially load all the tenants into the DB */
    @PostMapping(path = "/tenants", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<TenantConfig> pushTenants(@RequestBody List<TenantConfig> tenants) {
        for (TenantConfig tConf : tenants) {
            dbtenant.addData(tConf);
        }
        return tenants;
    }

    @GetMapping(path = "/tenants")
    @ResponseStatus(HttpStatus.OK)
    public List<TenantConfig> getTenants() {
        List<TenantConfig> list = new ArrayList<>();
        int size = dbtenant.getNumTenants();
        for (int i = 1; i < size + 1; i++) {
            list.add(dbtenant.getData(i));
        }
        return list;
    }

}
