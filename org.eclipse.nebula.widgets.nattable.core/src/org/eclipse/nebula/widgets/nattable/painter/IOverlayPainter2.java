/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * An overlay painter is given a chance to paint on the canvas once the layers
 * have finished rendering. Extending {@link IOverlayPainter} to add API that
 * gets more detailed information about the rendering area.
 *
 * @see NatTable#addOverlayPainter(IOverlayPainter)
 * @since 1.4
 */
public interface IOverlayPainter2 extends IOverlayPainter {

    /**
     * Render an overlay over the painted layers.
     *
     * @param layer
     *            The layer as base for the overlay rendering.
     * @param gc
     *            The GC.
     * @param xOffset
     *            The x offset.
     * @param yOffset
     *            The y offset.
     * @param rectangle
     *            The print bounds for the rendering action.
     */
    public void paintOverlay(ILayer layer, GC gc,
            int xOffset, int yOffset, Rectangle rectangle);

}
