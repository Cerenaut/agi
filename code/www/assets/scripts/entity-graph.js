
var EntityGraph = {

  show : function() {

    var entity = $( "#entity" ).val();
    console.log( "Generate graph for subtree starting with entity: " + entity );

    EntityGraph.drawSubGraph( entity );
  },

  drawSubGraph : function( entity ) {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getSubtree( entity, EntityGraph.onGetEntities );
  },

  onGetEntities : function( json ) {
    if( json.length == 0 ) {
      return;
    }

    if( json.status != 200 ) {
      return;
    }

    EntityGraph.entities = JSON.parse( json.responseText );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    filter = "";
    Framework.getDataMeta( filter, EntityGraph.onGetData );
  },

  onGetData : function( json ) {
    if( json.length == 0 ) {
      return;
    }

    if( json.status != 200 ) {
      return;
    }

    EntityGraph.datas = JSON.parse( json.responseText );

    var nodes = {};
    var links = [];

    // parse through entities
    var classValue = "entity";

    for( var i = 0; i < EntityGraph.entities.length; ++i ) {
      var entity = EntityGraph.entities[ i ];
      var entityName = entity.name;
      var entityParent = entity.parent

      if ( entityParent == null || entityParent == "null" ) {
        continue;
      }
  
      var link = { source: entityName, target: entityParent, classValue: classValue, strokeWidth: 3 };
      links.push( link );

      console.log( link.source + " ====> " + link.target );  
    }

    // parse through data refs
    var classValue = "data";

    for( var i = 0; i < EntityGraph.datas.length; ++i ) {
      var data = EntityGraph.datas[ i ];
      var dataName = data.name;
      var dataRefNames = data.refKeys;
      if( dataRefNames == "null" ) {
        continue;
      }

      var dataEntityName = "?";

      for( var j = 0; j < EntityGraph.entities.length; ++j ) {
          var entity = EntityGraph.entities[ j ];
          var entityName = entity.name;
  
          // It's bad that we don't break down the data keys properly with some delimiter..
          if( dataName.indexOf( entityName ) == 0 ) {
            dataEntityName = entityName;
            break;
          }
        }

      var dataRefNameList = dataRefNames.split( "," );

      for( var k = 0; k < dataRefNameList.length; ++k ) {
        var dataRefName = dataRefNameList[ k ];
        var dataRefEntityName = "?";
 
        for( var j = 0; j < EntityGraph.entities.length; ++j ) {
          var entity = EntityGraph.entities[ j ];
          var entityName = entity.name;
  
          // It's bad that we don't break down the data keys properly with some delimiter..
          if( dataRefName.indexOf( entityName ) == 0 ) {
            dataRefEntityName = entityName;
            break;
          }
        }

        var link = { source: dataEntityName, target: dataRefEntityName, classValue: classValue, strokeWidth: 1 };
        links.push( link );

        console.log( link.source + " ----> " + link.target ); 
      }

      console.log( "data: " + dataName );

/*      if ( entityParent == null || entityParent == "null" ) {
        continue;
      }
  
      var link = { source: entityName, target: entityParent, classValue: classValue, strokeWidth: 2 };
      links.push( link );

      console.log( link.source + " ----> " + link.target );  */
    }
    
    // Compute the distinct nodes from the links.
    strokeStyle = "#555555";
    fillStyle = "#eeeeee";
    nodeValue = "20";

    links.forEach(function(link) {
      link.source = nodes[link.source] || (nodes[link.source] = { name:link.source, strokeStyle:strokeStyle, fillStyle:fillStyle, value:nodeValue });
      link.target = nodes[link.target] || (nodes[link.target] = { name:link.target, strokeStyle:strokeStyle, fillStyle:fillStyle, value:nodeValue });
    });

    var typesArray = [];
    typesArray.push( classValue ); // not needed?
    new AgiGraph( "#entities-graph", nodes, links, typesArray );

  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( EntityGraph.onParameter );
    EntityGraph.show();
  }

};

$( document ).ready( function() {
  EntityGraph.setup();
} );
