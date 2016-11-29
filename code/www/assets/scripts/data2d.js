
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Data2d = {

  showLabels : false,

  update : function() {
    var dataName = $( "#data" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getData( dataName, Data2d.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var normalize = $( "#normalize" ).is( ":checked" );
    var size = $( "#size" ).val();

    DataCanvas.pxPerElement = size; 

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];

    var matrices = 1;
    var series = [];
    series.length = matrices;

    var dataName = data.name;
    var dataSizes = data.sizes;
    var dataElements = data.elements;

    // undo the sparse coding, if present:
    Framework.decode( data );

    var dataSize = DataCanvas.resizeCanvas( "#canvas", data, 1, 1 );
    if( !dataSize ) {
      return; // can't paint
    }

    var w = dataSize.w;
    var h = dataSize.h;

    var stride = $( "#stride" ).val();
    if( stride > 0 ) {
      w = stride;
      h = Math.floor( dataElements.length / w );
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
    }

  },

  onParameter : function( key, value ) {
    if( key == "data" ) {
      var oldValue = $("#data").val();
      if( oldValue.length > 0 ) {
        oldValue = oldValue + ",";
      }
      var newValue = oldValue + value;
      $("#data").val( newValue ); 
    }
    else if( key == "interval" ) {
      $("#interval").val( value ); 
    }
    else if( key == "start" ) {
      Data2d.resume();
    }
  },

  setup : function() {
    Framework.setNodeHost(); // in case override by param
    Parameters.extract( Data2d.onParameter );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Loop.setup( Data2d.update );
  }

};

$( document ).ready( function() {
  Data2d.setup();
  Data2d.update(); // once
} );


