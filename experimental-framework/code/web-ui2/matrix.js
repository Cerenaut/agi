
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Matrix = {

  update : function() {
    var key = $( "#data" ).val();
    Postgrest.getJson( "data?key=eq."+key, Matrix.onGetData );
  },

  onGetData : function( response ) {
    if( response.length == 0 ) {
      return;
    }

    var matrices = 1;
    var series = [];
    series.length = matrices;

    var data = response[ 0 ];
    var key = data.key;
    var dataElements = JSON.parse( data.elements );
    var dataSizes = JSON.parse( data.sizes );
    var elements = dataElements.elements.length;

    var w = 0; 
    var h = 0; 

    for( var i = 0; i < dataSizes.labels.length; ++i ) {
      var label = dataSizes.labels[ i ];
      if( label == "x" ) w = dataSizes.sizes[ i ];
      if( label == "y" ) h = dataSizes.sizes[ i ];
    }

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
    values.length = elements;
    var i = 0; 
    for( var x = 0; x < w; ++x ) {
      for( var y = 0; y < h; ++y ) {
        // for HighCharts heatmap, the origin is lower-left (col major). For AGI data, the origin is top-left (row major).
        var y2 = h-y-1;
        var offset = y2 * w + x;
        var value = dataElements.elements[ offset ].toFixed( 3 );
        values[ i ] = [ x, y, value ];
        ++i;
      }
    }


    series[ 0 ] = {
            name: key,
            borderWidth: 1,
            animation: false,
            data: values,
            dataLabels: {
                enabled: true,
                color: '#000000'
            }
    }

    var chart = {
        chart: {
            type: 'heatmap',
            marginTop: 40,
            marginBottom: 80,
            plotBorderWidth: 1
        },
        colorAxis: {
            min: 0,
            max: 1,
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
                return '<b>(' + this.series.xAxis.categories[this.point.x] + ',' + this.series.yAxis.categories[this.point.y] + ')</b> =' +
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
    Postgrest.setup();
    Loop.setup( Matrix.update );
  }

};

$( document ).ready( function() {
  Matrix.setup();
  Matrix.update(); // once
} );


