package io.agi.framework.persistence;

import java.util.HashMap;

/**
 * Created by gideon on 29/03/2016.
 */
public class BlahPersistence implements Persistence
{

    HashMap< String, Class > _keyToType;

    // fetch from storage and populate model
    void getPropertyObject( String key, Object propertyObject )
    {
        query = createQueryToGetObjectForKey( key );    // depends on db type
        response = db.query( query );                   // could be straight JSON, probably couchbase can read directly into a model

        propertyObject = parseResponse( key, response );     // convert from JSON to model, or couchbase model into domain model
    }

    // persist to storage
    void setPropertyObject( String key, Object propertyObject )
    {
        _keyToType.put( key, propertyObject.getClass() );

        // if couchbase, not sure if it can persist a model directly
        // if not, then:
        String json = gson.toJson( propertyObject );
        couchbase.persistAsAModel( key, json );
    }


    // convert db response into a model
    private Object parseResponse( String key, String response ) {

        // if stored in sql in a conventional way, then it is obvious

        // if stored in sql as a json string
        Class class = _keyToType.get( key );

        propertyObject  = gson.fromJson( response, class );

        // couchbase would be one of the above two options


        return propertyObject;
    }
}

