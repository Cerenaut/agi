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

package io.agi.framework.persistence.models;

import io.agi.framework.Node;

/**
 * Created by dave on 17/02/16.
 */
public class ModelNode {

    public String _name;
    public String _host;
    public int _port;

    public ModelNode( String key, String host, int port ) {
        _name = key;
        _host = host;
        _port = port;
    }

    public ModelNode( Node n ) {
        _name = n.getName();
        _host = n.getHost();
        _port = n.getPort();
    }
}
