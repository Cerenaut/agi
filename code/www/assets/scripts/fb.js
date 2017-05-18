
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
  pixelsPerBitWidth  : 3,
  pixelsPerBitHeight : 6,
  pixelsPerBitSummary : 10,
  prefix : "",

  regionSuffixes : [ "-activity-new", "-activity-old", "-prediction-old", "-prediction-new", "-ff-input-1", "-ff-input-2", "-fb-input", "-predictor-weights", "-organizer-output-mask", "-organizer-output-weights", "-classifier-output-weights", "-classifier-output-mask" ],
  regionSuffixIdx : 0,
  dataMap : {
  },

  setStatus : function( status ) {
    $( "#status" ).html( status );
  },

  onMouseMove : function( e, mx, my ) {
  },
  onMouseClick : function( e, mx, my ) {
  },

  repaint : function() {

    var dataActivityNew = Region.findData( "-activity-new" );
    if( !dataActivityNew ) {
      return; // can't paint
    }

    var dataPredictionNew = Region.findData( "-prediction-new" );
    if( !dataPredictionNew ) {
      return; // can't paint
    }

    var dataOrganizerOutputMask = Region.findData( "-organizer-output-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    var dataFbInput = Region.findData( "-fb-input" );
    if( !dataFbInput ) {
      return; // can't paint
    }

    var dataPredictorWeights = Region.findData( "-predictor-weights" );
    if( !dataPredictorWeights ) {
      return; // can't paint
    }

    // rw = RegionWidth
    // ow = OrganizerWidth
    // cw = Column or Classifier Width
    var regionDataSize = Framework.getDataSize( dataActivityNew );
    var rw = regionDataSize.w;
    var rh = regionDataSize.h;
    var ra = rw * rh;

    var organizerDataSize = Framework.getDataSize( dataOrganizerOutputMask );
    var ow = organizerDataSize.w;
    var oh = organizerDataSize.h;
    var oa = ow * oh;

    // computer classifier size (column size)
    if( ( rw == 0 ) || ( rh == 0 ) ) {
      return;
    }

    if( ( ow == 0 ) || ( oh == 0 ) ) {
      return;
    }
    
    var cw = rw / ow;
    var ch = rh / oh;
    var cd = Region.config.classifierDepthCells;
    var classifierHeight = ( ch / cd );

    // Col:
    // [ s1 ][ feedback , feedback ]
    // w w w w                      --
    //                              s2
    //                              -- 
    var fbDataSize = Framework.getDataSize( dataFbInput );
    var fbInputs = fbDataSize.w;
    var fbWeights = ra + fbInputs;
    var fbStates = cw * ch;     
    var fbStride = fbWeights * fbStates;

//                int offset = s1 * contexts * states
//                           +      c        * states
//                           +                 s2; // the weight from state s1, with context bit c, to state s2.
    var fbWeightsPerColumn = fbStates * fbWeights * fbStates;
    var fbTotal = fbWeightsPerColumn * (ow*oh);

    var c = $( "#region-canvas" )[ 0 ];
    c.width  = fbWeights * Region.pixelsPerBitWidth + Region.pixelsPerBitSummary;
    c.height = (ow * oh) * fbStates * Region.pixelsPerBitHeight;

    var ctx = c.getContext( "2d" );
    ctx.fillStyle = "#808080";
    ctx.fillRect( 0, 0, c.width, c.height );
    ctx.lineWidth = 1;

    var scale = ( 1.0 / parseFloat( Region.config.predictorLearningRate ) ) * 255.0;
 
    console.log( "Predictor LearningRate: " + Region.config.predictorLearningRate );
    console.log( "Scale factor: " + scale );

    var y = 0;

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {

        // render one column:
        var classifierOffset = oy * ow + ox;

        // Many rows per cell in the column
        var activeRegionOffset1 = 0;
        var activeColumnOffset1 = 0;
        var activeFound = false;

        for( var cy1 = 0; cy1 < ch; ++cy1 ) {
          if( activeFound ) {
            break;
          }
          for( var cx1 = 0; cx1 < cw; ++cx1 ) {

            var rx = cx1 + ox * cw;
            var ry = cy1 + oy * ch;

            var regionOffset1 = ry  * rw + rx;
            var columnOffset1 = cy1 * cw + cx1; // this is s1.

            var activityNew = dataActivityNew.elements.elements[ regionOffset1 ];
            if( activityNew > 0.0 ) {
              activeRegionOffset1 = regionOffset1;
              activeColumnOffset1 = columnOffset1;
              activeFound = true;
              break;
            }
          }
        }

        if( !activeFound ) {
          continue;
        }

console.log( "Active cell: " + activeColumnOffset1 );

        // paint a row of weights for each target state.
        stateSumWeights = new Array( fbStates );

        for( var cy2 = 0; cy2 < ch; ++cy2 ) {
          for( var cx2 = 0; cx2 < cw; ++cx2 ) {

            var columnOffset2 = cy2 * cw + cx2;

            var sumWeight = 0;

            // paint a row of weights for THIS target state.
            for( var i = 0; i < fbWeights; ++i ) {

              var x = i * Region.pixelsPerBitWidth + Region.pixelsPerBitSummary; // offset by 1 wide cell
              var y = classifierOffset * fbStates * Region.pixelsPerBitHeight
                    +               columnOffset2 * Region.pixelsPerBitHeight;
// Java:
//                int offset = s1 * contexts * states
//                           +      c        * states
//                           +                 s2; // the weight from state s1, with context bit c, to state s2.

                  var offsetWeights = classifierOffset * fbWeightsPerColumn
                                    + columnOffset1 * fbWeights * fbStates 
                                    +                 i         * fbStates 
                                    + columnOffset2;

                  var value = dataPredictorWeights.elements.elements[ offsetWeights ];

//                  sumWeight += value;
//console.log( value );
//console.log( Region.config.predictorLearningRate );
                  var byteValue = parseFloat( value ) * scale; //parseFloat( value ) * 2; /// parseFloat( Region.config.predictorLearningRate );
//                  ctx.fillStyle = "rgba(255,0,0,"+value.toFixed( 3 ) + ")";
                  var red = Math.floor( byteValue );

                  var green = 0;
                  var blue = 0;

                  if( i < ra ) {
                    blue = dataActivityNew.elements.elements[ i ];
                  }
                  else {
                    green = dataFbInput.elements.elements[ i -ra ];
                  }

                  if( green > 0 ) {
                    green = 20;
                    sumWeight += value;
                  }
                  if( blue > 0 ) {
                    blue = 30;
                    sumWeight += value;
                  }
//if( ( red < 0 ) || ( red > 255 ) ) {
//console.log( "red:" + red );
// red = 0;
// green = 255;/
//}
//if( isNaN( value ) ) {
// red = 0;
// blue = 255;
//}

                  ctx.fillStyle = "rgb("+red+","+green+","+blue+")";
//                  ctx.strokeStyle = "rgb(0,255,0)";

                  ctx.fillRect( x, y, Region.pixelsPerBitWidth, Region.pixelsPerBitHeight );        
                  ctx.fill();
//                  ctx.strokeRect( x, y, Region.pixelsPerBit, Region.pixelsPerBit );        
//                  ctx.stroke();

  
            } // end: foreach( input )

            stateSumWeights[ columnOffset2 ] = sumWeight;

          } // end: foreach( target state )
        }

        var sumStateWeights = 0.0;

        for( var cy2 = 0; cy2 < ch; ++cy2 ) {
          for( var cx2 = 0; cx2 < cw; ++cx2 ) {
            var columnOffset2 = cy2 * cw + cx2;
            sumStateWeights += stateSumWeights[ columnOffset2 ];
          } // end: foreach( target state )
        }

        if( sumStateWeights > 0.0 ) {
          for( var cy2 = 0; cy2 < ch; ++cy2 ) {
            for( var cx2 = 0; cx2 < cw; ++cx2 ) {
              var columnOffset2 = cy2 * cw + cx2;

              sumWeight = stateSumWeights[ columnOffset2 ];

console.log( "Col : " + classifierOffset + " cell: " + columnOffset2 + " weight: " + sumWeight );

              sumWeight = sumWeight / sumStateWeights;
              stateSumWeights[ columnOffset2 ] = sumWeight;


              var byteValue = sumWeight * 255.0
              var green = Math.floor( byteValue );

              ctx.fillStyle = "rgb("+0+","+green+",0)";
              ctx.strokeStyle = "rgb(127,127,127)";

              var x = 0;
              var y = classifierOffset * fbStates * Region.pixelsPerBitHeight
                    +               columnOffset2 * Region.pixelsPerBitHeight;
              
              ctx.fillRect( x, y, Region.pixelsPerBitSummary, Region.pixelsPerBitHeight );        
              ctx.fill();
              ctx.beginPath();
              ctx.strokeRect( x, y, Region.pixelsPerBitSummary, Region.pixelsPerBitHeight );        
              ctx.stroke();
 
            } // end: foreach( target state )
          }
        }

        // end: per-column (one row per column)
        var w = c.width;
        var y = classifierOffset * fbStates * Region.pixelsPerBitHeight;

        ctx.beginPath();
        ctx.strokeStyle = "#FFFF00";
        ctx.moveTo( 0, y );
        ctx.lineTo( w, y );
        ctx.stroke();
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
//    var dataElement = $( "#region-data" )[ 0 ];
//    var infoElement = $( "#region-info" )[ 0 ];
//    var infoArea = infoElement.getBoundingClientRect();
//    var height = window.innerHeight - infoArea.height -getScrollbarWidth() -1;
//    $( "#region-data-left" ).css( "height", height );
//    $( "#region-data-right" ).css( "height", height );
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
    //Region.pixelsPerBit = parseInt( pxPerBit );

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

    $( "#region-canvas" )[ 0 ].addEventListener( 'mousemove', function( e ) {
      Region.onMouseMove( e, e.offsetX, e.offsetY );
    } );

    $( "#region-canvas" )[ 0 ].addEventListener( 'click', function( e ) {
      Region.onMouseClick( e, e.offsetX, e.offsetY );
    } );
  }

};

$( document ).ready( function() {
  Region.setup();
} );


