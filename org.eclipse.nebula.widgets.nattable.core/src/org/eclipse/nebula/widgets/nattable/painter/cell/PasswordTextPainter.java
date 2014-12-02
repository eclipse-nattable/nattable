/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.PasswordCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialized {@link TextPainter} that will paint the text that should be
 * showed within a cell anonymized. Like in {@link PasswordCellEditor} every
 * character will be replaced with another echo character. The echo character
 * can be configured by setting the attribute
 * {@link CellStyleAttributes#PASSWORD_ECHO_CHAR} to the cell style to use. If
 * no echo character is configured, the bullet character will be used as echo
 * character.
 *
 * @author Dirk Fauth
 *
 * @see PasswordCellEditor
 */
public class PasswordTextPainter extends TextPainter {

    /**
     * The echo character to use for anonymization. Stored as member variable
     * because getTextToDisplay() has no context information. Will be set on
     * every paintCell() so changes to the cell style will be taken into account
     * on runtime. Default value is the bullet character.
     */
    private Character echoChar = '\u2022';

    public PasswordTextPainter() {
        super();
    }

    public PasswordTextPainter(boolean wrapText, boolean paintBg) {
        super(wrapText, paintBg);
    }

    public PasswordTextPainter(boolean wrapText, boolean paintBg, int spacing) {
        super(wrapText, paintBg, spacing);
    }

    public PasswordTextPainter(boolean wrapText, boolean paintBg,
            boolean calculate) {
        super(wrapText, paintBg, calculate);
    }

    public PasswordTextPainter(boolean wrapText, boolean paintBg, int spacing,
            boolean calculate) {
        super(wrapText, paintBg, spacing, calculate);
    }

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle,
            IConfigRegistry configRegistry) {
        // check for the configuration of a echo character in the corresponding
        // cell style
        IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
        Character configEchoChar = cellStyle
                .getAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR);
        if (configEchoChar != null) {
            this.echoChar = configEchoChar;
        }
        super.paintCell(cell, gc, rectangle, configRegistry);
    }

    @Override
    protected String getTextToDisplay(ILayerCell cell, GC gc,
            int availableLength, String text) {
        String result = super.getTextToDisplay(cell, gc, availableLength, text);
        // replace all characters with the configured echo character
        return result.replaceAll(".", this.echoChar.toString()); //$NON-NLS-1$
    }
}
