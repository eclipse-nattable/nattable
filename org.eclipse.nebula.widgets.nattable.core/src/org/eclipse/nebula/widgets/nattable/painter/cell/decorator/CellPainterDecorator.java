/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell.decorator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Decorates a cell painter with another cell painter.
 */
public class CellPainterDecorator implements ICellPainter {

    /**
     * The base {@link ICellPainter} that is decorated.
     */
    private ICellPainter baseCellPainter;
    /**
     * The edge of the cell at which the decoration is applied.
     */
    private CellEdgeEnum cellEdge;
    /**
     * The {@link ICellPainter} that is used to render the decoration.
     */
    private ICellPainter decoratorCellPainter;
    /**
     * The spacing to use between base painter and decoration painter. Note: If
     * you want to add <b>padding</b> between the decoration and the cell border
     * you need to add a PaddingDecorator to your painter stack.
     */
    private int spacing;
    /**
     * Flag to specify whether the base painter should render dependent to the
     * decoration painter or not. This will have effect on the boundary
     * calculation. Setting this flag to <code>true</code> the bounds of the
     * base painter will be modified regarding the bounds of the decoration
     * painter. This means that the starting coordinates for the base painter
     * are moving e.g. if the base painter renders centered the text will move
     * to the left because the decoration consumes space. If this flag is set to
     * <code>false</code> you can think of the decoration painter painting on
     * top of the base painter, possibly painting over the base painter.
     */
    private boolean paintDecorationDependent;
    /**
     * Flag to specify whether this decorator should paint the background or
     * not. Typically it will paint the background to avoid rendering gaps for
     * the configured spacing. Using a default background this will work well as
     * all painters will use the same background color. Using e.g. the
     * GradientBackgroundPainter, this flag needs to be <code>false</code> as
     * otherwise there will be no gradient background because it gets
     * overpainted by the internal background painting.
     */
    private boolean paintBg;

    /**
     * Creates a {@link CellPainterDecorator} without any configuration to
     * support initialization afterwards.
     *
     * @since 1.4
     */
    public CellPainterDecorator() {
        this(null, null, 0, null, true, true);
    }

    /**
     * Will create a {@link CellPainterDecorator} with the default spacing of 2
     * between base and decoration painter, where the base painter is rendered
     * dependent to the decoration.
     *
     * @param baseCellPainter
     *            The base {@link ICellPainter} that should be decorated
     * @param cellEdge
     *            The edge of the cell at which the decoration should be applied
     * @param decoratorCellPainter
     *            The {@link ICellPainter} that should be used to render the
     *            decoration.
     */
    public CellPainterDecorator(
            ICellPainter baseCellPainter,
            CellEdgeEnum cellEdge,
            ICellPainter decoratorCellPainter) {
        this(baseCellPainter, cellEdge, 2, decoratorCellPainter);
    }

    /**
     * Will create a {@link CellPainterDecorator} with the default spacing of 2
     * between base and decoration painter. If paintDecorationDependent is set
     * to <code>false</code>, the spacing will be ignored.
     *
     * @param baseCellPainter
     *            The base {@link ICellPainter} that should be decorated
     * @param cellEdge
     *            The edge of the cell at which the decoration should be applied
     * @param decoratorCellPainter
     *            The {@link ICellPainter} that should be used to render the
     *            decoration.
     * @param paintDecorationDependent
     *            Flag to specify whether the base painter should render
     *            dependent to the decoration painter or not.
     */
    public CellPainterDecorator(
            ICellPainter baseCellPainter,
            CellEdgeEnum cellEdge,
            ICellPainter decoratorCellPainter,
            boolean paintDecorationDependent) {
        this(baseCellPainter, cellEdge, 2, decoratorCellPainter, paintDecorationDependent);
    }

    /**
     * Will create a {@link CellPainterDecorator} with the given amount of
     * pixels as spacing between base and decoration painter, where the base
     * painter is rendered dependent to the decoration.
     *
     * @param baseCellPainter
     *            The base {@link ICellPainter} that should be decorated
     * @param cellEdge
     *            The edge of the cell at which the decoration should be applied
     * @param spacing
     *            The amount of pixels that should be used as spacing between
     *            decoration and base painter
     * @param decoratorCellPainter
     *            The {@link ICellPainter} that should be used to render the
     *            decoration.
     */
    public CellPainterDecorator(
            ICellPainter baseCellPainter,
            CellEdgeEnum cellEdge,
            int spacing,
            ICellPainter decoratorCellPainter) {
        this(baseCellPainter, cellEdge, spacing, decoratorCellPainter, true);
    }

    /**
     * Will create a {@link CellPainterDecorator} with the given amount of
     * pixels as spacing between base and decoration painter. If
     * paintDecorationDependent is set to <code>false</code>, the spacing will
     * be ignored while the decoration is mainly rendered over the base painter.
     *
     * @param baseCellPainter
     *            The base {@link ICellPainter} that should be decorated
     * @param cellEdge
     *            The edge of the cell at which the decoration should be applied
     * @param decoratorCellPainter
     *            The {@link ICellPainter} that should be used to render the
     *            decoration.
     * @param paintDecorationDependent
     *            Flag to specify whether the base painter should render
     *            dependent to the decoration painter or not.
     */
    public CellPainterDecorator(
            ICellPainter baseCellPainter,
            CellEdgeEnum cellEdge,
            int spacing,
            ICellPainter decoratorCellPainter,
            boolean paintDecorationDependent) {
        this(baseCellPainter, cellEdge, spacing, decoratorCellPainter, paintDecorationDependent, true);
    }

    /**
     * Will create a {@link CellPainterDecorator} with the given amount of
     * pixels as spacing between base and decoration painter. If
     * paintDecorationDependent is set to <code>false</code>, the spacing will
     * be ignored while the decoration is mainly rendered over the base painter.
     *
     * @param baseCellPainter
     *            The base {@link ICellPainter} that should be decorated
     * @param cellEdge
     *            The edge of the cell at which the decoration should be applied
     * @param decoratorCellPainter
     *            The {@link ICellPainter} that should be used to render the
     *            decoration.
     * @param paintDecorationDependent
     *            Flag to specify whether the base painter should render
     *            dependent to the decoration painter or not.
     * @param paintBg
     *            <code>true</code> if the PaddingDecorator should paint the
     *            background, <code>false</code> if not.
     */
    public CellPainterDecorator(
            ICellPainter baseCellPainter,
            CellEdgeEnum cellEdge,
            int spacing,
            ICellPainter decoratorCellPainter,
            boolean paintDecorationDependent,
            boolean paintBg) {
        this.baseCellPainter = baseCellPainter;
        this.cellEdge = cellEdge;
        this.spacing = spacing;
        this.decoratorCellPainter = decoratorCellPainter;
        this.paintDecorationDependent = paintDecorationDependent;
        this.paintBg = paintBg;
    }

    /**
     * @param paintDecorationDependent
     *            <code>true</code> if the base painter should render dependent
     *            to the decoration painter, <code>false</code> if the
     *            decoration should be rendered over the base painter.
     */
    public void setPaintDecorationDependent(boolean paintDecorationDependent) {
        this.paintDecorationDependent = paintDecorationDependent;
    }

    /**
     * Configure whether this CellPainterDecorator should paint the background
     * or not. By default it will paint the background to ensure the spacing is
     * also painted in the configured background color. This will only cause
     * issues in case another background painter like the
     * GradientBackroundPainter should be used.
     *
     * @param paintBg
     *            <code>true</code> if this CellPainterDecorator should also
     *            paint the background, <code>false</code> if the background
     *            should not be painted.
     */
    public void setPaintBackground(boolean paintBg) {
        this.paintBg = paintBg;
    }

    /**
     *
     * @deprecated use setPaintDecorationDependent() instead, note that the
     *             semantic is different
     */
    @Deprecated
    public void setBaseCellPainterSpansWholeCell(boolean interiorPainterToSpanFullWidth) {
        this.paintDecorationDependent = !interiorPainterToSpanFullWidth;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {

        int baseWidth = (this.baseCellPainter != null
                ? this.baseCellPainter.getPreferredWidth(cell, gc, configRegistry) : 0);
        int decorationWidth = (this.decoratorCellPainter != null
                ? this.decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry) : 0);

        switch (this.cellEdge) {
            case TOP_LEFT:
            case TOP_RIGHT:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
            case BOTTOM:
            case TOP:
                return this.spacing + Math.max(baseWidth, decorationWidth);
            default:
                break;
        }

        return baseWidth + this.spacing + decorationWidth;
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {

        int baseHeight = (this.baseCellPainter != null
                ? this.baseCellPainter.getPreferredHeight(cell, gc, configRegistry) : 0);
        int decorationHeight = (this.decoratorCellPainter != null
                ? this.decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry) : 0);

        switch (this.cellEdge) {
            case TOP_LEFT:
            case TOP_RIGHT:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
            case LEFT:
            case RIGHT:
                return this.spacing + Math.max(baseHeight, decorationHeight);
            default:
                break;
        }

        return baseHeight + this.spacing + decorationHeight;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
        Rectangle baseCellPainterBounds = this.paintDecorationDependent
                ? getBaseCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry) : adjustedCellBounds;
        Rectangle decoratorCellPainterBounds = getDecoratorCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry);

        if (this.paintBg) {
            Color originalBg = gc.getBackground();
            gc.setBackground(CellStyleUtil
                    .getCellStyle(cell, configRegistry)
                    .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
            gc.fillRectangle(adjustedCellBounds);
            gc.setBackground(originalBg);
        }

        if (this.baseCellPainter != null) {
            this.baseCellPainter.paintCell(cell, gc, baseCellPainterBounds, configRegistry);
        }

        if (this.decoratorCellPainter != null) {
            this.decoratorCellPainter.paintCell(cell, gc, decoratorCellPainterBounds, configRegistry);
        }
    }

    /**
     *
     * @return The Rectangle which can be used by the base cell painter.
     */
    public Rectangle getBaseCellPainterBounds(
            ILayerCell cell,
            GC gc,
            Rectangle adjustedCellBounds,
            IConfigRegistry configRegistry) {

        int preferredDecoratorWidth = (this.decoratorCellPainter != null
                ? this.decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry) : 0);
        int preferredDecoratorHeight = (this.decoratorCellPainter != null
                ? this.decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry) : 0);

        // grab any extra space:
        int grabbedPreferredWidth = adjustedCellBounds.width - preferredDecoratorWidth - this.spacing;
        int grabbedPreferredHeight = adjustedCellBounds.height - preferredDecoratorHeight - this.spacing;

        switch (this.cellEdge) {
            case LEFT:
                return new Rectangle(
                        adjustedCellBounds.x + preferredDecoratorWidth + this.spacing,
                        adjustedCellBounds.y,
                        grabbedPreferredWidth,
                        adjustedCellBounds.height)
                                .intersection(adjustedCellBounds);
            case RIGHT:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y,
                        grabbedPreferredWidth,
                        adjustedCellBounds.height)
                                .intersection(adjustedCellBounds);
            case TOP:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y + preferredDecoratorHeight + this.spacing,
                        adjustedCellBounds.width,
                        grabbedPreferredHeight)
                                .intersection(adjustedCellBounds);
            case BOTTOM:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y,
                        adjustedCellBounds.width,
                        grabbedPreferredHeight)
                                .intersection(adjustedCellBounds);
            case TOP_LEFT:
                return new Rectangle(
                        adjustedCellBounds.x + preferredDecoratorWidth + this.spacing,
                        adjustedCellBounds.y + preferredDecoratorHeight + this.spacing,
                        grabbedPreferredWidth,
                        grabbedPreferredHeight)
                                .intersection(adjustedCellBounds);
            case TOP_RIGHT:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y + preferredDecoratorHeight + this.spacing,
                        grabbedPreferredWidth,
                        grabbedPreferredHeight)
                                .intersection(adjustedCellBounds);
            case BOTTOM_LEFT:
                return new Rectangle(
                        adjustedCellBounds.x + preferredDecoratorWidth + this.spacing,
                        adjustedCellBounds.y,
                        grabbedPreferredWidth,
                        grabbedPreferredHeight)
                                .intersection(adjustedCellBounds);
            case BOTTOM_RIGHT:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y,
                        grabbedPreferredWidth,
                        grabbedPreferredHeight)
                                .intersection(adjustedCellBounds);
            case NONE:
                break;
        }

        return null;
    }

    /**
     * @return The Rectangle to paint the decoration.
     */
    public Rectangle getDecoratorCellPainterBounds(
            ILayerCell cell,
            GC gc,
            Rectangle adjustedCellBounds,
            IConfigRegistry configRegistry) {

        int preferredDecoratorWidth = (this.decoratorCellPainter != null
                ? this.decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry) : 0);
        int preferredDecoratorHeight = (this.decoratorCellPainter != null
                ? this.decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry) : 0);

        switch (this.cellEdge) {
            case LEFT:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y + ((adjustedCellBounds.height - preferredDecoratorHeight) / 2),
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case RIGHT:
                return new Rectangle(
                        adjustedCellBounds.x + adjustedCellBounds.width - preferredDecoratorWidth,
                        adjustedCellBounds.y + ((adjustedCellBounds.height - preferredDecoratorHeight) / 2),
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case TOP:
                return new Rectangle(
                        adjustedCellBounds.x + ((adjustedCellBounds.width - preferredDecoratorWidth) / 2),
                        adjustedCellBounds.y,
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case BOTTOM:
                return new Rectangle(
                        adjustedCellBounds.x + ((adjustedCellBounds.width - preferredDecoratorWidth) / 2),
                        adjustedCellBounds.y + adjustedCellBounds.height - preferredDecoratorHeight,
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case TOP_LEFT:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y,
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case TOP_RIGHT:
                return new Rectangle(
                        adjustedCellBounds.x + adjustedCellBounds.width - preferredDecoratorWidth,
                        adjustedCellBounds.y,
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case BOTTOM_LEFT:
                return new Rectangle(
                        adjustedCellBounds.x,
                        adjustedCellBounds.y + adjustedCellBounds.height - preferredDecoratorHeight,
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case BOTTOM_RIGHT:
                return new Rectangle(
                        adjustedCellBounds.x + adjustedCellBounds.width - preferredDecoratorWidth,
                        adjustedCellBounds.y + adjustedCellBounds.height - preferredDecoratorHeight,
                        preferredDecoratorWidth,
                        preferredDecoratorHeight);
            case NONE:
                break;
        }

        return null;
    }

    @Override
    public ICellPainter getCellPainterAt(
            int x,
            int y,
            ILayerCell cell,
            GC gc,
            Rectangle adjustedCellBounds,
            IConfigRegistry configRegistry) {

        Rectangle decoratorCellPainterBounds =
                getDecoratorCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry);
        if (this.decoratorCellPainter != null && decoratorCellPainterBounds.contains(x, y)) {
            return this.decoratorCellPainter.getCellPainterAt(x, y, cell, gc, decoratorCellPainterBounds, configRegistry);
        } else {
            Rectangle baseCellPainterBounds = this.paintDecorationDependent
                    ? getBaseCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry)
                    : adjustedCellBounds;
            if (this.baseCellPainter != null && baseCellPainterBounds.contains(x, y)) {
                return this.baseCellPainter.getCellPainterAt(x, y, cell, gc, baseCellPainterBounds, configRegistry);
            }
        }
        return this;
    }

    /**
     * @return The base {@link ICellPainter} that is decorated.
     */
    public ICellPainter getBaseCellPainter() {
        return this.baseCellPainter;
    }

    /**
     * @param baseCellPainter
     *            The base {@link ICellPainter} that should be decorated.
     */
    public void setBaseCellPainter(ICellPainter baseCellPainter) {
        this.baseCellPainter = baseCellPainter;
    }

    /**
     * @return The {@link ICellPainter} that is used to render the decoration.
     */
    public ICellPainter getDecoratorCellPainter() {
        return this.decoratorCellPainter;
    }

    /**
     * @param decoratorCellPainter
     *            The {@link ICellPainter} that should be used to render the
     *            decoration.
     */
    public void setDecoratorCellPainter(ICellPainter decoratorCellPainter) {
        this.decoratorCellPainter = decoratorCellPainter;
    }

    /**
     * @return The edge of the cell at which the decoration is applied.
     */
    public CellEdgeEnum getCellEdge() {
        return this.cellEdge;
    }

    /**
     * @param cellEdge
     *            The edge of the cell at which the decoration should be
     *            applied.
     */
    public void setCellEdge(CellEdgeEnum cellEdge) {
        this.cellEdge = cellEdge;
    }

    /**
     * @return The spacing that is used between base painter and decoration
     *         painter.
     */
    public int getSpacing() {
        return this.spacing;
    }

    /**
     * @param spacing
     *            The spacing that should be used between base painter and
     *            decoration painter.
     */
    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }
}
