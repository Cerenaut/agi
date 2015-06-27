

import javax.naming.ldap.Control;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.swagger.api.ControlApi;
import io.swagger.api.ControlApiService;
import io.swagger.api.DataApiService;
import io.swagger.api.factories.ControlApiServiceFactory;
import io.swagger.api.factories.DataApiServiceFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.container.servlet.ServletContainer;


public class Main {

    public static void main(String[] args) throws Exception {

        DataApiServiceFactory.setService( new DataApiServiceImpl() );
        ControlApiServiceFactory.setService( new ControlApiServiceImpl() );


        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "io.swagger");//Set the package where the services reside
        sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        Server server = new Server(9999);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet( sh, "/*" );
        server.start();
        server.join();
    }
}
