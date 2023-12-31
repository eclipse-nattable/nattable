/*****************************************************************************
 * Copyright (c) 2015, 2023 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.richtext.RichTextPainter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * {@link ICellPainter} that is able to render HTML formatted text by using the
 * Nebula {@link RichTextPainter}.
 */
public class RichTextCellPainter extends AbstractCellPainter {

    protected RichTextPainter richTextPainter;

    /**
     * @since 1.1
     */
    protected boolean calculateByTextLength;
    /**
     * @since 1.1
     */
    protected boolean calculateByTextHeight;
    /**
     * @since 2.1
     */
    private boolean wrapText;

    /**
     * Creates a new {@link RichTextCellPainter} with text wrapping enabled and
     * auto-resizing disabled.
     */
    public RichTextCellPainter() {
        this(true, false, false);
    }

    /**
     * Creates a new {@link RichTextCellPainter} with auto-resizing disabled.
     *
     * @param wrapText
     *            <code>true</code> to enable text wrapping, which means that
     *            text is wrapped for whole words. Words itself are not wrapped.
     */
    public RichTextCellPainter(boolean wrapText) {
        this(wrapText, false, false);
    }

    /**
     * Creates a new {@link RichTextCellPainter}.
     *
     * @param wrapText
     *            <code>true</code> to enable text wrapping, which means that
     *            text is wrapped for whole words. Words itself are not wrapped.
     * @param calculate
     *            <code>true</code> to configure the text painter for
     *            auto-resizing which means to calculate the cell borders
     *            regarding the content
     * @since 1.1
     */
    public RichTextCellPainter(boolean wrapText, boolean calculate) {
        this(wrapText, calculate, calculate);
    }

    /**
     * Creates a new {@link RichTextCellPainter}.
     *
     * @param wrapText
     *            <code>true</code> to enable text wrapping, which means that
     *            text is wrapped for whole words. Words itself are not wrapped.
     * @param calculateByTextLength
     *            <code>true</code> to configure the text painter to calculate
     *            the cell border by containing text length. This means the
     *            width of the cell is calculated by content.
     * @param calculateByTextHeight
     *            <code>true</code> to configure the text painter to calculate
     *            the cell border by containing text height. This means the
     *            height of the cell is calculated by content.
     * @since 1.1
     */
    public RichTextCellPainter(boolean wrapText, boolean calculateByTextLength, boolean calculateByTextHeight) {
        this.richTextPainter = new RichTextPainter(wrapText);
        this.calculateByTextLength = calculateByTextLength;
        this.calculateByTextHeight = calculateByTextHeight;
        this.wrapText = wrapText;
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
        setupGCFromConfig(gc, cellStyle);

        String htmlText = getHtmlText(cell, configRegistry);

        Rectangle initialPainterBounds = new Rectangle(bounds.x, bounds.y - this.richTextPainter.getParagraphSpace(), bounds.width, bounds.height);
        Rectangle painterBounds = new Rectangle(bounds.x, bounds.y - this.richTextPainter.getParagraphSpace(), bounds.width, bounds.height);

        // if a vertical alignment is set != TOP we need to update the bounds
        // Note:
        // to make the vertical alignment handling work correctly, you need to
        // use at least Nebula 3.0, as it contains the necessary fix for the
        // content height calculation. In case you can not consume Nebula >= 3.0
        // as it requires Java 8, it is recommended to configure
        // VerticalAlignmentEnum.TOP to get the same result as in previous
        // versions of the RichTextPainter
        VerticalAlignmentEnum verticalAlignment =
                cellStyle.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
        if (verticalAlignment != VerticalAlignmentEnum.TOP) {

            this.richTextPainter.preCalculate(htmlText, gc, painterBounds, this.wrapText);
            int contentHeight = this.richTextPainter.getPreferredSize().y - 2 * this.richTextPainter.getParagraphSpace();
            int verticalAlignmentPadding = CellStyleUtil.getVerticalAlignmentPadding(
                    cellStyle,
                    painterBounds,
                    contentHeight);
            painterBounds.y = painterBounds.y + verticalAlignmentPadding;
            painterBounds.height = contentHeight;
        }

        this.richTextPainter.paintHTML(htmlText, gc, painterBounds);

        int height = this.richTextPainter.getPreferredSize().y - 2 * this.richTextPainter.getParagraphSpace();
        if (performRowResize(height, initialPainterBounds)) {
            cell.getLayer().doCommand(
                    new RowResizeCommand(
                            cell.getLayer(),
                            cell.getRowPosition(),
                            GUIHelper.convertVerticalDpiToPixel(height, configRegistry) + (cell.getBounds().height - bounds.height)));
        }

        if (performColumnResize(this.richTextPainter.getPreferredSize().x, initialPainterBounds)) {
            cell.getLayer().doCommand(
                    new ColumnResizeCommand(
                            cell.getLayer(),
                            cell.getColumnPosition(),
                            GUIHelper.convertHorizontalDpiToPixel(this.richTextPainter.getPreferredSize().x, configRegistry) + (cell.getBounds().width - bounds.width)));
        }
    }

    @Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
        String htmlText = getHtmlText(cell, configRegistry);

        // using a zero size rectangle for calculation results in a content
        // related preferred size
        this.richTextPainter.preCalculate(htmlText, gc, new Rectangle(0, 0, 0, cell.getBounds().height), false);
        return this.richTextPainter.getPreferredSize().x;
    }

    @Override
    public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
        setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
        String htmlText = getHtmlText(cell, configRegistry);

        // using a zero size rectangle for calculation results in a content
        // related preferred size
        this.richTextPainter.preCalculate(htmlText, gc, new Rectangle(0, 0, cell.getBounds().width, 0), true);
        // we subtract the top and bottom paragraph space
        return this.richTextPainter.getPreferredSize().y - 2 * this.richTextPainter.getParagraphSpace();
    }

    /**
     * Setup the {@link GC} by the values defined in the given cell style.
     *
     * @param gc
     *            The {@link GC} that is used for rendering.
     * @param cellStyle
     *            The {@link IStyle} that contains the styles to apply.
     */
    public void setupGCFromConfig(GC gc, IStyle cellStyle) {
        Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
        Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
        Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);

        gc.setAntialias(GUIHelper.DEFAULT_ANTIALIAS);
        gc.setTextAntialias(GUIHelper.DEFAULT_TEXT_ANTIALIAS);
        gc.setFont(font);
        gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
        gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);
    }

    /**
     * Checks if a row resize needs to be triggered.
     *
     * @param contentHeight
     *            The necessary height to show the content completely
     * @param rectangle
     *            The available rectangle to render to
     * @return <code>true</code> if a row resize needs to be performed,
     *         <code>false</code> if not
     * @since 1.1
     */
    protected boolean performRowResize(int contentHeight, Rectangle rectangle) {
        return ((contentHeight > rectangle.height) && this.calculateByTextHeight);
    }

    /**
     * Checks if a column resize needs to be triggered.
     *
     * @param contentWidth
     *            The necessary width to show the content completely
     * @param rectangle
     *            The available rectangle to render to
     * @return <code>true</code> if a column resize needs to be performed,
     *         <code>false</code> if not
     * @since 1.1
     */
    protected boolean performColumnResize(int contentWidth, Rectangle rectangle) {
        return ((contentWidth > rectangle.width) && this.calculateByTextLength);
    }

    /**
     * @return <code>true</code> if this text painter is calculating the cell
     *         dimensions by containing text length. This means the <b>width</b>
     *         of the cell is calculated by content.
     * @since 1.1
     */
    public boolean isCalculateByTextLength() {
        return this.calculateByTextLength;
    }

    /**
     * Configure whether the text painter should calculate the cell dimensions
     * by containing text length. This means the <b>width</b> of the cell is
     * calculated by content.
     *
     * @param calculateByTextLength
     *            <code>true</code> to calculate and modify the cell dimension
     *            according to the text length, <code>false</code> to not
     *            modifying the cell dimensions.
     * @since 1.1
     */
    public void setCalculateByTextLength(boolean calculateByTextLength) {
        this.calculateByTextLength = calculateByTextLength;
    }

    /**
     * @return <code>true</code> if this text painter is calculating the cell
     *         dimensions by containing text height. This means the
     *         <b>height</b> of the cell is calculated by content.
     * @since 1.1
     */
    public boolean isCalculateByTextHeight() {
        return this.calculateByTextHeight;
    }

    /**
     * Configure whether the text painter should calculate the cell dimensions
     * by containing text height. This means the <b>height</b> of the cell is
     * calculated by content.
     *
     * @param calculateByTextHeight
     *            <code>true</code> to calculate and modify the cell dimension
     *            according to the text height, <code>false</code> to not
     *            modifying the cell dimensions.
     * @since 1.1
     */
    public void setCalculateByTextHeight(boolean calculateByTextHeight) {
        this.calculateByTextHeight = calculateByTextHeight;
    }

    /**
     * Returns the HTML text for the data that should be shown in the given
     * {@link ILayerCell}.
     *
     * @param cell
     *            The cell for which the data should be shown.
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the configurations
     *            like e.g. the {@link IDisplayConverter}.
     * @return The data to be shown in the cell as HTML text.
     * @since 2.1
     */
    protected String getHtmlText(ILayerCell cell, IConfigRegistry configRegistry) {
        Object displayValue = CellDisplayConversionUtils.convertDataType(cell, configRegistry);

        IDisplayConverter markupDisplayConverter = configRegistry.getConfigAttribute(
                RichTextConfigAttributes.MARKUP_DISPLAY_CONVERTER,
                cell.getDisplayMode(),
                cell.getConfigLabels());

        if (markupDisplayConverter != null) {
            displayValue = markupDisplayConverter.canonicalToDisplayValue(cell, configRegistry, displayValue);
        }

        return String.valueOf(displayValue);
    }
}
