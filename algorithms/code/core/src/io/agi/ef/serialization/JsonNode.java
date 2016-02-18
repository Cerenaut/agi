package io.agi.ef.serialization;

import io.agi.core.data.Data;
import io.agi.ef.Node;

/**
 * Convert a Node object to and from its JSON equivalent.
 *
 * Created by dave on 17/02/16.
 */
public class JsonNode {

    public String _key;
    public String _host;
    public int _port;

    public JsonNode( String key, String host, int port ) {
        _key = key;
        _host = host;
        _port = port;
    }

    public JsonNode( Node n ) {
        _key = n.getName();
        _host = n.getHost();
        _port = n.getPort();
    }

}
