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
package org.eclipse.nebula.widgets.nattable.painter.cell.decorator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * CellPainterWrapper that adds a padding between the cell border and the
 * interior painter.
 */
public class PaddingDecorator extends CellPainterWrapper {

    private final int topPadding;
    private final int rightPadding;
    private final int bottomPadding;
    private final int leftPadding;

    private final boolean paintBg;

    /**
     * Create a PaddingDecorator around the given interior painter, adding a
     * padding of 2 pixels on every side.<br>
     * If will paint the background color to fill the resulting gaps, in case
     * the PaddingDecorator wraps e.g. a TextPainter but is itself not wrapped
     * by a BackgroundPainter.
     *
     * @param interiorPainter
     *            The painter that should be wrapped.
     */
    public PaddingDecorator(ICellPainter interiorPainter) {
        this(interiorPainter, 2);
    }

    /**
     * Create a PaddingDecorator around the given interior painter, adding a
     * padding of 2 pixels on every side.<br>
     * This constructor allows to configure whether the PaddingDecorator should
     * itself paint the background to avoid gaps or not. In case the
     * PaddingDecorator is wrapped in another background painter, e.g.
     * BackgroundImagePainter or GradientBackroundPainter, the paintBg parameter
     * needs to be <code>false</code> to avoid rendering issues.
     *
     * @param interiorPainter
     *            The painter that should be wrapped.
     * @param paintBg
     *            <code>true</code> if the PaddingDecorator should paint the
     *            background, <code>false</code> if not.
     */
    public PaddingDecorator(ICellPainter interiorPainter, boolean paintBg) {
        this(interiorPainter, 2, paintBg);
    }

    /**
     * Create a PaddingDecorator around the given interior painter, adding a
     * padding of the given number of pixels on every side.<br>
     * If will paint the background color to fill the resulting gaps, in case
     * the PaddingDecorator wraps e.g. a TextPainter but is itself not wrapped
     * by a BackgroundPainter.
     *
     * @param interiorPainter
     *            The painter that should be wrapped.
     * @param padding
     *            The number of pixels that should be used as padding on every
     *            side.
     */
    public PaddingDecorator(ICellPainter interiorPainter, int padding) {
        this(interiorPainter, padding, padding, padding, padding);
    }

    /**
     * Create a PaddingDecorator around the given interior painter, adding a
     * padding of the given number of pixels on every side.<br>
     * This constructor allows to configure whether the PaddingDecorator should
     * itself paint the background to avoid gaps or not. In case the
     * PaddingDecorator is wrapped in another background painter, e.g.
     * BackgroundImagePainter or GradientBackroundPainter, the paintBg parameter
     * needs to be <code>false</code> to avoid rendering issues.
     *
     * @param interiorPainter
     *            The painter that should be wrapped.
     * @param padding
     *            The number of pixels that should be used as padding on every
     *            side.
     * @param paintBg
     *            <code>true</code> if the PaddingDecorator should paint the
     *            background, <code>false</code> if not.
     */
    public PaddingDecorator(ICellPainter interiorPainter, int padding, boolean paintBg) {
        this(interiorPainter, padding, padding, padding, padding, paintBg);
    }

    /**
     * Create a PaddingDecorator around the given interior painter, adding the
     * padding specified for each side.<br>
     * If will paint the background color to fill the resulting gaps, in case
     * the PaddingDecorator wraps e.g. a TextPainter but is itself not wrapped
     * by a BackgroundPainter.
     *
     * @param interiorPainter
     *            The painter that should be wrapped.
     * @param topPadding
     *            The number of pixels that should be used as padding on top.
     * @param rightPadding
     *            The number of pixels that should be used as padding to the
     *            right.
     * @param bottomPadding
     *            The number of pixels that should be used as padding at the
     *            bottom.
     * @param leftPadding
     *            The number of pixels that should be used as padding to the
     *            left.
     */
    public PaddingDecorator(ICellPainter interiorPainter,
            int topPadding, int rightPadding, int bottomPadding, int leftPadding) {
        this(interiorPainter, topPadding, rightPadding, bottomPadding, leftPadding, true);
    }

    /**
     * Create a PaddingDecorator around the given interior painter, adding the
     * padding specified for each side.<br>
     * This constructor allows to configure whether the PaddingDecorator should
     * itself paint the background to avoid gaps or not. In case the
     * PaddingDecorator is wrapped in another background painter, e.g.
     * BackgroundImagePainter or GradientBackroundPainter, the paintBg parameter
     * needs to be <code>false</code> to avoid rendering issues.
     *
     * @param interiorPainter
     *            The painter that should be wrapped.
     * @param topPadding
     *            The number of pixels that should be used as padding on top.
     * @param rightPadding
     *            The number of pixels that should be used as padding to the
     *            right.
     * @param bottomPadding
     *            The number of pixels that should be used as padding at the
     *            bottom.
     * @param leftPadding
     *            The number of pixels that should be used as padding to the
     *            left.
     * @param paintBg
     *            <code>true</code> if the PaddingDecorator should paint the
     *            background, <code>false</code> if not.
     */
    public PaddingDecorator(ICellPainter interiorPainter,
            int topPadding, int rightPadding, int bottomPadding, int leftPadding, boolean paintBg) {
        super(interiorPainter);
        this.topPadding = topPadding;
        this.rightPadding = rightPadding;
        this.bottomPadding = bottomPadding;
        this.leftPadding = leftPadding;
        this.paintBg = paintBg;
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return GUIHelper.convertHorizontalPixelToDpi(this.leftPadding, configRegistry)
                + super.getPreferredWidth(cell, gc, configRegistry)
                + GUIHelper.convertHorizontalPixelToDpi(this.rightPadding, configRegistry);
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        return GUIHelper.convertVerticalPixelToDpi(this.topPadding, configRegistry)
                + super.getPreferredHeight(cell, gc, configRegistry)
                + GUIHelper.convertVerticalPixelToDpi(this.bottomPadding, configRegistry);
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
        Rectangle interiorBounds = getInteriorBounds(adjustedCellBounds, configRegistry);

        if (this.paintBg) {
            Color originalBg = gc.getBackground();
            Color cellStyleBackground = getBackgroundColor(cell, configRegistry);
            gc.setBackground(cellStyleBackground != null ? cellStyleBackground : originalBg);
            gc.fillRectangle(adjustedCellBounds);
            gc.setBackground(originalBg);
        }

        if (interiorBounds.width > 0 && interiorBounds.height > 0) {
            super.paintCell(cell, gc, interiorBounds, configRegistry);
        }
    }

    /**
     * Calculates the cell bounds that should be used for the internal painter
     * out of the available bounds for this PaddingDecorator and the configured
     * padding.
     *
     * @param adjustedCellBounds
     *            The cell bounds of the cell to render.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed for accessing the dpi
     *            converter.
     * @return The cell bounds that are available for the interior painter.
     * @since 2.0
     */
    public Rectangle getInteriorBounds(Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
        return new Rectangle(
                adjustedCellBounds.x + GUIHelper.convertHorizontalPixelToDpi(this.leftPadding, configRegistry),
                adjustedCellBounds.y + GUIHelper.convertVerticalPixelToDpi(this.topPadding, configRegistry),
                adjustedCellBounds.width
                        - GUIHelper.convertHorizontalPixelToDpi(this.leftPadding, configRegistry)
                        - GUIHelper.convertHorizontalPixelToDpi(this.rightPadding, configRegistry),
                adjustedCellBounds.height
                        - GUIHelper.convertVerticalPixelToDpi(this.topPadding, configRegistry)
                        - GUIHelper.convertVerticalPixelToDpi(this.bottomPadding, configRegistry));
    }

    /**
     * Extract the background color that is registered for the given ILayerCell.
     *
     * @param cell
     *            The cell for which the background color is requested.
     * @param configRegistry
     *            The IConfigRegistry which contains the style configurations.
     * @return The background color that should be used to render the background
     *         of the given cell.
     */
    protected Color getBackgroundColor(ILayerCell cell, IConfigRegistry configRegistry) {
        return CellStyleUtil.getCellStyle(cell, configRegistry)
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
    }

    @Override
    public ICellPainter getCellPainterAt(
            int x, int y,
            ILayerCell cell, GC gc,
            Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {

        // need to take the alignment into account
        IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);

        HorizontalAlignmentEnum horizontalAlignment =
                cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
        int horizontalAlignmentPadding = 0;
        switch (horizontalAlignment) {
            case LEFT:
                horizontalAlignmentPadding = GUIHelper.convertHorizontalPixelToDpi(this.leftPadding, configRegistry);
                break;
            case CENTER:
                horizontalAlignmentPadding = GUIHelper.convertHorizontalPixelToDpi(this.leftPadding, configRegistry) / 2;
                break;
        }

        VerticalAlignmentEnum verticalAlignment =
                cellStyle.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
        int verticalAlignmentPadding = 0;
        switch (verticalAlignment) {
            case TOP:
                verticalAlignmentPadding = GUIHelper.convertVerticalPixelToDpi(this.topPadding, configRegistry);
                break;
            case MIDDLE:
                verticalAlignmentPadding = GUIHelper.convertVerticalPixelToDpi(this.topPadding, configRegistry) / 2;
                break;
        }

        return super.getCellPainterAt(
                x - horizontalAlignmentPadding,
                y - verticalAlignmentPadding,
                cell,
                gc,
                adjustedCellBounds,
                configRegistry);
    }

    /**
     *
     * @return The top padding added by this {@link PaddingDecorator}.
     *
     * @since 1.4
     */
    public int getTopPadding() {
        return this.topPadding;
    }

    /**
     *
     * @return The right padding added by this {@link PaddingDecorator}.
     *
     * @since 1.4
     */
    public int getRightPadding() {
        return this.rightPadding;
    }

    /**
     *
     * @return The bottom padding added by this {@link PaddingDecorator}.
     *
     * @since 1.4
     */
    public int getBottomPadding() {
        return this.bottomPadding;
    }

    /**
     *
     * @return The left padding added by this {@link PaddingDecorator}.
     *
     * @since 1.4
     */
    public int getLeftPadding() {
        return this.leftPadding;
    }
}
