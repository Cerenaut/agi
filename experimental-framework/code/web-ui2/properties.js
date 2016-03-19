
var Properties = {

  refresh : function() {
    var search = $( "#search" ).val();
    Postgrest.getJson( "properties?key=like.*"+search+"*&order=key.asc", Properties.onGetData );
  },

  save : function() {

    $( ".new-value" ).each( function( index ) {
                                var key = this.id;
                                var val = this.value;

                                console.log( index + ": " + key + " = " + val );

                                if ( val ) {
                                    var postJson = { "key":key, "value":val };

                                    // replace property
                                    Postgrest.deleteJson( "properties?key=eq." + key, function( response ) {
                                            Postgrest.postJson( "properties", postJson, Properties.onPostData );
                                        });
                                }
                            });
  },

  copy : function( fid, value ) {

    console.log( fid, value ); // , text_field_name, value );

    $( "#"+fid).val( value );
  },

  onPostData : function( response ) {
    console.log( "Post response = " + response );
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
      html = html + "</td><td><input type='button' id=copy-'"+property.key+"' class='btn btn-xs btn-default' style='' onclick='Properties.copy( \"" + property.key + "\", \"" + property.value + "\" );' value='Copy value'/>";
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