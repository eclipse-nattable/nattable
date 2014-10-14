/*******************************************************************************
 * Copyright (c) 2014 Roman Flueckiger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roman Flueckiger <roman.flueckiger@mac.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.events.MouseEvent;

/**
 * This class is used to check a mouse event against the {@link DisplayMode} of
 * the cell at the mouse event's position. Optionally, the matcher can be
 * chained with an additional {@link IMouseEventMatcher} (results are and'ed).
 */
public class DisplayModeMouseEventMatcher implements IMouseEventMatcher {

    private final String displayMode;
    private final IMouseEventMatcher aggregate;

    /**
     * Creates a {@link DisplayModeMouseEventMatcher} that checks only if the
     * specified display mode matches.
     *
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     */
    public DisplayModeMouseEventMatcher(String displayMode) {
        this(displayMode, null);
    }

    /**
     * Creates a {@link DisplayModeMouseEventMatcher} that checks if the
     * specified display mode matches and the aggregate matcher.
     *
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @param aggregate
     *            (optional) an additional {@link IMouseEventMatcher} to be
     *            chained with the result of this matcher (results are and'ed).
     */
    public DisplayModeMouseEventMatcher(String displayMode,
            IMouseEventMatcher aggregate) {
        if (displayMode == null || displayMode.length() == 0) {
            throw new IllegalArgumentException("displayMode must not be blank."); //$NON-NLS-1$
        }

        this.displayMode = displayMode;
        this.aggregate = aggregate;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event,
            LabelStack regionLabels) {
        ILayerCell cell = natTable.getCellByPosition(
                natTable.getColumnPositionByX(event.x),
                natTable.getRowPositionByY(event.y));

        if (cell != null) {
            boolean displayModeMatches = this.displayMode.equals(cell
                    .getDisplayMode());
            if (this.aggregate != null) {
                return displayModeMatches
                        && this.aggregate
                                .matches(natTable, event, regionLabels);
            }
            return displayModeMatches;
        }

        return false;
    }

    /**
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @param aggregate
     *            (optional) an additional {@link IMouseEventMatcher} to be
     *            chained with the result of this matcher (results are and'ed).
     * @return a {@link DisplayModeMouseEventMatcher} that checks if the
     *         specified display mode matches and the aggregate matcher.
     */
    public static DisplayModeMouseEventMatcher displayMode(String displayMode,
            IMouseEventMatcher aggregate) {
        return new DisplayModeMouseEventMatcher(displayMode, aggregate);
    }

    /**
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @return a {@link DisplayModeMouseEventMatcher} that checks only if the
     *         specified display mode matches.
     */
    public static DisplayModeMouseEventMatcher displayMode(String displayMode) {
        return displayMode(displayMode, null);
    }

}
