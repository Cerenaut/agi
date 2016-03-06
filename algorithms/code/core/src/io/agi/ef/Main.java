package io.agi.ef;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.PropertiesUtil;
import io.agi.ef.http.HttpCoordination;
import io.agi.ef.monolithic.SingleProcessCoordination;
import io.agi.ef.sql.JdbcPersistence;

/**
 * Created by dave on 6/03/16.
 */
public class Main {

    public static final String PROPERTY_DATABASE_USER = "database-user";
    public static final String PROPERTY_DATABASE_PASSWORD = "database-password";
    public static final String PROPERTY_DATABASE_URL = "database-url";
    public static final String PROPERTY_DATABASE_DRIVER_CLASS = "database-driver-class";

    public static final String PROPERTY_NODE_NAME = "node-name";
    public static final String PROPERTY_NODE_HOST = "node-host";
    public static final String PROPERTY_NODE_PORT = "node-port";

    public static final String PROPERTY_DISTRIBUTED = "distributed";

    public String _databaseUser;
    public String _databasePassword;
    public String _databaseUrl;
    public String _databaseDriverClass;

    public String _nodeName;
    public String _nodeHost;
    public int _nodePort = 0;

    public boolean _distributed = false;

    public EntityFactory _ef;
    public ObjectMap _om;
    public Coordination _c;
    public Persistence _p;
    public Node _n;

    public Main() {

    }

    public void setup( String propertiesFile, ObjectMap om, EntityFactory ef ) {
        _ef = ef;

        if( om == null ) {
            om = ObjectMap.GetInstance();
        }

        _om = om;

        _databaseUser = PropertiesUtil.get(propertiesFile, PROPERTY_DATABASE_USER, "agiu");
        _databasePassword = PropertiesUtil.get(propertiesFile, PROPERTY_DATABASE_PASSWORD, "password");
        _databaseUrl = PropertiesUtil.get(propertiesFile, PROPERTY_DATABASE_URL, "jdbc:postgresql://localhost:5432/agidb");
        _databaseDriverClass = PropertiesUtil.get(propertiesFile, PROPERTY_DATABASE_DRIVER_CLASS, JdbcPersistence.DRIVER_POSTGRESQL );

        _nodeName = PropertiesUtil.get(propertiesFile, PROPERTY_NODE_NAME, "node-1");
        _nodeHost = PropertiesUtil.get(propertiesFile, PROPERTY_NODE_HOST, "localhost");
        _nodePort = Integer.valueOf(PropertiesUtil.get(propertiesFile, PROPERTY_NODE_PORT, "8491"));

        _distributed = Boolean.valueOf( PropertiesUtil.get(propertiesFile, PROPERTY_DISTRIBUTED, "true" ) );

        // Create persistence & Node now so you can create entities in code that are hosted and persisted on the Node.
        try {
            JdbcPersistence p = new JdbcPersistence();
            p.setup(_databaseDriverClass, _databaseUser, _databasePassword, _databaseUrl);
            _p = p;

            if( _distributed ) {
                _c = new HttpCoordination();
            }
            else {
                _c = new SingleProcessCoordination();
            }

            // The persistent description of this Node
            Node n = new Node();
            n.setup( _om, _nodeName, _nodeHost, _nodePort, ef, _c, _p );
            _n = n;

            ef.setNode(n);
        }
        catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void run() {
        try {
            if( _distributed ) {
                HttpCoordination c = (HttpCoordination)_c;
                c.setNode(_n);
                c.start();
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }
}
