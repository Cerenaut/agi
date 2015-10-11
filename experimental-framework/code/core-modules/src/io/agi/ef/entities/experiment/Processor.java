package io.agi.ef.entities.experiment;

import io.agi.ef.Entity;

import java.util.HashSet;

/**
 * A processor is a generic entity that waits for some Data to be dirty, then processes it.
 * Created by dave on 17/09/15.
 */
public class Processor extends Entity {

    public static final String ENTITY_TYPE = "processor";

    protected HashSet< String > _inputDataNames = new HashSet<>();

    public Processor() {

    }

    public void configure(String config) {
    }

    public void doStep( HashSet< String > dirtyData ) {
        super.doStep( dirtyData );

        HashSet< String > dirtyInput = new HashSet<>();

        synchronized( this ) {
            for( String dataName : _inputDataNames ) {
                if( dirtyData.contains( dataName ) ) {
                    dirtyInput.add( dataName );
                }
            }
        }

        if( dirtyInput.isEmpty() ) {
            return; // nothing to process
        }
    }

    /**
     * Called on a step where there is data to process.
     *
     * @param dirtyData
     * @param dirtyInput
     */
    public void onDirtyData( HashSet< String > dirtyData, HashSet< String > dirtyInput ) {

    }
}

