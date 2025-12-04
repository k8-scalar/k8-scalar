package upgradeendpoint;


import datamanagement.DatabaseDeployment;
import datatypes.Deployment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class DeploymentController {

    DatabaseDeployment dbupgrade = DatabaseDeployment.getInstance();
    // DEVELOPER STUFF

    @GetMapping("/deployment")
    @ResponseStatus(HttpStatus.OK)
    public Deployment getDeployment(@RequestParam(value = "app") String app, @RequestParam(value = "version") String version, @RequestParam(value = "slaClass") String slaClass) {
        return dbupgrade.getData(app, version, slaClass);
    }

    @PostMapping(path = "/deployment")
    @ResponseStatus(HttpStatus.OK)
    public Deployment PushDeploymentData(@RequestParam(value = "deployment-name") String name) {
        Deployment dep = dbupgrade.addData(name);
        return dep;
    }
}
