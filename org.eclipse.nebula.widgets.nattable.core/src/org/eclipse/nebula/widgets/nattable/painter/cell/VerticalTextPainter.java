/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 454506
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

/**
 * TextPainter that draws text into a cell vertically. Can handle word wrapping
 * and/or word cutting and/or automatic calculation and resizing of the cell
 * width and height if the text does not fit into the cell.
 * <p>
 * <b>Note:</b><br>
 * This is the new implementation that uses {@link Transform} to create the
 * rotated vertical text. If you face any issues with this implementation, e.g.
 * wrong automatic size calculations for the rotated text (which can appear for
 * several fonts), you can still try to use the old implementation which is now
 * called {@link VerticalTextImagePainter}.
 * </p>
 */
public class VerticalTextPainter extends AbstractTextPainter {

    /**
     * Flag to configure whether the rotation transformation of the text should
     * be performed clockwise or counter-clockwise. By default the rotation to
     * get vertical text will be performed counter-clockwise. This means a
     * rotation transformation with -90 degree will be performed.
     */
    private boolean rotateClockwise = false;

    public VerticalTextPainter() {
        this(false, true);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     */
    public VerticalTextPainter(boolean wrapText, boolean paintBg) {
        this(wrapText, paintBg, 0);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     * @param spacing
     *            The space between text and cell border
     */
    public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing) {
        this(wrapText, paintBg, spacing, false);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     * @param calculate
     *            tells the text painter to calculate the cell borders regarding
     *            the content
     */
    public VerticalTextPainter(boolean wrapText, boolean paintBg, boolean calculate) {
        this(wrapText, paintBg, 0, calculate);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     * @param calculateByTextLength
     *            tells the text painter to calculate the cell border by
     *            containing text length. For horizontal text rendering, this
     *            means the width of the cell is calculated by content, for
     *            vertical text rendering the height is calculated
     * @param calculateByTextHeight
     *            tells the text painter to calculate the cell border by
     *            containing text height. For horizontal text rendering, this
     *            means the height of the cell is calculated by content, for
     *            vertical text rendering the width is calculated
     */
    public VerticalTextPainter(boolean wrapText, boolean paintBg,
            boolean calculateByTextLength, boolean calculateByTextHeight) {
        this(wrapText, paintBg, 0, calculateByTextLength, calculateByTextHeight);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     * @param spacing
     *            The space between text and cell border
     * @param calculate
     *            tells the text painter to calculate the cell borders regarding
     *            the content
     */
    public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing, boolean calculate) {
        super(wrapText, paintBg, spacing, calculate);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     * @param spacing
     *            The space between text and cell border
     * @param calculateByTextLength
     *            tells the text painter to calculate the cell border by
     *            containing text length. For horizontal text rendering, this
     *            means the width of the cell is calculated by content, for
     *            vertical text rendering the height is calculated
     * @param calculateByTextHeight
     *            tells the text painter to calculate the cell border by
     *            containing text height. For horizontal text rendering, this
     *            means the height of the cell is calculated by content, for
     *            vertical text rendering the width is calculated
     */
    public VerticalTextPainter(boolean wrapText, boolean paintBg, int spacing,
            boolean calculateByTextLength, boolean calculateByTextHeight) {
        super(wrapText, paintBg, spacing, calculateByTextLength,
                calculateByTextHeight);
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
        return gc.textExtent(convertDataType(cell, configRegistry)).y + (this.spacing * 2);
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
        return getLengthFromCache(gc, convertDataType(cell, configRegistry)) + (this.spacing * 2) + 1;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
        if (this.paintBg) {
            super.paintCell(cell, gc, rectangle, configRegistry);
        }

        if (this.paintFg) {
            Rectangle originalClipping = gc.getClipping();
            gc.setClipping(rectangle.intersection(originalClipping));

            IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
            setupGCFromConfig(gc, cellStyle);

            boolean underline = renderUnderlined(cellStyle);
            boolean strikethrough = renderStrikethrough(cellStyle);

            int fontHeight = gc.getFontMetrics().getHeight();
            String text = convertDataType(cell, configRegistry);

            // Draw Text
            text = getTextToDisplay(cell, gc, rectangle.height, text);

            int numberOfNewLines = getNumberOfNewLines(text);

            // if the content height is bigger than the available column width
            // we're extending the column width (only if word wrapping is
            // enabled)
            int contentHeight = (fontHeight * numberOfNewLines) + (this.spacing * 2);
            int contentToCellDiff = (cell.getBounds().width - rectangle.width);

            if ((contentHeight > rectangle.width) && this.calculateByTextHeight) {
                ILayer layer = cell.getLayer();
                layer.doCommand(
                        new ColumnResizeCommand(layer, cell.getColumnPosition(), contentHeight + contentToCellDiff));
            }

            if (text != null && text.length() > 0) {

                if (numberOfNewLines == 1) {
                    int contentWidth = Math.min(getLengthFromCache(gc, text), rectangle.height);

                    Transform transform = new Transform(gc.getDevice());
                    if (!isRotateClockwise()) {
                        transform.rotate(-90f);

                        int xOffset = -rectangle.x
                                + (-contentWidth - rectangle.y)
                                - CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentWidth);
                        int yOffset = rectangle.x
                                + -rectangle.y
                                + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentHeight)
                                + this.spacing;

                        transform.translate(xOffset, yOffset);
                    } else {
                        transform.rotate(90f);

                        int horizontalPadding = CellStyleUtil.getHorizontalAlignmentPadding(
                                cellStyle, rectangle, contentHeight);
                        if (horizontalPadding != 0) {
                            horizontalPadding += gc.getFontMetrics().getLeading();
                        }

                        int xOffset = rectangle.y
                                - rectangle.x
                                + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentWidth);
                        int yOffset = -contentHeight - rectangle.y - rectangle.x - horizontalPadding + this.spacing;

                        transform.translate(xOffset, yOffset);
                    }

                    gc.setTransform(transform);

                    gc.drawText(text, rectangle.x, rectangle.y, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);

                    if (underline || strikethrough) {
                        int length = gc.textExtent(text).x;
                        if (length > 0) {
                            // check and draw underline and strikethrough
                            // separately so it is possible to combine both
                            if (underline) {
                                // y = start y of text + font height
                                // - half of the font descent so the underline
                                // is between the baseline and the bottom
                                int underlineY = rectangle.y + fontHeight - (gc.getFontMetrics().getDescent() / 2);
                                gc.drawLine(rectangle.x, underlineY, rectangle.x + length, underlineY);
                            }

                            if (strikethrough) {
                                // y = start y of text + half of font height +
                                // ascent so lower case characters are also
                                // strikethrough
                                int strikeY = rectangle.y + (fontHeight / 2) + (gc.getFontMetrics().getLeading() / 2);
                                gc.drawLine(rectangle.x, strikeY, rectangle.x + length, strikeY);
                            }
                        }
                    }
                } else {
                    // draw every line by itself because of the alignment,
                    // otherwise the whole text is always aligned right
                    int lineAdjustment = 0;
                    String[] lines = text.split("\n"); //$NON-NLS-1$
                    for (String line : lines) {
                        int lineContentWidth = Math.min(getLengthFromCache(gc, line), rectangle.height);

                        Transform transform = new Transform(gc.getDevice());

                        if (!isRotateClockwise()) {
                            transform.rotate(-90f);

                            int xOffset = -rectangle.x + (-lineContentWidth - rectangle.y)
                                    - CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, lineContentWidth);
                            int yOffset = rectangle.x + -rectangle.y + lineAdjustment
                                    + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentHeight)
                                    + this.spacing;

                            transform.translate(xOffset, yOffset);
                        } else {
                            transform.rotate(90f);

                            int horizontalPadding = CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentHeight);
                            if (horizontalPadding != 0) {
                                horizontalPadding += gc.getFontMetrics().getLeading();
                            }

                            int xOffset = rectangle.y - rectangle.x + CellStyleUtil.getVerticalAlignmentPadding(
                                    cellStyle, rectangle, lineContentWidth);
                            int yOffset = -contentHeight - rectangle.y - rectangle.x + lineAdjustment - horizontalPadding + this.spacing;

                            transform.translate(xOffset, yOffset);
                        }

                        gc.setTransform(transform);

                        gc.drawText(line, rectangle.x, rectangle.y, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);

                        if (underline || strikethrough) {
                            int length = gc.textExtent(line).x;
                            if (length > 0) {
                                // check and draw underline and strikethrough
                                // separately so it is possible to combine both
                                if (underline) {
                                    // y = start y of text + font height - half
                                    // of the font descent so the underline is
                                    // between the baseline and the bottom
                                    int underlineY = rectangle.y + fontHeight - (gc.getFontMetrics().getDescent() / 2);
                                    gc.drawLine(rectangle.x, underlineY, rectangle.x + length, underlineY);
                                }

                                if (strikethrough) {
                                    // y = start y of text + half of font height
                                    // + ascent so lower case characters are
                                    // also strikethrough
                                    int strikeY = rectangle.y + (fontHeight / 2) + (gc.getFontMetrics().getLeading() / 2);
                                    gc.drawLine(rectangle.x, strikeY, rectangle.x + length, strikeY);
                                }
                            }
                        }

                        // after every line calculate the lineAdjustment for
                        // offset calculation
                        lineAdjustment += fontHeight;
                    }
                }

                gc.setTransform(null);
            }

            gc.setClipping(originalClipping);
        }
    }

    @Override
    protected void setNewMinLength(ILayerCell cell, int contentHeight) {
        int cellLength = cell.getBounds().height;
        if (cellLength < contentHeight) {

            ILayer layer = cell.getLayer();
            layer.doCommand(
                    new RowResizeCommand(layer, cell.getRowPosition(), contentHeight));
        }
    }

    @Override
    protected int calculatePadding(ILayerCell cell, int availableLength) {
        return cell.getBounds().height - availableLength;
    }

    /**
     * @return <code>true</code> if the rotation transformation should be
     *         performed clockwise (90 degree), <code>false</code> if it should
     *         be performed counter-clockwise (-90 degree). Default is
     *         <code>false</code>.
     */
    public boolean isRotateClockwise() {
        return this.rotateClockwise;
    }

    /**
     * @param rotateClockwise
     *            <code>true</code> if the rotation transformation should be
     *            performed clockwise (90 degree), <code>false</code> if it
     *            should be performed counter-clockwise (-90 degree).
     */
    public void setRotateClockwise(boolean rotateClockwise) {
        this.rotateClockwise = rotateClockwise;
    }
}