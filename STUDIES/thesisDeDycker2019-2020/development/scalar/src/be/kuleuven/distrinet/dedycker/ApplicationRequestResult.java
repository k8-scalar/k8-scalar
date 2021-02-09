package be.kuleuven.distrinet.dedycker;

import org.apache.http.Header;

public class ApplicationRequestResult {

    public final int statusCode;
    public final Header[] headers;
    public final String responseString;

    public ApplicationRequestResult(int statusCode, Header[] headers, String responseString) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.responseString = responseString;
    }
}
