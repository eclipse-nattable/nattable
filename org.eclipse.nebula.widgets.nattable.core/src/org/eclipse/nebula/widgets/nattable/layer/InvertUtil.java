/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
