package be.kuleuven.distrinet.dedycker.tenants;

import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;

import java.util.concurrent.atomic.AtomicInteger;

public class Tenant1 extends Tenant {

    private String username;
    private static AtomicInteger counter = new AtomicInteger(0);


    public Tenant1(UserPool pool) {
        super(pool);
        try {
            String name = pool.manager().data().getAsString("name_tenant1");
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

    @Override
    public void mainLoop() throws DataException {
        if(registration) this.username = getName() + "-" + counter.getAndIncrement();
        super.mainLoop();
    }

    private static int TOTAL_REQUESTS = 0;

    @Override
    public void requestDone() {
        System.out.println("[TENANT 1] requests: " + TOTAL_REQUESTS++);
    }

}
