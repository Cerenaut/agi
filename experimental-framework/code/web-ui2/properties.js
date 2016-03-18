
var Properties = {

  refresh : function() {
    var search = $( "#search" ).val();
    Postgrest.getJson( "properties?key=like.*"+search+"*&order=key.asc", Properties.onGetData );
  },

  save : function() {

    $( ".new-value" ).each(  );
  },

  onGetData : function( response ) {
    if( response.length == 0 ) {
      return;
    }

    var html = "";

    for( var d = 0; d < response.length; ++d ) {
      var property = response[ d ]; // TODO generalize to multiple responses.
      html = html + "<tr><td>";
      html = html + property.key;
      html = html + "</td><td>";
      html = html + property.value;
      html = html + "</td><td><input type='text' id='"+property.key+"' style='display:inline;' class='new-value' placeholder='New value' value=''/>";
      html = html + "</td></tr>";
    }

    $( "#table-body" ).html( html );
  },

  onParameter : function( key, value ) {
    if( key == "search" ) {
      $("#search").val( value );
    }
    else if( key == "interval" ) {
      $("#interval").val( value ); 
    }
    else if( key == "start" ) {
      Loop.resume();
    }
  },

  setup : function() {
    Parameters.extract( Properties.onParameter );
    Postgrest.setup();
    Loop.setup( Properties.refresh );
  }

};

$( document ).ready( function() {
  Properties.setup();
  Properties.refresh(); // once
} );


