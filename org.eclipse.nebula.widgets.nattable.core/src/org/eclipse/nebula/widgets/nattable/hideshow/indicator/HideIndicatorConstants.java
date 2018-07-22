/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.indicator;

/**
 * Constants for labels with regards to hidden columns and rows.
 *
 * @since 1.6
 */
public final class HideIndicatorConstants {

    /**
     * Label that indicates that there are columns hidden to the left of the
     * cell.
     */
    public static final String COLUMN_LEFT_HIDDEN = "COLUMN_LEFT_HIDDEN"; //$NON-NLS-1$

    /**
     * Label that indicates that there are columns hidden to the right of the
     * cell. Only applied to the right most column.
     */
    public static final String COLUMN_RIGHT_HIDDEN = "COLUMN_RIGHT_HIDDEN"; //$NON-NLS-1$

    /**
     * Label that indicates that there are rows hidden on top of the cell.
     */
    public static final String ROW_TOP_HIDDEN = "ROW_TOP_HIDDEN"; //$NON-NLS-1$

    /**
     * Label that indicates that there are rows hidden at the bottom of the
     * cell.
     */
    public static final String ROW_BOTTOM_HIDDEN = "ROW_BOTTOM_HIDDEN"; //$NON-NLS-1$

    private HideIndicatorConstants() {
        // empty constructor for constants class
    }
}
