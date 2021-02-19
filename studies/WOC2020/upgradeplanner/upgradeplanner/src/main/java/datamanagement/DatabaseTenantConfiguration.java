package datamanagement;

import datatypes.TenantConfig;

import java.sql.*;
import java.time.LocalTime;
import java.util.Arrays;


public class DatabaseTenantConfiguration {

    private static DatabaseTenantConfiguration postdb;
    private static String dbName = "postgressvc";

    //region Database setup
    private DatabaseTenantConfiguration() {
        createDatabase();
        createTablesTenant();
    }

    public static DatabaseTenantConfiguration getInstance() {
        if (postdb == null) {
            postdb = new DatabaseTenantConfiguration();
        }
        return postdb;
    }

    private static Connection getConnectionTenant() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/tenantdb", "arnout", "123");
        return conn;
    }

    private void createDatabase() {
        try {
            Connection conn = null;
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/", "arnout", "123");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT datname from pg_database WHERE datname like 'tenantdb';";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return;
            }
            Statement dbstmt = conn.createStatement();
            String dbsql = "CREATE DATABASE tenantdb;";
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

    /* Creates the tables for the TenantConfigurationDB */
    private void createTablesTenant() {
        try {
            Connection conn = getConnectionTenant();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS TENANTSCONFIG " +
                    "(ID                SERIAL PRIMARY KEY," +
                    " TENANT            INT       NOT NULL," +
                    " APP               TEXT      NOT NULL," +
                    " SLA               TEXT      NOT NULL," +
                    " CURRVERSION       TEXT      NOT NULL," +
                    " NEWVERSION        TEXT      NULL," +
                    " DEADLINE          TIME      NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    //endregion

    //region AddData and UpdateData
    public void addData(TenantConfig upg) {
        try {
            // Check if the request is valid meaning that tenantId and currentVersion match in the DB with request.
            boolean valid = checkIfValid(upg);
            if (valid) {
                Connection conn = getConnectionTenant();
                conn.setAutoCommit(false);

                // prepare statement to insert into database
                PreparedStatement upgst = conn.prepareStatement("INSERT INTO TENANTSCONFIG(TENANT,APP,SLA,CURRVERSION,NEWVERSION,DEADLINE)"
                        + "VALUES(?, ?, ?, ?, ?, ?);");
                upgst.setInt(1, upg.getTenantId());
                upgst.setString(2, upg.getAppName());
                upgst.setString(3, upg.getSlaClass());
                upgst.setString(4, upg.getCurrVersion());
                upgst.setString(5, upg.getUpgVersion());
                if (upg.getDeadline() == null) {
                    upgst.setTime(6, null);
                } else {
                    Time time = Time.valueOf(upg.getDeadline());
                    upgst.setTime(6, time);
                }
                upgst.executeUpdate();

                // Closing down open connections and committing the changes
                upgst.close();
                conn.commit();
                conn.close();
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            System.exit(0);
        }
    }

    public boolean checkIfValid(TenantConfig upg) {
        try {
            Connection conn = getConnectionTenant();
            conn.setAutoCommit(false);

            // Check if tenant already has an ID set.
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TENANTSCONFIG WHERE TENANT = ?");
            ps.setInt(1, upg.getTenantId());
            ResultSet rs = ps.executeQuery();
            // If there is an upgrade set then we delete it.
            if (rs.next()) {
                // Check if the current version is the same
                if (rs.getString(5).equals(upg.getCurrVersion())) {
                    PreparedStatement del = conn.prepareStatement("DELETE FROM TENANTSCONFIG WHERE TENANT=?");
                    del.setInt(1, upg.getTenantId());
                    del.executeUpdate();
                    del.close();
                    conn.commit();
                    conn.close();
                    return true;
                } else {
                    rs.close();
                    ps.close();
                    conn.close();
                    return false;
                }
            }
            rs.close();
            ps.close();
            conn.close();
            return true;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return false;
    }

    public void updateData(TenantConfig upg) {
        try {
            // Check if the request is valid meaning that tenantId and currentVersion match in the DB with request.
            boolean valid = deletePrevious(upg);
            if (valid) {
                Connection conn = getConnectionTenant();
                conn.setAutoCommit(false);

                // prepare statement to insert into database
                PreparedStatement upgst = conn.prepareStatement("INSERT INTO TENANTSCONFIG(TENANT,APP,SLA,CURRVERSION,NEWVERSION,DEADLINE)"
                        + "VALUES(?, ?, ?, ?, ?, ?);");
                upgst.setInt(1, upg.getTenantId());
                upgst.setString(2, upg.getAppName());
                upgst.setString(3, upg.getSlaClass());
                upgst.setString(4, upg.getCurrVersion());
                upgst.setString(5, upg.getUpgVersion());
                if (upg.getDeadline() == null) {
                    upgst.setTime(6, null);
                } else {
                    Time time = Time.valueOf(upg.getDeadline());
                    upgst.setTime(6, time);
                }
                upgst.executeUpdate();

                // Closing down open connections and committing the changes
                upgst.close();
                conn.commit();
                conn.close();
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            System.exit(0);
        }
    }

    private boolean deletePrevious(TenantConfig upg) {
        try {
            Connection conn = getConnectionTenant();
            conn.setAutoCommit(false);

            // Check if tenant already has an ID set.
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TENANTSCONFIG WHERE TENANT = ?");
            ps.setInt(1, upg.getTenantId());
            ResultSet rs = ps.executeQuery();
            // If there is an upgrade set then we delete it.
            if (rs.next()) {
                // Check if the current version is the same
                PreparedStatement del = conn.prepareStatement("DELETE FROM TENANTSCONFIG WHERE TENANT=?");
                del.setInt(1, upg.getTenantId());
                del.executeUpdate();
                del.close();
                conn.commit();
                conn.close();
                return true;
            }
            rs.close();
            ps.close();
            conn.close();
            return true;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return false;
    }
    //endregion

    //region getData
    public TenantConfig getData(int tenantId) {
        try {
            Connection conn = getConnectionTenant();
            conn.setAutoCommit(false);

            // preparing get statement
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TENANTSCONFIG WHERE TENANT=?;");
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(2);
                String app = rs.getString(3);
                String sla = rs.getString(4);
                String currVersion = rs.getString(5);
                String newVersion = rs.getString(6);
                LocalTime deadLine;
                if (rs.getTime(7) == null) {
                    deadLine = null;
                } else {
                    deadLine = rs.getTime(7).toLocalTime();
                }
                // closing down open connections
                rs.close();
                ps.close();
                conn.close();
                return new TenantConfig(id, app, sla, currVersion, newVersion, deadLine);
            }
            // closing down open connections
            rs.close();
            ps.close();
            conn.close();
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return null;
        }
    }

    public Integer getNumTenantsVersion(String version, String app) {
        try {
            Connection conn = getConnectionTenant();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TENANTSCONFIG WHERE CURRVERSION=? AND APP=?;");
            ps.setString(1, version);
            ps.setString(2, app);
            ResultSet rs = ps.executeQuery();
            int sum = 0;
            while (rs.next()) {
                sum++;
            }

            return sum;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return null;
        }

    }

    public Integer getNumTenants() {
        try {
            Connection conn = getConnectionTenant();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TENANTSCONFIG;");
            ResultSet rs = ps.executeQuery();
            int sum = 0;
            while (rs.next()) {
                sum++;
            }

            return sum;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
            return null;
        }

    }
    //endregion
}
