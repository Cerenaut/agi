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

package io.agi.framework.entities;

import io.agi.core.sdr.NumberEncoder;
import io.agi.core.sdr.ScalarEncoder;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.EntityConfig;

/**
 * Created by gideon on 26/03/2016.
 */
public class EncoderFactory {

    public static SparseDistributedEncoder create( EncoderEntityConfig config ) {

        if( config.encoderType.equals( ScalarEncoder.class.getSimpleName() ) ) {
            ScalarEncoder encoder = new ScalarEncoder();
            encoder.setup( config.bits, config.density, config.encodeZero );
            return encoder;
        }

        if( config.encoderType.equals( NumberEncoder.class.getSimpleName() ) ) {
            NumberEncoder encoder = new NumberEncoder();
            encoder.setup( config.digits, config.numbers );
            return encoder;
        }

        System.err.println( "ERROR: EncoderFactory.create() - could not create an encoder for " + config.encoderType );

        return null;
    }
}
