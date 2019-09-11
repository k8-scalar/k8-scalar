package be.kuleuven.distrinet.scalar.users;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.requests.CassandraWriteRequest;

public class CassandraWriteUser extends User {
    CassandraWriteUser(UserPool pool) {
        super(pool);
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