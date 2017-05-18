
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var DataQueue = {

  dataCount : 0,
  dataMap : {},

  queueLength : 0,
  queueHead : 0,

  update : function() {
    DataQueue.loadConfig();
  },

  repaint : function() {
    console.log( "repainting..." );

    var normalize = $( "#normalize" ).is( ":checked" );
    var size = $( "#size" ).val();

    DataCanvas.pxPerElement = size; 

    var entityName = $( "#entity" ).val();
    
    var index = DataQueue.queueHead;

    for( var d = 0; d < DataQueue.queueLength; ++d ) {

      var dataName = entityName + "-queue-" + index;
      
      var data = DataQueue.dataMap[ dataName ];

      if( !data ) {
        continue;
      }

      console.log( "Painting data: " + dataName );

      var dataSize = Framework.getDataSize( data );
      var w = dataSize.w;
      var h = dataSize.h;

      var dw = DataCanvas.pxPerElement * w;
      var dh = DataCanvas.pxPerElement * h;
      
      var dataPerLine = Math.floor( window.innerWidth / dw );
      var dataLines = Math.floor( DataQueue.queueLength / dataPerLine );

      if( ( dataLines * dataPerLine ) < DataQueue.queueLength ) {
        dataLines += 1;
      }

      var cw = dataPerLine * dw;
      var ch = dataLines * dh;

      var c = $( "#canvas" )[ 0 ];

      // resize the canvas first time
      if( d == 0 ) {
        c.width  = cw;
        c.height = ch;

        var ctx = c.getContext( "2d" );
        ctx.fillStyle = "#333";
        ctx.fillRect( 0, 0, c.width, c.height );
      }

      var dx = Math.floor( d % dataPerLine );
      var dy = Math.floor( d / dataPerLine );
      var dcx = dx * dw;
      var dcy = dy * dh;

      var ctx = c.getContext( "2d" );

      DataCanvas.fillElementsUnitRgb( ctx, dcx, dcy, w, h, data, data, data );//, null );

      ctx.strokeStyle = "#888";
      ctx.strokeRect( dcx, dcy, dw, dh );

      index = index +1;
      if( index >= DataQueue.queueLength ) index = 0;
    }
/*
    var w = dataSize.w;
    var h = dataSize.h;

    var stride = $( "#stride" ).val();
    if( stride > 0 ) {
      w = stride;
      h = Math.floor( dataElements.length / w );
    }


    var dataName = data.name;
    var dataSizes = data.sizes;
    var dataElements = data.elements;

    var dataSize = DataCanvas.resizeCanvas( "#canvas", data, 1, 1 );
    if( !dataSize ) {
      return; // can't paint
    }

    var ctx = dataSize.ctx;

    var values = [];
    values.length = dataElements.length;

    var userMinValue = $( "#min" ).val();
    var userMaxValue = $( "#max" ).val();

    var x0 = DataCanvas.pxMargin;
    var y0 = DataCanvas.pxMargin;
//    if( normalize ) {
      DataCanvas.fillElementsRgbRange( ctx, x0, y0, w, h, null, data, null, userMinValue, userMaxValue );
//    }
//    else {
//      DataCanvas.fillElementsRgbUnit( ctx, x0, y0, w, h, dataR, dataG, dataB );
//    }

    var i = 0; 
    for( var x = 0; x < w; ++x ) {
      for( var y = 0; y < h; ++y ) {
        // for HighCharts heatmap, the origin is lower-left (col major). For AGI data, the origin is top-left (row major).
        var y2 = h-y-1;
        var offset = y2 * w + x;
        var value = dataElements.elements[ offset ];
        value = value.toFixed( 3 );
        values[ i ] = [ x, y, value ];
        ++i;
      }
    }*/

  },

  onGetEntityConfig : function( json ) {

    var entityConfig = JSON.parse( json.responseText );
    var config = entityConfig.value;
    var qLength = config["queueLength"];
    var qHead   = config["queueHead"];

    DataQueue.queueLength = parseInt( qLength );
    DataQueue.queueHead = parseInt( qHead );

    $( "#q-length" ).val( DataQueue.queueLength );
    $( "#q-head" ).val( DataQueue.queueHead );

    // Now refresh the data
    DataQueue.dataCount = 0;
    DataQueue.dataMap = {};
    DataQueue.getData();
  },

  getData : function() {

    if( DataQueue.dataCount >= DataQueue.queueLength ) {
      DataQueue.onGotData();
      return;
    }

    var entityName = $( "#entity" ).val();
    var dataName = entityName + "-queue-" + DataQueue.dataCount;

    console.log( "Getting " + DataQueue.dataCount + " of " + DataQueue.queueLength + ", name: " + dataName );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    DataQueue.dataCount = DataQueue.dataCount +1; // get next
    Framework.getData( dataName, DataQueue.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      console.log( "Error getting data" );
      return;
    }

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];

    Framework.decode( data );

    DataQueue.dataMap[ data.name ] = data;
    DataQueue.getData(); // get next data.
  },

  onGotData : function() {
    DataQueue.repaint();
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];

    Framework.decode( data );

    DataQueue.dataMap[ data.name ] = data;
    DataQueue.getData(); // get next data (if any).
  },

  loadConfig : function() {
    var entityName = $( "#entity" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getConfig( entityName, DataQueue.onGetEntityConfig );
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value );
      DataQueue.loadConfig();
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( DataQueue.onParameter );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Loop.setup( DataQueue.update );
  }

};

$( document ).ready( function() {
  DataQueue.setup();
  DataQueue.update(); // once
} );


