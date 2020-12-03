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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Rectangle;

public class CellFixture extends LayerCell {

    public static final Rectangle TEST_BOUNDS = new Rectangle(0, 0, 10, 10);
    public static final String TEST_CONFIG_TYPE = "default";
    public static final String TEST_CELL_DATA = "Test cell data";

    private DisplayMode displayMode;
    private LabelStack configLabels;
    private Object dataValue;
    private Rectangle bounds;

    public CellFixture() {
        this(TEST_CELL_DATA);
    }

    public CellFixture(Object dataValue) {
        super(null, 0, 0);

        this.dataValue = dataValue;

        this.displayMode = DisplayMode.NORMAL;
        this.configLabels = new LabelStack(TEST_CONFIG_TYPE);
        this.bounds = TEST_BOUNDS;
    }

    @Override
    public DisplayMode getDisplayMode() {
        return this.displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    public LabelStack getConfigLabels() {
        return this.configLabels;
    }

    public void setConfigLabels(LabelStack configLabels) {
        this.configLabels = configLabels;
    }

    public void addConfigLabels(String... additionalConfigLabels) {
        for (String configLabel : additionalConfigLabels) {
            this.configLabels.addLabel(configLabel);
        }
    }

    @Override
    public Object getDataValue() {
        return this.dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }

    @Override
    public Rectangle getBounds() {
        return this.bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

}
