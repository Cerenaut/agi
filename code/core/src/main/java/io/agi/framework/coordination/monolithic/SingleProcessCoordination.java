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

package io.agi.framework.coordination.monolithic;

import io.agi.framework.Node;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.persistence.models.ModelData;

/**
 * Runs everything locally.
 *
 * Created by dave on 16/02/16.
 */
public class SingleProcessCoordination implements Coordination {

    public Node _n;

    public SingleProcessCoordination() {

    }

    public Node getNode() {
        return _n;
    }

    public void setNode( Node n ) {
        _n = n;
    }

    public void doUpdate( String entityName ) {
        _n.doUpdate( entityName );
    }

    public void onUpdated( String entityName ) {
        _n.onUpdated( entityName );
    }

    /**
     * Notification that a Data has been updated in the local Node, which
     * invalidates any remote copies.
     *
     * @param dataName
     * @param nodeName
     */
    public void onSetData( String dataName, String nodeName ) {
        // Nothing to broadcast to.
    }

    /**
     * Fetches a Data from the Node that has the most recent copy.
     *
     * @param dataName
     * @param nodeName
     * @return
     */
    public ModelData getData( String dataName, String nodeName ) {
        return null; // Only 1 Node in this case.
    }

}
