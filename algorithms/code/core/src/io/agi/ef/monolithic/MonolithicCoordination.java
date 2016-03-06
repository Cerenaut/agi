package io.agi.ef.monolithic;

import io.agi.ef.Coordination;
import io.agi.ef.Node;

/**
 * Created by dave on 16/02/16.
 */
public class MonolithicCoordination implements Coordination {

    public Node _n;

    public MonolithicCoordination() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    public void doUpdate(String entityName) {
        _n.doUpdate(entityName);
    }

    public void onUpdated(String entityName) {
        _n.onUpdated( entityName );
    }
}
