/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import io.agi.core.orm.Callback;

/**
 * Copy contents of floatarray when called.
 * @author dave
 */
public class DataCopyCallback implements Callback {
    
    public static DataCopyCallback create( Data from, Data to ) {
        DataCopyCallback cc = new DataCopyCallback();
        cc._from = from;
        cc._to = to;
        return cc;
    }
    
    public Data _from;
    public Data _to;
        
    public DataCopyCallback() {
        
    }

    @Override public void call() {
        if( ( _from == null ) || ( _to == null) ) {
            return;
        }
        
        _to.copy( _from );
    }
    
}
    