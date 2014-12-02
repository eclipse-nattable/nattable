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
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.swt.SWT;

public enum VerticalAlignmentEnum {

    TOP, MIDDLE, BOTTOM;

    public static int getSWTStyle(IStyle cellStyle) {
        VerticalAlignmentEnum verticalAlignment = cellStyle
                .getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);

        if (verticalAlignment == null) {
            return SWT.NONE;
        }

        switch (verticalAlignment) {
            case TOP:
                return SWT.TOP;
            case MIDDLE:
                return SWT.CENTER;
            case BOTTOM:
                return SWT.BOTTOM;
            default:
                return SWT.NONE;
        }
    }
}
