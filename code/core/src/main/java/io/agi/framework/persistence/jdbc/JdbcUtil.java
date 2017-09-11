/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework.persistence.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * TODO - implement some performance improvements, this list seems sensible:
 * http://javarevisited.blogspot.com.au/2012/01/improve-performance-java-database.html
 * Also try to reduce writes by having a flag whether the data has changed.
 * <p/>
 * Created by dave on 17/02/16.
 */
public class JdbcUtil {

    private static final Logger logger = LogManager.getLogger();

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
            //_logger.info( "JDBC T: {} @1 jdbc = {}", System.currentTimeMillis(), sql );
            s = c.createStatement();
            //_logger.info( "JDBC T: {} @2 ", System.currentTimeMillis() );
            s.execute( sql );
            //_logger.info( "JDBC T: {} @3 ", System.currentTimeMillis() );

            //STEP 6: Clean-up environment
//            s.close();
//            c.close();
        }
        catch( SQLException se ) {
            logger.error( se.toString(), se );
        }
        catch( Exception e ) {
            logger.error( s.toString(), s );
        }
        finally {
            try {
                if( s != null ) s.close();
            }
            catch( SQLException se2 ) {
                logger.error( se2.toString(), se2 );
            }
            finally {
                try {
                    if( c != null ) c.close();
                }
                catch( SQLException se ) {
                    logger.error( se.toString(), se );
                }
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

            if( cb != null ) {
                cb.onResultSet( rs );
            }

            //STEP 6: Clean-up environment
            rs.close();
            s.close();
            c.close();
        }
        catch( SQLException se ) {
            logger.error( se.toString(), se );
        }
        catch( Exception e ) {
            logger.error( s.toString(), s );
        }
        finally {
            try {
                if( s != null ) s.close();
            }
            catch( SQLException se2 ) {
                logger.error( se2.toString(), se2 );
            }

            try {
                if( c != null ) c.close();
            }
            catch( SQLException se ) {
                logger.error( se.toString(), se );
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
        if( _dataSource == null ) {
            CreateDataSource( dbUrl, user, password, JdbcPersistence.DRIVER_POSTGRESQL );
        }

        try {
            Connection c = _dataSource.getConnection();
            return c;
        }
        catch( Exception e ) {
            logger.error( e.toString(), e );
            return null;
        }
    }
}
