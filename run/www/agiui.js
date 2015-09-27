
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
 
     // fields: id, id_entity_type, id_entity_parent, name, type_name, parent_name, parent_type_name
     var sourceValue = value.name;
     var targetValue = "null";
     if( value.parent_name ) {
       targetValue = value.parent_name;
     }
     typeValue = "parent";
     
     var link = { source: sourceValue, target: targetValue, type: typeValue };
     links.push( link );
      
//      nodes[ value.id ] = { name: value.name };
    } );

    entities = entities + "<option value='null' >None</option>";

    $( "#entities-parent" ).html( entities );
    $( "#entities-table" ).html( entityRows );

//var links = [
//  {source: "myExperiment", target: "myWorld", type: "licensing"} ];
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

  // a single Data object to show
  onGetData : function( response ) {

    var data = response[ 0 ];
    var d = new AgiData( data );

    var idElement = "data-plot"
    var wCell = 50; // how big each element is rendered
    var wAxes = 100; // how much space is reserved for axes' labels
    
    d.plot2d( idElement, wCell, wAxes );
  },

  onGetEntitiesData : function( response ) {
    //var o = JSON.parse( response );
    //console.log( "success: " + JSON.stringify( response ) );
    var nodes = "";

    response.forEach( function( value, index ) {
      nodes = nodes + "<tr>";

      nodes = nodes + "<td>";
      nodes = nodes + value.name_data + " (" + value.id_data + ")";
      nodes = nodes + "</td>";

      nodes = nodes + "<td>";
      nodes = nodes + value.name + " (" + value.id + ")";
      nodes = nodes + "</td>";

      nodes = nodes + "<td><input type='text' class='form-control input-sm' id='data-value-"+value.id_data+"' value='"+value.size_data+"' /></td>";

      nodes = nodes + "<td>";
      nodes = nodes + "<input type='button' class='btn btn-default' onclick='Agiui.getData( "+value.id_data+" );' value='Show' /> ";
      nodes = nodes + "</td>";

      nodes = nodes + "</tr>";

    } );

    $( "#data-table" ).html( nodes );
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
      nodes = nodes + "</td>";

      nodes = nodes + "</tr>";

    } );

    $( "#nodes-table" ).html( nodes );
  },

  getNodes : function() {
    console.log( "get nodes" );
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

  getProperty : function( key, callback ) {
      http://localhost:3000/entities?id_entity_type=eq.1 
      var query = "properties?name=eq." + key;
      Agidb.getJson( query, callback );
  },

  getProperties : function() {
    //console.log( "refresh properties" );
    Agidb.getJson( "properties", Agiui.onGetProperties );
  },

  getEntitiesData : function() {
    //console.log( "refresh properties" );
    Agidb.getJson( "entities_data", Agiui.onGetEntitiesData );
  },

  getData : function( id ) {
    //console.log( "refresh properties" );
    Agidb.getJson( "data?id=eq."+id, Agiui.onGetData );
  },

  setEntityTypes : function() {
    Agidb.getJson( "entity_types", Agiui.onGetEntityTypes );
  },

  setEntities : function() {
    Agidb.getJson( "entities_parents_types", Agiui.onGetEntities );
  },

  setDatabase : function() {
    var hostValue = $( "#database-host" ).val();
    var portValue = $( "#database-port" ).val();
    Agidb.setup( hostValue, portValue );
    Agiui.setEntityTypes();
    Agiui.setEntities();
  },

  addEntity : function() {
//    var   typeValue = $( "#entities-type"   ).text();  this retrieves the ID from the select
//    var parentValue = $( "#entities-parent" ).text();
    var   nameValue = $( "#entities-name"   ).val(); // this retrieves the name from the select
    var   configValue = $( "#entities-config"   ).val();

    var eType = document.getElementById( "entities-type" );
    var typeValue = eType.options[eType.selectedIndex].text;
    var eParent = document.getElementById( "entities-parent" );
    var parentValue = eParent.options[eParent.selectedIndex].text;
    //var data = { "name": nameValue, "id_entity_type": typeValue };
    //
    // if( parentValue != "null" ) {
    //   data.id_entity_parent = parentValue;
    // }
    //
    // Request a create action on the code.
    //Agidb.postJson( "entities", data, Agiui.setEntities );
    Agief.entityCreate( nameValue, typeValue, parentValue, configValue, "create-entity-response" );
  },

  doEntityAction : function() {
    var entityName = $( "#control-entity-name" ).val();
    var entityAction = $( "#control-entity-action" ).val();
    Agief.entityAction( entityName, entityAction, "control-entity-response" );
  },
  doEntity : function( entityAction ) {
    var entityName = $( "#control-entity-name" ).val();
    Agief.entityAction( entityName, entityAction, "control-entity-response" );
  },

  setDatabase : function() {
    var hostValue = $( "#database-host" ).val();
    var portValue = $( "#database-port" ).val();
    Agidb.setup( hostValue, portValue );
    Agief.setup();
    Agiui.setEntityTypes();
    Agiui.setEntities();
    Agiui.getNodes();
  },

  onReady : function() {
    Agiui.setDatabase();
  }

};

$( document ).ready(function() {
  Agiui.onReady();
});

