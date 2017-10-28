/*
 * Copyright (c) 2017.
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

package io.agi.framework.entities;

import io.agi.framework.EntityConfig;
import io.agi.framework.Framework;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;

/**
 * Created by dave on 2/04/16.
 */
public class DataFileEntityConfig extends EntityConfig {

    public String encoding = DataJsonSerializer.ENCODING_DENSE;

    // for writing to disk:
    public String fileNameWrite = "";
    public String fileNameRead = "";

    public boolean write = true;
    public boolean append = true;
    public boolean read = false;

    public int ioCapacity = 100;
    public String newLine = "\n";

//    public String getFileNameWrite() {
//        String filePathName = filePath + File.separator + fileName;
//        return filePathName;
//    }

    public static void Set(
            String entityName,
            boolean cache,
            boolean write,
            boolean read,
            boolean append,
            String encoding,
            String fileNameWrite,
            String fileNameRead ) {

        DataFileEntityConfig entityConfig = new DataFileEntityConfig();

        entityConfig.cache = cache;
        entityConfig.write = write;
        entityConfig.read = read;
        entityConfig.append = append;
        entityConfig.encoding = encoding;
        entityConfig.fileNameWrite = fileNameWrite;
        entityConfig.fileNameRead = fileNameRead;

        PersistenceUtil.SetConfig( entityName, entityConfig );
    }

}
