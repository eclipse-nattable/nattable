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

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
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
}
