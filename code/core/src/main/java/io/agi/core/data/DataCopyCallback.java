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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import io.agi.core.orm.Callback;

/**
 * Copy contents of floatarray when called.
 *
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

    @Override
    public void call() {
        if( ( _from == null ) || ( _to == null ) ) {
            return;
        }

        _to.copy( _from );
    }

}
    