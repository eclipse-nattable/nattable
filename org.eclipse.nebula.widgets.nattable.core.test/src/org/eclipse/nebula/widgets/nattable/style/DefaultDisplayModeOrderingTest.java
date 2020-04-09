/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DefaultDisplayModeOrderingTest {

    private DefaultDisplayModeOrdering ordering;

    @Before
    public void setup() {
        this.ordering = new DefaultDisplayModeOrdering();
    }

    @Test
    public void orderingForSelectMode() {
        List<String> selectModeOrdering = this.ordering.getDisplayModeOrdering(DisplayMode.SELECT);

        assertEquals(2, selectModeOrdering.size());
        assertEquals(DisplayMode.SELECT, selectModeOrdering.get(0));
        assertEquals(DisplayMode.NORMAL, selectModeOrdering.get(1));
    }

    @Test
    public void orderingForEditMode() {
        List<String> editModeOrdering = this.ordering.getDisplayModeOrdering(DisplayMode.EDIT);

        assertEquals(2, editModeOrdering.size());
        assertEquals(DisplayMode.EDIT, editModeOrdering.get(0));
        assertEquals(DisplayMode.NORMAL, editModeOrdering.get(1));
    }

    @Test
    public void orderingForNormalMode() {
        List<String> selectModeOrdering = this.ordering.getDisplayModeOrdering(DisplayMode.NORMAL);

        assertEquals(1, selectModeOrdering.size());
        assertEquals(DisplayMode.NORMAL, selectModeOrdering.get(0));
    }
}
