
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var CompetitiveLearning = {

  update : function() {
    var dataName = $( "#data" ).val();
    var suffix = "name="+dataName;
    Framework.getData( suffix, CompetitiveLearning.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var datas = JSON.parse( json.responseText );
    var data = datas[ 0 ];

    // undo the sparse coding, if present:
    Framework.removeSparseUnitCoding( data );

    var matrices = 1;
    var series = [];
    series.length = matrices;

    var dataName = data.name;
    var dataElements = data.elements;
    var dataSizes = data.sizes;
    var elements = dataElements.elements.length;

    var w = 0; 
    var h = 0; 
    var d = 0; 

    for( var i = 0; i < dataSizes.labels.length; ++i ) {
      var label = dataSizes.labels[ i ];
      if( label == "x" ) w = dataSizes.sizes[ i ];
      if( label == "y" ) h = dataSizes.sizes[ i ];
      if( label == "z" ) d = dataSizes.sizes[ i ];
    }

    var values = [];
    values.length = w * h;
    var i = 0; 
    for( var x = 0; x < w; ++x ) {
      for( var y = 0; y < h; ++y ) {

        var offset = y * w * d 
                   +     x * d;

        var xValue = parseFloat( dataElements.elements[ offset    ].toFixed( 3 ) );
        var yValue = parseFloat( dataElements.elements[ offset +1 ].toFixed( 3 ) );

        values[ i ] = [ xValue, yValue ];
        ++i;
      }
    }

    series[ 0 ] = {
      name: dataName,
      color: 'rgba(223, 83, 83, .5)',
      data: values
    }

    var chart = {
        chart: {
          type: 'heatmap',
          type: 'scatter',
          zoomType: 'xy',
          margin : [ 50, 50, 100, 50 ],
          width  : 500,
          height : 500
        },
        title: {
            text: '2D scatter plot'
        },
        xAxis: {
            min: 0,
            max: 1,
            title: {
                enabled: true,
                text: 'Values'
            },
            startOnTick: true,
            endOnTick: true,
            showLastLabel: true
        },
        yAxis: {
            min: 0,
            max: 1,
            title: {
                text: 'Values'
            },
            startOnTick: true,
            endOnTick: true,
            showLastLabel: true
        },
        legend: {
        },

        plotOptions: {
            scatter: {
                animation: false,
                marker: {
                    radius: 5,
                    states: {
                        hover: {
                            enabled: true,
                            lineColor: 'rgb(100,100,100)'
                        }
                    }
                },
                states: {
                    hover: {
                        marker: {
                            enabled: false
                        }
                    }
                },
                tooltip: {
                    headerFormat: 'Point:<br>',
                    pointFormat: '{point.x}, {point.y}'
                }
            }
        },

        credits: {
            enabled: false
        },
        series: series
    };

    $( function() { $('#container').highcharts( chart ); } );
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
      CompetitiveLearning.resume();
    }
  },

  setup : function() {
    Parameters.extract( CompetitiveLearning.onParameter );
    Framework.setup();
    Loop.setup( CompetitiveLearning.update );
  }

};

$( document ).ready( function() {
  CompetitiveLearning.setup();
  CompetitiveLearning.update(); // once
} );


