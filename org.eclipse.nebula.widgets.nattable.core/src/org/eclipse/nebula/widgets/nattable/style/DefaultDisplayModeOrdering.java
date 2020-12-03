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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultDisplayModeOrdering implements IDisplayModeOrdering {

    private static final List<DisplayMode> NORMAL_ORDERING =
            Arrays.asList(DisplayMode.NORMAL);

    private static final List<DisplayMode> SELECT_ORDERING =
            Arrays.asList(DisplayMode.SELECT, DisplayMode.NORMAL);

    private static final List<DisplayMode> EDIT_ORDERING =
            Arrays.asList(DisplayMode.EDIT, DisplayMode.NORMAL);

    private static final List<DisplayMode> EMPTY_ORDERING =
            Collections.emptyList();

    private static final List<DisplayMode> HOVER_ORDERING =
            Arrays.asList(DisplayMode.HOVER, DisplayMode.NORMAL);

    private static final List<DisplayMode> SELECT_HOVER_ORDERING =
            Arrays.asList(DisplayMode.SELECT_HOVER, DisplayMode.SELECT, DisplayMode.HOVER, DisplayMode.NORMAL);

    /**
     * See DefaultDisplayModeOrderingTest
     */
    @Override
    public List<DisplayMode> getDisplayModeOrdering(DisplayMode targetDisplayMode) {
        if (DisplayMode.NORMAL.equals(targetDisplayMode)) {
            return NORMAL_ORDERING;
        } else if (DisplayMode.SELECT.equals(targetDisplayMode)) {
            return SELECT_ORDERING;
        } else if (DisplayMode.EDIT.equals(targetDisplayMode)) {
            return EDIT_ORDERING;
        } else if (DisplayMode.HOVER.equals(targetDisplayMode)) {
            return HOVER_ORDERING;
        } else if (DisplayMode.SELECT_HOVER.equals(targetDisplayMode)) {
            return SELECT_HOVER_ORDERING;
        } else {
            return EMPTY_ORDERING;
        }
    }
}
