/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.Test;

public class PersistenceHelperTest {

    @Test
    public void testGetAvailableStates() {
        Properties properties = new Properties();

        NatTable natTable = new NatTableFixture();
        natTable.saveState("", properties);
        natTable.saveState("Blubb", properties);
        natTable.saveState("Temp", properties);

        Collection<String> stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));
    }

    @Test
    public void testGetAvailableStatesOnNull() {
        assertTrue("Resulting state name collection is not empty",
                PersistenceHelper.getAvailableStates(null).isEmpty());
    }

    @Test
    public void testGetAvailableStatesOnEmpty() {
        assertTrue("Resulting state name collection is not empty",
                PersistenceHelper.getAvailableStates(new Properties()).isEmpty());
    }

    @Test
    public void testDeleteState() {
        Properties properties = new Properties();

        NatTable natTable = new NatTableFixture();
        natTable.saveState("", properties);
        natTable.saveState("Blubb", properties);
        natTable.saveState("Temp", properties);

        Collection<String> stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));

        PersistenceHelper.deleteState("Blubb", properties);

        stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertFalse(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));
    }

    @Test
    public void testDeleteStateOnNullProperties() {
        Properties properties = new Properties();

        NatTable natTable = new NatTableFixture();
        natTable.saveState("", properties);
        natTable.saveState("Blubb", properties);
        natTable.saveState("Temp", properties);

        Collection<String> stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));

        PersistenceHelper.deleteState("Blubb", null);

        // no impact
        stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));
    }

    @Test
    public void testDeleteStateOnNullState() {
        Properties properties = new Properties();

        NatTable natTable = new NatTableFixture();
        natTable.saveState("", properties);
        natTable.saveState("Blubb", properties);
        natTable.saveState("Temp", properties);

        Collection<String> stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));

        PersistenceHelper.deleteState(null, properties);

        // no impact
        stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));
    }

    @Test
    public void testDeleteStateOnEmptyProperties() {
        Properties properties = new Properties();

        NatTable natTable = new NatTableFixture();
        natTable.saveState("", properties);
        natTable.saveState("Blubb", properties);
        natTable.saveState("Temp", properties);

        Collection<String> stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));

        PersistenceHelper.deleteState("Blubb", new Properties());

        // no impact
        stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));
    }

    @Test
    public void testDeleteStateOnEmptyState() {
        Properties properties = new Properties();

        NatTable natTable = new NatTableFixture();
        natTable.saveState("", properties);
        natTable.saveState("Blubb", properties);
        natTable.saveState("Temp", properties);

        Collection<String> stateNames = PersistenceHelper.getAvailableStates(properties);
        assertTrue(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));

        PersistenceHelper.deleteState("", properties);

        // no impact
        stateNames = PersistenceHelper.getAvailableStates(properties);
        assertFalse(
                "Resulting state name collection does not contain the empty default state",
                stateNames.contains(""));
        assertTrue(
                "Resulting state name collection does not contain the 'Blubb' state",
                stateNames.contains("Blubb"));
        assertTrue(
                "Resulting state name collection does not contain the 'Temp' state",
                stateNames.contains("Temp"));
    }
}
