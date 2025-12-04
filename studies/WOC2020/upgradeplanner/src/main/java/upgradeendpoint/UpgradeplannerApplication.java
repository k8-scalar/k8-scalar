package upgradeendpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@EnableScheduling //Needed for the scheduled checkConstraints function
@SpringBootApplication
@Controller
public class UpgradeplannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpgradeplannerApplication.class, args);
    }

    @GetMapping("/")
    @ResponseBody
    public String welcome() {
        return "index";
    }


}
