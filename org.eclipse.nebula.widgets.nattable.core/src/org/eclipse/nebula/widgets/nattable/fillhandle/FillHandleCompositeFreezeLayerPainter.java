/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.GraphicsUtils;
import org.eclipse.nebula.widgets.nattable.painter.layer.CompositeFreezeLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialization of the {@link CompositeFreezeLayerPainter} that retrieves the
 * fill handle bounds from the separate paint calls and stores them locally.
 *
 * @since 2.5
 */
public class FillHandleCompositeFreezeLayerPainter extends CompositeFreezeLayerPainter implements FillHandleBoundsProvider {

    /**
     * The bounds of the current visible selection handle or <code>null</code>
     * if no fill handle is currently rendered.
     */
    protected Rectangle handleBounds;

    /**
     * Creates a {@link FillHandleCompositeFreezeLayerPainter} that can be set
     * directly on a {@link CompositeFreezeLayer}. This way the freeze border
     * will be rendered only inside the {@link CompositeFreezeLayer}.
     *
     * @param compositeFreezeLayer
     *            The {@link CompositeFreezeLayer} for rendering the freeze
     *            border.
     */
    public FillHandleCompositeFreezeLayerPainter(CompositeFreezeLayer compositeFreezeLayer) {
        super(compositeFreezeLayer);
    }

    /**
     * Creates a {@link FillHandleCompositeFreezeLayerPainter} that can be set
     * on a {@link CompositeLayer} that contains a {@link CompositeFreezeLayer}.
     * This way the freeze border will be rendered also on the adjacent regions.
     * For this typically the given {@link #compositeLayer} is inspected and the
     * freeze border is moved by the width/height of the first layers on top and
     * to the left, as we do not know the needed offset values on the higher
     * level composition.
     *
     * @param compositeLayer
     *            The top level {@link CompositeLayer}, e.g. a GridLayer.
     * @param compositeFreezeLayer
     *            The {@link CompositeFreezeLayer} for rendering the freeze
     *            border.
     */
    public FillHandleCompositeFreezeLayerPainter(CompositeLayer compositeLayer, CompositeFreezeLayer compositeFreezeLayer) {
        super(compositeLayer, compositeFreezeLayer);
    }

    /**
     * Creates a {@link FillHandleCompositeFreezeLayerPainter} that can be set
     * on a {@link CompositeLayer} that contains a {@link CompositeFreezeLayer}.
     * This way the freeze border will be rendered also on the adjacent regions.
     * For this typically the given {@link #compositeLayer} is inspected and the
     * freeze border is moved by the width/height of the first layers on top and
     * to the left, as we do not know the needed offset values on the higher
     * level composition.
     * <p>
     * <b>Note: </b> Via the <code>inspectComposite</code> parameter the
     * behavior in more complex layer compositions with nested CompositeLayer
     * can be adjusted.
     * </p>
     *
     * @param compositeLayer
     *            The top level {@link CompositeLayer}, e.g. a GridLayer.
     * @param compositeFreezeLayer
     *            The {@link CompositeFreezeLayer} for rendering the freeze
     *            border.
     * @param inspectComposite
     *            <code>true</code> if the given {@link #compositeLayer} should
     *            be inspected for the position of the freeze border,
     *            <code>false</code> if not. Default is <code>true</code>.
     *            Remember to add nested layers to manually configure the freeze
     *            border shift when setting this value to <code>false</code>.
     *
     * @see #addNestedVerticalLayer(ILayer)
     * @see #addNestedHorizontalLayer(ILayer)
     */
    public FillHandleCompositeFreezeLayerPainter(CompositeLayer compositeLayer, CompositeFreezeLayer compositeFreezeLayer, boolean inspectComposite) {
        super(compositeLayer, compositeFreezeLayer, inspectComposite);
    }

    @Override
    public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configRegistry) {
        // before we paint, we first clear the local stored handle bounds
        this.handleBounds = null;
        // paint the freeze composition
        super.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);
        // paint the fill handle on top so it is shown completely over the
        // freeze borders
        if (this.handleBounds != null) {
            paintFillHandle(gc, configRegistry);
        }
    }

    @Override
    protected void processLayerPainterInformation(ILayerPainter painter) {
        if (painter instanceof FillHandleLayerPainter) {
            Rectangle bounds = ((FillHandleLayerPainter) painter).getSelectionHandleBounds();
            if (bounds != null) {
                // if handle bounds are set in the painter, we store them
                this.handleBounds = bounds;
            }
        }
    }

    /**
     * Paint the fill handle.
     *
     * @param gc
     *            The {@link GC} used for painting.
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve fill handle
     *            configurations.
     */
    protected void paintFillHandle(GC gc, IConfigRegistry configRegistry) {
        // Save gc settings
        Color originalBackground = gc.getBackground();

        Color color = getHandleColor(configRegistry);
        BorderStyle borderStyle = getHandleBorderStyle(configRegistry);

        gc.setBackground(color);

        Rectangle handleInterior = GraphicsUtils.getInternalBounds(this.handleBounds, borderStyle);
        GraphicsUtils.fillRectangle(gc, handleInterior);
        GraphicsUtils.drawRectangle(gc, handleInterior, borderStyle);

        // Restore original gc settings
        gc.setBackground(originalBackground);
    }

    /**
     * Returns the color that should be used to render the fill handle. If the
     * {@link IConfigRegistry} is <code>null</code> or does not contain
     * configurations for the color of the fill handle, a default dark green
     * color is used.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to determine the configured
     *            fill handle color. Can be <code>null</code> which results in
     *            returning a default dark green color.
     *
     * @return the color that should be used
     */
    protected Color getHandleColor(IConfigRegistry configRegistry) {
        return FillHandleLayerPainterHelper.getHandleColor(configRegistry);
    }

    /**
     * Returns the border style that should be used to render the border of the
     * fill handle. If the {@link IConfigRegistry} is <code>null</code> or does
     * not contain configurations for styling the border of the fill handle, a
     * default style is used.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to determine the configured
     *            fill handle border style. Can be <code>null</code> which
     *            results in returning a default style.
     *
     * @return the border style that should be used
     */
    protected BorderStyle getHandleBorderStyle(IConfigRegistry configRegistry) {
        return FillHandleLayerPainterHelper.getHandleBorderStyle(configRegistry);
    }

    @Override
    public Rectangle getSelectionHandleBounds() {
        return this.handleBounds;
    }

}
