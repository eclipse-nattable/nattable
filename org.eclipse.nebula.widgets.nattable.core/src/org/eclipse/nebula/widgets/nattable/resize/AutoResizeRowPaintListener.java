/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;

/**
 * {@link PaintListener} that is used to auto-resize rows lazily when they
 * become visible in the viewport.
 *
 * @since 1.6
 */
public class AutoResizeRowPaintListener implements PaintListener {

    /**
     * The NatTable to which this {@link PaintListener} is attached to. Needed
     * to create a temporary {@link GC} and retrieve the
     * {@link IConfigRegistry}.
     */
    private final NatTable natTable;
    /**
     * The {@link ViewportLayer} to ensure that the auto row resize is only
     * triggered for visible rows.
     */
    private final ViewportLayer viewportLayer;
    /**
     * The {@link DataLayer} of the body region to inspect all columns in a row,
     * even if not visible in the viewport.
     */
    private final ILayer bodyDataLayer;

    /**
     *
     * @param natTable
     *            The NatTable to which this {@link PaintListener} is attached
     *            to. Needed to create a temporary {@link GC} and retrieve the
     *            {@link IConfigRegistry}.
     * @param viewportLayer
     *            The {@link ViewportLayer} to ensure that the auto row resize
     *            is only triggered for visible rows.
     * @param bodyDataLayer
     *            The {@link DataLayer} of the body region to inspect all
     *            columns in a row, even if not visible in the viewport.
     */
    public AutoResizeRowPaintListener(NatTable natTable, ViewportLayer viewportLayer, ILayer bodyDataLayer) {
        this.natTable = natTable;
        this.viewportLayer = viewportLayer;
        this.bodyDataLayer = bodyDataLayer;
    }

    @Override
    public void paintControl(PaintEvent e) {
        AutoResizeHelper.autoResizeRows(this.natTable, this.viewportLayer, this.bodyDataLayer);
    }

}
