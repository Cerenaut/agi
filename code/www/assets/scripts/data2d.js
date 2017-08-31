
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Data2d = {

  showLabels : false,
  size2d : null,

  update : function() {
    var dataName = $( "#data" ).val();
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.getData( dataName, Data2d.onGetData );
  },

  auto : function() {
    var dataMin = parseFloat( $( "#data-min" ).val() ).toFixed( 1 );
    var dataMax = parseFloat( $( "#data-max" ).val() ).toFixed( 1 );

    $( "#user-min" ).val( dataMin );
    $( "#user-max" ).val( dataMax );

    if( Data2d.size2d ) {
      var wIdeal = parseInt( screen.width  * 0.8 );
      var hIdeal = parseInt( screen.height * 0.8 );
      var wElements = Data2d.size2d.w;
      var hElements = Data2d.size2d.h;
      var wPxIdeal = wIdeal / wElements;
      var hPxIdeal = hIdeal / hElements;
      var pxIdeal = parseInt( Math.min( wPxIdeal, hPxIdeal ) );

      pxIdeal = Math.max( 2, pxIdeal );
 
      $( "#size" ).val( pxIdeal );
    }

    Data2d.update();
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

    // Ask it the size in 2d
    var dataSize = Framework.getDataSize( data );
    if( !dataSize ) {
      return; // can't paint
    }

    $( "#data-size" ).val( JSON.stringify( dataSize ) );

    // Allow stride to specify the size
    var w = dataSize.w;
    var h = dataSize.h;

    var userW = parseInt( $( "#user-w" ).val() );
    var userH = parseInt( $( "#user-h" ).val() );
    if( userW > 0 ) {
      w = userW;
      h = userH;
    }

    // Display length of elements:    
    $( "#data-length" ).val( dataElements.length );

    // Show if the data isn't sized to fit.
    if( ( w * h ) != dataElements.length ) {
      $( "#data-length" ).css( "color", "#ff0000" );
    }
    else {
      $( "#data-length" ).css( "color", "#000000" );
    }

    // note last rendered size:
    Data2d.size2d = { w : w, h : h };

    // Get range
    var dataMin = Number.MAX_VALUE;
    var dataMax = - Number.MAX_VALUE;
    for( var i = 0; i < dataElements.length; ++i ) {
      var x = dataElements.elements[ i ];
      if( x > dataMax ) dataMax = x;
      if( x < dataMin ) dataMin = x;
    }

    $( "#data-min" ).val( dataMin );
    $( "#data-max" ).val( dataMax );

    console.log( "Matrix size: w: " + w + " h: " + h + " length: " + dataElements.length );

    dataSize = DataCanvas.resizeCanvasWithSize( "#canvas", w, h, 1, 1 );
    if( !dataSize ) {
      return; // can't paint
    }

    var ctx = dataSize.ctx;

    var values = [];
    values.length = dataElements.length;

    var userMinValue = parseFloat( $( "#user-min" ).val() );
    var userMaxValue = parseFloat( $( "#user-max" ).val() );

    var dataStride = parseInt( $( "#data-stride" ).val() );
    var dataOffset = parseInt( $( "#data-offset" ).val() );

    var x0 = DataCanvas.pxMargin;
    var y0 = DataCanvas.pxMargin;
//    if( normalize ) {
      DataCanvas.fillElementsRgbRange( ctx, x0, y0, w, h, data, null, null, userMinValue, userMaxValue, dataStride, dataOffset );
//    }
//    else {
//      DataCanvas.fillElementsRgbUnit( ctx, x0, y0, w, h, dataR, dataG, dataB );
//    }
/*
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


