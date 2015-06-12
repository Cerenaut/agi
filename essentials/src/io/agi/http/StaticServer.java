/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.http;

import java.util.ArrayList;

/**
 * Serves static or client-side code content (ie HTML, CSS and JS).
 * 
 * A persistent service for the user and to manage multiple clients.
 * 
 * @author dave
 */
public class StaticServer extends StaticServletServer {
    
    public static void run( ArrayList< String > files ) {
        StaticServer s = new StaticServer();
        s.setProperties( files );
        s.setup();
        s.run();
    }

    
}
