package be.kuleuven.distrinet.scalar.requests;

import be.kuleuven.distrinet.scalar.cassandra.DatastaxCassandraClient;
import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;

import java.time.LocalDateTime;
import java.util.Set;

public class CassandraReadRequest extends Request {
    public CassandraReadRequest(User usr) {
        super(usr, true);
    }

    public void doRequest() throws RequestException {
        DatastaxCassandraClient cassandra = DatastaxCassandraClient.getInstance(user().targetUrl());
        try {
            startTimer();
            cassandra.readAllLogs();
            stopTimer();

            done(RequestResult.SUCCEEDED);
        } catch (Exception e) {
            System.out.println("### Write request failed, reason:");
            e.printStackTrace();
            done(RequestResult.FAILED);
        }
    }
}
