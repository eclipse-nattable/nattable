/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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

        assertEquals(6, properties.size());
        assertEquals("100", properties.getProperty("prefix.defaultSize"));
        assertEquals("5:50,6:60,",
                properties.getProperty("prefix.defaultSizes"));
        assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
        assertFalse(Boolean.valueOf(properties
                .getProperty("prefix.resizableByDefault")));
        assertEquals("3:true,9:true,",
                properties.getProperty("prefix.resizableIndexes"));
        assertFalse(Boolean.valueOf(properties
                .getProperty("prefix.percentageSizing")));
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

        assertEquals(7, properties.size());
        assertEquals("100", properties.getProperty("prefix.defaultSize"));
        assertEquals("5:50,6:60,",
                properties.getProperty("prefix.defaultSizes"));
        assertEquals("2:88,4:57,5:25,", properties.getProperty("prefix.sizes"));
        assertFalse(Boolean.valueOf(properties
                .getProperty("prefix.resizableByDefault")));
        assertEquals("3:true,9:true,",
                properties.getProperty("prefix.resizableIndexes"));
        assertFalse(Boolean.valueOf(properties
                .getProperty("prefix.percentageSizing")));
        assertEquals("3:false,7:true,8:true,9:false,",
                properties.getProperty("prefix.percentageSizingIndexes"));
    }

    @Test
    public void testLoadEnhancedPercentageSizingState() {
        Properties properties = new Properties();
        properties.setProperty("prefix.defaultSize", "40");
        properties.setProperty("prefix.defaultSizes", "1:10,2:20,3:30,");
        properties.setProperty("prefix.sizes", "1:100,4:400,5:500,");
        properties.setProperty("prefix.resizableByDefault", "true");
        properties.setProperty("prefix.resizableIndexes", "1:false,6:false,");
        properties.setProperty("prefix.percentageSizing", "true");
        properties.setProperty("prefix.percentageSizingIndexes",
                "3:false,7:true,8:true,9:false,");

        this.sizeConfig.loadState("prefix", properties);

        assertTrue(this.sizeConfig.isResizableByDefault());
        assertTrue(this.sizeConfig.isPercentageSizing());

        assertFalse(this.sizeConfig.isPercentageSizing(3));
        assertTrue(this.sizeConfig.isPercentageSizing(7));
        assertTrue(this.sizeConfig.isPercentageSizing(8));
        assertFalse(this.sizeConfig.isPercentageSizing(9));

        assertTrue(this.sizeConfig.isPercentageSizing(2));
        assertTrue(this.sizeConfig.isPercentageSizing(6));
    }
}