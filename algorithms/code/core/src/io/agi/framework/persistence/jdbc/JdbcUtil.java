package io.agi.framework.persistence.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;

/**
 * TODO - implement some performance improvements, this list seems sensible:
 * http://javarevisited.blogspot.com.au/2012/01/improve-performance-java-database.html
 * Also try to reduce writes by having a flag whether the data has changed.
 * 
 * Created by dave on 17/02/16.
 */
public class JdbcUtil {


    /**
     * Execute an INSERT or UPDATE statement, that doesn't return any data.
     *
     * @param dbUrl
     * @param user
     * @param password
     * @param sql
     */
    public static void Execute( String dbUrl, String user, String password, String sql ) {
        Connection c = null;
        Statement s = null;
        try {
            c = DriverManager.getConnection( dbUrl, user, password );

            //STEP 4: Execute a query
            System.err.println( "JDBC T: " + System.currentTimeMillis() + " @1 jdbc = " + sql );
            s = c.createStatement();
            System.err.println( "JDBC T: " + System.currentTimeMillis() + " @2 " );
            s.execute( sql );
            System.err.println( "JDBC T: " + System.currentTimeMillis() + " @3 " );

            //STEP 6: Clean-up environment
            s.close();
            c.close();
        }
        catch ( SQLException se ) {
            se.printStackTrace();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
            try {
                if ( s != null ) s.close();
            }
            catch ( SQLException se2 ) {
            }

            try {
                if ( c != null ) c.close();
            }
            catch ( SQLException se ) {
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

//            c = DriverManager.getConnection(dbUrl, user, password);
            c = GetConnection( dbUrl, user, password );

            //STEP 4: Execute a query
            s = c.createStatement();
            ResultSet rs = s.executeQuery( sql );

            if ( cb != null ) {
                cb.onResultSet( rs );
            }

            //STEP 6: Clean-up environment
            rs.close();
            s.close();
            c.close();
        }
        catch ( SQLException se ) {
            se.printStackTrace();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
            try {
                if ( s != null ) s.close();
            }
            catch ( SQLException se2 ) {
            }

            try {
                if ( c != null ) c.close();
            }
            catch ( SQLException se ) {
                se.printStackTrace();
            }

        }
    }

    private static BasicDataSource _dataSource;

    public static void CreateDataSource( String dbUrl, String user, String password, String driverClassName ) {
        _dataSource = new BasicDataSource();
        _dataSource.setDriverClassName( driverClassName );
        _dataSource.setUrl( dbUrl );
        _dataSource.setUsername( user );
        _dataSource.setPassword( password );
        _dataSource.setMaxIdle( 100 );
    }

    public static Connection GetConnection( String dbUrl, String user, String password ) {
        if ( _dataSource == null ) {
            CreateDataSource( dbUrl, user, password, JdbcPersistence.DRIVER_POSTGRESQL );
        }

        try {
            Connection c = _dataSource.getConnection();
            return c;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }
}
