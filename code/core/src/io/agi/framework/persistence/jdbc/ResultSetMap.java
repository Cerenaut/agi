/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A Generic but perhaps less efficient way to store and later browser data returned from SQL query.
 * <p/>
 * Created by dave on 18/02/16.
 */
public class ResultSetMap implements ResultSetCallback {

    public ArrayList< HashMap< String, String > > _rows = new ArrayList< HashMap< String, String > >();

    public HashSet< String > _fields = new HashSet< String >();

    public ResultSetMap() {

    }

    public String getRowValue( int row, String field ) {

        if( row >= _rows.size() ) {
            return null;
        }

        HashMap< String, String > values = _rows.get( row );

        String value = values.get( field );
        return value;
    }

    public void onResultSet( ResultSet rs ) throws SQLException {
        while( rs.next() ) {
            HashMap< String, String > values = new HashMap< String, String >();

            for( String field : _fields ) {
                String value = rs.getString( field );
                values.put( field, value );
            }

            _rows.add( values );
        }
    }

}
