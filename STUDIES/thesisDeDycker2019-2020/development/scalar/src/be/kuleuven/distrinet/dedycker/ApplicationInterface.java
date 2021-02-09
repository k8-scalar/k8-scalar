package be.kuleuven.distrinet.dedycker;

import be.kuleuven.distrinet.dedycker.tenants.Tenant;
import be.kuleuven.distrinet.scalar.core.Manager;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ApplicationInterface {

    public final static String AUTH_HEADER = "authentication-token";
    public final static String TENANT_HEADER = "tenant-id";
    private static ApplicationInterface instance;

    private final String baseURL;
    private boolean secureHTTP = false;

    private ApplicationInterface(String baseURL, boolean secureHTTP){
        System.out.println("Creating interface for: " + baseURL);
        this.baseURL = baseURL;
        this.secureHTTP = secureHTTP;
    }

    /**
     * Returns an instance of the ApplicationInterface
     * @param baseURL The url of the application
     * @return Instance of ApplicationInterface
     */
    public static ApplicationInterface getInstance(String baseURL, boolean secureHTTP) {
        if(instance == null) instance = new ApplicationInterface(baseURL, secureHTTP);
        return instance;
    }

    /**
     * Creates a formatted url string
     * @param path path of the url, ex: login, request
     * @return url ex: "http://localhost:80/api/login"
     */
    public String createURLString(String path) {
        String protocol = "http";
        if(secureHTTP) protocol = "https";

        return String.format("%s://%s/%s", protocol, baseURL, path);
    }

    /**
     * Requests general information from the api.
     * @return Tuple with headers and the response converted to a string.
     */
    public ApplicationRequestResult info(CloseableHttpClient client, String tenantID) {
        final String path = "/api/info";
        HttpGet base = new HttpGet(createURLString(path));
        base.addHeader(TENANT_HEADER, tenantID);
        return executeRequest(client, base);
    }

    /**
     * Performs a request to register a user.
     * @return Tuple with headers and the response converted to a string.
     */
    public ApplicationRequestResult register(CloseableHttpClient client, String tenantID, String username, String password)  {
        final String path = "/api/register";
        HttpPost base = new HttpPost(createURLString(path));
        final String json = String.format("{\"name\":\"%s\",\"password\":\"%s\"}", username, password);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        base.setEntity(entity);
        base.addHeader(TENANT_HEADER, tenantID);
        return executeRequest(client, base);
    }

    /**
     * Performs a request to log the user in.
     * @return Tuple with headers and the response converted to a string.
     */
    public ApplicationRequestResult login(CloseableHttpClient client, String tenantID, String username, String password) {
        final String path = "/api/login";
        HttpPost base = new HttpPost(createURLString(path));
        final String json = String.format("{\"name\":\"%s\",\"password\":\"%s\"}", username, password);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        base.setEntity(entity);
        base.addHeader(TENANT_HEADER, tenantID);
        return executeRequest(client, base);
    }

    /**
     * Performs a long request.
     * @return Tuple with headers and the response converted to a string.
     */
    public ApplicationRequestResult wait(CloseableHttpClient client, String tenantID, String authToken) {
        final String path = "/api/wait";
        HttpGet base = new HttpGet(createURLString(path));
        base.addHeader(TENANT_HEADER, tenantID);
        base.addHeader(AUTH_HEADER, authToken);
        return executeRequest(client, base);
    }

    /**
     * Requests the home page affiliated with the user.
     * @return Tuple with headers and the response converted to a string.
     */
    public ApplicationRequestResult home(CloseableHttpClient client, String tenantID, String authToken) {
        final String path = "/home";
        HttpGet base = new HttpGet(createURLString(path));
        base.addHeader(TENANT_HEADER, tenantID);
        base.addHeader(AUTH_HEADER, authToken);
        return executeRequest(client, base);
    }

    /**
     * Performs a HTTP request.
     * @param client Client used to perform the request with
     * @param request The request object to eecute
     * @return Tuple with headers and the response converted to a string.
     */
    private ApplicationRequestResult executeRequest(CloseableHttpClient client, HttpRequestBase request) {

        ApplicationRequestResult requestResult = null;

        try {
            CloseableHttpResponse response = client.execute(request);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            Header[] headers = response.getAllHeaders();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
            int statusCode = statusLine.getStatusCode();
            requestResult = new ApplicationRequestResult(statusCode, headers, responseString);
        } catch (IOException e) {
            System.out.println("### Request failed, reason:");
            e.printStackTrace();
            throw new InternalException(e);
        }
        return requestResult;

    }

}
