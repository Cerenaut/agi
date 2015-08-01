package io.agi.ef.core.network;

/**
 * Created by gideon on 19/07/15.
 */
public class EndpointUtils {

    private static int BASE_PORT = 8080;     // Shared between all servers in the agi framework

    public static int coordinatorListenPort() {
        return BASE_PORT;
    }

    public static int agentListenPort() {
        return BASE_PORT + 2;
    }

    public static int worldListenPort() {
        return BASE_PORT + 4;
    }

    public static String coordinatorContextPath() {
        return "coordinator";
    }

    public static String basePath( int port, String contextPath ) {
        return "http://localhost:" + port + "/" + contextPath;
    }


}
