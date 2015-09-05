package io.agi.ef.interprocess;

/**
 * Created by gideon on 19/07/15.
 */
public class EndpointUtils {

    public static String basePath( String host, int port, String contextPath ) {
        return "http://" + host + ":" + port + "/" + contextPath;
    }

}
