package be.kuleuven.distrinet.dedycker.tenants;

import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;

import java.util.concurrent.atomic.AtomicInteger;

public class Tenant3 extends Tenant {

    private static AtomicInteger counter = new AtomicInteger(0);

    private final String username;

    public Tenant3(UserPool pool) {
        super(pool);
        try {
            String name = pool.manager().data().getAsString("name_tenant3");
            username = name + "-" + counter.getAndIncrement();
            setName(name);
        } catch (DataException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    private static int TOTAL_REQUESTS = 0;

    @Override
    public void requestDone() {
        System.out.println("[TENANT 3] requests: " + TOTAL_REQUESTS++);
    }

}
