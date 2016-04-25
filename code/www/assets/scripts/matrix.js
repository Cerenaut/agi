
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Matrix = {

  showLabels : false,

  update : function() {
    var dataName = $( "#data" ).val();
    Framework.getData( dataName, Matrix.onGetData );
  },

  toggleLabels : function() {
    Matrix.showLabels = !Matrix.showLabels;
    Matrix.update();
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var normalize = $( "#normalize" ).is( ":checked" );

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

    var dataSize = Framework.getDataSize( data );
    var w = dataSize.w;
    var h = dataSize.h;

    var xCategories = [];
    var yCategories = [];
    xCategories.length = w;
    yCategories.length = h;

    for( var x = 0; x < w; ++x ) {
      xCategories[ x ] = x;
    }

    for( var y = 0; y < h; ++y ) {
      yCategories[ y ] = h-y-1;
    }

    var values = [];
    values.length = dataElements.length;

    var maxValue = 0.0;
    if( normalize ) {
      for( var i = 0; i < dataElements.elements.length; ++i ) {
        var value = dataElements.elements[ i ];
        maxValue = Math.max( value, maxValue );
      }
    }
    else {
      maxValue = 1.0;
    }

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


    series[ 0 ] = {
            name: dataName,
            borderWidth: Matrix.showLabels ? 1 : 0,
            animation: false,
            data: values,
            dataLabels: {
                enabled: Matrix.showLabels,
                color: '#ffffff'
            }
    }

    var chart = {
        chart: {
            type: 'heatmap',
            marginTop: 40,
            marginBottom: 40,
            plotBorderWidth: 1
        },
        colorAxis: {
            min: 0,
            max: maxValue,
            minColor: '#000000',
            maxColor: '#ff0000'//Highcharts.getOptions().colors[0]
        },
        title: {
            text: '2D matrix plot'
        },
        xAxis: {
            categories: xCategories
            //categories: ['Alexander', 'Marie', 'Maximilian', 'Sophia', 'Lukas', 'Maria', 'Leon', 'Anna', 'Tim', 'Laura']
        },
        yAxis: {
            categories: yCategories
            //categories: ['Alexander', 'Marie', 'Maximilian', 'Sophia', 'Lukas', 'Maria', 'Leon', 'Anna', 'Tim', 'Laura']
        },
        legend: {
            align: 'right',
            layout: 'vertical',
            margin: 0,
            verticalAlign: 'top',
            y: 25,
            symbolHeight: 280
        },
        tooltip: {
            formatter: function () {
                return '<b>( x' + this.series.xAxis.categories[this.point.x] + ', y' + this.series.yAxis.categories[this.point.y] + ' )</b> = ' +
                    this.point.value;
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
      Matrix.resume();
    }
  },

  setup : function() {
    Parameters.extract( Matrix.onParameter );
    Framework.setup();
    Loop.setup( Matrix.update );
  }

};

$( document ).ready( function() {
  Matrix.setup();
  Matrix.update(); // once
} );


