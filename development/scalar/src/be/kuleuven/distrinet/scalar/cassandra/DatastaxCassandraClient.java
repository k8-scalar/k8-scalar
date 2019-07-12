package be.kuleuven.distrinet.scalar.cassandra;

import be.kuleuven.distrinet.scalar.requests.Log;
import be.kuleuven.distrinet.scalar.requests.LogId;
import com.datastax.driver.core.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class DatastaxCassandraClient {
    private static int CASSANDRA_PORT = 9042;
    private static int TOTAL_WRITES = 0;
    private static int TOTAL_READS = 0;

    private static DatastaxCassandraClient instance = null;
    
    protected Session session = null;

    protected DatastaxCassandraClient(String hosts) {
        String[] cassandra_hosts = hosts.split(" ");
        System.out.print("Connecting to: " + cassandra_hosts[0]);
        for (int i=1; i < cassandra_hosts.length; i++) {
            System.out.print(", " + cassandra_hosts[i]);
        }
        System.out.println("");
        QueryOptions qo = new QueryOptions().setConsistencyLevel(ConsistencyLevel.ONE);
        cluster = Cluster.builder()
        	.withQueryOptions(qo)
            .addContactPoints(cassandra_hosts)
            .withPort(CASSANDRA_PORT)
            .build();
        
        session = cluster.connect();

//        createSchema();

//        System.out.printf("[CASSANDRA] Connected to cluster: %s%n", cluster.getMetadata().getClusterName());
    }

    public static DatastaxCassandraClient getInstance(String hosts) {
        if(instance == null) {
            instance = new DatastaxCassandraClient(hosts);
        }
        return instance;
    }
    
   private Cluster cluster;

    public void createSchema() {
   //     Session session = cluster.connect();

        try {
            session.execute("CREATE KEYSPACE IF NOT EXISTS scalar WITH replication " +
                    "= {'class':'SimpleStrategy', 'replication_factor':1};");

            session.execute("CREATE TABLE IF NOT EXISTS scalar.logs (" +
                    "id text PRIMARY KEY," +
                    "timestamp text," +
                    "message text" +
                    ");");
        } finally {
            //session.close();
        	System.out.println("[CASSANDRA] Created schema.");
        }

        
    }

    public void write(Log aLog) {
       // Session session = cluster.connect();
        try {
            session.execute("INSERT INTO scalar.logs (id, timestamp, message) " +
                "VALUES (" +
                "'" + aLog.getId().toString() + "'," +
                "'" + aLog.getDate().toString() + "'," +
                "'" + aLog.getMessage() + "'" +
                ");");
        } finally {
            //session.close();
            System.out.println("[CASSANDRA] writes: " + TOTAL_WRITES++);
        }
    }

    public Set<Log> readAllLogs() {
        Set<Log> result = new HashSet<Log>();

        //Session session = cluster.connect();
        try {
            ResultSet rows = session.execute("SELECT * FROM scalar.logs;");
            for (Row row : rows) {
                LogId logId        = new LogId(row.getString("id"));
                LocalDateTime date = LocalDateTime.parse(row.getString("timestamp"));
                String message     = row.getString("message");

                result.add(new Log(logId, date, message));
            }
        } finally {
          //  session.close();
            System.out.println("[CASSANDRA] reads: " + TOTAL_READS++);
        }

//        System.out.println("[CASSANDRA] Retrieved all logs ( " + result.size() + " ).");
        return result;
    }

   /* public void close() {
        cluster.close();
    }*/
}
