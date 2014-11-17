/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

/**
 * Interface to add support for scaling.
 * <p>
 * This interface is inspired by Riena and the corresponding talk at EclipseCon
 * 2014 <a
 * href="http://www.slideshare.net/da152/swt-scalingece2014-aktuell">Scaling SWT
 * on high-resolution screens</a>
 * </p>
 */
public interface IDpiConverter {

    /**
     * Returns the dots per inch of the display.
     *
     * @return the horizontal and vertical DPI
     */
    int getDpi();

    /**
     *
     * @return The factor that will be used for the current DPI.
     */
    float getCurrentDpiFactor();

    /**
     * Converts the given amount of pixels to a DPI scaled value.
     *
     * @param pixel
     *            the amount of pixels to convert.
     * @return The converted pixels.
     */
    int convertPixelToDpi(int pixel);

    /**
     * Converts the given DPI scaled value to a pixel value.
     *
     * @param dpi
     *            the DPI value to convert.
     * @return The pixel value related to the given DPI
     */
    int convertDpiToPixel(int dpi);
}
