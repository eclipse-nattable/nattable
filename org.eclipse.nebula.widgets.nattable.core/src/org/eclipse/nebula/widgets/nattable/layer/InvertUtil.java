/*******************************************************************************
 * Copyright (c) 2012, 2020 Edwin Park and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import org.eclipse.swt.graphics.Rectangle;

public class InvertUtil {

    public static Rectangle invertRectangle(Rectangle rect) {
        if (rect != null)
            return new Rectangle(rect.y, rect.x, rect.height, rect.width);
        else
            return null;
    }

}
