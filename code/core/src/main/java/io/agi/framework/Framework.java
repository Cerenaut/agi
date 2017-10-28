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

package io.agi.framework;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.NamedObject;
import io.agi.core.util.FileUtil;
import io.agi.core.util.MemoryUtil;
import io.agi.framework.coordination.http.HttpExportHandler;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelDataReference;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelEntityConfigPath;
import io.agi.framework.references.DataRef;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Functions used throughout the experimental framework, i.e. not specific to Entity or Node.
 * Created by dave on 2/04/16.
 */
public class Framework {

    protected static final Logger _logger = LogManager.getLogger();

}
