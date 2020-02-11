/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * Abstract implementation for {@link IDpiConverter} that predefines dpi
 * conversion factors and methods that convert based on these factors.
 */
public abstract class AbstractDpiConverter implements IDpiConverter {

    protected int dpi = -1;
    /**
     * @since 2.0
     */
    protected float scaleFactor = -1;

    /**
     * Sets the value for the dpi member variable.
     * <p>
     * <b>IMPORTANT:</b><br>
     * Ensure the load operation is done in the UI thread!
     * </p>
     */
    protected abstract void readDpiFromDisplay();

    @Override
    public int getDpi() {
        if (this.dpi < 0) {
            readDpiFromDisplay();
            this.scaleFactor = GUIHelper.getDpiFactor(getDpi());
        }
        return this.dpi;
    }

    @Override
    public float getCurrentDpiFactor() {
        if (this.scaleFactor < 0) {
            this.scaleFactor = GUIHelper.getDpiFactor(getDpi());
        }
        return this.scaleFactor;
    }

    @Override
    public int convertPixelToDpi(int pixel) {
        if (getCurrentDpiFactor() == 1) {
            return pixel;
        }
        return Math.round(pixel * this.scaleFactor);
    }

    @Override
    public int convertDpiToPixel(int dpi) {
        if (getCurrentDpiFactor() == 1) {
            return dpi;
        }
        return Math.round(dpi / this.scaleFactor);
    }
}
