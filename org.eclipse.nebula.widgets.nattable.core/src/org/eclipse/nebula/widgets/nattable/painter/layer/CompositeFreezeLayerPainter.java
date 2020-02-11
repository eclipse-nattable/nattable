/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.layer;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.IFreezeConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer.CompositeLayerPainter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * {@link CompositeLayer} that is used to render the freeze border. Can also be
 * used set on a top-level {@link CompositeLayer} to render the freeze border
 * also on adjacent regions, e.g. in a GridLayer to render the freeze border
 * also inside the column header and row header.
 *
 * @see IFreezeConfigAttributes#SEPARATOR_COLOR
 * @see IFreezeConfigAttributes#SEPARATOR_WIDTH
 *
 * @since 1.6
 */
public class CompositeFreezeLayerPainter extends CompositeLayerPainter {

    private CompositeLayer compositeLayer;
    private CompositeFreezeLayer compositeFreezeLayer;
    private ILayer freezeLayer;
    private boolean inspectComposite = true;

    /**
     * ILayer that should be used to shift the freeze border down in case of
     * nested composite layers, e.g. with fixed summary rows.
     */
    private final Collection<ILayer> nestedVerticalLayers = new ArrayList<ILayer>();
    /**
     * ILayer that should be used to shift the freeze border to the right in
     * case of nested composite layers.
     */
    private final Collection<ILayer> nestedHorizontalLayers = new ArrayList<ILayer>();

    /**
     * Creates a {@link CompositeFreezeLayerPainter} that can be set directly on
     * a {@link CompositeFreezeLayer}. This way the freeze border will be
     * rendered only inside the {@link CompositeFreezeLayer}.
     *
     * @param compositeFreezeLayer
     *            The {@link CompositeFreezeLayer} for rendering the freeze
     *            border.
     */
    public CompositeFreezeLayerPainter(CompositeFreezeLayer compositeFreezeLayer) {
        compositeFreezeLayer.super();
        this.compositeFreezeLayer = compositeFreezeLayer;
        this.freezeLayer = compositeFreezeLayer.getChildLayerByLayoutCoordinate(0, 0);
    }

    /**
     * Creates a {@link CompositeFreezeLayerPainter} that can be set on a
     * {@link CompositeLayer} that contains a {@link CompositeFreezeLayer}. This
     * way the freeze border will be rendered also on the adjacent regions. For
     * this typically the given {@link #compositeLayer} is inspected and the
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
    public CompositeFreezeLayerPainter(CompositeLayer compositeLayer, CompositeFreezeLayer compositeFreezeLayer) {
        this(compositeLayer, compositeFreezeLayer, true);
    }

    /**
     * Creates a {@link CompositeFreezeLayerPainter} that can be set on a
     * {@link CompositeLayer} that contains a {@link CompositeFreezeLayer}. This
     * way the freeze border will be rendered also on the adjacent regions. For
     * this typically the given {@link #compositeLayer} is inspected and the
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
    public CompositeFreezeLayerPainter(CompositeLayer compositeLayer, CompositeFreezeLayer compositeFreezeLayer, boolean inspectComposite) {
        compositeLayer.super();
        this.compositeLayer = compositeLayer;
        this.compositeFreezeLayer = compositeFreezeLayer;
        this.freezeLayer = compositeFreezeLayer.getChildLayerByLayoutCoordinate(0, 0);
        this.inspectComposite = inspectComposite;
    }

    @Override
    public void paintLayer(
            ILayer natLayer, GC gc,
            int xOffset, int yOffset,
            Rectangle rectangle, IConfigRegistry configRegistry) {

        super.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);

        Color separatorColor = configRegistry.getConfigAttribute(
                IFreezeConfigAttributes.SEPARATOR_COLOR,
                DisplayMode.NORMAL);
        if (separatorColor == null) {
            separatorColor = GUIHelper.COLOR_BLUE;
        }

        Integer separatorWidth = configRegistry.getConfigAttribute(
                IFreezeConfigAttributes.SEPARATOR_WIDTH,
                DisplayMode.NORMAL);
        if (separatorWidth == null) {
            separatorWidth = 1;
        }

        gc.setClipping(rectangle);
        Color oldFg = gc.getForeground();
        int oldWidth = gc.getLineWidth();
        gc.setForeground(separatorColor);
        gc.setLineWidth(GUIHelper.convertHorizontalPixelToDpi(separatorWidth, configRegistry));
        final int freezeWidth = this.freezeLayer.getWidth() - 1;
        if (freezeWidth > 0) {
            int x = getFreezeX(xOffset);
            gc.drawLine(
                    x,
                    yOffset,
                    x,
                    yOffset + getHeight() - 1);
        }
        final int freezeHeight = this.freezeLayer.getHeight() - 1;
        if (freezeHeight > 0) {
            int y = getFreezeY(yOffset);
            gc.drawLine(
                    xOffset,
                    y,
                    xOffset + getWidth() - 1,
                    y);
        }
        gc.setForeground(oldFg);
        gc.setLineWidth(oldWidth);
    }

    /**
     * Returns the height of the freeze border dependent on the configured
     * layers this painter is attached to.
     *
     * @return The height of the freeze border.
     */
    protected int getHeight() {
        if (this.compositeLayer != null) {
            return this.compositeLayer.getHeight();
        }
        return this.compositeFreezeLayer.getHeight();
    }

    /**
     * Returns the width of the freeze border dependent on the configured layers
     * this painter is attached to.
     *
     * @return The width of the freeze border.
     */
    protected int getWidth() {
        if (this.compositeLayer != null) {
            return this.compositeLayer.getWidth();
        }
        return this.compositeFreezeLayer.getWidth();
    }

    /**
     * Returns the x coordinate of the freeze border.
     *
     * @param xOffset
     *            The composition offset.
     * @return The x coordinate value for the horizontal freeze border.
     */
    protected int getFreezeX(int xOffset) {
        int result = xOffset + this.freezeLayer.getWidth() - 1;
        if (this.compositeLayer != null && this.inspectComposite && this.compositeLayer.getLayoutXCount() > 1) {
            result += this.compositeLayer.getChildLayerByLayoutCoordinate(0, 0).getWidth();
        }
        for (ILayer nested : this.nestedHorizontalLayers) {
            result += nested.getWidth();
        }
        return result;
    }

    /**
     * Returns the y coordinate of the freeze border.
     *
     * @param yOffset
     *            The composition offset.
     * @return The y coordinate value for the vertical freeze border.
     */
    protected int getFreezeY(int yOffset) {
        int result = yOffset + this.freezeLayer.getHeight() - 1;
        if (this.compositeLayer != null && this.inspectComposite && this.compositeLayer.getLayoutYCount() > 1) {
            result += this.compositeLayer.getChildLayerByLayoutCoordinate(0, 0).getHeight();
        }
        for (ILayer nested : this.nestedVerticalLayers) {
            result += nested.getHeight();
        }
        return result;
    }

    /**
     * Adds the given layer to the list of nested vertical layers that are used
     * to shift the freeze border down. Needed in case of nested compositions,
     * e.g. using a fixed summary row in the body region.
     *
     * @param layer
     *            The {@link ILayer} to add.
     */
    public void addNestedVerticalLayer(ILayer layer) {
        if (layer != null) {
            this.nestedVerticalLayers.add(layer);
        }
    }

    /**
     * Adds the given layer to the list of nested horizontal layers that are
     * used to shift the freeze border to the right. Needed in case of nested
     * compositions in the body region.
     *
     * @param layer
     *            The {@link ILayer} to add.
     */
    public void addNestedHorizontalLayer(ILayer layer) {
        if (layer != null) {
            this.nestedHorizontalLayers.add(layer);
        }
    }
}
