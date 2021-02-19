package be.kuleuven.distrinet.dedycker.requests;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.requests.Request;

public abstract class ApplicationRequest extends Request {

    private static final String STANDARD_MESSAGE = "No message.";

    public ApplicationRequest(User usr, boolean keepAccountOfRequest, String shortName) {
        super(usr, keepAccountOfRequest, shortName);
    }

    public String messageOnFailure() {
        return STANDARD_MESSAGE;
    }
}
