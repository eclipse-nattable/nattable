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

import org.eclipse.swt.SWT;

public enum HorizontalAlignmentEnum {

    LEFT, CENTER, RIGHT;

    public static int getSWTStyle(IStyle cellStyle) {
        HorizontalAlignmentEnum horizontalAlignment = cellStyle
                .getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);

        if (horizontalAlignment == null) {
            return SWT.NONE;
        }

        switch (horizontalAlignment) {
            case CENTER:
                return SWT.CENTER;
            case LEFT:
                return SWT.LEFT;
            case RIGHT:
                return SWT.RIGHT;
            default:
                return SWT.NONE;
        }
    }

}
