package be.kuleuven.distrinet.dedycker.requests;

import be.kuleuven.distrinet.dedycker.ApplicationInterface;
import be.kuleuven.distrinet.dedycker.ApplicationRequestResult;
import be.kuleuven.distrinet.dedycker.tenants.Tenant;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.requests.Request;
import be.kuleuven.distrinet.scalar.requests.RequestResult;
import org.apache.http.Header;

import java.util.Arrays;

public class LoginRequest extends ApplicationRequest {

    public LoginRequest(Tenant tenant) {
        super(tenant, true, "login");
    }


    public void doRequest() throws RequestException {
        Tenant tenant = ((Tenant) user());
        ApplicationInterface applicationInterface = tenant.getAppInterface();

        startTimer();
        ApplicationRequestResult result = applicationInterface.login(tenant.getHttpClient(), tenant.getName(), tenant.username(), tenant.password());
        stopTimer();

        if (result != null) {
            if (result.statusCode >= 200 && result.statusCode <= 299){
                Header token = Arrays.stream(result.headers)
                        .filter(header -> header.getName().equals(ApplicationInterface.AUTH_HEADER))
                        .findFirst()
                        .orElse(null);
                if(token == null || token.getValue().isEmpty()){
                    System.out.println("### LoginRequest failed, reason: No token header.");
                    done(RequestResult.FAILED);
                } else {
                    tenant.setJWT(token.getValue());
                    done(RequestResult.SUCCEEDED);
                }
            }
            else done(RequestResult.FAILED);
        } else done(RequestResult.NO_RESULT);


    }

}