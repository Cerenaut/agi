package io.agi.interprocess;

/**
 * Created by gideon on 19/07/15.
 */
public class EndpointUtils {

    private static int BASE_PORT = 8080;     // Shared between all servers in the agi framework

    public static int coordinatorListenPort() {
        return BASE_PORT;
    }

    public static String coordinatorContextPath() {
        return "coordinator";
    }

    public static String basePath( int port, String contextPath ) {
        return "http://localhost:" + port + "/" + contextPath;
    }


}
