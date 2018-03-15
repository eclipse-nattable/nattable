/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class SizeConfigPersistenceTest {

    private static final int DEFAULT_SIZE = 100;
    private SizeConfig sizeConfig;

    @Before
    public void setup() {
        this.sizeConfig = new SizeConfig(DEFAULT_SIZE);
    }

    @Test
    public void testSaveState() {
        this.sizeConfig.setDefaultSize(5, 50);
        this.sizeConfig.setDefaultSize(6, 60);

        this.sizeConfig.setSize(5, 25);
        this.sizeConfig.setSize(2, 88);
        this.sizeConfig.setSize(4, 57);

        this.sizeConfig.setResizableByDefault(false);

        this.sizeConfig.setPositionResizable(3, true);
        this.sizeConfig.setPositionResizable(9, true);

        Properties properties = new Properties();
        this.sizeConfig.saveState("prefix", properties);

        assertEquals(8, properties.size());
        assertEquals("100", properties.getProperty("prefix.defaultSize"));
        assertEquals("5:50,6:60,", properties.getProperty("prefix.defaultSizes"));
        assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.resizableByDefault")));
        assertEquals("3:true,9:true,", properties.getProperty("prefix.resizableIndexes"));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.percentageSizing")));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.distributeRemainingSpace")));
        assertEquals("0", properties.getProperty("prefix.defaultMinSize"));
    }

    @Test
    public void testLoadState() {
        Properties properties = new Properties();
        properties.setProperty("prefix.defaultSize", "40");
        properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
        properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
        properties.setProperty("prefix.resizableByDefault", "true");
        properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");

        this.sizeConfig.loadState("prefix", properties);

        assertEquals(40, this.sizeConfig.getSize(0));
        assertEquals(100, this.sizeConfig.getSize(1));
        assertEquals(20, this.sizeConfig.getSize(2));
        assertEquals(30, this.sizeConfig.getSize(3));
        assertEquals(400, this.sizeConfig.getSize(4));
        assertEquals(500, this.sizeConfig.getSize(5));
        assertEquals(40, this.sizeConfig.getSize(6));

        assertTrue(this.sizeConfig.isPositionResizable(0));
        assertFalse(this.sizeConfig.isPositionResizable(1));
        assertTrue(this.sizeConfig.isPositionResizable(2));
        assertTrue(this.sizeConfig.isPositionResizable(3));
        assertTrue(this.sizeConfig.isPositionResizable(4));
        assertTrue(this.sizeConfig.isPositionResizable(5));
        assertFalse(this.sizeConfig.isPositionResizable(6));
    }

    @Test
    public void testLoadStatePercentageSizing() {
        Properties properties = new Properties();
        properties.setProperty("prefix.defaultSize", "40");
        properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
        properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
        properties.setProperty("prefix.resizableByDefault", "true");
        properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");
        properties.setProperty("prefix.percentageSizing", "true");

        this.sizeConfig.loadState("prefix", properties);

        assertTrue(this.sizeConfig.isResizableByDefault());
        assertTrue(this.sizeConfig.isPercentageSizing());
    }

    @Test
    public void loadStateFromEmptyPropertiesObject() throws Exception {
        Properties properties = new Properties();
        this.sizeConfig.loadState("prefix", properties);

        assertTrue(this.sizeConfig.isResizableByDefault());
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(0));
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(1));
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(2));
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(3));
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(4));
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(5));
        assertEquals(DEFAULT_SIZE, this.sizeConfig.getSize(6));
    }

    @Test
    public void testSaveEnhancedPercentageSizingState() {
        this.sizeConfig.setDefaultSize(5, 50);
        this.sizeConfig.setDefaultSize(6, 60);

        this.sizeConfig.setSize(5, 25);
        this.sizeConfig.setSize(2, 88);
        this.sizeConfig.setSize(4, 57);

        this.sizeConfig.setResizableByDefault(false);

        this.sizeConfig.setPositionResizable(3, true);
        this.sizeConfig.setPositionResizable(9, true);

        this.sizeConfig.setPercentageSizing(3, false);
        this.sizeConfig.setPercentageSizing(9, false);
        this.sizeConfig.setPercentageSizing(7, true);
        this.sizeConfig.setPercentageSizing(8, true);

        Properties properties = new Properties();
        this.sizeConfig.saveState("prefix", properties);

        assertEquals(9, properties.size());
        assertEquals("100", properties.getProperty("prefix.defaultSize"));
        assertEquals("5:50,6:60,", properties.getProperty("prefix.defaultSizes"));
        assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.resizableByDefault")));
        assertEquals("3:true,9:true,", properties.getProperty("prefix.resizableIndexes"));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.percentageSizing")));
        assertEquals("3:false,7:true,8:true,9:false,", properties.getProperty("prefix.percentageSizingIndexes"));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.distributeRemainingSpace")));
        assertEquals("0", properties.getProperty("prefix.defaultMinSize"));
    }

    @Test
    public void testLoadEnhancedPercentageSizingState() {
        // we set the initial value for column 2 to false
        // this way we test if that value is reset so after load the value is
        // true because of the default in the properties
        this.sizeConfig.setPercentageSizing(2, false);

        Properties properties = new Properties();
        properties.setProperty("prefix.defaultSize", "40");
        properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
        properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
        properties.setProperty("prefix.resizableByDefault", "true");
        properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");
        properties.setProperty("prefix.percentageSizing", "true");
        properties.setProperty("prefix.percentageSizingIndexes", "3:false,7:true,8:true,9:false,");

        this.sizeConfig.loadState("prefix", properties);

        // we have not specified a specific value in the properties
        // therefore percentageSizing is true for column 2 as this is the
        // default percentageSizing value
        assertTrue(this.sizeConfig.isPercentageSizing(2));

        assertTrue(this.sizeConfig.isResizableByDefault());
        assertTrue(this.sizeConfig.isPercentageSizing());

        assertFalse(this.sizeConfig.isPercentageSizing(3));
        assertTrue(this.sizeConfig.isPercentageSizing(7));
        assertTrue(this.sizeConfig.isPercentageSizing(8));
        assertFalse(this.sizeConfig.isPercentageSizing(9));

        assertTrue(this.sizeConfig.isPercentageSizing(2));
        assertTrue(this.sizeConfig.isPercentageSizing(6));
    }

    @Test
    public void testSaveEnhancedPercentageSizingWithMinSizeState() {
        this.sizeConfig.setDefaultSize(5, 50);
        this.sizeConfig.setDefaultSize(6, 60);

        this.sizeConfig.setSize(5, 25);
        this.sizeConfig.setSize(2, 88);
        this.sizeConfig.setSize(4, 57);

        this.sizeConfig.setPercentageSizing(3, false);
        this.sizeConfig.setPercentageSizing(9, false);
        this.sizeConfig.setPercentageSizing(7, true);
        this.sizeConfig.setPercentage(8, 20d);

        this.sizeConfig.setResizableByDefault(false);

        this.sizeConfig.setPositionResizable(3, true);
        this.sizeConfig.setPositionResizable(9, true);

        this.sizeConfig.setDefaultMinSize(50);

        this.sizeConfig.setMinSize(7, 100);
        this.sizeConfig.setMinSize(8, 75);

        Properties properties = new Properties();
        this.sizeConfig.saveState("prefix", properties);

        assertEquals(11, properties.size());
        assertEquals("100", properties.getProperty("prefix.defaultSize"));
        assertEquals("5:50,6:60,", properties.getProperty("prefix.defaultSizes"));
        assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.resizableByDefault")));
        assertEquals("3:true,9:true,", properties.getProperty("prefix.resizableIndexes"));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.percentageSizing")));
        assertEquals("8:20.0,", properties.getProperty("prefix.percentageSizes"));
        assertEquals("3:false,7:true,8:true,9:false,", properties.getProperty("prefix.percentageSizingIndexes"));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.distributeRemainingSpace")));
        assertEquals("50", properties.getProperty("prefix.defaultMinSize"));
        assertEquals("7:100,8:75,", properties.getProperty("prefix.minSizes"));
    }

    @Test
    public void testLoadEnhancedPercentageSizingWithMinSizeState() {
        // we set the initial value for column 2 to false
        // this way we test if that value is reset so after load the value is
        // true because of the default in the properties
        this.sizeConfig.setMinSize(5, 50);

        Properties properties = new Properties();
        properties.setProperty("prefix.defaultSize", "40");
        properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
        properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
        properties.setProperty("prefix.resizableByDefault", "true");
        properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");
        properties.setProperty("prefix.percentageSizing", "true");
        properties.setProperty("prefix.percentageSizingIndexes", "3:false,7:true,8:true,9:false,");
        properties.setProperty("prefix.percentageSizes", "8:20.0,");
        properties.setProperty("prefix.distributeRemainingSpace", "true");
        properties.setProperty("prefix.defaultMinSize", "25");
        properties.setProperty("prefix.minSizes", "7:50,8:75,");

        this.sizeConfig.loadState("prefix", properties);

        assertTrue(this.sizeConfig.isResizableByDefault());
        assertTrue(this.sizeConfig.isPercentageSizing());

        assertFalse(this.sizeConfig.isPercentageSizing(3));
        assertTrue(this.sizeConfig.isPercentageSizing(7));
        assertTrue(this.sizeConfig.isPercentageSizing(8));
        assertFalse(this.sizeConfig.isPercentageSizing(9));

        assertTrue(this.sizeConfig.isPercentageSizing(2));
        assertTrue(this.sizeConfig.isPercentageSizing(6));

        // we have not specified a specific value in the properties
        // therefore the min size is 25 which is the default min size
        assertEquals(25, this.sizeConfig.getMinSize(5));
        assertEquals(25, this.sizeConfig.getMinSize(6));
        assertEquals(50, this.sizeConfig.getMinSize(7));
        assertEquals(75, this.sizeConfig.getMinSize(8));

        assertEquals(25, this.sizeConfig.getDefaultMinSize());

        assertTrue(this.sizeConfig.isDistributeRemainingSpace());

        assertEquals(20d, this.sizeConfig.getConfiguredPercentageSize(8), 0.1);
    }

    @Test
    public void testResetStatesOnLoad() {
        SizeConfig sizeConfig = new SizeConfig(100);

        // save the default
        Properties properties = new Properties();
        sizeConfig.saveState("prefix", properties);

        // apply some changes
        sizeConfig.setMinSize(2, 20);
        sizeConfig.setSize(3, 30);
        sizeConfig.setPositionResizable(4, false);
        sizeConfig.setPercentage(5, 15);

        // as one column is percentage sized, we need to trigger calculation
        // first
        sizeConfig.calculatePercentages(100, 10);

        // verify all values are applied
        assertEquals(20, sizeConfig.getMinSize(2));
        assertEquals(30, sizeConfig.getSize(3));
        assertFalse(sizeConfig.isPositionResizable(4));
        assertEquals(15, sizeConfig.getSize(5));
        assertEquals(-1, sizeConfig.getConfiguredSize(5));
        assertEquals(15d, sizeConfig.getConfiguredPercentageSize(5), 0.1);
        assertTrue(sizeConfig.isPercentageSizing(5));

        // load default state and verify that all settings are reset
        sizeConfig.loadState("prefix", properties);

        assertEquals(0, sizeConfig.getMinSize(2));
        assertEquals(100, sizeConfig.getSize(3));
        assertTrue(sizeConfig.isPositionResizable(4));
        assertEquals(100, sizeConfig.getSize(5));
        assertEquals(-1d, sizeConfig.getConfiguredPercentageSize(5), 0.1);
        assertFalse(sizeConfig.isPercentageSizing(5));
    }

    @Test
    public void testResetStatesOnReset() {
        SizeConfig sizeConfig = new SizeConfig(100);

        // apply some changes
        sizeConfig.setMinSize(2, 20);
        sizeConfig.setSize(3, 30);
        sizeConfig.setPositionResizable(4, false);
        sizeConfig.setPercentage(5, 15);

        // as one column is percentage sized, we need to trigger calculation
        // first
        sizeConfig.calculatePercentages(100, 10);

        // verify all values are applied
        assertEquals(20, sizeConfig.getMinSize(2));
        assertEquals(30, sizeConfig.getSize(3));
        assertFalse(sizeConfig.isPositionResizable(4));
        assertEquals(15, sizeConfig.getSize(5));
        assertEquals(-1, sizeConfig.getConfiguredSize(5));
        assertEquals(15d, sizeConfig.getConfiguredPercentageSize(5), 0.1);
        assertTrue(sizeConfig.isPercentageSizing(5));

        // reset and test that everything is reset
        sizeConfig.reset();

        assertEquals(0, sizeConfig.getMinSize(2));
        assertEquals(100, sizeConfig.getSize(3));
        assertTrue(sizeConfig.isPositionResizable(4));
        assertEquals(100, sizeConfig.getSize(5));
        assertEquals(-1d, sizeConfig.getConfiguredPercentageSize(5), 0.1);
        assertFalse(sizeConfig.isPercentageSizing(5));
    }
}