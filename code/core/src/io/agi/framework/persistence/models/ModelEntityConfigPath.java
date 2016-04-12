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

/**
 * Allows a serial representation of a set of separate
 * Created by gideon on 23/03/2016.
 */
public class ModelEntityConfigPath {

    public String _entityName;
    public String _configPath;
    public String _configValue;

    public ModelEntityConfigPath( String entityName, String configPath, String configValue ) {
        this._entityName = entityName;
        this._configPath = configPath;
        this._configValue = configValue;
    }
}
