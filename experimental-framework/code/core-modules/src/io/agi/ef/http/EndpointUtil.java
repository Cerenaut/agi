package io.agi.ef.http;

/**
 * Utilities for managing connections to a HTTP endpoint.
 *
 * Created by gideon on 19/07/15.
 */
public class EndpointUtil {

    /**
     * For when we want to add the context path later.
     * @param host
     * @param port
     * @return
     */
    public static String getBasePath(String host, int port) {
        return getBasePath(host, port, "");
    }

    /**
     * A default protocol of http is assumed.
     * @param host
     * @param port
     * @param contextPath
     * @return
     */
    public static String getBasePath(String host, int port, String contextPath) {
        return getBasePath("http", host, port, contextPath);
    }

    /**
     * Builds a URL from the given arguments.
     *
     * @param protocol
     * @param host
     * @param port
     * @param contextPath
     * @return
     */
    public static String getBasePath(String protocol, String host, int port, String contextPath) {
        String path = protocol + "://" + host + ":" + port;
        if ( !contextPath.isEmpty() && contextPath != null ) {
            path += "/" + contextPath;
        }
        path += "/";
        return path;
    }

}
