package io.agi.ef.interprocess;

import io.agi.ef.clientapi.ApiClient;

/**
 * Data structure to encapsulate information about a connection, as a client, to an AGIEF experiment server.
 * It may not yet be established.
 */
public class ServerConnection {

    private ApiClient client;
    static public int _sCount = 0;

    private int _id = ++_sCount;             // unique id
    private int _port = -1;
    private String _host = null;
    private String _contextPath = null;      // the context that appears after the base basic url and listenerPort name. Together with _contextPath you have the 'basepath'. DO NOT include preceding or trailing slashes.
    private ApiClient _client = null;        // data structure that enables making requests


    public ServerConnection( String host, String contextPath, int port ) {
        _host = host;
        _contextPath = contextPath;
        _port = port;
    }

    public String basePath() {
        return EndpointUtils.basePath( _host, _port, _contextPath );
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

    @Override
    public String toString() {
        String str = "[id:" + "(" + _id + "), host:(" + _host + "), contextPath:(" + _contextPath + "), listenerPort:(" + _port + "), clientApi:(" + _client + ")";
        return str;
    }
}
