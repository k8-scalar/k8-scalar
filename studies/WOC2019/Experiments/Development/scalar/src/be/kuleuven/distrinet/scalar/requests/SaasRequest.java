package be.kuleuven.distrinet.scalar.requests;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.users.SaaSUser;
import java.time.LocalDateTime;
import java.util.Set;

import java.io.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class SaaSRequest extends Request {
    public SaaSRequest(User usr) {
        super(usr, true);
    }

    public void doRequest() throws RequestException {
    	HttpClient httpClient = new DefaultHttpClient();
    	try {
        	HttpGet httpGetRequest = new HttpGet("http://10.103.25.118/request/0");
            startTimer();
            HttpResponse httpResponse = httpClient.execute(httpGetRequest);
            stopTimer();
            
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
            	done(RequestResult.SUCCEEDED);
            } else {
            	System.out.println("### Write request failed, http response:");
            	System.out.println(httpResponse.getStatusLine().getStatusCode() + ": " + httpResponse.getLocale());
                System.out.println(httpResponse.getEntity());
                done(RequestResult.FAILED);
            }
        } catch (Exception e) {
            System.out.println("### Write request failed, reason:");
            e.printStackTrace();
            done(RequestResult.FAILED);
        } finally {
            httpClient.getConnectionManager().shutdown();
      }
    }
}
