
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

  prefix : "",

  regionSuffixes : [ "-activity-new", "-activity-old", "-prediction-old", "-prediction-new", "-organizer-output-mask", "-organizer-output-weights", "-classifier-output-weights", "-classifier-output-mask", "-classifier-output-edges", "-classifier-output-edges-ages", "-classifier-output-cell-ages", "-classifier-output-cell-stress" ],
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

    var dataOrganizerOutputMask = Region.findData( "-organizer-output-mask" );
    if( !dataOrganizerOutputMask ) {
      return; // can't paint
    }

    var dataClassifierOutputMask = Region.findData( "-classifier-output-mask" );
    if( !dataClassifierOutputMask ) {
      return; // can't paint
    }

    var dataClassifierOutputEdges = Region.findData( "-classifier-output-edges" );
    if( !dataClassifierOutputEdges ) {
      return; // can't paint
    }

    var dataClassifierOutputEdgesAges = Region.findData( "-classifier-output-edges-ages" );
    if( !dataClassifierOutputEdgesAges ) {
      return; // can't paint
    }

    var dataClassifierOutputCellAges = Region.findData( "-classifier-output-cell-ages" );
    if( !dataClassifierOutputCellAges ) {
      return; // can't paint
    }

    var dataClassifierOutputCellStress = Region.findData( "-classifier-output-cell-stress" );
    if( !dataClassifierOutputCellStress ) {
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
    var classifierSelected = 0; // TODO make UI option
    var classifierSize = cw * ch;

    var nodes = {};
    var links = [];
    var edgeValue = "edge1";

    for( var oy = 0; oy < oh; ++oy ) {
      for( var ox = 0; ox < ow; ++ox ) {

        // render one column:
        var classifierOffset = oy * ow + ox;
        var classifierName = "Co(" + ox + "," + oy +") ";

        if( classifierOffset != classifierSelected ) {
          continue;
        }

        var maxStress = 0.0;

        for( var cy1 = 0; cy1 < ch; ++cy1 ) {
          for( var cx1 = 0; cx1 < cw; ++cx1 ) {
            var cellOffset1 = cy1 * cw + cx1; // this is s1.
            var packedOffset1 = classifierOffset * classifierSize + cellOffset1;
            var cellStress = dataClassifierOutputCellStress.elements.elements[ packedOffset1 ];
            maxStress = Math.max( cellStress, maxStress );
          }
        }

        for( var cy1 = 0; cy1 < ch; ++cy1 ) {
          for( var cx1 = 0; cx1 < cw; ++cx1 ) {

            var rx1 = cx1 + ox * cw;
            var ry1 = cy1 + oy * ch;

            var regionOffset1 = ry1 * rw + rx1;
            var cellOffset1 = cy1 * cw + cx1; // this is s1.
            var cellName1 = "C(" + cx1 + "," + cy1 +")";

            var packedOffset1 = classifierOffset * classifierSize + cellOffset1;

            var activityNew = dataActivityNew.elements.elements[ regionOffset1 ];
            var cellMask = dataClassifierOutputMask.elements.elements[ packedOffset1 ];
            var cellAge = dataClassifierOutputCellAges.elements.elements[ packedOffset1 ];
            var cellStress = dataClassifierOutputCellStress.elements.elements[ packedOffset1 ];

            var maxSize = 50;
            var minSize = 5;
            var ageScale = 0.25;
            var cellSize = cellAge * ageScale;
            if( cellSize > maxSize ) cellSize = maxSize;
//            cellSize = maxSize - cellSize;
            if( cellSize < minSize ) cellSize = minSize;
         
            var strokeStyle = "#333333";
            if( activityNew > 0.5 ) {
              strokeStyle = "#ff0000";
            }

            var fillStyle = "#000055";
            // console.log( "Stress: " + cellStress + " Max: " + maxStress );
            if( cellMask > 0.5 ) {
              var red = ( cellStress / maxStress ) * 255.0;
              red = Math.floor( Math.min( 255, red ) );
              fillStyle = "rgb( "+red+",0,0 )";
            }

/* Delete cell: when no edges
   Delete edgE: when reaches max age
   Age edge: Every step
   Zero edge age: When 1st-2nd best

   OK stress: fill color (special color if dead/detached)
   OK active: border color
   OK age: size
   OK edges: drawn
   OK edge ages: thickness. if max age, then 1 px;*/

            nodes[ cellName1 ] = {
              name: cellName1,
              strokeStyle: strokeStyle,
              fillStyle: fillStyle,
              value: cellSize
            };

            for( var cy2 = 0; cy2 < ch; ++cy2 ) {
              for( var cx2 = 0; cx2 < cw; ++cx2 ) {

                var rx2 = cx2 + ox * cw;
                var ry2 = cy2 + oy * ch;

                var regionOffset2 = ry2 * rw + rx2;
                var cellOffset2 = cy2 * cw + cx2; // this is s1.
                var cellName2 = "C(" + cx2 + "," + cy2 +")";

                var classifierSizeSq = classifierSize * classifierSize;
                var packedOffsetEdges12 = classifierOffset * classifierSizeSq 
                                        + cellOffset1 * classifierSize + cellOffset2;

                var edgeExists = dataClassifierOutputEdges.elements.elements[ packedOffsetEdges12 ];
                var edgeAge    = dataClassifierOutputEdgesAges.elements.elements[ packedOffsetEdges12 ];

                if( edgeExists < 1.0 ) {
                  continue;
                }

                var maxEdgeWidth = 16;
                var edgeWidth = ( 1.0 - ( edgeAge / Region.config.classifierEdgeMaxAge ) ) * maxEdgeWidth;
                var link = { 
                  source: cellName1, 
                  target: cellName2, 
                  classValue: "neighbour",
                  strokeWidth: edgeWidth 
                };

                links.push( link );
                console.log( link.source + " ----> " + link.target );  
              }
            }


          }
        }


      }
    }

    // Compute the distinct nodes from the links.
/*    links.forEach(function(link) {
      link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
      link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
    });*/
    links.forEach(function(link) {
      link.source = nodes[link.source];
      link.target = nodes[link.target];
    });

    var typesArray = [];
    typesArray.push( edgeValue );
    new AgiGraph( "#region-layer-graph", nodes, links, typesArray, screen.width, screen.height * 0.8, false );
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

  update : function() {
    var entity = $( "#root-entity" ).val();
    console.log( "Updating " + entity );

    Framework.setup( $( "#host" ).val(), $( "#port" ).val() );
    Framework.update( entity, Region.onUpdate );
  },

  onUpdate : function() {
  },

  refresh : function() {

    var pxPerBit = $( "#size" ).val();
    //Region.pixelsPerBit = parseInt( pxPerBit );

    var entity = $( "#entity" ).val();

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

  }

};

$( document ).ready( function() {
  Region.setup();
} );


