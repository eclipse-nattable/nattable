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
