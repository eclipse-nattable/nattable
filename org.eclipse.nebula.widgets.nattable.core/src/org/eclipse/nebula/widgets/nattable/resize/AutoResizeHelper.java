/*******************************************************************************
 * Copyright (c) 2015, 2016 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Helper class that renders a {@link ILayer} in-memory to trigger auto-resizing
 * of rows and columns in case content painters are configured to calculate the
 * necessary dimensions.
 * <p>
 * Note that this operation is expensive in terms of memory consumption and
 * processing time. Be careful when using this helper for huge tables.
 * </p>
 *
 * @since 1.4
 */
public class AutoResizeHelper {

    /**
     * The {@link ILayer} that should be used for in-memory rendering to trigger
     * auto-resizing.
     */
    protected final ILayer layer;
    /**
     * The {@link IConfigRegistry} needed for rendering.
     */
    protected final IConfigRegistry configRegistry;
    /**
     * The total area needed to render the whole layer at once.
     */
    protected Rectangle totalArea;
    /**
     * The total area of the previous in-memory rendering. Needed to reduce the
     * rendering area on consecutive calls.
     *
     * @since 1.5
     */
    protected Rectangle prevArea = null;
    /**
     * The original {@link IClientAreaProvider} needed to restore the original
     * state after processing.
     */
    protected IClientAreaProvider originalClientAreaProvider;

    /**
     * Flag to indicate that an automatic resize was triggered on rendering.
     */
    protected volatile boolean resizedOnPrinting = true;

    /**
     * {@link ILayerListener} that is added to the {@link ILayer} to get
     * informed about {@link RowResizeEvent} and {@link ColumnResizeEvent} to
     * know if an automatic resize was triggered on rendering.
     */
    protected ILayerListener resizeListener = new ILayerListener() {

        @Override
        public void handleLayerEvent(ILayerEvent event) {
            if (!AutoResizeHelper.this.resizedOnPrinting &&
                    (event instanceof RowResizeEvent || event instanceof ColumnResizeEvent)) {
                AutoResizeHelper.this.resizedOnPrinting = true;
            }
        }
    };

    /**
     * The {@link IClientAreaProvider} that is used for rendering the whole
     * layer in-memory.
     */
    protected IClientAreaProvider clientAreaProvider = new IClientAreaProvider() {

        @Override
        public Rectangle getClientArea() {
            return AutoResizeHelper.this.totalArea;
        }
    };

    /**
     *
     * @param layer
     *            The {@link ILayer} that should be used for in-memory rendering
     *            to trigger auto-resizing.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed for rendering.
     */
    private AutoResizeHelper(ILayer layer, IConfigRegistry configRegistry) {
        this.layer = layer;
        this.configRegistry = configRegistry;
        this.originalClientAreaProvider = layer.getClientAreaProvider();
        calculateTotalArea();
    }

    /**
     * Executes in-memory rendering of the given {@link ILayer} to trigger
     * content based auto-resizing.
     *
     * @param layer
     *            The {@link ILayer} that should be used for in-memory rendering
     *            to trigger auto-resizing.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed for rendering.
     */
    public static void autoResize(ILayer layer, IConfigRegistry configRegistry) {
        AutoResizeHelper helper = new AutoResizeHelper(layer, configRegistry);

        helper.init();
        try {
            // as long as resize events were triggered on rendering, we paint
            // in-memory again to ensure everything was at least rendered once
            // and resized correctly
            while (helper.resizedOnPrinting) {
                helper.resizedOnPrinting = false;
                helper.calculateTotalArea();
                helper.paintInMemory();

                helper.prevArea = helper.totalArea;
            }
        } finally {
            helper.restore();
        }
    }

    /**
     * Paints the layer on a temporary image GC. If painters are configured for
     * automatic size calculation, this painting will trigger the resize events.
     */
    protected void paintInMemory() {
        Image tmpImage = new Image(Display.getDefault(), 100, 100);
        GC tempGC = new GC(tmpImage);

        try {
            if (this.prevArea != null) {
                Rectangle bottom = new Rectangle(
                        0,
                        this.prevArea.height,
                        this.totalArea.width,
                        this.totalArea.height - this.prevArea.height);
                if (bottom.height > 0) {
                    paintLayer(tempGC, bottom);
                }

                Rectangle right = new Rectangle(
                        this.prevArea.width,
                        0,
                        this.totalArea.width - this.prevArea.width,
                        this.totalArea.height);
                if (right.width > 0) {
                    paintLayer(tempGC, right);
                }
            } else {
                // render the layer on the temporary GC
                paintLayer(tempGC, this.totalArea);
            }
        } finally {
            // ensure the temporary created resources are disposed after
            // processing
            tempGC.dispose();
            tmpImage.dispose();
        }
    }

    /**
     * Print the part of the layer that matches the given print bounds.
     *
     * @param gc
     *            The print GC to render the layer to.
     * @param printBounds
     *            The bounds of the print page.
     */
    protected void paintLayer(GC gc, Rectangle printBounds) {
        this.layer.getLayerPainter().paintLayer(
                this.layer, gc, 0, 0, printBounds, this.configRegistry);
    }

    /**
     * Calculate the total area needed to render the whole layer.
     */
    protected void calculateTotalArea() {
        this.totalArea = new Rectangle(0, 0, this.layer.getWidth(), this.layer.getHeight());
    }

    /**
     * Prepare the layer for complete in-memory rendering.
     */
    protected void init() {
        this.layer.addLayerListener(this.resizeListener);
        this.layer.setClientAreaProvider(this.clientAreaProvider);
        this.layer.doCommand(new TurnViewportOffCommand());
        this.layer.doCommand(new PrintEntireGridCommand());
    }

    /**
     * Restore the original state of the layer before in-memory rendering
     * preparations.
     */
    protected void restore() {
        this.layer.removeLayerListener(this.resizeListener);
        this.layer.setClientAreaProvider(this.originalClientAreaProvider);
        this.layer.doCommand(new TurnViewportOnCommand());
    }

    /**
     * Trigger auto-resizing of rows based on the content of the whole row.
     *
     * @param natTable
     *            The NatTable on which the auto row resize should be performed.
     *            Needed to create a temporary {@link GC} and retrieve the
     *            {@link IConfigRegistry}.
     * @param rowLayer
     *            The {@link ILayer} that should be used to determine the rows
     *            to auto-resize. Can be the {@link ViewportLayer} to ensure
     *            that the auto row resize is only triggered for visible rows or
     *            the {@link DataLayer} of the body region to auto-resize all
     *            rows.
     * @param bodyDataLayer
     *            The {@link DataLayer} of the body region to inspect all
     *            columns in a row, even if not visible in the viewport.
     *
     * @since 1.6
     */
    public static void autoResizeRows(final NatTable natTable, final ILayer rowLayer, final ILayer bodyDataLayer) {
        natTable.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                int[] rowPos = new int[rowLayer.getRowCount()];
                int[] rowHeights = new int[rowLayer.getRowCount()];
                for (int i = 0; i < rowLayer.getRowCount(); i++) {
                    rowPos[i] = rowLayer.getRowIndexByPosition(i);
                    rowHeights[i] = rowLayer.getRowHeightByPosition(i);
                }

                int[] calculatedRowHeights = MaxCellBoundsHelper.getPreferredRowHeights(
                        natTable.getConfigRegistry(),
                        new GCFactory(natTable),
                        bodyDataLayer,
                        rowPos);

                // only perform row resize where necessary
                // avoid unnecessary commands
                final List<Integer> positions = new ArrayList<Integer>();
                final List<Integer> heights = new ArrayList<Integer>();
                for (int i = 0; i < rowPos.length; i++) {
                    if (rowHeights[i] != calculatedRowHeights[i]) {
                        positions.add(rowPos[i]);
                        heights.add(calculatedRowHeights[i]);
                    }
                }

                if (!positions.isEmpty()) {
                    bodyDataLayer.doCommand(
                            new MultiRowResizeCommand(
                                    bodyDataLayer,
                                    ObjectUtils.asIntArray(positions),
                                    ObjectUtils.asIntArray(heights),
                                    true));
                }
            }
        });
    }
}
