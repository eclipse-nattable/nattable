/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.config;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;

public class ConfigRegistryTest {

    private ConfigRegistry configRegistry = new ConfigRegistry();

    ConfigAttribute<String> testAttribute = new ConfigAttribute<>();
    ConfigAttribute<String> testAttribute1 = new ConfigAttribute<>();
    ConfigAttribute<String> testAttribute2 = new ConfigAttribute<>();

    @Test
    public void registrationWithoutDisplayModeOrConfigLabel() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");

        // The attribute can be retrieved by any DisplayMode
        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValue", actual);

        // The attribute can also be retrieved by any config label
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute, DisplayMode.SELECT,
                "testLabel");
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT,
                "testLabel",
                "testLabel2");
        assertEquals("testValue", actual);
    }

    @Test
    public void registrationWithASingleDisplayMode() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue",
                DisplayMode.NORMAL);

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValue", actual);

        // If it finds the attribute under another DisplayMode, it picks it ups
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValue", actual);

        // If the attribute is not registered by label, it falls back to the
        // closest display mode it can find
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT,
                "testLabel");
        assertEquals("testValue", actual);
    }

    @Test
    public void registrationWithMultipleDisplayModes() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormal",
                DisplayMode.NORMAL);
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueEdit",
                DisplayMode.EDIT);

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValueEdit", actual);

        // If the attribute is not registered under this display mode, it falls
        // back to the NORMAL mode
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValueNormal", actual);

        // Again if a label is not registered it falls back to the closest
        // display mode
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "testLabel");
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.EDIT,
                "testLabel");
        assertEquals("testValueEdit", actual);
    }

    @Test
    public void registerWithDisplayModeAndConfigLabel() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormal",
                DisplayMode.NORMAL);
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueEdit",
                DisplayMode.EDIT);
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormalLabel",
                DisplayMode.NORMAL,
                "testLabel");

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL);
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "testLabel");
        assertEquals("testValueNormalLabel", actual);

        // If the label is not found, fall back to the closest Display mode
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "nonExistentTestLabel");
        assertEquals("testValueNormal", actual);
    }

    @Test
    public void registerWithDisplayModeAndMultipleConfigLabels() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormal",
                DisplayMode.NORMAL);
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueEdit",
                DisplayMode.EDIT);
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormalLabel",
                DisplayMode.NORMAL,
                "testLabel");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormalLabel_1",
                DisplayMode.NORMAL,
                "testLabel_1");

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "testLabel");
        assertEquals("testValueNormalLabel", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "testLabel_1");
        assertEquals("testValueNormalLabel_1", actual);

        // If multiple labels are present, stops at the first matching label
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "testLabel",
                "testLabel_1");
        assertEquals("testValueNormalLabel", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.NORMAL,
                "testLabel_1",
                "testLabel");
        assertEquals("testValueNormalLabel_1", actual);
    }

    @Test
    public void regressionRegistrationWithoutDisplayModeOrConfigLabel() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");

        // The attribute can be retrieved by any DisplayMode
        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL");
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.EDIT);
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT);
        assertEquals("testValue", actual);

        // The attribute can also be retrieved by any config label
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute, DisplayMode.SELECT,
                "testLabel");
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                DisplayMode.SELECT,
                "testLabel",
                "testLabel2");
        assertEquals("testValue", actual);
    }

    @Test
    public void regressionRegistrationWithASingleDisplayMode() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue",
                "NORMAL");

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL");
        assertEquals("testValue", actual);

        // If it finds the attribute under another DisplayMode, it picks it ups
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "EDIT");
        assertEquals("testValue", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "SELECT");
        assertEquals("testValue", actual);

        // If the attribute is not registered by label, it falls back to the
        // closest display mode it can find
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "SELECT",
                "testLabel");
        assertEquals("testValue", actual);
    }

    @Test
    public void regressionRegistrationWithMultipleDisplayModes() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormal",
                "NORMAL");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueEdit",
                "EDIT");

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL");
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "EDIT");
        assertEquals("testValueEdit", actual);

        // If the attribute is not registered under this display mode, it falls
        // back to the NORMAL mode
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "SELECT");
        assertEquals("testValueNormal", actual);

        // Again if a label is not registered it falls back to the closest
        // display mode
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "testLabel");
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "EDIT",
                "testLabel");
        assertEquals("testValueEdit", actual);
    }

    @Test
    public void regressionRegisterWithDisplayModeAndConfigLabel() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormal",
                "NORMAL");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueEdit",
                "EDIT");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormalLabel",
                "NORMAL",
                "testLabel");

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL");
        assertEquals("testValueNormal", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "testLabel");
        assertEquals("testValueNormalLabel", actual);

        // If the label is not found, fall back to the closest Display mode
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "nonExistentTestLabel");
        assertEquals("testValueNormal", actual);
    }

    @Test
    public void regressionRegisterWithDisplayModeAndMultipleConfigLabels() {
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValue");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormal",
                "NORMAL");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueEdit",
                "EDIT");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormalLabel",
                "NORMAL",
                "testLabel");
        this.configRegistry.registerConfigAttribute(
                this.testAttribute,
                "testValueNormalLabel_1",
                "NORMAL",
                "testLabel_1");

        String actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "testLabel");
        assertEquals("testValueNormalLabel", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "testLabel_1");
        assertEquals("testValueNormalLabel_1", actual);

        // If multiple labels are present, stops at the first matching label
        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "testLabel",
                "testLabel_1");
        assertEquals("testValueNormalLabel", actual);

        actual = this.configRegistry.getConfigAttribute(
                this.testAttribute,
                "NORMAL",
                "testLabel_1",
                "testLabel");
        assertEquals("testValueNormalLabel_1", actual);
    }
}
