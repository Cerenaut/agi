package io.agi.ef.agent;


/**
 * Created by gideon on 25/06/15.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // create an agent at contextPath /agent
        // we could make this string anything we wanted, and in theory create multiple agents
        // ---> except that at this point, the port is hardcoded and it will conflict
        Agent agent = new Agent( "agent" );
    }

}