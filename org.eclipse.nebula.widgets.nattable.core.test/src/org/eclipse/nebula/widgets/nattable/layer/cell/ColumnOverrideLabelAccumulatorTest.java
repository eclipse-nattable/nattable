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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import static org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator.PERSISTENCE_KEY;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnOverrideLabelAccumulatorTest {

    private static final String TEST_PREFIX = "TestPrefix";
    private static final String TEST_LABEL1 = "TestLabel1";
    private static final String TEST_LABEL2 = "TestLabel2";
    private static final String TEST_LABEL3 = "TestLabel3";

    private static final String TEST_MIX_KEY = "BASE";
    private static final String TEST_MIX_LABEL = "TestMixLabel";

    private ColumnOverrideLabelAccumulator labelAccumulator;
    private Properties testProperties;

    @Before
    public void setUp() {
        this.labelAccumulator = new ColumnOverrideLabelAccumulator(new DataLayerFixture());
        this.testProperties = new Properties();
    }

    @Test
    public void testRegisterOverrides() {
        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL1);
        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL2);

        LabelStack configLabels = new LabelStack();
        this.labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        assertEquals(2, configLabels.size());
        assertEquals(TEST_LABEL1, configLabels.get(0));
        assertEquals(TEST_LABEL2, configLabels.get(1));
    }

    @Test
    public void testRegisterOverridesEllipse() {
        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL1);
        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL2, TEST_LABEL3);

        LabelStack configLabels = new LabelStack();
        this.labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        assertEquals(3, configLabels.size());
        assertEquals(TEST_LABEL1, configLabels.get(0));
        assertEquals(TEST_LABEL2, configLabels.get(1));
        assertEquals(TEST_LABEL3, configLabels.get(2));
    }

    @Test
    public void testRegisterOverridesCollection() {
        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL1);
        List<String> labels = new ArrayList<String>();
        labels.add(TEST_LABEL2);
        labels.add(TEST_LABEL3);
        this.labelAccumulator.registerColumnOverrides(0, labels);

        LabelStack configLabels = new LabelStack();
        this.labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        assertEquals(3, configLabels.size());
        assertEquals(TEST_LABEL1, configLabels.get(0));
        assertEquals(TEST_LABEL2, configLabels.get(1));
        assertEquals(TEST_LABEL3, configLabels.get(2));
    }

    @Test
    public void testRegisterOverridesOnTop() {
        this.labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL1);
        this.labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL2);

        LabelStack configLabels = new LabelStack();
        this.labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        assertEquals(2, configLabels.size());
        assertEquals(TEST_LABEL2, configLabels.get(0));
        assertEquals(TEST_LABEL1, configLabels.get(1));
    }

    @Test
    public void testRegisterOverridesEllipseOnTop() {
        this.labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL1);
        this.labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL2,
                TEST_LABEL3);

        LabelStack configLabels = new LabelStack();
        this.labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        assertEquals(3, configLabels.size());
        assertEquals(TEST_LABEL2, configLabels.get(0));
        assertEquals(TEST_LABEL3, configLabels.get(1));
        assertEquals(TEST_LABEL1, configLabels.get(2));
    }

    @Test
    public void testRegisterOverridesCollectionOnTop() {
        this.labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL1);
        List<String> labels = new ArrayList<String>();
        labels.add(TEST_LABEL2);
        labels.add(TEST_LABEL3);
        this.labelAccumulator.registerColumnOverridesOnTop(0, labels);

        LabelStack configLabels = new LabelStack();
        this.labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        assertEquals(3, configLabels.size());
        assertEquals(TEST_LABEL2, configLabels.get(0));
        assertEquals(TEST_LABEL3, configLabels.get(1));
        assertEquals(TEST_LABEL1, configLabels.get(2));
    }

    @Test
    public void testSaveStateToProperties() throws Exception {
        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL1, TEST_LABEL2);
        this.labelAccumulator.registerColumnOverrides(1, TEST_LABEL2);

        this.labelAccumulator.saveState(TEST_PREFIX, this.testProperties);

        String baseKey = TEST_PREFIX + PERSISTENCE_KEY;

        assertEquals(TEST_LABEL1 + "," + TEST_LABEL2,
                this.testProperties.getProperty(baseKey + ".0"));
        assertEquals(TEST_LABEL2,
                this.testProperties.getProperty(baseKey + ".1"));
    }

    @Test
    public void testMixedSaveStateToProperties() throws Exception {
        this.labelAccumulator.registerOverrides(TEST_MIX_KEY, TEST_MIX_LABEL);

        this.labelAccumulator.registerColumnOverrides(0, TEST_LABEL1, TEST_LABEL2);
        this.labelAccumulator.registerColumnOverrides(1, TEST_LABEL2);

        this.labelAccumulator.registerOverrides(TEST_LABEL3);

        this.labelAccumulator.saveState(TEST_PREFIX, this.testProperties);

        String baseKey = TEST_PREFIX + PERSISTENCE_KEY;

        assertEquals(TEST_MIX_LABEL,
                this.testProperties.getProperty(baseKey + "." + TEST_MIX_KEY));
        assertEquals(TEST_LABEL1 + "," + TEST_LABEL2,
                this.testProperties.getProperty(baseKey + ".0"));
        assertEquals(TEST_LABEL2,
                this.testProperties.getProperty(baseKey + ".1"));
        assertEquals(
                TEST_LABEL3,
                this.testProperties.getProperty(baseKey + "."
                        + ColumnOverrideLabelAccumulator.ALL_COLUMN_KEY));
    }

    @Test
    public void testLoadLabelsFromProperties() throws Exception {
        this.testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".0",
                TEST_LABEL1);
        this.testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".5",
                TEST_LABEL1 + "," + TEST_LABEL2);

        this.labelAccumulator.loadState(TEST_PREFIX, this.testProperties);

        List<String> overrides = this.labelAccumulator.getOverrides(0);
        assertEquals(1, overrides.size());
        assertEquals(TEST_LABEL1, overrides.get(0));

        overrides = this.labelAccumulator.getOverrides(5);
        assertEquals(2, overrides.size());
        assertEquals(TEST_LABEL1, overrides.get(0));
        assertEquals(TEST_LABEL2, overrides.get(1));
    }

    @Test
    public void testLoadMixedLabelsFromProperties() throws Exception {
        this.testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + "."
                + TEST_MIX_KEY, TEST_MIX_LABEL);
        this.testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".0",
                TEST_LABEL1);
        this.testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".5",
                TEST_LABEL1 + "," + TEST_LABEL2);
        this.testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + "."
                + ColumnOverrideLabelAccumulator.ALL_COLUMN_KEY, TEST_LABEL3);

        this.labelAccumulator.loadState(TEST_PREFIX, this.testProperties);

        List<String> overrides = this.labelAccumulator.getOverrides(0);
        assertEquals(1, overrides.size());
        assertEquals(TEST_LABEL1, overrides.get(0));

        overrides = this.labelAccumulator.getOverrides(5);
        assertEquals(2, overrides.size());
        assertEquals(TEST_LABEL1, overrides.get(0));
        assertEquals(TEST_LABEL2, overrides.get(1));

        overrides = this.labelAccumulator.getOverrides(TEST_MIX_KEY);
        assertEquals(1, overrides.size());
        assertEquals(TEST_MIX_LABEL, overrides.get(0));

        overrides = this.labelAccumulator
                .getOverrides(ColumnOverrideLabelAccumulator.ALL_COLUMN_KEY);
        assertEquals(1, overrides.size());
        assertEquals(TEST_LABEL3, overrides.get(0));
    }
}
