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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import static org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator.PERSISTENCE_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
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
        labelAccumulator = new ColumnOverrideLabelAccumulator(
                new DataLayerFixture());
        testProperties = new Properties();
    }

    @Test
    public void testRegisterOverrides() {
        labelAccumulator.registerColumnOverrides(0, TEST_LABEL1);
        labelAccumulator.registerColumnOverrides(0, TEST_LABEL2);

        LabelStack configLabels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        Assert.assertEquals(2, configLabels.getLabels().size());
        Assert.assertEquals(TEST_LABEL1, configLabels.getLabels().get(0));
        Assert.assertEquals(TEST_LABEL2, configLabels.getLabels().get(1));
    }

    @Test
    public void testRegisterOverridesEllipse() {
        labelAccumulator.registerColumnOverrides(0, TEST_LABEL1);
        labelAccumulator.registerColumnOverrides(0, TEST_LABEL2, TEST_LABEL3);

        LabelStack configLabels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        Assert.assertEquals(3, configLabels.getLabels().size());
        Assert.assertEquals(TEST_LABEL1, configLabels.getLabels().get(0));
        Assert.assertEquals(TEST_LABEL2, configLabels.getLabels().get(1));
        Assert.assertEquals(TEST_LABEL3, configLabels.getLabels().get(2));
    }

    @Test
    public void testRegisterOverridesCollection() {
        labelAccumulator.registerColumnOverrides(0, TEST_LABEL1);
        List<String> labels = new ArrayList<String>();
        labels.add(TEST_LABEL2);
        labels.add(TEST_LABEL3);
        labelAccumulator.registerColumnOverrides(0, labels);

        LabelStack configLabels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        Assert.assertEquals(3, configLabels.getLabels().size());
        Assert.assertEquals(TEST_LABEL1, configLabels.getLabels().get(0));
        Assert.assertEquals(TEST_LABEL2, configLabels.getLabels().get(1));
        Assert.assertEquals(TEST_LABEL3, configLabels.getLabels().get(2));
    }

    @Test
    public void testRegisterOverridesOnTop() {
        labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL1);
        labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL2);

        LabelStack configLabels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        Assert.assertEquals(2, configLabels.getLabels().size());
        Assert.assertEquals(TEST_LABEL2, configLabels.getLabels().get(0));
        Assert.assertEquals(TEST_LABEL1, configLabels.getLabels().get(1));
    }

    @Test
    public void testRegisterOverridesEllipseOnTop() {
        labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL1);
        labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL2,
                TEST_LABEL3);

        LabelStack configLabels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        Assert.assertEquals(3, configLabels.getLabels().size());
        Assert.assertEquals(TEST_LABEL2, configLabels.getLabels().get(0));
        Assert.assertEquals(TEST_LABEL3, configLabels.getLabels().get(1));
        Assert.assertEquals(TEST_LABEL1, configLabels.getLabels().get(2));
    }

    @Test
    public void testRegisterOverridesCollectionOnTop() {
        labelAccumulator.registerColumnOverridesOnTop(0, TEST_LABEL1);
        List<String> labels = new ArrayList<String>();
        labels.add(TEST_LABEL2);
        labels.add(TEST_LABEL3);
        labelAccumulator.registerColumnOverridesOnTop(0, labels);

        LabelStack configLabels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(configLabels, 0, 0);

        Assert.assertEquals(3, configLabels.getLabels().size());
        Assert.assertEquals(TEST_LABEL2, configLabels.getLabels().get(0));
        Assert.assertEquals(TEST_LABEL3, configLabels.getLabels().get(1));
        Assert.assertEquals(TEST_LABEL1, configLabels.getLabels().get(2));
    }

    @Test
    public void testSaveStateToProperties() throws Exception {
        labelAccumulator.registerColumnOverrides(0, TEST_LABEL1, TEST_LABEL2);
        labelAccumulator.registerColumnOverrides(1, TEST_LABEL2);

        labelAccumulator.saveState(TEST_PREFIX, testProperties);

        String baseKey = TEST_PREFIX + PERSISTENCE_KEY;

        Assert.assertEquals(TEST_LABEL1 + "," + TEST_LABEL2,
                testProperties.getProperty(baseKey + ".0"));
        Assert.assertEquals(TEST_LABEL2,
                testProperties.getProperty(baseKey + ".1"));
    }

    @Test
    public void testMixedSaveStateToProperties() throws Exception {
        labelAccumulator.registerOverrides(TEST_MIX_KEY, TEST_MIX_LABEL);

        labelAccumulator.registerColumnOverrides(0, TEST_LABEL1, TEST_LABEL2);
        labelAccumulator.registerColumnOverrides(1, TEST_LABEL2);

        labelAccumulator.registerOverrides(TEST_LABEL3);

        labelAccumulator.saveState(TEST_PREFIX, testProperties);

        String baseKey = TEST_PREFIX + PERSISTENCE_KEY;

        Assert.assertEquals(TEST_MIX_LABEL,
                testProperties.getProperty(baseKey + "." + TEST_MIX_KEY));
        Assert.assertEquals(TEST_LABEL1 + "," + TEST_LABEL2,
                testProperties.getProperty(baseKey + ".0"));
        Assert.assertEquals(TEST_LABEL2,
                testProperties.getProperty(baseKey + ".1"));
        Assert.assertEquals(
                TEST_LABEL3,
                testProperties.getProperty(baseKey + "."
                        + ColumnOverrideLabelAccumulator.ALL_COLUMN_KEY));
    }

    @SuppressWarnings("boxing")
    @Test
    public void testLoadLabelsFromProperties() throws Exception {
        testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".0",
                TEST_LABEL1);
        testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".5",
                TEST_LABEL1 + "," + TEST_LABEL2);

        labelAccumulator.loadState(TEST_PREFIX, testProperties);

        List<String> overrides = labelAccumulator.getOverrides(0);
        Assert.assertEquals(1, overrides.size());
        Assert.assertEquals(TEST_LABEL1, overrides.get(0));

        overrides = labelAccumulator.getOverrides(5);
        Assert.assertEquals(2, overrides.size());
        Assert.assertEquals(TEST_LABEL1, overrides.get(0));
        Assert.assertEquals(TEST_LABEL2, overrides.get(1));
    }

    @SuppressWarnings("boxing")
    @Test
    public void testLoadMixedLabelsFromProperties() throws Exception {
        testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + "."
                + TEST_MIX_KEY, TEST_MIX_LABEL);
        testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".0",
                TEST_LABEL1);
        testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".5",
                TEST_LABEL1 + "," + TEST_LABEL2);
        testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + "."
                + ColumnOverrideLabelAccumulator.ALL_COLUMN_KEY, TEST_LABEL3);

        labelAccumulator.loadState(TEST_PREFIX, testProperties);

        List<String> overrides = labelAccumulator.getOverrides(0);
        Assert.assertEquals(1, overrides.size());
        Assert.assertEquals(TEST_LABEL1, overrides.get(0));

        overrides = labelAccumulator.getOverrides(5);
        Assert.assertEquals(2, overrides.size());
        Assert.assertEquals(TEST_LABEL1, overrides.get(0));
        Assert.assertEquals(TEST_LABEL2, overrides.get(1));

        overrides = labelAccumulator.getOverrides(TEST_MIX_KEY);
        Assert.assertEquals(1, overrides.size());
        Assert.assertEquals(TEST_MIX_LABEL, overrides.get(0));

        overrides = labelAccumulator
                .getOverrides(ColumnOverrideLabelAccumulator.ALL_COLUMN_KEY);
        Assert.assertEquals(1, overrides.size());
        Assert.assertEquals(TEST_LABEL3, overrides.get(0));
    }
}
