
var Agief = {
  protocol : "http",
  host : "localhost",
  port : "3000",

  controlCallback : function( json ) {
    //var s = JSON.stringify( json );
    //console.log( "Coordinator response to command: " + s );

    var status = "Code "+json.status;
    if( json.status == 200 ) {
      status = "OK";
    }

    var result = "Status: "+status+"<br>Message: \""+json.responseJSON.message + "\"";

    $( "#control-entity-response" ).html( result );
    $( "#control-entity-response" ).attr( "style", "block" );
  },

  entityAction : function( entityName, entityAction ) {
    var suffix = "coordinator/control/entity/" + entityName + "/command/" + entityAction;
    var verb = "GET";
    Agief.control( suffix, Agief.controlCallback, verb );
  },

  control : function( suffix, callback, verb ) {
    // example http://localhost:8080/coordinator/control/entity/exp1/command/bob
    var url = Agief.protocol + "://" + Agief.host + ":" + Agief.port + "/" + suffix;
    // console.log( "POST: "+ s );
    // TODO add error handling, success callbacks
    $.ajax( url, {
        type: verb,
        complete: function( json ) {
          callback( json );
        }
    } );
  },

  setup : function( host, port ) {
    Agief.host = host;
    Agief.port = port;

    $( "#control-coordinator-host" ).html( host );
    $( "#control-coordinator-port" ).html( port );
  }

};

var Agidb = {

  // TODO set host/port properly somehow
  protocol : "http",
  host : "localhost",
  port : "3000",

  getJson : function( table, callback ) {
    var url = Agidb.protocol + "://" + Agidb.host + ":" + Agidb.port + "/" + table;
 
    $.ajax( url, {
        type: "GET",
        success: function( json ) {
          callback( json );
        }
      } );
  },

  postJson : function( table, json, callback ) {
    Agidb.setJson( table, json, callback, "POST" );
  },

  patchJson : function( table, json, callback ) {
    Agidb.setJson( table, json, callback, "PATCH" );
  },

  deleteJson : function( table, json, callback ) {
    Agidb.setJson( table, json, callback, "DELETE" );
  },

  setJson : function( table, json, callback, verb ) {
    var url = Agidb.protocol + "://" + Agidb.host + ":" + Agidb.port + "/" + table;
    var s = JSON.stringify( json );
    // console.log( "POST: "+ s );
    // TODO add error handling, success callbacks
    $.ajax( url, {
        type: verb,
        data: s,
        complete: function() {
          callback();
        }
    } );
  },

  setup : function( host, port ) {
    Agidb.host = host;
    Agidb.port = port;
  }

};

var Agiui = {

  onGetEntityTypes : function( response ) {
    var types = "";

    response.forEach( function( value, index ) {
      types = types + "<option value='"+ value.id + "' >" + value.name + "</option>";
    } );

    $( "#entities-type" ).html( types );
  },

  onGetEntities : function( response ) {
    var entities = "";
    var entityRows = "";

    var nodes = {};
    var links = [];

    response.forEach( function( value, index ) {
      entities = entities + "<option value='"+ value.id + "' >" + value.name + "</option>";

      entityRows = entityRows + "<tr><td>" + value.id + "</td><td>" + value.id_entity_type + "</td><td>" + value.id_entity_parent + "</td><td>" + value.name + "</td><td><a href='entity.html?id="+ value.id +"' target='new'>View</a></td></tr>";

//      nodes[ value.id ] = { name: value.name };
    } );

    entities = entities + "<option value='null' >None</option>";

    $( "#entities-parent" ).html( entities );
    $( "#entities-table" ).html( entityRows );

var links = [
  {source: "myExperiment", target: "myWorld", type: "licensing"} ];
/*    response.forEach( function( value, index ) {
      if( value.id_entity_parent ) {
        var link = { source: value.id, target: value.id_entity_parent, type: "parent" };
        links.push( link );
      }
    } );*/

    // Compute the distinct nodes from the links.
    links.forEach(function(link) {
      link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
      link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
    });


    // build a d3 js graph of the entities:
    //var g = new AgiGraph( "entities-graph", 500, 500 ); 
/*var links = [
  {source: "Microsoft", target: "Amazon", type: "licensing"},
  {source: "Microsoft", target: "HTC", type: "licensing"},
  {source: "Samsung", target: "Apple", type: "suit"},
  {source: "Motorola", target: "Apple", type: "suit"},
  {source: "Nokia", target: "Apple", type: "resolved"},
  {source: "HTC", target: "Apple", type: "suit"},
  {source: "Kodak", target: "Apple", type: "suit"},
  {source: "Microsoft", target: "Barnes & Noble", type: "suit"},
  {source: "Microsoft", target: "Foxconn", type: "suit"},
  {source: "Oracle", target: "Google", type: "suit"},
  {source: "Apple", target: "HTC", type: "suit"},
  {source: "Microsoft", target: "Inventec", type: "suit"},
  {source: "Samsung", target: "Kodak", type: "resolved"},
  {source: "LG", target: "Kodak", type: "resolved"},
  {source: "RIM", target: "Kodak", type: "suit"},
  {source: "Sony", target: "LG", type: "suit"},
  {source: "Kodak", target: "LG", type: "resolved"},
  {source: "Apple", target: "Nokia", type: "resolved"},
  {source: "Qualcomm", target: "Nokia", type: "resolved"},
  {source: "Apple", target: "Motorola", type: "suit"},
  {source: "Microsoft", target: "Motorola", type: "suit"},
  {source: "Motorola", target: "Microsoft", type: "suit"},
  {source: "Huawei", target: "ZTE", type: "suit"},
  {source: "Ericsson", target: "ZTE", type: "suit"},
  {source: "Kodak", target: "Samsung", type: "resolved"},
  {source: "Apple", target: "Samsung", type: "suit"},
  {source: "Kodak", target: "RIM", type: "suit"},
  {source: "Nokia", target: "Qualcomm", type: "suit"}
];*/

    new AgiGraph( "#entities-graph", nodes, links );
  },

  onGetProperties : function( response ) {
    //var o = JSON.parse( response );
    //console.log( "success: " + JSON.stringify( response ) );
    var nodes = "";

    response.forEach( function( value, index ) {
      nodes = nodes + "<tr>";

      nodes = nodes + "<td>";
      nodes = nodes + value.id;
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      nodes = nodes + value.name;
      nodes = nodes + "</td>";

      nodes = nodes + "<td><input type='text' class='form-control input-sm' id='properties-value-"+value.id+"' value='"+value.value+"' /></td>";

      nodes = nodes + "<td>";
      nodes = nodes + "<input type='button' class='btn btn-default' onclick='Agiui.setProperty( "+value.id+" );' value='Save' /> ";
      nodes = nodes + "<input type='button' class='btn' onclick='Agiui.delProperty( "+value.id+" );' value='Delete' />";
      nodes = nodes + "</td>";

      nodes = nodes + "</tr>";

    } );

    $( "#properties-table" ).html( nodes );
  },

  onGetNodes : function( response ) {
    //var o = JSON.parse( response );
    //console.log( "success: " + JSON.stringify( response ) );
    var nodes = "";

    response.forEach( function( value, index ) {
      nodes = nodes + "<tr>";

      nodes = nodes + "<td>";
      nodes = nodes + value.id;
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      nodes = nodes + value.name;
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      nodes = nodes + value.host;
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      nodes = nodes + value.port;
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      //nodes = nodes + value.name_role;
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      nodes = nodes + "</td>";

      nodes = nodes + "</tr>";

    } );

    $( "#nodes-table" ).html( nodes );
  },

  getNodes : function() {
    console.log( "refresh" );
    Agidb.getJson( "nodes", Agiui.onGetNodes );
  },

  delProperty : function( id ) {
      data = {};
      Agidb.deleteJson( "properties?id=eq." + id, data, Agiui.getProperties );
  },

  setProperty : function( id ) {

    if( id != null ) {
      var valueValue = $( "#properties-value-"+id ).val();
      var data = { "value": valueValue };
      Agidb.patchJson( "properties?id=eq." + id, data, Agiui.getProperties );
    }
    else {
      var  nameValue = $( "#properties-name" ).val();
      var valueValue = $( "#properties-value" ).val();
      var data = { "name": nameValue, "value": valueValue };
      Agidb.postJson( "properties", data, Agiui.getProperties );
    } 
  },

  getProperties : function() {
    //console.log( "refresh properties" );
    Agidb.getJson( "properties", Agiui.onGetProperties );
  },

  addEntity : function() {
    var   typeValue = $( "#entities-type"   ).val();
    var parentValue = $( "#entities-parent" ).val();
    var   nameValue = $( "#entities-name"   ).val();

    var data = { "name": nameValue, "id_entity_type": typeValue };

    if( parentValue != "null" ) {
      data.id_entity_parent = parentValue;
    }

    Agidb.postJson( "entities", data, Agiui.setEntities );
  },

  setEntityTypes : function() {
    Agidb.getJson( "entity_types", Agiui.onGetEntityTypes );
  },

  setEntities : function() {
    Agidb.getJson( "entities", Agiui.onGetEntities );
  },

  setDatabase : function() {
    var hostValue = $( "#database-host" ).val();
    var portValue = $( "#database-port" ).val();
    Agidb.setup( hostValue, portValue );
    Agiui.setEntityTypes();
    Agiui.setEntities();
  },

  doEntityAction : function() {
    var entityName = $( "#control-entity-name" ).val();
    var entityAction = $( "#control-entity-action" ).val();
    Agief.entityAction( entityName, entityAction );
  },

  setCoordinator : function() {
    // TODO: Should query from DB, but coordinator doesn't populate yet.
    Agief.setup( "localhost", "8080" );
  },

  setDatabase : function() {
    var hostValue = $( "#database-host" ).val();
    var portValue = $( "#database-port" ).val();
    Agidb.setup( hostValue, portValue );
    Agiui.setEntityTypes();
    Agiui.setEntities();
    Agiui.setCoordinator();
  },

  onReady : function() {
    Agiui.setDatabase();
  }

};

$( document ).ready(function() {
  Agiui.onReady();
});

