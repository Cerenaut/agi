
var Properties = {

  refresh : function() {
    var searchValue = $( "#search" ).val();
    Framework.getProperty( "search=" + searchValue, Properties.onGetData );
  },

  saveNew : function() {
    var key = $( "#key-new-value" ).val();
    var val = $( "#value-new-value" ).val();
    console.log( "New: " + key + " = " + val );
    Framework.setProperty( key + "=" + val );
  },

  saveAll : function() {

    $( ".new-value" ).each( function( index ) {
      var key = this.id;
      var val = this.value;

      console.log( index + ": " + key + " = " + val );

      if( val ) {
        Framework.setProperty( key, val, Properties.onPostData );
      }
    });
  },

  copy : function( fid, value ) {

    console.log( fid, value ); // , text_field_name, value );

    $( "#"+fid ).val( value );
  },

  onPostData : function( response ) {
    console.log( "Post response = " + response );
  },

  onGetData : function( json ) {
    var properties = JSON.parse( json.responseText );

    var html = "";

    for( var i = 0; i < properties.length; ++i ) {
      var property = properties[ i ]; // TODO generalize to multiple responses.
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
    Framework.setup();
    Loop.setup( Properties.refresh );
  }

};

$( document ).ready( function() {
  Properties.setup();
  Properties.refresh(); // once
} );
