/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Special {@link TextPainter} that will always calculate the row height of the
 * cell dependent to the content shown in the cell. It uses word wrapping and
 * calculation of the cell height to support showing long texts in a single
 * cell. It will grow/shrink the row height on resizing so always the optimal
 * height is used for the row the cell resides.
 *
 * <p>
 * This {@link TextPainter} should preferably be used for tables that use
 * percentage sizing so the calculated row heights for example will grow/shrink
 * correctly when resizing the composite that contains the table.
 * </p>
 *
 * <p>
 * It shouldn't be used for large tables that can be scrolled as the
 * growing/shrinking on scrolling can cause some side effects, like jumping
 * layouts on scrolling.
 * </p>
 *
 * @see TextPainter
 * @see DataLayer#setColumnPercentageSizing(boolean)
 */
public class AutomaticRowHeightTextPainter extends TextPainter {

    public AutomaticRowHeightTextPainter() {
        super(true, true, true);
    }

    public AutomaticRowHeightTextPainter(int spacing) {
        super(true, true, spacing, true);
    }

    @Override
    protected boolean performRowResize(int contentHeight, Rectangle rectangle) {
        if (this.calculateByTextHeight) {
            // on scaling there could be a difference of 1 pixel because of
            // rounding issues.
            // in that case we do not trigger a resize to avoid endless useless
            // resizing
            int diff = contentHeight - rectangle.height;
            return diff < -1 || diff > 1;
        }
        return false;
    }

}
