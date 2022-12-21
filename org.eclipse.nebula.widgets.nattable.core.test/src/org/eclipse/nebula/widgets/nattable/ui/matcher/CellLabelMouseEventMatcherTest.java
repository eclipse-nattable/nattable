/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.SWTUtils;
import org.eclipse.swt.events.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CellLabelMouseEventMatcherTest {

    private static final String TEST_LABEL = "testLabel";
    private NatTableFixture natTableFixture;

    @BeforeEach
    public void setUpCustomCellLabel() {
        DummyGridLayerStack gridLayerStack = new DummyGridLayerStack(5, 5);
        this.natTableFixture = new NatTableFixture(gridLayerStack);

        // Register custom label
        DataLayer bodyDataLayer = (DataLayer) gridLayerStack.getBodyDataLayer();
        this.natTableFixture.registerLabelOnColumn(bodyDataLayer, 0, TEST_LABEL);
    }

    @Test
    public void shouldMatchCellsWithCustomLabels() throws Exception {
        CellLabelMouseEventMatcher matcher = new CellLabelMouseEventMatcher(
                GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, TEST_LABEL);

        boolean match = matcher.matches(this.natTableFixture, new MouseEvent(
                SWTUtils.getLeftClickEvent(100, 100, 0, this.natTableFixture)),
                new LabelStack(GridRegion.BODY));

        assertTrue(match);
    }

    @Test
    public void shouldTakeTheRegionIntoAccountWhileMatching() throws Exception {
        CellLabelMouseEventMatcher matcher = new CellLabelMouseEventMatcher(
                GridRegion.COLUMN_HEADER, MouseEventMatcher.LEFT_BUTTON,
                TEST_LABEL);

        boolean match = matcher.matches(this.natTableFixture, new MouseEvent(
                SWTUtils.getLeftClickEvent(100, 100, 0, this.natTableFixture)),
                new LabelStack(GridRegion.BODY));

        assertFalse(match);
    }

    @Test
    public void shouldTakeTheButtomIntoAccountWhileMatching() throws Exception {
        CellLabelMouseEventMatcher matcher = new CellLabelMouseEventMatcher(
                GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON, TEST_LABEL);

        boolean match = matcher.matches(this.natTableFixture, new MouseEvent(
                SWTUtils.getLeftClickEvent(100, 100, 0, this.natTableFixture)),
                new LabelStack(GridRegion.BODY));

        assertFalse(match);
    }

}
