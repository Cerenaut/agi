/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.http;

import java.util.ArrayList;

/**
 *
 * @author dave
 */
public class ClientServer {

    public static void run( ArrayList< String > files ) {
        StaticServer s = new StaticServer();
        s.setProperties( files );
        s.setup();
        s.run();
    }
    
}
