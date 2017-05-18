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

package io.agi.core.orm;

/**
 * A callback that can be turned on and off.
 *
 * @author dave
 */
public class ToggleCallback implements Callback {

    public boolean _enabled = true;
    public Callback _c;

    public ToggleCallback() {

    }

    public ToggleCallback( boolean enabled, Callback c ) {
        _enabled = enabled;
        _c = c;
    }

    @Override
    public void call() {
        if( _enabled ) {
            if( _c != null ) {
                _c.call();
            }
        }
    }
}
