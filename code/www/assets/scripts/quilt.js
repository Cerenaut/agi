
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

  regionSuffixes : [ "-input", "-organizer-cell-weights", "-organizer-cell-mask", "-quilt-activity", "-classifier-cell-error", "-classifier-cell-mask", "-classifier-cell-activity", "-classifier-cell-weights" ],

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

  selectWinner : function() {
    Region.selectCells( "-classifier-cell-activity" );
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

    var dataWeights = Region.findData( "-classifier-cell-weights" );
    if( !dataWeights ) {
      return; // can't paint
    }

    var threshold = $( "#threshold" ).val();

    var dataSizeW = Framework.getDataSize( dataWeights );
    var weightsSize = dataSizeW.w * dataSizeW.h;
    var weightsStride = w1 * h1;

    var inputOffset = 0;

    Region.thresholdCellsInputWeights( w1, h1, inputOffset, weightsStride, dataWeights, Region.selectedCells, Region.selectedInput1, threshold );

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

    var dataNew = Region.findData( "-quilt-activity" );
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

    var dataSize1 = Framework.getDataSize( data1 );
    var iw1 = dataSize1.w;
    var ih1 = dataSize1.h;

    if( ( iw1 == 0 ) || ( ih1 == 0 ) ) {
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

    Region.setMouseRight( "Bit: I#" + i + ": [" + ix + "," + iy + "]" );
    Region.repaint();
  },

  repaint : function() {
    DataCanvas.pxPerElement = Region.pixelsPerBit;
    Region.repaintLeft();
    Region.repaintRight();
  },

  repaintLeft : function() {

    var active = Region.findData( "-classifier-cell-activity" );
    if( !active ) {
      return; // can't paint
    }

    var activeQuilt = Region.findData( "-quilt-activity" );
    if( !activeQuilt ) {
      return; // can't paint
    }

    var mask = Region.findData( "-classifier-cell-mask" );
    if( !mask ) {
      return; // can't paint
    }

    var panels = 1;
    var canvasDataSize = Region.resizeCanvas( "#left-canvas", activeQuilt, 1, panels );
    if( !canvasDataSize ) {
      return; // can't paint
    }

    var x0 = 0;
    var y0 = 0;

console.log( "Painting left w/h=" + canvasDataSize.w + "," + canvasDataSize.h );

    DataCanvas.fillElementsUnitRgb( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, active, activeQuilt, mask );
    DataCanvas.strokeElements( canvasDataSize.ctx, x0, y0, canvasDataSize.w, canvasDataSize.h, Region.selectedCells, "#00ffff" );
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
    var dataWeights = Region.findData( "-classifier-cell-weights" );
    if( !dataWeights ) {
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

    var inputDisplay = $('select[name=invert-display]').val();

    var weightGain = $('#weight').val();

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

    var dataOrganizerOutputWeights = Region.findData( "-organizer-cell-weights" );
    if( !dataOrganizerOutputWeights ) {
      return; // can't paint
    }

    var dataOrganizerOutputMask = Region.findData( "-organizer-cell-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    var organizerDataSize = Framework.getDataSize( dataOrganizerOutputMask );
    var ow = organizerDataSize.w;
    var oh = organizerDataSize.h;

    if( ( ow == 0 ) || ( oh == 0 ) ) {
      return;
    }

    var activeQuilt = Region.findData( "-quilt-activity" );
    if( !activeQuilt ) {
      return; // can't paint
    }

    var regionDataSize = Framework.getDataSize( activeQuilt );
    var rw = regionDataSize.w;
    var rh = regionDataSize.h;
    var cw = rw / ow;
    var ch = rh / oh;

    var classifierInputStride = w1 * h1;// + w2 * h2;
    var classifierInputOffset = 0;

    var x0 = 0;
    var y0 = 0;

    var inputOffset = 0;

    Region.paintInputData( canvasDataSize.ctx, x0, y0, w1, h1, data1 );

    if( inputDisplay == "weights" ) {
      Region.paintInputWeights( canvasDataSize.ctx, x0, y0, w1, h1, inputOffset, data1, weightsSize, weightsStride, dataWeights, Region.selectedCells, weightGain );
//      Region.paintInputWeights( canvasDataSize.ctx, x0, y0, w1, h1, 0, data1, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, Region.selectedInput1, Region.selectedCells, classifierInputStride, classifierInputOffset );

    }
    else {
      Region.paintInputErrors( canvasDataSize.ctx, x0, y0, w1, h1, inputOffset, data1, weightsSize, weightsStride, dataWeights, Region.selectedCells );
    }
    Region.paintInputDataSelected( canvasDataSize.ctx, x0, y0, w1, h1, Region.selectedInput1 );
  },

/*  paintInputWeights : function( ctx, x0, y0, w, h, inputIndex, dataInput, dataOrganizerOutputMask, dataOrganizerOutputWeights, ow, oh, rw, rh, cw, ch, selectedInput, selectedCells, classifierInputStride, classifierInputOffset ) {

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;
        var offset = y * w + x;
        var value = dataInput.elements.elements[ offset ];
         
        ctx.fillStyle = "#000000";
        if( value > 0.5 ) {
          ctx.fillStyle = "#FFFFFF";
        }
        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();

        ctx.strokeStyle = "#808080";

        if( selectedInput.length > 0 ) {
          if( selectedInput[ 0 ] == offset ) { // select one at a time
            ctx.strokeStyle = "#00FFFF";
          }
        }

        ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
      }
    }

    // paint organizer receptive field centroids
    var rw = ow * cw;
//    var inputArea = w * h;

    // paint weights of selected cells
    for( var i = 0; i < selectedCells.length; ++i ) {
      var regionOffset = selectedCells[ i ]; // a cell within the region     
      var rx = Math.floor( regionOffset % rw );
      var ry = Math.floor( regionOffset / rw );

      var ox = Math.floor( rx / cw ); // coordinates of column in grid (region).
      var oy = Math.floor( ry / ch );    

      var dataClassifierOutputWeights = Region.findData( "-classifier-cell-weights" );
      if( !dataClassifierOutputWeights ) {
        continue; // can't paint
      }

      var cx = rx - ( ox * cw ); // coordinates in column.
      var cy = ry - ( oy * ch );    

      var classifierHeight = ( ch );/// cd );
      var classifierY = cy - ( classifierHeight * Math.floor( cy / classifierHeight ) );
      var classifierOffset = ( ( classifierY * cw ) + cx ) * classifierInputStride;

      var classifierSize = cw * classifierHeight * classifierInputStride; // Each classifier has this number of cells. 
      var classifierIndex = oy * ow + ox;
      var packedOffset = classifierIndex * classifierSize;

      // find max input
      maxWeight = 0.0;

      for( var y = 0; y < h; ++y ) {
        for( var x = 0; x < w; ++x ) {
          var inputOffset = y * w + x;
          var weightsOffset = packedOffset + classifierOffset + inputOffset + classifierInputOffset;

          var value = dataClassifierOutputWeights.elements.elements[ weightsOffset ];
          if( value > maxWeight ) {
            maxWeight = value;
          }
        }
      }

      var minWeight = 0.01;
      if( maxWeight < minWeight ) maxWeight = minWeight;
      // examine each input:
      for( var y = 0; y < h; ++y ) {
        for( var x = 0; x < w; ++x ) {
          var inputOffset = y * w + x;
          var weightsOffset = packedOffset + classifierOffset + inputOffset + classifierInputOffset;

          var value = dataClassifierOutputWeights.elements.elements[ weightsOffset ];
          value = value / maxWeight;
          if( value < minWeight ) { // if less than this frac of max weight, don't bother drawing
            continue;
          }

          ctx.fillStyle = "rgba(255,0,0,"+value.toFixed( 3 ) + ")";
          var cx = x * Region.pixelsPerBit;
          var cy = y * Region.pixelsPerBit;
       
          ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        }
      }

    }

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {
        var offset = oy * ow + ox;
        var mask = dataOrganizerOutputMask.elements.elements[ offset ];
        if( mask == 0 ) {
          continue; // dead, don't paint
        }

        var organizerDimensions = 2;
        var organizerInputs = 2;
        var organizerStride = organizerDimensions * organizerInputs;
        var organizerWeightsOffset = offset * organizerStride + inputIndex * organizerDimensions;

        var xWeight = dataOrganizerOutputWeights.elements.elements[ organizerWeightsOffset +0 ];
        var yWeight = dataOrganizerOutputWeights.elements.elements[ organizerWeightsOffset +1 ];

        var x = w * xWeight * Region.pixelsPerBit;
        var y = h * yWeight * Region.pixelsPerBit;

        ctx.strokeStyle = "rgb(0,255,0)";

        for( var i = 0; i < Region.selectedCells.length; ++i ) {
          var regionOffset = Region.selectedCells[ i ]; // a cell within the region     

          var rx = Math.floor( regionOffset % rw );
          var ry = Math.floor( regionOffset / rw );

          var oxSelected = Math.floor( rx / cw ); // coordinates of column in grid (region).
          var oySelected = Math.floor( ry / ch );    

          if( ( oxSelected == ox ) && ( oySelected == oy ) ) {
            ctx.strokeStyle = "rgb(255,255,0)";
            //console.log( "selected col is : " + ox + ", " + oy );

            break;
          }
        }

        ctx.beginPath();
        ctx.moveTo( x0 + x,                              y0 + y -Region.pixelsReceptiveField );
        ctx.lineTo( x0 + x,                              y0 + y +Region.pixelsReceptiveField );
        ctx.moveTo( x0 + x -Region.pixelsReceptiveField, y0 + y );
        ctx.lineTo( x0 + x +Region.pixelsReceptiveField, y0 + y );
        ctx.stroke();   
      }
    }

    // paint selected input bits
    ctx.strokeStyle = "#0000ff";
    for( var i = 0; i < selectedInput.length; ++i ) {
      var offset = selectedInput[ i ];
      var x = Math.floor( offset % w );
      var y = Math.floor( offset / w );
      var cx = x * Region.pixelsPerBit;
      var cy = y * Region.pixelsPerBit;
      
      ctx.strokeRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
    }
  },*/

  thresholdCellsInputWeights : function( w, h, inputOffset, weightsStride, dataWeights, selectedCells, selectedInputs, threshold ) {

    if( selectedCells.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedCells.length; ++i ) {
          var e = selectedCells[ i ];

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;

          var weight = dataWeights.elements.elements[ weightsOffset ];
          sumWeight += ( weight * 1 ); // * 1, which doesn't matter
        }
 
        if( sumWeight > threshold ) {
          selectedInputs.push( inputBit );
        }
      }
    }
  },

  paintInputWeights : function( ctx, x0, y0, w, h, inputOffset, dataInput, weightsSize, weightsStride, dataWeights, selectedCells, weightGain ) {

    if( selectedCells.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedCells.length; ++i ) {
          var e = selectedCells[ i ];

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/

          var weight = dataWeights.elements.elements[ weightsOffset ];

          sumWeight += ( weight * 1 ); // * 1, which doesn't matter
        }

        sumWeight *= weightGain;
 
        sumWeight = Math.min(  1.0, sumWeight );
        sumWeight = Math.max( -1.0, sumWeight );

        var cx = x * Region.pixelsPerBit;
        var cy = y * Region.pixelsPerBit;

        if( sumWeight > 0.0 ) {
          ctx.fillStyle = "rgba(255,0,0,"+sumWeight+")";
        }
        else {
          ctx.fillStyle = "rgba(0,0,255,"+Math.abs( sumWeight )+")";
        }

        ctx.fillRect( x0 + cx, y0 + cy, Region.pixelsPerBit, Region.pixelsPerBit );        
        ctx.fill();
      }
    }

  },

  paintInputErrors : function( ctx, x0, y0, w, h, inputOffset, dataInput, weightsSize, weightsStride, dataWeights, selectedCells ) {

    if( selectedCells.length < 1 ) {
      return;
    }

    for( var y = 0; y < h; ++y ) {
      for( var x = 0; x < w; ++x ) {
        var inputBit = y * w + x;

        var sumWeight = 0.0;

        for( var i = 0; i < selectedCells.length; ++i ) {
          var e = selectedCells[ i ];

          var weightsOffset = weightsStride * e 
                            + inputOffset 
                            + inputBit;//*/

          var weight = dataWeights.elements.elements[ weightsOffset ];

          sumWeight += ( weight * 1 ); // * 1, which doesn't matter
        }
 
        sumWeight = Math.min(  1.0, sumWeight );
        sumWeight = Math.max( -1.0, sumWeight );

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


