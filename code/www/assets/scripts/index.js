
var Search = {

  type : null,

  start : function() {
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );

    if( Search.type == "entities" ) {
      Framework.getEntities( Search.onGetObjects );    
    }
    else if( Search.type == "data" ) {
      Framework.getDataNames( Search.onGetObjects );        
    }
    else if( Search.type == "nodes" ) {
      Framework.getNodes( Search.onGetObjects );    
    }
  },
  
  getNodes : function() {
    Search.type = "nodes";
    Search.start();
  },

  getData : function() {
    Search.type = "data";
    Search.start();
  },

  getEntities : function() {
    Search.type = "entities";
    Search.start();
  },

  onGetObjects : function( json ) {
    if( json.length == 0 ) {
      return;
    }

    if( json.status != 200 ) {
      return;
    }

    var filter = $("#search-text").val();

    var html = "";
    var objects = JSON.parse( json.responseText );
    for( var i = 0; i < objects.length; ++i ) {
      var object = objects[ i ];
      var objectName = object.name;
      var objectValue = objectName;
      var root = false;
      var type = null;
      if( "parent" in object ) {
        if( object.parent == "null" ) {
          objectValue = objectValue + " <b style='color:#ff0000'>(Root)</b>";
          root = true;
        }
      }
      if( "type" in object ) {
        type = object.type;
      }

      if( filter.length > 0 ) {
        if( objectName.indexOf( filter ) < 0 ) {
          continue;
        }
      }

      var linksValue = "";

      if( Search.type == "entities" ) {
        linksValue = "<a href='config.html?entity="+ objectName + "' title='Open config' target='_blank'>Config</a>"
                   + " / <a href='update.html?entity="+ objectName + "' title='Open update UI' target='_blank'>Update</a>";
        if( root ) {
          linksValue = linksValue 
                     + " / <a href='graph.html?entity="+ objectName + "' title='Open as Graph' target='_blank'>Graph</a>"
                     + " / <a href='root.html?entity="+ objectName + "' title='Open as Root' target='_blank'><b>Root</b></a>";
        }
        if( type ) {
          if( type == "hq-cl-region-layer" ) {
            linksValue = linksValue + " / <a href='hqcl-region.html?entity="+ objectName + "' title='Open Region-Layer UI' target='_blank'><b>Region</b></a>"
          }
          if( type == "auto-region-layer" ) {
            linksValue = linksValue + " / <a href='auto-region.html?entity="+ objectName + "' title='Open Region-Layer UI' target='_blank'><b>Region</b></a>"
          }
          if( type == "pyramid-region-layer" ) {
            linksValue = linksValue + " / <a href='pyramid-region.html?entity="+ objectName + "' title='Open Region-Layer UI' target='_blank'><b>Region</b></a>"
          }
          if( type == "k-sparse-autoencoder" ) {
            linksValue = linksValue + " / <a href='k-sparse-autoencoder.html?entity="+ objectName + "' title='Open Autoencoder UI' target='_blank'><b>K-Sparse UI</b></a>"
          }
        }
      }
      else if( Search.type == "data" ) {
        linksValue = "<a href='matrix.html?data="+ objectName + "' title='Open as Matrix' target='_blank'>Matrix</a> / "
                   + "<a href='vector.html?data="+ objectName + "' title='Open as Vector' target='_blank'>Vector</a> / "
                   + "<a href='data.html?data="+ objectName + "' title='Open as Json' target='_blank'>Json</a>";
      }

      html = html + "<tr><td style='text-align:left;'>" + objectValue;

      if( type ) { 
        html = html + " <small>["+type+"]</small>";
      }

      html = html + "</td><td>" + linksValue + "</td></tr>";
    }

    $( "#table-body" ).html( html );
  },

  update : function() {

    var entity = $( "#entity" ).val();
    console.log( "Updating " + entity );

    Framework.update( entity, Search.updateCallback );
  },

  onParameter : function( key, value ) {
    if( key == "type" ) {
      Search.type = value;      
    }
    if( key == "filter" ) {
      $("#search-text").val( value ); 
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( Search.onParameter );

    if( Search.type != null ) {
      Search.start();
    }
    else {
      Search.getEntities();
    }
  }

};

$( document ).ready( function() {
  Search.setup();
} );


