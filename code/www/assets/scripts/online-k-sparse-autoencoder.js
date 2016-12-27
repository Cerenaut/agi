
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
  selectedInput1 : [  ],

  regionSuffixes : [ "-input", "-ages", "-weights", "-biases-2", "-spikes-top-ka", "-spikes-top-k" ],//, "-reconstruction-ka", "-reconstruction-k" ],

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
      if( value > 0.01 ) {
        Region.selectedCells.push( i );
      }
    }

    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },

  selectKA : function() {
    Region.selectCells( "-spikes-top-ka" );
  },
  selectK : function() {
    Region.selectCells( "-spikes-top-k" );
  },

  toggleSelectCell : function( offset ) {
    Region.toggleSelection( Region.selectedCells, offset );
    Region.updateSelection( "sel-cells", Region.selectedCells );
    Region.repaint();
  },
  toggleSelectInput : function( offset ) {
    Region.toggleSelection( Region.selectedInput1, offset );
    Region.updateSelection( "sel-input", Region.selectedInput1 );
    Region.repaint();
  },

  selectThreshold : function() {
    Region.selectedInput1 = [];

    var data1 = Region.findData( "-input" );
    if( !data1 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

    var dataWeights = Region.findData( "-weights" );
    if( !dataWeights ) {
      return; // can't paint
    }

    var dataBiases = Region.findData( "-biases-2" );
    if( !dataBiases ) {
      return; // can't paint
    }

    var gain = $( "#gain" ).val();
    var threshold = $( "#threshold" ).val();

    var dataSizeW = Framework.getDataSize( dataWeights );
    var weightsSize = dataSizeW.w * dataSizeW.h;
    var weightsStride = w1 * h1;

    var inputOffset = 0;

    Region.thresholdCellsInputWeights( w1, h1, inputOffset, weightsStride, dataWeights, dataBiases, Region.selectedCells, Region.selectedInput1, gain, threshold );

    Region.updateSelection( "sel-input", Region.selectedInput1 );
    Region.repaint();
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
  onMouseMoveRight : function( e, mx, my ) {
  },

  onMouseClickLeft : function( e, mx, my ) {

    var dataNew = Region.findData( "-spikes-top-ka" );
    if( !dataNew ) {
      return; // can't paint
    }

    var dataSize = Framework.getDataSize( dataNew );
    var w = dataSize.w;
    var h = dataSize.h;

    if( ( w == 0 ) || ( h == 0 ) ) {
      return;
    }

    var cx = Math.floor( mx / ( Region.pixelsPerBit ) );
    var cy = Math.floor( my / ( Region.pixelsPerBit ) );

    var offset = cy * w + cx;

    Region.toggleSelectCell( offset );
    //Region.toggleSelection( Region.selectedCells, offset );
    //Region.updateSelection( "sel-cells", Region.selectedCells );

    //Region.
    //Region.repaint();
  },

  onMouseClickRight : function( e, mx, my ) {
    var data1 = Region.findData( "-input" );
    if( !data1 ) {
      return; // can't paint
    }

    var data2 = Region.findData( "-input-c2" );
    if( !data2 ) {
      return; // can't paint
    }

    var dataSize1 = Framework.getDataSize( data1 );
    var iw1 = dataSize1.w;
    var ih1 = dataSize1.h;

    var dataSize2 = Framework.getDataSize( data2 );
    var iw2 = dataSize2.w;
    var ih2 = dataSize2.h;

    if( ( iw1 == 0 ) || ( ih1 == 0 ) ) {
      return;
    }

    if( ( iw2 == 0 ) || ( ih2 == 0 ) ) {
      return;
    }

    var ix = Math.floor( mx / Region.pixelsPerBit );
    var iy = Math.floor( my / Region.pixelsPerBit );

    if( ( ix < 0 ) || ( iy < 0 ) ) {
      return;
    }

    var i = 1;

    if( iy < ih1 ) { // 1st FF input
      if( ix >= iw1 ) {
        return; // out of bounds
      }      

      var offset = iy * iw1 + ix;
      Region.toggleSelectInput1( offset );
    }
    else { // maybe 2nd FF input
      if( ix >= iw2 ) {
        return; // out of bounds
      }      

      my -= ( Region.pixelsPerBit * ih1 + Region.pixelsMargin );    
      iy = Math.floor( my / Region.pixelsPerBit );
 
      var offset = iy * iw2 + ix;
      Region.toggleSelectInput2( offset );

      i = 2;
    }

    Region.setMouseRight( "Bit: I#" + i + ": [" + ix + "," + iy + "]" );
    Region.repaint();
  },

  repaint : function() {
    DataCanvas.pxPerElement = Region.pixelsPerBit;
    Region.repaintLeft();
    Region.repaintRight();
  },

  repaintLeft : function() {

//  regionSuffixes : [ "-input", "-ages", "-weights", "-biases-2", "-spikes-top-ka", "-spikes-top-k", "-reconstruction-ka", "-reconstruction-k" ],

    var spikesTopKA = Region.findData( "-spikes-top-ka" );
    if( !spikesTopKA ) {
      return; // can't paint
    }

    var spikesTopK = Region.findData( "-spikes-top-k" );
    if( !spikesTopK ) {
      return; // can't paint
    }

 //   var reconstructionK = Region.findData( "-reconstruction-k" );
 //   if( !reconstructionK ) {
 //     return; // can't paint
 //   }

    var panels = 1;
    var canvasDataSize = Region.resizeCanvas( "#left-canvas", spikesTopKA, 1, panels );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

console.log( "Painting left w/h=" + canvasDataSize.w + "," + canvasDataSize.h );

    DataCanvas.fillElementsUnitRgb( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, spikesTopKA, spikesTopK, null );
    DataCanvas.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#00ffff" );

//    DataCanvas.fillElementsUnitRgb( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, reconstructionK, reconstructionK, reconstructionK );
//    DataCanvas.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#00ffff" );
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

  repaintRight : function() {
    var dataWeights = Region.findData( "-weights" );
    if( !dataWeights ) {
      return; // can't paint
    }

    var dataBiases = Region.findData( "-biases-2" );
    if( !dataBiases ) {
      return; // can't paint
    }

    var data1 = Region.findData( "-input" );
    if( !data1 ) {
      return; // can't paint
    }

    var panels = 1;
    var canvasDataSize = Region.resizeCanvas( "#right-canvas", data1, 1, panels );
    if( !canvasDataSize ) {
      return; // can't paint
    }

//    var c = $( "#right-canvas" )[ 0 ];
//    var ctx = c.getContext( "2d" );

    var gain = $( "#gain" ).val();
    var nonlinear = $( "#nonlinear" ).is( ":checked" );
    var inputDisplay = $('select[name=invert-display]').val();

    var dataSize1 = Framework.getDataSize( data1 );
    var w1 = dataSize1.w;
    var h1 = dataSize1.h;

    if( ( w1 == 0 ) || ( h1 == 0 ) ) {
      return;
    }

console.log( "Painting right w/h=" + w1 + "," + h1 );
    var dataSizeW = Framework.getDataSize( dataWeights );
    var weightsSize = dataSizeW.w * dataSizeW.h;
    var weightsStride = w1 * h1;

    var x0 = 0;
    var y0 = 0;

    var inputOffset = 0;

    Region.paintInputData( canvasDataSize.ctx, x0, y0, w1, h1, data1 );

    if( inputDisplay == "weights" ) {
      Region.paintInputWeights( canvasDataSize.ctx, x0, y0, w1, h1, inputOffset, data1, weightsSize, weightsStride, dataWeights, dataBiases, Region.selectedCells, gain, nonlinear );
    }
    else {
      Region.paintInputErrors( canvasDataSize.ctx, x0, y0, w1, h1, inputOffset, data1, weightsSize, weightsStride, dataWeights, dataBiases, Region.selectedCells, gain, nonlinear );
    }
    Region.paintInputDataSelected( canvasDataSize.ctx, x0, y0, w1, h1, Region.selectedInput1 );

  },

  thresholdCellsInputWeights : function( w, h, inputOffset, weightsStride, dataWeights, dataBiases, selectedCells, selectedInputs, gain, threshold ) {

    if( selectedCells.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedCells.length; ++i ) {
          var e = selectedCells[ i ];

	  var r = gain;

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;

          var weight = dataWeights.elements.elements[ weightsOffset ];
          sumWeight += ( weight * r ); // * 1, which doesn't matter
        }
 
        var biasesOffset = inputOffset + inputBit;
        var bias = dataBiases.elements.elements[ biasesOffset ];
        sumWeight += bias;      

        if( sumWeight > threshold ) {
          selectedInputs.push( inputBit );
        }
      }
    }
  },

  logisticSigmoid : function( x ) {
    // http://www.wolframalpha.com/input/?i=1%2F(+1%2Be%5E(-x)+)+for+x+%3D+-5+to+5
        var denominator = 1.0 + Math.pow( Math.E, -x );
        var y = 1.0 / denominator; // equation 3.4
        return y;
  },

  paintInputWeights : function( ctx, x0, y0, w, h, inputOffset, dataInput, weightsSize, weightsStride, dataWeights, dataBiases, selectedCells, gain, nonlinear ) {

    if( selectedCells.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedCells.length; ++i ) {
          var e = selectedCells[ i ];

	  var r = gain;
//          if( e <= 0.0 ) {
//            r = 0.0;
//          }

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/

/*          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/
//          var weightsOffset = ( inputOffset + inputBit ) * weightsSize + e;

          var weight = dataWeights.elements.elements[ weightsOffset ];
//          if( weight > 0.0 ) {
////            sumMaxWeight += weight;//Math.min( minWeight, weight );
//console.log( "weight: " + weight );
//          }
//          else {
//           sumMinWeight += Math.abs( weight );//Math.max( maxWeight, weight );
//          }
          sumWeight += ( weight * r ); // * 1, which doesn't matter
  
//          minWeight = Math.min( weight, minWeight );
//          maxWeight = Math.max( weight, maxWeight );
        }
 
        var biasesOffset = inputOffset + inputBit;
        var bias = dataBiases.elements.elements[ biasesOffset ];
        sumWeight += bias;      
//        minWeight = minWeight / minWeightGlobal;        
//        maxWeight = maxWeight / maxWeightGlobal;        
//        var meanWeight = sumWeight / selectedElements.length;
//        var maxWeight = maxWeight / weightScale;
//        var minWeight = minWeight / weightScale;

//sumWeight = sumWeight * 0.5;

        var tf = sumWeight; //Region.logisticSigmoid( sumWeight );
        if( nonlinear ) {
          tf = Region.logisticSigmoid( sumWeight );
        }
        tf = Math.min(  1.0, tf );
        tf = Math.max( -1.0, tf );

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;

        if( sumWeight > 0.0 ) {
          ctx.fillStyle = "rgba(255,0,0,"+tf+")";
//          ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        }
        else {
          ctx.fillStyle = "rgba(0,0,255,"+Math.abs( tf )+")";
        }

        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();
      }
    }

/*    var minWeightGlobal = 0.0;
    var maxWeightGlobal = 0.0;
//var maxWeightAt = 0;

    for( var j = 0; j < selectedElements.length; ++j ) {
//    for( var e = 0; e < weightsSize; ++e ) {
      var e = selectedElements[ j ];

      for( var i = 0; i < weightsStride; ++i ) {
        var weightsOffset = weightsStride * e + i;
        var weight = dataWeights.elements.elements[ weightsOffset ];
//if( weight > maxWeightGlobal ) maxWeightAt = e;
        minWeightGlobal = Math.min( minWeightGlobal, weight );
        maxWeightGlobal = Math.max( maxWeightGlobal, weight );
      }
    }

    minWeightLimit = 0.01;

    var weightScale = Math.max( minWeightLimit, maxWeightGlobal );
        weightScale = Math.max( weightScale,   -minWeightGlobal );

//    if( minWeightGlobal > -minWeightLimit ) minWeightGlobal = -minWeightLimit;
//    if( maxWeightGlobal <  minWeightLimit ) maxWeightGlobal =  minWeightLimit;

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        
//        var sumMinWeight = 0.0;
//        var sumMaxWeight = 0.0;
        var maxWeight = 0.0;
        var minWeight = 0.0;

        for( var i = 0; i < selectedElements.length; ++i ) {
          var e = selectedElements[ i ];
          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;
//          var weightsOffset = inputBit * weightsSize + e;

          var weight = dataWeights.elements.elements[ weightsOffset ];
//          if( weight > 0.0 ) {
////            sumMaxWeight += weight;//Math.min( minWeight, weight );
//console.log( "weight: " + weight );
//          }
//          else {
//           sumMinWeight += Math.abs( weight );//Math.max( maxWeight, weight );
//          }
//          sumWeight += weight;
  
          minWeight = Math.min( weight, minWeight );
          maxWeight = Math.max( weight, maxWeight );
        }

//        minWeight = minWeight / minWeightGlobal;        
//        maxWeight = maxWeight / maxWeightGlobal;        
//        var meanWeight = sumWeight / selectedElements.length;
        var maxWeight = maxWeight / weightScale;
        var minWeight = minWeight / weightScale;

//        if( unitWeight > 0.0 ) {
          ctx.fillStyle = "rgba(255,0,0,"+maxWeight+")";
          ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
//        }
//        else {
          ctx.fillStyle = "rgba(0,0,255,"+Math.abs( minWeight )+")";
//        }

        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();
      }
    }*/
  },

  paintInputErrors : function( ctx, x0, y0, w, h, inputOffset, dataInput, weightsSize, weightsStride, dataWeights, dataBiases, selectedCells, gain, nonlinear ) {

    if( selectedCells.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedCells.length; ++i ) {
          var e = selectedCells[ i ];

	  var r = gain;

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/

          var weight = dataWeights.elements.elements[ weightsOffset ];

          sumWeight += ( weight * r ); // * 1, which doesn't matter
        }
 
        var biasesOffset = inputOffset + inputBit;
        var bias = dataBiases.elements.elements[ biasesOffset ];
        sumWeight += bias;      

        var tf = sumWeight; //Region.logisticSigmoid( sumWeight );
        if( nonlinear ) {
          tf = Region.logisticSigmoid( sumWeight );
        }

        tf = Math.min(  1.0, tf );
        tf = Math.max( -1.0, tf );

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;

        var inputValue = dataInput.elements.elements[ inputBit ];
        var inputError = Math.abs( inputValue - sumWeight );         

        if( ( inputValue > sumWeight ) && ( inputValue > 0.0 ) ) {
          ctx.fillStyle = "rgba(255,0,0,"+inputError+")";
        }
        else {
          ctx.fillStyle = "rgba(0,0,255,"+Math.abs( inputError * Math.max( inputValue, sumWeight ) )+")";
        }

        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();
      }
    }
  },

  paintInputData : function( ctx, x0, y0, w, h, dataInput ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {	
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var value = dataInput.elements.elements[ offset ];
  
        var byteValue = Math.floor( value * 255.0 ).toString(16);
        //ctx.fillStyle = "#000000";
        //if( value > 0.5 ) {
        //  ctx.fillStyle = "#FFFFFF";
        //}
        //ctx.fillStyle = "rgba( 255,255,255,"+value + ")";
        ctx.fillStyle = "#"+byteValue+byteValue+byteValue;
        
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";

/*        for( var i = 0; i < selectedElements.length; ++i ) {
          if( selectedElements[ i ] == offset ) { // select one at a time
            ctx.strokeStyle = "#FFFFFF";
          }
        }*/

        ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );       
      }
    }
  },

  paintInputDataSelected : function( ctx, x0, y0, w, h, selectedElements ) {

    ctx.strokeStyle = "#00FF00";

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
         
        for( var i = 0; i < selectedElements.length; ++i ) {
          if( selectedElements[ i ] == offset ) { // select one at a time
            ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
          }
        }

      }
    }
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
    $( "#right-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMoveRight( e, e.offsetX, e.offsetY );
    } );

    $( "#left-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickLeft( e, e.offsetX, e.offsetY );
    } );
    $( "#right-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClickRight( e, e.offsetX, e.offsetY );
    } );
  }

};

$( document ).ready( function() {
  Region.setup();
} );


