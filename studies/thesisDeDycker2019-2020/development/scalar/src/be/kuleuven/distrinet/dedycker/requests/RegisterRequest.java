package be.kuleuven.distrinet.dedycker.requests;

import be.kuleuven.distrinet.dedycker.ApplicationInterface;
import be.kuleuven.distrinet.dedycker.ApplicationRequestResult;
import be.kuleuven.distrinet.dedycker.tenants.Tenant;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.requests.Request;
import be.kuleuven.distrinet.scalar.requests.RequestResult;
import org.apache.http.Header;

import java.util.Arrays;

public class RegisterRequest extends ApplicationRequest {

    Tenant tenant;

    public RegisterRequest(Tenant tenant) {
        super(tenant, true, "register");
    }


    public void doRequest() throws RequestException {
        Tenant tenant = ((Tenant) user());
        this.tenant = tenant;
        ApplicationInterface applicationInterface = tenant.getAppInterface();

        startTimer();
        ApplicationRequestResult result = applicationInterface.register(tenant.getHttpClient(), tenant.getName(), tenant.username(), tenant.password());
        stopTimer();

        if (result != null) {
            if (result.statusCode >= 200 && result.statusCode <= 299){
                done(RequestResult.SUCCEEDED);
            }
            else done(RequestResult.FAILED);
        } else done(RequestResult.NO_RESULT);
    }

    @Override
    public String messageOnFailure() {
        return String.format("REGISTER: [%s,%s]", tenant.username(), tenant.password());
    }
}