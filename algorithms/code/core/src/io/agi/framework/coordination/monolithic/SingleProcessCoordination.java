package io.agi.framework.coordination.monolithic;

import io.agi.framework.coordination.Coordination;
import io.agi.framework.Node;

/**
 * Created by dave on 16/02/16.
 */
public class SingleProcessCoordination implements Coordination {

    public Node _n;

    public SingleProcessCoordination() {

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
