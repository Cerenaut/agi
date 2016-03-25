package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.Node;

import java.util.Collection;

/**
 * Created by gideon on 20/03/2016.
 */
public class ExperimentEntity extends Entity {

    public ExperimentEntity( String name, ObjectMap om, String type, Node n ) {
        super( name, om, type, n );
    }

    @Override
    public void getInputKeys( Collection< String > keys ) {

    }

    @Override
    public void getOutputKeys( Collection< String > keys ) {

    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {

    }
}
