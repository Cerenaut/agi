
var Agief = {

  PROPERTY_COORDINATOR_NODE : "coordinator-node",

  coordinatorNodeName : null,
  protocol : "http",
  host : null,
  port : null,

  controlCallback : function( json, idResult ) {
    //var s = JSON.stringify( json );
    //console.log( "Coordinator response to command: " + s );

    var status = "Code "+json.status;
    if( json.status == 200 ) {
      status = "OK";
    }

    var result = "Status: "+status+"<br>Message: \""+json.responseText + "\"";

    $( "#"+ idResult ).html( result );
    $( "#"+ idResult ).css( "display", "block" );
  },

  entityCreate : function( entityName, entityType, entityParentName, entityConfig, idResult ) {
//    var suffix = "coordinator/control/entity/" + entityName + "/command/" + entityAction;
    // e.g. http://localhost:8081/api/entity?name=exp&action=step
    var suffix = "api/create" 
               + "?name=" + entityName 
               + "&type=" + entityType
               + "&parent=" + entityParentName
               + "&config=" + entityConfig;
    var verb = "POST";
    Agief.control( suffix, Agief.controlCallback, verb, idResult );
  },

  entityAction : function( entityName, entityAction, idResult ) {
//    var suffix = "coordinator/control/entity/" + entityName + "/command/" + entityAction;
    // e.g. http://localhost:8081/api/entity?name=exp&action=step
    var suffix = "api/entity?name=" + entityName + "&action=" + entityAction;
    var verb = "POST";
    Agief.control( suffix, Agief.controlCallback, verb, idResult );
  },

  control : function( suffix, callback, verb, idResult ) {
    // example http://localhost:8080/coordinator/control/entity/exp1/command/bob
    var url = Agief.protocol + "://" + Agief.host + ":" + Agief.port + "/" + suffix;
    // console.log( "POST: "+ s );
    // TODO add error handling, success callbacks
    $.ajax( url, {
        type: verb,
        complete: function( json ) {
          callback( json, idResult );
        },
        error: function( json ) {
          callback( json, idResult );
        }
    } );
  },

  setup : function() {
    Agiui.getProperty( Agief.PROPERTY_COORDINATOR_NODE, Agief.setup2 );
  },
  setup2 : function( json ) {
    Agief.coordinatorNodeName = json[0].value;
    if( Agief.coordinatorNodeName == null ) {
      console.log( "Coordinator name is not known." );
      return;
    }
    else {
      console.log( "Coordinator is located at node: " + Agief.coordinatorNodeName );
    }

    var query = "nodes?name=eq."+Agief.coordinatorNodeName;
    Agidb.getJson( query, Agief.setup3 );
  },
  setup3 : function( json ) {
    if( Agief.coordinatorNodeName == null ) {
      return;
    }

    json.forEach( function( value, index ) {
      if( value.name == Agief.coordinatorNodeName ) {
        Agief.setCoordinator( value.host, value.port );
      }
    } );

  },

  setCoordinator : function( host, port ) {
    Agief.host = host;
    Agief.port = port;

    $( "#control-coordinator-host" ).html( host );
    $( "#control-coordinator-port" ).html( port );
  }

};


