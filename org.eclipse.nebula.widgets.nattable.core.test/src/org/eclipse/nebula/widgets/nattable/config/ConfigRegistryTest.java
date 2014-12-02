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
package org.eclipse.nebula.widgets.nattable.config;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Before;
import org.junit.Test;

public class ConfigRegistryTest {

    ConfigAttribute<String> testAttribute = new ConfigAttribute<String>();
    ConfigAttribute<String> testAttribute1 = new ConfigAttribute<String>();
    ConfigAttribute<String> testAttribute2 = new ConfigAttribute<String>();
    private ConfigRegistry configRegistry;

    @Before
    public void setup() {
        this.configRegistry = new ConfigRegistry();
    }

    @Test
    public void registrationWithoutDisplayModeOrConfigLabel() throws Exception {
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValue");

        // The attribute can be retrieved by any DisplayMode
        String actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValue", actual);

        // The attribute can also be retrieved by any config label
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.SELECT, "testLabel");
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.SELECT, "testLabel", "testLabel2");
        assertEquals("testValue", actual);
    }

    @Test
    public void registrationWithASingleDisplayMode() throws Exception {
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValue",
                DisplayMode.NORMAL);

        String actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValue", actual);

        // If it finds the attribute under another DisplayMode, it picks it ups
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValue", actual);

        // If the attribute is not registered by label, it falls back to the
        // closest display mode it can find
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.SELECT, "testLabel");
        assertEquals("testValue", actual);
    }

    @Test
    public void registrationWithMultipleDisplayModes() throws Exception {
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValue");
        this.configRegistry.registerConfigAttribute(this.testAttribute,
                "testValueNormal", DisplayMode.NORMAL);
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValueEdit",
                DisplayMode.EDIT);

        String actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValueEdit", actual);

        // If the attribute is not registered under this display mode, it falls
        // back to the NORMAL mode
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValueNormal", actual);

        // Again if a label is not registered it falls back to the closest
        // display mode
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "testLabel");
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.EDIT, "testLabel");
        assertEquals("testValueEdit", actual);
    }

    @Test
    public void registerWithDisplayModeAndConfigLabel() throws Exception {
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValue");
        this.configRegistry.registerConfigAttribute(this.testAttribute,
                "testValueNormal", DisplayMode.NORMAL);
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValueEdit",
                DisplayMode.EDIT);
        this.configRegistry.registerConfigAttribute(this.testAttribute,
                "testValueNormalLabel", DisplayMode.NORMAL, "testLabel");

        String actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "testLabel");
        assertEquals("testValueNormalLabel", actual);

        // If the label is not found, fall back to the closest Display mode
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "nonExistentTestLabel");
        assertEquals("testValueNormal", actual);
    }

    @Test
    public void registerWithDisplayModeAndMultipleConfigLabels()
            throws Exception {
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValue");
        this.configRegistry.registerConfigAttribute(this.testAttribute,
                "testValueNormal", DisplayMode.NORMAL);
        this.configRegistry.registerConfigAttribute(this.testAttribute, "testValueEdit",
                DisplayMode.EDIT);
        this.configRegistry.registerConfigAttribute(this.testAttribute,
                "testValueNormalLabel", DisplayMode.NORMAL, "testLabel");
        this.configRegistry.registerConfigAttribute(this.testAttribute,
                "testValueNormalLabel_1", DisplayMode.NORMAL, "testLabel_1");

        String actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "testLabel");
        assertEquals("testValueNormalLabel", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "testLabel_1");
        assertEquals("testValueNormalLabel_1", actual);

        // If multiple labels are present, stops at the first matching label
        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "testLabel", "testLabel_1");
        assertEquals("testValueNormalLabel", actual);

        actual = this.configRegistry.getConfigAttribute(this.testAttribute,
                DisplayMode.NORMAL, "testLabel_1", "testLabel");
        assertEquals("testValueNormalLabel_1", actual);
    }
}
