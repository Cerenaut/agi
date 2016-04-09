package io.agi.core.ann;

import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * A separate object to an actual algorithm implementation to allow configuration and instantiation to be divorced.
 * Since algorithms may be composed out of several smaller networks, need a hierarchical way to generate networks within
 * networks. Hence, let's use a systematic way of naming things to avoid clashes.
 * <p>
 * Created by dave on 10/01/16.
 */
public class NetworkConfig {

    public String _name;
    public ObjectMap _om;
    public Random _r;

    /**
     * Use the specified ObjectMap with prefix name to generate keys for parameters.
     *
     * @param om
     * @param name
     */
    public void setup( ObjectMap om, String name, Random r ) {
        _om = om;
        _name = name;
        _r = r;
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        _om = nc._om;
        _name = name;
        _r = nc._r;
    }

    public String getKey( String suffix ) {
        return Keys.concatenate( _name, suffix );
    }

}
