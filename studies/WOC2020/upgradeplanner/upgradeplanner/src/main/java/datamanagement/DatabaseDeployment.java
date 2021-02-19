package datamanagement;

import datatypes.Deployment;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1beta2Api;
import io.kubernetes.client.openapi.models.V1beta2Deployment;
import io.kubernetes.client.openapi.models.V1beta2DeploymentList;
import io.kubernetes.client.util.ClientBuilder;

import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class DatabaseDeployment {
    private static DatabaseDeployment postdb;
    private static String dbName = "postgressvc";

    //region Database Setup
    private DatabaseDeployment() {
        createDatabase();
        createTablesUpgrade();
    }

    public static DatabaseDeployment getInstance() {
        if (postdb == null) {
            postdb = new DatabaseDeployment();
        }
        return postdb;
    }

    private static Connection getConnectionUpgrade() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/upgradedb", "arnout", "123");
        return conn;
    }

    private void createDatabase() {
        try {
            Connection conn = null;
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/", "arnout", "123");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT datname from pg_database WHERE datname like 'upgradedb';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return;
            }
            Statement dbstmt = conn.createStatement();
            String dbsql = "CREATE DATABASE upgradedb;";
            dbstmt.executeUpdate(dbsql);

            dbstmt.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private void createTablesUpgrade() {
        try {
            Connection conn = getConnectionUpgrade();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS UPGRADE " +
                    "(ID            SERIAL PRIMARY KEY," +
                    " DEPLOYMENT    TEXT    NOT NULL," +
                    " APP           TEXT    NOT NULL," +
                    " VERSION       TEXT    NOT NULL," +
                    " SLA           TEXT    NOT NULL," +
                    " REQUESTS      INT     NOT NULL," +
                    " LIMITS        INT     NOT NULL," +
                    " TENANTREQUESTS      INT     NOT NULL," +
                    " TENANTLIMITS        INT     NOT NULL," +
                    " UPGRADETIME         INT     NOT NULL)"; // tenantRequest and Limits show the per tenant value for requests and limits
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
    //endregion

    //region addData
    public Deployment addData(String name) {
        try {
            // Check if the data is already there
            checkIfPresent(name);

            // variables and no autocommit
            Connection conn = getConnectionUpgrade();
            conn.setAutoCommit(false);

            // gets the Deployment data from the Kubernetes server.
            Deployment dep = getDataFromDeployment(name);
            PreparedStatement upgst = conn.prepareStatement(
                    "INSERT INTO UPGRADE(DEPLOYMENT,APP,VERSION,SLA,REQUESTS,LIMITS,TENANTREQUESTS,TENANTLIMITS,UPGRADETIME)"
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);");
            upgst.setString(1, dep.getDeploymentName());
            upgst.setString(2, dep.getAppName());
            upgst.setString(3, dep.getVersion());
            upgst.setString(4, dep.getSlaClass());
            upgst.setInt(5, dep.getPodRequests());
            upgst.setInt(6, dep.getPodLimits());
            upgst.setInt(7, dep.getTenantRequests());
            upgst.setInt(8, dep.getTenantLimits());
            upgst.setInt(9, dep.getUpgradeTime());
            upgst.executeUpdate();

            upgst.close();
            conn.commit();
            conn.close();
            return dep;
        } catch (SQLException | ClassNotFoundException | IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return null;
        }
    }

    private void checkIfPresent(String name) {
        try {
            // variables and no autocommit
            Connection conn = getConnectionUpgrade();
            conn.setAutoCommit(false);

            // Check if the deployment is already in the database
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM UPGRADE WHERE DEPLOYMENT=?;");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            // If so then delete this record
            if (rs.next()) {
                PreparedStatement del = conn.prepareStatement("DELETE FROM UPGRADE WHERE DEPLOYMENT=?;");
                del.setString(1, name);
                del.executeUpdate();
                del.close();
                conn.commit();
            }
            ps.close();
            rs.close();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }


    //endregion

    //region getData
    public Deployment getData(String app, String version, String sla) {
        try {
            Connection conn = getConnectionUpgrade();
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM UPGRADE WHERE APP=? AND VERSION=? AND SLA=?");
            stmt.setString(1, app);
            stmt.setString(2, version);
            stmt.setString(3, sla);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dep = rs.getString(2);
                int request = rs.getInt(6);
                int limits = rs.getInt(7);
                int tenantRequest = rs.getInt(8);
                int tenantLimits = rs.getInt(9);
                int upgradeTime = rs.getInt(10);
                rs.close();
                stmt.close();
                conn.close();
                return new Deployment(dep, app, version, sla, request, limits, tenantRequest, tenantLimits, upgradeTime);
            }
            rs.close();
            stmt.close();
            conn.close();
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private Deployment getDataFromDeployment(String name) throws IOException {
        try {
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);

            AppsV1beta2Api api = new AppsV1beta2Api();
            V1beta2DeploymentList deploymentList = api.listDeploymentForAllNamespaces(null, null, "metadata.name=" + name, null, null, null, null, null, null);
            V1beta2Deployment dep = deploymentList.getItems().get(0);

            // For urgency halen we dit uit V1ObjectMetadata, we kunnen ook verwachten dat ze hier requests en limits zouden zetten voor makkelijkheid anders moeten we zeer diep gaan graven
            Map<String, String> labels = dep.getMetadata().getLabels();
            String app = labels.get("app");
            String version = labels.get("version");
            String slaClass = labels.get("sla");
            int tenantRequests = Integer.parseInt(labels.get("requests"));
            int tenantLimits = Integer.parseInt(labels.get("limits"));
            Map<String, Quantity> reqs = dep.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests();
            Map<String, Quantity> lims = dep.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits();
            int requests = (int) (reqs.get("cpu").getNumber().doubleValue() * 1000);
            int limits = (int) (lims.get("cpu").getNumber().doubleValue() * 1000);
            int upgradeTime = Integer.parseInt(labels.get("upgradeTime"));

            return new Deployment(name, app, version, slaClass, requests, limits, tenantRequests, tenantLimits, upgradeTime);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppsV1beta2Api#listDeploymentForAllNamespaces");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return null;
        }
    }


    //endregion
}
