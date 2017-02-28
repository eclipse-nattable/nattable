/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 461261
 *     Loris Securo <lorissek@gmail.com> - Bug 472668
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.TextDecorationEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;

/**
 * Abstract TextPainter the contains general methods for drawing text into a
 * cell. Can handle word wrapping and/or word cutting and/or automatic
 * calculation and resizing of the cell height or width if the text does not fit
 * into the cell.
 */
public abstract class AbstractTextPainter extends BackgroundPainter {

    public static final String EMPTY = ""; //$NON-NLS-1$
    public static final String DOT = "..."; //$NON-NLS-1$

    /**
     * The regular expression to find predefined new lines in the text to show.
     * Is used for word wrapping to preserve user defined new lines. To be
     * platform independent \n and \r and the combination of both are used to
     * find user defined new lines.
     */
    public static final String NEW_LINE_REGEX = "\\n\\r|\\r\\n|\\n|\\r"; //$NON-NLS-1$

    public static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

    /**
     * @since 1.5
     */
    protected boolean wordWrapping = false;
    protected boolean wrapText;
    protected final boolean paintBg;
    protected boolean paintFg = true;
    protected int spacing = 5;
    /**
     * @since 1.5
     */
    protected int lineSpacing = 0;
    // can only grow but will not calculate the minimal length
    protected boolean calculateByTextLength;
    protected boolean calculateByTextHeight;
    private boolean underline;
    private boolean strikethrough;

    private boolean trimText = true;

    private Color originalBackground;
    private Color originalForeground;
    private Font originalFont;

    private static Map<String, Integer> temporaryMap = new WeakHashMap<String, Integer>();
    private static Map<org.eclipse.swt.graphics.Font, FontData[]> fontDataCache = new WeakHashMap<org.eclipse.swt.graphics.Font, FontData[]>();

    public AbstractTextPainter() {
        this(false, true);
    }

    /**
     * @param wrapText
     *            split text over multiple lines
     * @param paintBg
     *            skips painting the background if is FALSE
     */
    public AbstractTextPainter(boolean wrapText, boolean paintBg) {
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
    public AbstractTextPainter(boolean wrapText, boolean paintBg, int spacing) {
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
    public AbstractTextPainter(boolean wrapText, boolean paintBg,
            boolean calculate) {
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
    public AbstractTextPainter(boolean wrapText, boolean paintBg,
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
    public AbstractTextPainter(boolean wrapText, boolean paintBg, int spacing,
            boolean calculate) {
        this(wrapText, paintBg, spacing, calculate, calculate);
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
    public AbstractTextPainter(boolean wrapText, boolean paintBg, int spacing,
            boolean calculateByTextLength, boolean calculateByTextHeight) {
        this.wrapText = wrapText;
        this.paintBg = paintBg;
        this.spacing = spacing;
        this.calculateByTextLength = calculateByTextLength;
        this.calculateByTextHeight = calculateByTextHeight;
    }

    /**
     * Convert the data value of the cell using the {@link IDisplayConverter}
     * from the {@link IConfigRegistry}
     */
    protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
        return CellDisplayConversionUtils.convertDataType(cell, configRegistry);
    }

    /**
     * Setup the GC by the values defined in the given cell style.
     *
     * @param gc
     * @param cellStyle
     */
    public void setupGCFromConfig(GC gc, IStyle cellStyle) {
        Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
        Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
        Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);

        // remember previous settings
        this.originalFont = gc.getFont();
        this.originalForeground = gc.getForeground();
        this.originalBackground = gc.getBackground();

        gc.setAntialias(GUIHelper.DEFAULT_ANTIALIAS);
        gc.setTextAntialias(GUIHelper.DEFAULT_TEXT_ANTIALIAS);
        gc.setFont(font);
        gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
        gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);
    }

    /**
     * Reset the GC to the original values.
     *
     * @param gc
     *
     * @since 1.4
     */
    public void resetGC(GC gc) {
        gc.setFont(this.originalFont);
        gc.setForeground(this.originalForeground);
        gc.setBackground(this.originalBackground);
    }

    /**
     * Checks if there is a underline text decoration configured within the
     * given cell style.
     *
     * @param cellStyle
     *            The cell style of the current cell to check for the text
     *            decoration.
     * @return <code>true</code> if there is a underline text decoration
     *         configured, <code>false</code> otherwise.
     */
    protected boolean renderUnderlined(IStyle cellStyle) {
        TextDecorationEnum decoration = cellStyle.getAttributeValue(CellStyleAttributes.TEXT_DECORATION);
        if (decoration != null) {
            return (decoration.equals(TextDecorationEnum.UNDERLINE)
                    || decoration.equals(TextDecorationEnum.UNDERLINE_STRIKETHROUGH));
        }
        return this.underline;
    }

    /**
     * Checks if there is a strikethrough text decoration configured within the
     * given cell style.
     *
     * @param cellStyle
     *            The cell style of the current cell to check for the text
     *            decoration.
     * @return <code>true</code> if there is a strikethrough text decoration
     *         configured, <code>false</code> otherwise.
     */
    protected boolean renderStrikethrough(IStyle cellStyle) {
        TextDecorationEnum decoration = cellStyle.getAttributeValue(CellStyleAttributes.TEXT_DECORATION);
        if (decoration != null) {
            return (decoration.equals(TextDecorationEnum.STRIKETHROUGH)
                    || decoration.equals(TextDecorationEnum.UNDERLINE_STRIKETHROUGH));
        }
        return this.strikethrough;
    }

    /**
     * Scans for new line characters and counts the number of lines for the
     * given text.
     *
     * @param text
     *            the text to scan
     * @return the number of lines for the given text
     */
    protected int getNumberOfNewLines(String text) {
        String[] lines = text.split(NEW_LINE_REGEX);
        return lines.length;
    }

    /**
     * Calculates the length of a given text by using the GC. To minimize the
     * count of calculations, the calculation result will be stored within a
     * Map, so the next time the length of the same text is asked for, the
     * result is only returned by cache and is not calculated again.
     *
     * @param gc
     *            the current GC
     * @param text
     *            the text to get the length for
     * @return the length of the text
     */
    protected int getLengthFromCache(GC gc, String text) {
        String originalString = text;
        StringBuilder buffer = new StringBuilder();
        buffer.append(text);
        if (gc.getFont() != null) {
            FontData[] datas = fontDataCache.get(gc.getFont());
            if (datas == null) {
                datas = gc.getFont().getFontData();
                fontDataCache.put(gc.getFont(), datas);
            }
            if (datas != null && datas.length > 0) {
                buffer.append(datas[0].getName());
                buffer.append(","); //$NON-NLS-1$
                buffer.append(datas[0].getHeight());
                buffer.append(","); //$NON-NLS-1$
                buffer.append(datas[0].getStyle());
            }
        }
        text = buffer.toString();
        Integer width = temporaryMap.get(text);
        if (width == null) {
            width = Integer.valueOf(gc.textExtent(originalString).x);
            temporaryMap.put(text, width);
        }

        return width.intValue();
    }

    /**
     * Computes dependent on the configuration of the TextPainter the text to
     * display. If word wrapping is enabled new lines are inserted if the
     * available space is not enough. If calculation of available space is
     * enabled, the space is automatically widened for the text to display, and
     * if no calculation is enabled the text is cut and modified to end with
     * "..." to fit into the available space
     *
     * @param cell
     *            the current cell to paint
     * @param gc
     *            the current GC
     * @param availableLength
     *            the available space for the text to display
     * @param text
     *            the text that should be modified for display
     * @return the modified text
     */
    protected String getTextToDisplay(ILayerCell cell, GC gc, int availableLength, String text) {
        StringBuilder output = new StringBuilder();

        if (isTrimText()) {
            text = text.trim();
        }

        // take the whole width of the text
        int textLength = getLengthFromCache(gc, text);

        if (this.wordWrapping || (!this.calculateByTextLength && this.wrapText)) {
            String[] lines = text.split(NEW_LINE_REGEX);
            for (String line : lines) {
                if (output.length() > 0) {
                    output.append(LINE_SEPARATOR);
                }

                String[] words = line.split("\\s"); //$NON-NLS-1$

                // concat the words with spaces and newlines
                String computedText = ""; //$NON-NLS-1$
                for (String word : words) {
                    computedText = computeTextToDisplay(computedText, word, gc, availableLength);
                }

                output.append(computedText);
            }

        } else if (this.calculateByTextLength && this.wrapText) {
            if (availableLength < textLength) {
                // calculate length by finding the longest word in text
                textLength = (availableLength - (2 * this.spacing));

                String[] lines = text.split(NEW_LINE_REGEX);
                for (String line : lines) {
                    if (output.length() > 0) {
                        output.append(LINE_SEPARATOR);
                    }

                    String[] words = line.split("\\s"); //$NON-NLS-1$
                    for (String word : words) {
                        textLength = Math.max(textLength, getLengthFromCache(gc, word));
                    }

                    // concat the words with spaces and newlines to be always
                    // smaller then available
                    String computedText = ""; //$NON-NLS-1$
                    for (String word : words) {
                        computedText = computeTextToDisplay(computedText, word, gc, textLength);
                    }
                    output.append(computedText);
                }
            } else {
                output.append(text);
            }

            setNewMinLength(cell, textLength + calculatePadding(cell, availableLength) + (2 * this.spacing));
        } else if (this.calculateByTextLength && !this.wrapText) {
            output.append(modifyTextToDisplay(text, gc, textLength));

            // add padding and spacing to textLength because they are needed for
            // correct sizing
            // padding can occur on using decorators like the
            // BeveledBorderDecorator or the PaddingDecorator
            setNewMinLength(cell, textLength + calculatePadding(cell, availableLength) + (2 * this.spacing));
        } else if (!this.calculateByTextLength && !this.wrapText) {
            output.append(modifyTextToDisplay(text, gc, availableLength + (2 * this.spacing)));
        }

        return output.toString();
    }

    /**
     * This method gets only called if text wrapping is enabled. Concatenates
     * the two given words by taking the availableSpace into account. If
     * concatenating those two words with a space as delimiter does fit into the
     * available space the return value is exactly this. Else instead of a space
     * there will be a new line character used as delimiter.
     *
     * @param one
     *            the first word or the whole text before the next word
     * @param two
     *            the next word to add to the first parameter
     * @param gc
     *            the current GC
     * @param availableSpace
     *            the available space
     * @return the concatenated String of the first two parameters
     */
    private String computeTextToDisplay(String one, String two, GC gc, int availableSpace) {
        String result = one;
        // if one is empty or one ends with newline just add two
        if (one == null || one.length() == 0 || one.endsWith(LINE_SEPARATOR)) {
            result += modifyTextToDisplay(two, gc, availableSpace);
        } else {
            // if one contains a newline
            if (one.indexOf(LINE_SEPARATOR) != -1) {
                // get the end of the last part after the last newline
                one = one.substring(one.lastIndexOf(LINE_SEPARATOR) + LINE_SEPARATOR.length());
            }

            if (getLengthFromCache(gc, one) == availableSpace
                    || getLengthFromCache(gc, one + " " + two) >= availableSpace) { //$NON-NLS-1$
                result += LINE_SEPARATOR;
                result += modifyTextToDisplay(two, gc, availableSpace);
            } else {
                result += ' ';
                result += two;
            }
        }
        return result;
    }

    /**
     * Checks if the given text is bigger than the available space. If not the
     * given text is simply returned without modification. If the text does not
     * fit into the available space, it will be modified by cutting and adding
     * three dots.
     *
     * @param text
     *            the text to compute
     * @param gc
     *            the current GC
     * @param availableLength
     *            the available space
     * @return the modified text if it is bigger than the available space or the
     *         text as it was given if it fits into the available space
     */
    private String modifyTextToDisplay(String text, GC gc, int availableLength) {
        // length of the text on GC taking new lines into account
        // this means the textLength is the value of the longest line
        int textLength = getLengthFromCache(gc, text);
        if (textLength > availableLength) {
            // as looking at the text length without taking new lines into
            // account we have to look at every line itself
            StringBuilder result = new StringBuilder();
            String[] lines = text.split(NEW_LINE_REGEX);
            for (String line : lines) {
                if (result.length() > 0) {
                    result.append(LINE_SEPARATOR);
                }

                // now modify every line if it is longer than the available
                // space this way every line will get ... if it doesn't fit
                int lineLength = getLengthFromCache(gc, line);
                if (lineLength > availableLength) {
                    if (!this.wordWrapping) {
                        int numExtraChars = 0;

                        int newStringLength = line.length();
                        String trialLabelText = line + DOT;
                        int newTextExtent = getLengthFromCache(gc, trialLabelText);

                        while (newTextExtent > availableLength + 1 && newStringLength > 0) {
                            double avgWidthPerChar = (double) newTextExtent / trialLabelText.length();
                            numExtraChars += 1 + (int) ((newTextExtent - availableLength) / avgWidthPerChar);

                            newStringLength = line.length() - numExtraChars;
                            if (newStringLength > 0) {
                                trialLabelText = line.substring(0, newStringLength) + DOT;
                                newTextExtent = getLengthFromCache(gc, trialLabelText);
                            }
                        }

                        if (numExtraChars > line.length()) {
                            numExtraChars = line.length();
                        }

                        // now we have gone too short, lets add chars one at a
                        // time to exceed the width...
                        String testString = line;
                        for (int i = 0; i < line.length(); i++) {
                            testString = line.substring(0, line.length() + i - numExtraChars) + DOT;
                            textLength = getLengthFromCache(gc, testString);

                            if (textLength >= availableLength) {

                                // now roll back one as this was the first
                                // number that exceeded
                                if (line.length() + i - numExtraChars < 1) {
                                    line = EMPTY;
                                } else {
                                    line = line.substring(0, line.length() + i - numExtraChars - 1) + DOT;
                                }
                                break;
                            }
                        }
                    } else {
                        StringBuilder output = new StringBuilder();
                        StringBuilder wrap = new StringBuilder();
                        for (char c : line.toCharArray()) {
                            wrap.append(c);
                            int length = getLengthFromCache(gc, wrap.toString());
                            if (length >= availableLength) {
                                output.append(wrap.substring(0, wrap.length() - 1)).append(LINE_SEPARATOR);
                                wrap = new StringBuilder();
                                wrap.append(c);
                            }
                        }
                        line = output.append(wrap).toString();
                    }
                }
                result.append(line);
            }

            return result.toString();
        }
        return text;
    }

    /**
     * This method gets only called if automatic length calculation is enabled.
     * Calculate the new cell width/height by using the given content length and
     * the difference from current cell width/height to available length. If the
     * calculated cell is greater than the current set contentLength, update the
     * contentLength and execute a corresponding resize command.
     *
     * @param cell
     *            the current cell that is painted
     * @param contentLength
     *            the length of the content
     */
    protected abstract void setNewMinLength(ILayerCell cell, int contentLength);

    /**
     * This method is used to determine the padding from the cell to the
     * available length. A padding can occur for example by using a
     * BeveledBorderDecorator or PaddingDecorator. This TextPainter is called
     * with the available space rectangle which is calculated by the wrapping
     * painters and decorators by subtracting paddings. As this TextPainter does
     * not know his wrapping painters and decorators the existing padding needs
     * to be calculated for automatic resizing. Abstract because a horizontal
     * TextPainter uses the width while a VerticalTextPainter uses the height of
     * the cell and the Rectangle.
     *
     * @param cell
     *            the current cell which should be resized
     * @param availableLength
     *            the length value that is available and was given into
     *            paintCell() as Rectangle argument
     * @return the padding between the current cell length - availableLength
     */
    protected abstract int calculatePadding(ILayerCell cell, int availableLength);

    /**
     * Set if the text should be rendered underlined or not.
     *
     * @param underline
     *            <code>true</code> if the text should be printed underlined,
     *            <code>false</code> if not
     */
    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    /**
     * Set if the text should be rendered strikethrough or not.
     *
     * @param strikethrough
     *            <code>true</code> if the text should be printed strikethrough,
     *            <code>false</code> if not
     */
    public void setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    /**
     * @return <code>true</code> if this text painter is calculating the cell
     *         dimensions by containing text length. For horizontal text
     *         rendering, this means the <b>width</b> of the cell is calculated
     *         by content, for vertical text rendering the <b>height</b> is
     *         calculated.
     */
    public boolean isCalculateByTextLength() {
        return this.calculateByTextLength;
    }

    /**
     * Configure whether the text painter should calculate the cell dimensions
     * by containing text length. For horizontal text rendering, this means the
     * <b>width</b> of the cell is calculated by content, for vertical text
     * rendering the <b>height</b> is calculated.
     *
     * @param calculateByTextLength
     *            <code>true</code> to calculate and modify the cell dimension
     *            according to the text length, <code>false</code> to not
     *            modifying the cell dimensions.
     */
    public void setCalculateByTextLength(boolean calculateByTextLength) {
        this.calculateByTextLength = calculateByTextLength;
    }

    /**
     * @return <code>true</code> if this text painter is calculating the cell
     *         dimensions by containing text height. For horizontal text
     *         rendering, this means the <b>height</b> of the cell is calculated
     *         by content, for vertical text rendering the <b>width</b> is
     *         calculated.
     */
    public boolean isCalculateByTextHeight() {
        return this.calculateByTextHeight;
    }

    /**
     * Configure whether the text painter should calculate the cell dimensions
     * by containing text height. For horizontal text rendering, this means the
     * <b>height</b> of the cell is calculated by content, for vertical text
     * rendering the <b>width</b> is calculated.
     *
     * @param calculateByTextHeight
     *            <code>true</code> to calculate and modify the cell dimension
     *            according to the text height, <code>false</code> to not
     *            modifying the cell dimensions.
     */
    public void setCalculateByTextHeight(boolean calculateByTextHeight) {
        this.calculateByTextHeight = calculateByTextHeight;
    }

    /**
     * @return <code>true</code> if the text to display should be trimmed,
     *         <code>false</code> if not. Default is <code>true</code>
     *
     * @since 1.3
     */
    public boolean isTrimText() {
        return this.trimText;
    }

    /**
     * @param trimText
     *            <code>true</code> if the text to display should be trimmed,
     *            <code>false</code> if not.
     *
     * @since 1.3
     */
    public void setTrimText(boolean trimText) {
        this.trimText = trimText;
    }

    /**
     *
     * @return <code>true</code> if the text will be wrapped, <code>false</code>
     *         if not.
     *
     * @since 1.4
     */
    public boolean isWrapText() {
        return this.wrapText;
    }

    /**
     *
     * @param wrapText
     *            <code>true</code> if the text should be wrapped,
     *            <code>false</code> if not.
     *
     * @since 1.4
     */
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
    }

    /**
     * Render a decoration to the text, e.g. underline and/or strikethrough
     * lines.
     *
     * @param cellStyle
     *            The {@link IStyle} that contains the styling information for
     *            rendering.
     * @param gc
     *            the {@link GC} used to paint
     * @param x
     *            start x of the text
     * @param y
     *            start y of the text
     * @param length
     *            length of the text
     * @param fontHeight
     *            The height of the current font
     *
     * @since 1.4
     */
    protected void paintDecoration(IStyle cellStyle, GC gc, int x, int y, int length, int fontHeight) {
        boolean underline = renderUnderlined(cellStyle);
        boolean strikethrough = renderStrikethrough(cellStyle);

        if (underline || strikethrough) {
            if (length > 0) {
                // check and draw underline and strikethrough separately
                // so it is possible to combine both
                if (underline) {
                    // y = start y of text + font height - half of the font
                    // descent so the underline is between baseline and bottom
                    int underlineY = y + fontHeight - (gc.getFontMetrics().getDescent() / 2);
                    gc.drawLine(x, underlineY, x + length, underlineY);
                }

                if (strikethrough) {
                    // y = start y of text + half of font height + ascent
                    // this way lower case characters are also strikethrough
                    int strikeY = y + (fontHeight / 2) + (gc.getFontMetrics().getLeading() / 2);
                    gc.drawLine(x, strikeY, x + length, strikeY);
                }
            }
        }
    }

    /**
     * Return whether word wrapping is enabled or not.
     * <p>
     * Word wrapping is the wrapping behavior similar to spreadsheet
     * applications where words are wrapped if there is not enough space. Text
     * wrapping on the other hand only wraps whole words.
     * </p>
     * <p>
     * Enabling this feature could result in slow rendering performance. It is
     * therefore disabled by default.
     * </p>
     * <p>
     * <b>Note:</b> If word wrapping is enabled, features like automatic size
     * calculation by text length and text wrapping are ignored.
     * </p>
     *
     * @return <code>true</code> if word wrapping is enabled, <code>false</code>
     *         if not.
     *
     * @since 1.5
     */
    public boolean isWordWrapping() {
        return this.wordWrapping;
    }

    /**
     * Configure whether word wrapping should be enabled or not.
     * <p>
     * Word wrapping is the wrapping behavior similar to spreadsheet
     * applications where words are wrapped if there is not enough space. Text
     * wrapping on the other hand only wraps whole words.
     * </p>
     * <p>
     * Enabling this feature could result in slow rendering performance. It is
     * therefore disabled by default.
     * </p>
     * <p>
     * <b>Note:</b> If word wrapping is enabled, features like automatic size
     * calculation by text length and text wrapping are ignored.
     * </p>
     *
     * @param wordWrapping
     *            <code>true</code> to enable word wrapping, <code>false</code>
     *            to disable it.
     *
     * @since 1.5
     */
    public void setWordWrapping(boolean wordWrapping) {
        this.wordWrapping = wordWrapping;
    }

    /**
     * Return the number of pixels that are added between lines. Default is 0,
     * which means that the line height is defined by the font height only.
     *
     * @return The number of pixels that are added between lines
     *
     * @since 1.5
     */
    public int getLineSpacing() {
        return this.lineSpacing;
    }

    /**
     * Specify the number of pixels that should be added between lines. Default
     * is 0, which means that the line height is defined by the font height
     * only.
     *
     * @param spacing
     *            The number of pixels that should be added between lines
     *
     * @since 1.5
     */
    public void setLineSpacing(int spacing) {
        this.lineSpacing = spacing;
    }
}
