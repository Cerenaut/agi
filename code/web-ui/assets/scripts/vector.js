
// Demo URL: file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/vector-bar.html?data=myLight-random-output&data=mySwitch-light-output&data=myLight-light-output&interval=303
var Vector = {

  update : function() {
    var names = $( "#data" ).val();
    var nameList = names.split( "," );
    var suffix = "";
    for( var i = 0; i < nameList.length; ++i ) {
      dataName = nameList[ i ]; 
      if( i > 0 ) {
        suffix = suffix + "&";
      }
      suffix = suffix + "name=" + dataName;
    }
    Framework.getData( suffix, Vector.onGetData );
  },

  onGetData : function( json ) {
    if( json.status != 200 ) {
      return;
    }

    var datas = JSON.parse( json.responseText );

    var series = [];
    series.length = datas.length;
    var maxElements = 1;

    for( var d = 0; d < datas.length; ++d ) {
      var data = datas[ d ]; // TODO generalize to multiple responses.
      var elements = data.elements.length;
      maxElements = Math.max( elements, maxElements );

      Framework.removeSparseUnitCoding( data );
    }

    var categories = [];
    categories.length = maxElements;
    for( var i = 0; i < maxElements; ++i ) {
      categories[ i ] = i;
    }

    for( var d = 0; d < datas.length; ++d ) {
      var data = datas[ d ]; // TODO generalize to multiple responses.
      var dataElements = data.elements;
      var dataSizes = data.sizes;
      var elements = dataElements.elements.length;

      var dataName = data.name;
      var values = [];
      values.length = elements;

      var i = 0;
      for( i = 0; i < elements; ++i ) {
        values[ i ] = dataElements.elements[ i ];
      }

      while( i < maxElements ) { // pad series
        values[ i ] = 0;
        ++i;
      } 
     
      series[ d ] = {
            name: dataName,
            data: values,
            animation: false
      }
    }

    var chart = {
        chart: {
//            type: 'bar'
            type: 'column'
        },
        title: {
            text: 'Vector plot'
        },
        xAxis: {
//            categories: ['0', '1', '2'],
            categories: categories,
            title: {
                text: "Elements"
            }
        },
        yAxis: {
            min: 0,
            max: 1,
            title: {
                text: 'Value',
                align: 'high'
            },
            labels: {
                overflow: 'justify'
            }
        },
        plotOptions: {
            bar: {
                dataLabels: {
                    enabled: true
                }
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            x: -40,
            y: 80,
            floating: true,
            borderWidth: 1,
            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
            shadow: true
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
      Vector.resume();
    }
  },

  setup : function() {
    Parameters.extract( Vector.onParameter );
    Framework.setup();
    Loop.setup( Vector.update );
  }

};

$( document ).ready( function() {
  Vector.setup();
  Vector.update(); // once
} );


