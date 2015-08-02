package io.agi.ef.core.network;

import io.agi.ef.clientapi.ApiClient;

/**
 * Data structure to encapsulate information about a connection, as a client, to an AGIEF core server.
 * It may not yet be established.
 */
public class ServerConnection {

    private ApiClient client;

    public enum ServerType {
        Agent,
        World,
        Coordinator
    }

    static public int _sCount = 0;

    private int _id = ++_sCount;             // unique id
    private ServerType _type = null;         // which 'type' of server to connect to (Agents and Worlds are types of AGIEF core servers)
    private int _port = -1;
    private String _contextPath = null;      // the context that appears after the base basic url and port name. Together with _contextPath you have the 'basepath'. DO NOT include preceding or trailing slashes.
    private ApiClient _client = null;        // data structure that enables making requests


    public ServerConnection( ServerType type, int port, String contextPath ) {
        _type = type;
        _port = port;
        _contextPath = contextPath;
    }

    public String basePath() {
        return EndpointUtils.basePath( _port, _contextPath );
    }

    public void setClient( ApiClient client ) {
        _client = client;
    }

    public ApiClient getClientApi() {
        return _client;
    }

    public Object getId() {
        return _id;
    }

    public boolean isAgent() {
        if ( _type == ServerType.Agent ) {
            return true;
        }
        return false;
    }

    public boolean isWorld() {
        if ( _type == ServerType.World ) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String str = "[id:" + _id + "], type:(" + _type + "), port:(" + _port + "), contextPath:(" + _contextPath + "), clientApi:(" + _client + ")";
        return str;
    }
}
