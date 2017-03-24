
//       new & old   new,old,pred,fp,fn     
// input contextfree contextual
// input contextfree contextual
// input contextfree contextual

function getScrollbarWidth() {
  var outer = document.createElement("div");
  outer.style.visibility = "hidden";
  outer.style.width = "100px";
  outer.style.msOverflowStyle = "scrollbar"; // needed for WinJS apps

  document.body.appendChild(outer);

  var widthNoScroll = outer.offsetWidth;
  // force scrollbars
  outer.style.overflow = "scroll";

  // add innerdiv
  var inner = document.createElement("div");
  inner.style.width = "100%";
  outer.appendChild(inner);        

  var widthWithScroll = inner.offsetWidth;

  // remove divs
  outer.parentNode.removeChild(outer);

  return widthNoScroll - widthWithScroll;
}

var Region = {

  pixelsReceptiveField : 8,
  pixelsColumnGap : 3,
  pixelsMargin : 4,
  pixelsPerBit : 8,
  pixelsPerGap : 4,
  prefix : "",

  selectedCells : [  ],

  regionSuffixes : [ "-input-predicted", "-input-observed", "-prediction-error-fp", "-prediction-error-fn", "-output-spikes-new", "-output-spikes-old", "-output" ],

  regionSuffixIdx : 0,
  dataMap : {
  },

  setStatus : function( status ) {
    $( "#status" ).html( status );
  },

  selectClear : function() {
    Region.selectedCells = [];
    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectText : function() {
    var value = $( "#sel-cells" ).val();
    var values = value.split( "," );

    Region.selectedCells = [];

    for( var i = 0; i < values.length; ++i ) {
      var value = values[ i ];
      if( value.length < 1 ) {
        continue;
      }

      var cell = parseInt( value );
      Region.selectedCells.push( cell );
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectCells : function( dataSuffix ) {
    var data = Region.findData( dataSuffix );
    if( !data ) {
      return; // can't paint
    }

    Region.selectedCells = [];

    for( var i = 0; i < data.elements.elements.length; ++i ) {
      var value = data.elements.elements[ i ];
      if( value > 0.5 ) {
        Region.selectedCells.push( i );
      }
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectPrevious : function() {
//    Region.selectCells( "-classifier-spikes-old" );
  },

  selectObserved : function() {
    Region.selectCells( "-input-observed" );
  },
  selectPredicted : function() {
    Region.selectCells( "-input-predicted" );
  },
  selectErrorFp : function() {
    Region.selectCells( "-prediction-error-fp" );
  },
  selectErrorFn : function() {
    Region.selectCells( "-prediction-error-fn" );
  },

  toggleSelectCell : function( offset ) {
    Region.toggleSelection( Region.selectedCells, offset );
    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectThreshold : function() {
    var data1 = Region.findData( "-output" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var threshold = $( "#threshold" ).val();

    Region.selectedCells = [];
    Region.thresholdCells( w1, h1, data1, threshold, Region.selectedCells );
    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  thresholdCells : function( w, h, data, threshold, selectedCells ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var offset = y * w + x;
        var value = data.elements.elements[ offset ];

        if( value >= threshold ) {
          selectedCells.push( offset );
        }
      }
    }
  },

  updateSelection : function( id, list ) {
    var values = "";
    for( var i = 0; i < list.length; ++i ) {
      var selected = list[ i ];
      if( i > 0 ) {
        values = values + ",";
      }
      values = values + selected;
    }    
    $( "#"+id ).val( values );
  },

  toggleSelection : function( list, item ) {
    var found = false;

    for( var i = 0; i < list.length; ++i ) {
      var selected = list[ i ];
      if( selected == item ) { 
        found = true;
        list.splice( i, 1 ); // remove selection
        break;
      }
    }
 
    if( !found ) {
      list.push( item );
    }
  },

  // info about mouse actions
  setMouseLeft : function( status ) {
//    $( "#mouse-left" ).html( status );
  },
  setMouseRight : function( status ) {
//    $( "#mouse-right" ).html( status );
  },

  onMouseMoveLeft : function( e, mx, my ) {
  },
  onMouseMoveCentre : function( e, mx, my ) {
  },
  onMouseMoveRight : function( e, mx, my ) {
  },

  onMouseClickLeft : function( e, mx, my ) {
    Region.onMouseClickCentre( e, mx, my );
  },

  onMouseClickCentre : function( e, mx, my ) {

    var data = Region.findData( "-output" );
    if( !data ) {
      return; // can't paint
    }

    var dataSize = Framework.getDataSize( data );
    var w = dataSize.w;
    var h = dataSize.h;

    if( ( w == 0 ) || ( h == 0 ) ) {
      return;
    }

    var cx = Math.floor( mx / ( Region.pixelsPerBit ) );
    var cy = Math.floor( my / ( Region.pixelsPerBit ) );

    var offset = cy * w + cx;

    Region.toggleSelectCell( offset );
  },

  onMouseClickRight : function( e, mx, my ) {
    Region.onMouseClickCentre( e, mx, my );
  },

  repaint : function() {
    DataCanvas.pxPerElement = Region.pixelsPerBit;
    Region.repaintLeft();
    Region.repaintMiddle();
    Region.repaintRight();
  },

  
  // left: output
  // middle: errors
  // right: predc/obss

  repaintLeft : function() {
    var outputSpikesNew = Region.findData( "-output-spikes-new" );
    if( !outputSpikesNew ) {
      return; // can't paint
    }

    var output = Region.findData( "-output" );
    if( !output ) {
      return; // can't paint
    }

    var panels = 1;
    var canvasDataSize = Region.resizeCanvas( "#left-canvas", outputSpikesNew, 1, panels );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

    DataCanvas.fillElementsUnitRgb( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, outputSpikesNew, output, null );
    DataCanvas.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#ffffff" );

  },

  repaintMiddle : function() {

    var dataPredictionFp = Region.findData( "-prediction-error-fp" );
    if( !dataPredictionFp ) {
      return; // can't paint
    }

    var dataPredictionFn = Region.findData( "-prediction-error-fn" );
    if( !dataPredictionFn ) {
      return; // can't paint
    }

    var panels = 1;
    var canvasDataSize = Region.resizeCanvas( "#centre-canvas", dataPredictionFn, 1, panels );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

    DataCanvas.fillElementsUnitRgb( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredictionFn, null, dataPredictionFp );
    DataCanvas.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#ffffff" );
  },

  repaintRight : function() {

    var dataPredicted = Region.findData( "-input-predicted" );
    if( !dataPredicted ) {
      return; // can't paint
    }

    var dataObserved = Region.findData( "-input-observed" );
    if( !dataObserved ) {
      return; // can't paint
    }

    var panels = 1;
    var canvasDataSize = Region.resizeCanvas( "#right-canvas", dataPredicted, 1, panels );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

    // top panel: spikes-new + pred-old
    DataCanvas.fillElementsUnitRgb( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, dataPredicted, dataObserved, null );
    DataCanvas.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#ffffff" );
  },

  resizeCanvas : function( canvasSelector, data, repeatX, repeatY ) {
    var dataSize = Framework.getDataSize( data );
    var w = dataSize.w;
    var h = dataSize.h;

    if( ( w == 0 ) || ( h == 0 ) ) {
      return null;
    }

    if( !repeatX ) repeatX = 1;
    if( !repeatY ) repeatY = 1;

    var c = $( canvasSelector )[ 0 ];
    c.width  = ( w * Region.pixelsPerBit ) * repeatX + Region.pixelsPerGap * (repeatX -1);
    c.height = ( h * Region.pixelsPerBit ) * repeatY + Region.pixelsPerGap * (repeatY -1);

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#505050";
    ctx.fillRect( 0, 0, c.width, c.height );
    
    var size = {
      w: w,
      h: h,
      ctx: ctx
    };

    return size;
  },


  findData : function( suffix ) {
    var dataName = Region.prefix + suffix;
    var data = Region.dataMap[ dataName ];
    return data;
  },

  onGotData : function() {
    Region.setStatus( "Getting config... " );
    var entity = $( "#entity" ).val();
    Framework.getConfig( entity, Region.onGetRegionConfig );
  },

  onGetRegionConfig : function( json ) {
    var entityConfig = JSON.parse( json.responseText );
    Region.config = entityConfig.value;
    Region.setStatus( "Repainting... " );
    Region.repaint();    
    Region.setStatus( "Ready." );
  },

  getData : function() {

    if( Region.regionSuffixIdx >= Region.regionSuffixes.length ) {
      Region.onGotData();
      return;
    }

    var dataName = Region.prefix + Region.regionSuffixes[ Region.regionSuffixIdx ];

    Region.setStatus( "Getting " + Region.regionSuffixIdx + " of " + Region.regionSuffixes.length + ", name: " + dataName );
    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Region.regionSuffixIdx = Region.regionSuffixIdx +1; // get next
    Framework.getData( dataName, Region.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      Region.setStatus( "Error getting data" );
      return;
    }

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];

    Framework.decode( data );

    Region.dataMap[ data.name ] = data;
    Region.getData(); // get next data.
  },

  resizeDataArea : function() {
    var dataElement = $( "#region-data" )[ 0 ];
    var infoElement = $( "#region-info" )[ 0 ];
    var infoArea = infoElement.getBoundingClientRect();
    var height = window.innerHeight - infoArea.height -1;// -getScrollbarWidth() -1;
    $( ".column" ).css( "height", height );
  },

  update : function() {
    var entity = $( "#root-entity" ).val();
    console.log( "Updating " + entity );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.update( entity, Region.onUpdate );
  },

  onUpdate : function() {
    // TODO: Poll for changes then 
  },

  refresh : function() {

    var pxPerBit = $( "#size" ).val();
    Region.pixelsPerBit = pxPerBit;

    var entity = $( "#entity" ).val();
    console.log( "Repainting " + entity );

    Region.resizeDataArea();

    Region.setStatus( "Refreshing..." );
    Region.dataMap = {};
    Region.regionSuffixIdx = 0;
    Region.prefix = entity;
    Region.getData();    
  },

  onParameter : function( key, value ) {
    if( key == "entity" ) {
      $("#entity").val( value ); 
    }
    if( key == "root-entity" ) {
      $("#root-entity").val( value ); 
    }
  },

  setup : function() {
    Parameters.extract( Region.onParameter );

    $( "#left-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveLeft( e, e.offsetX, e.offsetY );
    } );
    $( "#centre-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveCentre( e, e.offsetX, e.offsetY );
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveRight( e, e.offsetX, e.offsetY );
    } );

    $( "#left-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickLeft( e, e.offsetX, e.offsetY );
    } );
    $( "#centre-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickCentre( e, e.offsetX, e.offsetY );
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickRight( e, e.offsetX, e.offsetY );
    } );
  }

};

$( document ).ready( function() {
  Region.setup();
} );


