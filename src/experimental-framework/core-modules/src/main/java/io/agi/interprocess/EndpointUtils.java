package io.agi.interprocess;

/**
 * Created by gideon on 19/07/15.
 */
public class EndpointUtils {

    public static String basePath( String host, String contextPath, int port ) {
        return "http://" + host + ":" + port + "/" + contextPath;
    }

}
