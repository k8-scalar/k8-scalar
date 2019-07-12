package be.kuleuven.distrinet.scalar.users;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.requests.CassandraWriteRequest;
import be.kuleuven.distrinet.scalar.cassandra.DatastaxCassandraClient;

public class CassandraWriteUser extends User {
	protected DatastaxCassandraClient cassandra;

//  DatastaxCassandraClient 
	
    CassandraWriteUser(UserPool pool) {
        super(pool);
        super.targetUrl();
        cassandra = DatastaxCassandraClient.getInstance(super.targetUrl());
    }
    
    public DatastaxCassandraClient getCassandraClient() {
    	return cassandra;
    }

    @Override
    public void mainLoop() throws DataException {
        CassandraWriteRequest request = new CassandraWriteRequest(this);
        try {
            request.doRequest();

        } catch (RequestException e) {
            throw new InternalException(e);
        }
    }
}