package upgradeendpoint;

import datatypes.Status;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatusController {

    public Status status = Status.getInstance();

    @GetMapping(path = "/status")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getStatusOfPlanner() {
        return status.getStatus();
    }
}
