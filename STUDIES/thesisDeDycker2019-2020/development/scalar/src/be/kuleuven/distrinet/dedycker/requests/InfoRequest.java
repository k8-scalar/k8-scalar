package be.kuleuven.distrinet.dedycker.requests;

import be.kuleuven.distrinet.dedycker.ApplicationInterface;
import be.kuleuven.distrinet.dedycker.ApplicationRequestResult;
import be.kuleuven.distrinet.dedycker.tenants.Tenant;
import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.requests.Request;
import be.kuleuven.distrinet.scalar.requests.RequestResult;

public class InfoRequest extends ApplicationRequest {

    public InfoRequest(Tenant tenant) {
        super(tenant, true, "info");
    }


    public void doRequest() throws RequestException {
        Tenant tenant = ((Tenant) user());
        ApplicationInterface applicationInterface = tenant.getAppInterface();

        startTimer();
        ApplicationRequestResult result = applicationInterface.info(tenant.getHttpClient(), tenant.getName());
        stopTimer();

        if (result != null) {
            if (result.statusCode >= 200 && result.statusCode <= 299) done(RequestResult.SUCCEEDED);
            else done(RequestResult.FAILED);
        } else done(RequestResult.NO_RESULT);


    }

}
