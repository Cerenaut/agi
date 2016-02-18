package io.agi.ef.sql;

import io.agi.ef.StringPersistence;
import io.agi.ef.serialization.JsonData;
import io.agi.ef.serialization.JsonEntity;
import io.agi.ef.serialization.JsonNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by dave on 16/02/16.
 */
public class JdbcPersistence extends StringPersistence {

//    Entity --< Properties
//           --< Data
//    Entity --- name, type, children, node
//               Could be JSON string for entities: { name: xxx, type: yyy } etc

    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    public static final String DRIVER_POSTGRESQL = "org.postgresql.Driver";

    protected String _user;
    protected String _password;
    protected String _url;

    public JdbcPersistence() {
//        this(DEFAULT_DRIVER);
    }
//    public JdbcPersistence(String driver) throws ClassNotFoundException {
//        Class.forName(driver);
//    }

    public void setup( String driver, String user, String password, String url ) throws ClassNotFoundException {
        Class.forName(driver);
        _user = user;
        _password = password;
        _url = url;
    }

    // Nodes
    public Collection< JsonNode > getNodes() {
        return null;
    }
    public void setNode( JsonNode e ) {

    }
    public JsonNode getNode( String nodeName ) {
        return null;
    }
    public void removeNode(String nodeName) {

    }

    // Entities
    public Collection<JsonEntity> getEntities() {
        return null;
    }
    public Collection< String > getChildEntities( String parent ) {
        return null;
    }
    public void setEntity( JsonEntity e ) {

    }
    public JsonEntity getEntity( String key ) {
        return null;
    }
    public void removeEntity(String key) {

    }

    // Data
//    public Collection< String > getDataKeys() {
//    }
    public void setData( JsonData jd ) {
        String sql1 = "UPDATE data SET sizes = '" + jd._sizes + "' WHERE key = '" + jd._key +"'";
        execute( sql1 );
        String sql2 = "INSERT INTO data (key, sizes, elements) SELECT '"+jd._key+"', '"+jd._sizes+"', '"+jd._elements+"' WHERE NOT EXISTS (SELECT key from data WHERE key = "+jd._key+"')";
        execute( sql2 );
    }
    public JsonData getData( String key ) {
        String sql = "SELECT key, value FROM properties where key = " + key;
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "value" );
        executeQuery(sql, rsm);
        String sizes = rsm.getRowValue( 0, "sizes" );
        String elements = rsm.getRowValue( 0, "elements" );
        JsonData jd = new JsonData( key, sizes, elements );
        return jd;
    }
    public void removeData(String key) {
        String sql = "DELETE FROM data WHERE key = " + key;
        execute(sql );
    }

    public String getPropertyString(String key) {
        String sql = "SELECT key, value FROM properties where key = '" + key +"'";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add("value");
        executeQuery(sql, rsm );
        return rsm.getRowValue( 0, "value" );
    }
    public void setPropertyString(String key, String value) {
        // https://www.sitepoint.com/community/t/how-to-use-on-duplicate-key-update-in-postgresql-with-php/200335/4
        String sql1 = "UPDATE properties SET value = " + value + " WHERE key = '" + key + "'";
        execute(sql1 );
        String sql2 = "INSERT INTO properties (key, value) SELECT '"+key+"', '"+value+"' WHERE NOT EXISTS (SELECT key from properties WHERE key = '"+key+"')";
        execute( sql2 );
    }

    public void execute( String sql ) {
        JdbcUtil.Execute(_url, _user, _password, sql );
    }

    public void executeQuery( String sql, ResultSetCallback cb ) {
        JdbcUtil.ExecuteQuery(_url, _user, _password, sql, cb);
    }


}
