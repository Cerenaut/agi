
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
/*  var dataSize = data.size;
  var o = JSON.parse( data.elements );
  var elements = o.elements;*/

  var d = new AgiData( data );
  var e = d.getElements();
  var l = d.getLength();

  var wAxes = 50;
  var wElement = 500;
  var wData = 20;
  var hData = 20;

  // Plots are always square, until I allow other shapes.
  var hElement = wElement;
  var wPlot = wElement - ( wAxes * 2 );
  var hPlot = wPlot; // same, both are missing an amount for an axis

//  var marginPlot = 20;
  var wCell = wPlot / wData;

  // offset plot providing space for axes
  var xPlot = wAxes;
  var yPlot = wAxes;
 
  $( "#data-plot" ).html( "" );

/*  var w = 940,
      h = 300,
      pad = 20,
      left_pad = 100;*/

function make_x_axis() {        
    return d3.svg.axis()
        .scale(x)
         .orient("top")
         .ticks(5)
}

function make_y_axis() {        
    return d3.svg.axis()
        .scale(y)
        .orient("left")
        .ticks(5)
}

  var svg = d3.select("#data-plot")
            .append("svg")
            .attr("width", wElement)
            .attr("height", hElement);

  // Domain and Range of a d3 scale explained:
  // https://www.dashingd3js.com/d3js-scales
  var x = d3.scale.linear()
                  .domain( [0, wData] )
                  .range( [0, wPlot] ),      // the space available
      y = d3.scale.linear()
                  .domain( [0, hData] )
                  .range( [0, hPlot] );
//  var x = d3.scale.linear().domain( [0, wElements] ).range( [ xPlot, wPlot ] ),
//      y = d3.scale.linear().domain( [0, hElements] ).range( [ yPlot, hPlot ] );

  var xAxis = d3.svg.axis().scale(x).orient("top"),
      yAxis = d3.svg.axis().scale(y).orient("left");

  // origin coordinates for these axes
  var xAxis_x = wAxes;
  var xAxis_y = wAxes;
  var yAxis_x = wAxes;
  var yAxis_y = wAxes;

svg.append("g")         
        .attr("class", "grid")
      .attr("transform", "translate("+xAxis_x+", "+xAxis_y+")")
        .call(make_x_axis()
            .tickSize(-hPlot, 0, 0)
            .tickFormat("")
        )

    svg.append("g")         
        .attr("class", "grid")
      .attr("transform", "translate("+yAxis_x+", "+yAxis_y+")")
        .call(make_y_axis()
            .tickSize(-wPlot, 0, 0)
            .tickFormat("")
        )


  svg.append("g")
      .attr("class", "axis")
      .attr("transform", "translate("+xAxis_x+", "+xAxis_y+")")
      .call(xAxis);
 
  svg.append("g")
      .attr("class", "axis")
      .attr("transform", "translate("+yAxis_x+", "+yAxis_y+")")
      .call(yAxis);

  svg.append("text")
      .attr("class", "loading")
      .text("Loading ...")
      .attr("x", function () { return wElement/2; })
    .  attr("y", function () { return hElement/2; });

// http://swizec.com/blog/quick-scatterplot-tutorial-for-d3-js/swizec/5337
/*d3.json( elements, function (elements) {

//    var max_r = d3.max(elements.map(
//                       function (d) { return d[2]; })),
    var max_r = d3.max(elements),
        r = d3.scale.linear()
//            .domain([0, d3.max(punchcard_data, function (d) { return d[2]; })])
            .domain([0, d3.max(elements)])
            .range([0, 1]);*/

//d3.
//});
//http://stackoverflow.com/questions/15764698/loading-d3-js-data-from-a-simple-json-string
//http://alignedleft.com/tutorials/d3/binding-data/

svg.selectAll(".loading").remove();
 
    svg.selectAll("circle")
        .data(e)
        .enter()
        .append("circle")
        .attr("class", "circle")
//        .attr("cx", function (d,i) { return ( i % 20 ) * wCell; })
//        .attr("cy", function (d,i) { return ( i / 20 ) * wCell; })
        .attr("cx", function ( d, i ) { 
             xe = i % wData;
             return xPlot + xe * wCell; 
        })
        .attr("cy", function ( d, i ) { 
             ye = Math.floor( i / wData );
             return yPlot + ye * wCell; 
        })
        .attr("title", function ( d, i ) { 
             xe = i % wData;
             ye = Math.floor( i / wData );
             return xe + "," + ye;
        })
        .attr("data-content", function ( d, i ) { 
//             xe = i % wData;
//             ye = Math.floor( i / wData );
             return e[ i ];
        })
        .transition()
        .duration(800)
        .attr("r", function (d,i) { return d * wCell * 0.5; });

// title: compulsary if you want a non-empty tooltip
//d3.selectAll('.circle').attr('title','This is my tooltip title');
 
// content: popovers only. 
//d3.selectAll('.circle').attr('data-content','This is some content');
 
// set the options – read more on the Bootstrap page linked to above
$('svg .circle').popover({
   'trigger':'hover'
   ,'container': 'body'
   ,'placement': 'top'
   ,'white-space': 'nowrap'
   ,'html':'true'
});
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

