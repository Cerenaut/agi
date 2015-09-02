
var Agidb = {

  // TODO set host/port properly somehow
  protocol : "http",
  host : "localhost",
  port : "3001",

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

var Agief = {

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

    response.forEach( function( value, index ) {
      entities = entities + "<option value='"+ value.id + "' >" + value.name + "</option>";

      entityRows = entityRows + "<tr><td>" + value.id + "</td><td>" + value.id_entity_type + "</td><td>" + value.id_entity_parent + "</td><td>" + value.name + "</td><td><a href='entity.html?id="+ value.id +"'>View</a></td></tr>";
    } );

    entities = entities + "<option value='null' >None</option>";

    $( "#entities-parent" ).html( entities );
    $( "#entities-table" ).html( entityRows );
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
      nodes = nodes + "<input type='button' class='btn btn-default' onclick='Agief.setProperty( "+value.id+" );' value='Save' /> ";
      nodes = nodes + "<input type='button' class='btn' onclick='Agief.delProperty( "+value.id+" );' value='Delete' />";
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
    Agidb.getJson( "nodes", Agief.onGetNodes );
  },

  delProperty : function( id ) {
      data = {};
      Agidb.deleteJson( "properties?id=eq." + id, data, Agief.getProperties );
  },

  setProperty : function( id ) {

    if( id != null ) {
      var valueValue = $( "#properties-value-"+id ).val();
      var data = { "value": valueValue };
      Agidb.patchJson( "properties?id=eq." + id, data, Agief.getProperties );
    }
    else {
      var  nameValue = $( "#properties-name" ).val();
      var valueValue = $( "#properties-value" ).val();
      var data = { "name": nameValue, "value": valueValue };
      Agidb.postJson( "properties", data, Agief.getProperties );
    } 
  },

  getProperties : function() {
    //console.log( "refresh properties" );
    Agidb.getJson( "properties", Agief.onGetProperties );
  },

  addEntity : function() {
    var   typeValue = $( "#entities-type"   ).val();
    var parentValue = $( "#entities-parent" ).val();
    var   nameValue = $( "#entities-name"   ).val();

    var data = { "name": nameValue, "id_entity_type": typeValue };

    if( parentValue != "null" ) {
      data.id_entity_parent = parentValue;
    }

    Agidb.postJson( "entities", data, Agief.setEntities );
  },

  setEntityTypes : function() {
    Agidb.getJson( "entity_types", Agief.onGetEntityTypes );
  },

  setEntities : function() {
    Agidb.getJson( "entities", Agief.onGetEntities );
  },

  setDatabase : function() {
    var hostValue = $( "#database-host" ).val();
    var portValue = $( "#database-port" ).val();
    Agidb.setup( hostValue, portValue );
    Agief.setEntityTypes();
    Agief.setEntities();
  },

  onReady : function() {
    Agief.setDatabase();
  }

};

$( document ).ready(function() {
  Agief.onReady();
});
