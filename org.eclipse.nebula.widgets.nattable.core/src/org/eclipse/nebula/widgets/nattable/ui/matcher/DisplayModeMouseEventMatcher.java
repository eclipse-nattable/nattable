/*******************************************************************************
 * Copyright (c) 2014, 2020 Roman Flueckiger.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    private final DisplayMode displayMode;
    private final IMouseEventMatcher aggregate;

    /**
     * Creates a {@link DisplayModeMouseEventMatcher} that checks only if the
     * specified display mode matches.
     *
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @deprecated Use constructor with {@link DisplayMode} parameter.
     */
    @Deprecated
    public DisplayModeMouseEventMatcher(String displayMode) {
        this(DisplayMode.valueOf(displayMode), null);
    }

    /**
     * Creates a {@link DisplayModeMouseEventMatcher} that checks only if the
     * specified display mode matches.
     *
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @since 2.0
     */
    public DisplayModeMouseEventMatcher(DisplayMode displayMode) {
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
     * @deprecated Use constructor with {@link DisplayMode} parameter.
     */
    @Deprecated
    public DisplayModeMouseEventMatcher(String displayMode, IMouseEventMatcher aggregate) {
        this(DisplayMode.valueOf(displayMode), aggregate);
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
     * @since 2.0
     */
    public DisplayModeMouseEventMatcher(DisplayMode displayMode, IMouseEventMatcher aggregate) {
        if (displayMode == null) {
            throw new IllegalArgumentException("displayMode can not be null."); //$NON-NLS-1$
        }

        this.displayMode = displayMode;
        this.aggregate = aggregate;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        ILayerCell cell = natTable.getCellByPosition(
                natTable.getColumnPositionByX(event.x),
                natTable.getRowPositionByY(event.y));

        if (cell != null) {
            boolean displayModeMatches = this.displayMode.equals(cell.getDisplayMode());
            if (this.aggregate != null) {
                return displayModeMatches
                        && this.aggregate.matches(natTable, event, regionLabels);
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
     * @deprecated Use {@link #displayMode(DisplayMode, IMouseEventMatcher)}
     */
    @Deprecated
    public static DisplayModeMouseEventMatcher displayMode(String displayMode, IMouseEventMatcher aggregate) {
        return new DisplayModeMouseEventMatcher(DisplayMode.valueOf(displayMode), aggregate);
    }

    /**
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @return a {@link DisplayModeMouseEventMatcher} that checks only if the
     *         specified display mode matches.
     * @deprecated Use {@link #displayMode(DisplayMode)}
     */
    @Deprecated
    public static DisplayModeMouseEventMatcher displayMode(String displayMode) {
        return displayMode(DisplayMode.valueOf(displayMode), null);
    }

    /**
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @param aggregate
     *            (optional) an additional {@link IMouseEventMatcher} to be
     *            chained with the result of this matcher (results are and'ed).
     * @return a {@link DisplayModeMouseEventMatcher} that checks if the
     *         specified display mode matches and the aggregate matcher.
     * @since 2.0
     */
    public static DisplayModeMouseEventMatcher displayMode(DisplayMode displayMode, IMouseEventMatcher aggregate) {
        return new DisplayModeMouseEventMatcher(displayMode, aggregate);
    }

    /**
     * @param displayMode
     *            the {@link DisplayMode} to be matched.
     * @return a {@link DisplayModeMouseEventMatcher} that checks only if the
     *         specified display mode matches.
     * @since 2.0
     */
    public static DisplayModeMouseEventMatcher displayMode(DisplayMode displayMode) {
        return displayMode(displayMode, null);
    }

}
