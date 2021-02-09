package be.kuleuven.distrinet.dedycker.tenants;

import be.kuleuven.distrinet.dedycker.ApplicationInterface;
import be.kuleuven.distrinet.dedycker.requests.HomeRequest;
import be.kuleuven.distrinet.dedycker.requests.InfoRequest;
import be.kuleuven.distrinet.dedycker.requests.LoginRequest;
import be.kuleuven.distrinet.dedycker.requests.RegisterRequest;
import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public abstract class Tenant extends User {

    private String name;
    private CloseableHttpClient httpClient;
    private ApplicationInterface appInterface;
    private String authToken;
    protected boolean registration;

    public Tenant(UserPool pool) {
        super(pool);
        try {
            this.registration = pool.manager().data().getAsBoolean("tenant_registration_flag");
        } catch (DataException e) {
            this.registration = true;
        }
        boolean safeHTTP = false;
        try {
            safeHTTP = pool.manager().data().getAsBoolean("use_https");
        } catch (DataException e) {
        }
        this.httpClient = HttpClients.createDefault();
        this.appInterface = ApplicationInterface.getInstance(super.targetUrl(), safeHTTP);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void mainLoop() throws DataException {

        try {
            if (registration) new RegisterRequest(this).doRequest();
            new LoginRequest(this).doRequest();
            new InfoRequest(this).doRequest();
            new HomeRequest(this).doRequest();
        } catch (RequestException e) {
            throw new InternalException(e);
        } finally {
            requestDone();
        }

    }

    @Override
    public String username() {
        if (!registration) return getRegisteredUsername();
        else return getUsername();
    }


    public abstract String getUsername();


    @Override
    public String password() {
        if (!registration) return getRegisteredPassword();
        else return getPassword();
    }

    public String getPassword() {
        return "password";
    }


    public abstract void requestDone();

    public String getRegisteredUsername() {
        return "user-" + getName() + "-1";
    }

    public String getRegisteredPassword() {
        return "password";
    }

    /**
     * Returns the name of the tenant.
     *
     * @return String representing the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the httpclient for the tenant.
     *
     * @return HTTPClient instance
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Returns tha interface of the application
     *
     * @return this.appInterface
     */
    public ApplicationInterface getAppInterface() {
        return appInterface;
    }

    public String getJWT() {
        if (authToken == null) throw new IllegalStateException("Tenant is not authenticated.");
        else return authToken;
    }

    public void setJWT(String token) {
        this.authToken = token;
    }


}
