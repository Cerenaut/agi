package io.agi.ef.coordinator;

import io.agi.ef.clientapi.*;

import java.util.ArrayList;

/**
 * Created by gideon on 1/07/15.
 */
public class Coord {

    static private Coord _coord = null;
    private ArrayList< ApiClient > _agents = new ArrayList<ApiClient>(  );
    private ApiClient _world;

    static public Coord getInstance() {

        if ( _coord == null ) {
            _coord = new Coord();
        }

        return _coord;
    }

    private Coord() {    }


    public void addWorldClient( ApiClient world ) {
        _world = world;
    }

    public void addAgentClient( ApiClient agent ) {
        _agents.add( agent );
    }

    public ArrayList< ApiClient > getAgents() {
        return _agents;
    }

    public ApiClient getWorld() {
        return _world;
    }
}
