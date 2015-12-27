package io.agi.ef.http.node;

import io.agi.core.orm.ObjectMap;
import io.agi.ef.http.servlets.CreateServlet;
import io.agi.ef.http.servlets.DataEventServlet;
import io.agi.ef.http.servlets.EntityEventServlet;
import io.agi.ef.http.servlets.StopServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * A simple Jetty Server to host a Node in the Experimental Framework
 *
 * Created by dave on 11/09/15.
 */
public class NodeServer {

    public static final String KEY_SERVER = "server";

    public static final String SERVLET_CONTEXT_PATHSPEC = "/api"; // xLabs Web Services
    public static final String STATIC_CONTEXT_PATHSPEC = "/"; // xLabs Web Services

    public static final int SHUTDOWN_TIMEOUT = 200;

    /**
     * Creates and runs a Jetty HTTP server.
     * The server can serve static files for the UI, in addition to hosting the servlets that make up the API.
     *
     * @param port
     * @param staticDirectory
     * @throws Exception
     */
    public static void run( int port, String staticDirectory ) throws Exception {

        // create the actual server
        Server s = new Server( port );
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        s.setHandler(contexts);

        ContextHandler ch = new ContextHandler();
        ch.setContextPath(STATIC_CONTEXT_PATHSPEC);
        ch.setClassLoader(Thread.currentThread().getContextClassLoader());

        if( staticDirectory != null ) {
            System.out.println( "Node will serve static files from: " + staticDirectory );
            // add file handling context:
            ResourceHandler rh = new ResourceHandler();
            rh.setDirectoriesListed(true);
            rh.setWelcomeFiles(new String[]{"index.html"});
            rh.setResourceBase(staticDirectory);

            ch.setHandler(rh);
        }

        contexts.addHandler( ch );

        // add servlet contexts:
        ServletContextHandler shc = new ServletContextHandler( contexts, SERVLET_CONTEXT_PATHSPEC );

        shc.addServlet(CreateServlet.class, CreateServlet.PATHSPEC);
        shc.addServlet(EntityEventServlet.class, EntityEventServlet.PATHSPEC);
        shc.addServlet(DataEventServlet.class, DataEventServlet.PATHSPEC);
        shc.addServlet(StopServlet.class, StopServlet.PATHSPEC);

        ObjectMap.Put(KEY_SERVER, s);

        s.start();
        s.join();
    }

    /**
     * Convenience method to gracefully shutdown the server.
     * Can be called from a HTTP request handler.
     */
    public static void stop() {

        System.out.println("Deleting... ");
        Node n = Node.getInstance();
        n.delete(); // clear up everything attached to this node. Database will cascade-delete.

        System.out.println("Stopping... ");
        Server s = (Server)ObjectMap.Get( KEY_SERVER );
        try {
            //s.stop();
            s.setStopTimeout(SHUTDOWN_TIMEOUT);
        }
        catch( Exception e ) {
            //e.printStackTrace();
        }

        System.out.println("Stopped. ");

        System.exit(0);
    }

}
