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
package org.eclipse.nebula.widgets.nattable.util;

import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;

public class GCFactory {

    private final Drawable drawable;

    public GCFactory(Drawable drawable) {
        this.drawable = drawable;
    }

    public GC createGC() {
        return new GC(this.drawable);
    }

}
