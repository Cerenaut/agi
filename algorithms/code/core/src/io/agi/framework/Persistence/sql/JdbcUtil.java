package io.agi.framework.Persistence.sql;

import java.sql.*;

/**
 * Created by dave on 17/02/16.
 */
public class JdbcUtil {

    /**
     * Execute an INSERT or UPDATE statement, that doesn't return any data.
     * @param dbUrl
     * @param user
     * @param password
     * @param sql
     */
    public static void Execute( String dbUrl, String user, String password, String sql ) {
        Connection c = null;
        Statement s = null;
        try {
            c = DriverManager.getConnection(dbUrl, user, password);

            //STEP 4: Execute a query
            s = c.createStatement();
            s.execute( sql );

            //STEP 6: Clean-up environment
            s.close();
            c.close();
        }
        catch(SQLException se){
            se.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if( s != null ) s.close();
            }
            catch(SQLException se2) {
            }

            try{
                if( c != null ) c.close();
            }
            catch( SQLException se ) {
                se.printStackTrace();
            }

        }
    }

    /**
     * Execute a SQL query that returns data.
     * 
     * @param dbUrl
     * @param user
     * @param password
     * @param sql
     * @param cb
     */
    public static void ExecuteQuery( String dbUrl, String user, String password, String sql, ResultSetCallback cb ) {
        Connection c = null;
        Statement s = null;
        try {
            c = DriverManager.getConnection(dbUrl, user, password);

            //STEP 4: Execute a query
            s = c.createStatement();
            ResultSet rs = s.executeQuery( sql );

            if( cb != null ) {
                cb.onResultSet( rs );
            }

            //STEP 6: Clean-up environment
            rs.close();
            s.close();
            c.close();
        }
        catch(SQLException se){
            se.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if( s != null ) s.close();
            }
            catch(SQLException se2) {
            }

            try{
                if( c != null ) c.close();
            }
            catch( SQLException se ) {
                se.printStackTrace();
            }

        }
    }

}
