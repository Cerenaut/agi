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

package io.agi.core.orm;

/**
 * Creates a simple unit test interface where it returns a system exit code.
 * <p/>
 * Created by dave on 10/01/16.
 */
public interface UnitTest {

    /**
     * Simulates the interface for a process, i.e. the test is run independently of anything else.
     *
     * @param args
     * @return
     */
    void test( String[] args );

}
