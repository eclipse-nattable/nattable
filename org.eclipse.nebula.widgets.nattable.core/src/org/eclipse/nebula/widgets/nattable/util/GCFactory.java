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
package org.eclipse.nebula.widgets.nattable.util;

import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

public class GCFactory {

    private final Drawable drawable;

    /**
     *
     * @param drawable
     *            the drawable to draw on by the {@link GC} instances created by
     *            this factory.
     */
    public GCFactory(Drawable drawable) {
        this.drawable = drawable;
    }

    /**
     * Constructs a new instance of this class which has been configured to draw
     * on the drawable configured in this {@link GCFactory}. You must dispose
     * the graphics context when it is no longer required.
     *
     * @return A new {@link GC} instance or <code>null</code> if the
     *         {@link Drawable} configured for this {@link GCFactory} is
     *         disposed.
     */
    public GC createGC() {
        if (this.drawable instanceof Control && ((Control) this.drawable).isDisposed()) {
            return null;
        }
        return new GC(this.drawable);
    }

}
