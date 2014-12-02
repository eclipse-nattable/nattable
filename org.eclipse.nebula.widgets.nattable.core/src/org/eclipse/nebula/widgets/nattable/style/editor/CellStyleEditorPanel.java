/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style.editor;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * EditorPanel for editing the core style attributes.
 */
public class CellStyleEditorPanel extends AbstractEditorPanel<Style> {

    private static final Color DEFAULT_FG_COLOR = GUIHelper.COLOR_BLACK;
    private static final Color DEFAULT_BG_COLOR = GUIHelper.COLOR_WHITE;
    private ColorPicker backgroundColorPicker;
    private ColorPicker foregroundColorPicker;
    private FontPicker fontPicker;
    private HorizontalAlignmentPicker horizontalAlignmentPicker;
    private VerticalAlignmentPicker verticalAlignmentPicker;
    private Color origBgColor;
    private Color origFgColor;
    private HorizontalAlignmentEnum origHAlign;
    private VerticalAlignmentEnum origVAlign;

    public CellStyleEditorPanel(Composite parent, int style) {
        super(parent, style);
        initComponents();
    }

    private void initComponents() {
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginLeft = 10;
        setLayout(gridLayout);

        new Label(this, SWT.NONE).setText(Messages
                .getString("CellStyleEditorPanel.backgroundColor")); //$NON-NLS-1$
        this.backgroundColorPicker = new ColorPicker(this, DEFAULT_BG_COLOR);

        new Label(this, SWT.NONE).setText(Messages
                .getString("CellStyleEditorPanel.foregroundColor")); //$NON-NLS-1$
        this.foregroundColorPicker = new ColorPicker(this, DEFAULT_FG_COLOR);

        new Label(this, SWT.NONE).setText(Messages
                .getString("CellStyleEditorPanel.font")); //$NON-NLS-1$
        this.fontPicker = new FontPicker(this, GUIHelper.DEFAULT_FONT);
        this.fontPicker.setLayoutData(new GridData(80, 20));

        new Label(this, SWT.NONE).setText(Messages
                .getString("CellStyleEditorPanel.horizontalAlignment")); //$NON-NLS-1$
        this.horizontalAlignmentPicker = new HorizontalAlignmentPicker(this,
                HorizontalAlignmentEnum.CENTER);

        new Label(this, SWT.NONE).setText(Messages
                .getString("CellStyleEditorPanel.verticalAlignment")); //$NON-NLS-1$
        this.verticalAlignmentPicker = new VerticalAlignmentPicker(this,
                VerticalAlignmentEnum.MIDDLE);
    }

    @Override
    public String getEditorName() {
        return Messages.getString("CellStyleEditorPanel.editorName"); //$NON-NLS-1$
    }

    @Override
    public void edit(Style style) throws Exception {
        this.origBgColor = style
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
        if (this.origBgColor == null) {
            this.origBgColor = GUIHelper.COLOR_WHITE;
        }
        this.backgroundColorPicker.setSelectedColor(this.origBgColor);

        this.origFgColor = style
                .getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
        if (this.origFgColor == null) {
            this.origFgColor = GUIHelper.COLOR_BLACK;
        }
        this.foregroundColorPicker.setSelectedColor(this.origFgColor);

        this.fontPicker.setOriginalFont(style
                .getAttributeValue(CellStyleAttributes.FONT));

        this.origHAlign = style
                .getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
        if (this.origHAlign == null) {
            this.origHAlign = HorizontalAlignmentEnum.CENTER;
        }
        this.horizontalAlignmentPicker.setSelectedAlignment(this.origHAlign);

        this.origVAlign = style
                .getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
        if (this.origVAlign == null) {
            this.origVAlign = VerticalAlignmentEnum.MIDDLE;
        }
        this.verticalAlignmentPicker.setSelectedAlignment(this.origVAlign);
    }

    @Override
    public Style getNewValue() {
        Style newStyle = new Style();

        Color bgColor = this.backgroundColorPicker.getSelectedColor();
        newStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
                bgColor);

        Color fgColor = this.foregroundColorPicker.getSelectedColor();
        newStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
                fgColor);

        Font font = this.fontPicker.getSelectedFont();
        newStyle.setAttributeValue(CellStyleAttributes.FONT, font);

        HorizontalAlignmentEnum hAlign = this.horizontalAlignmentPicker
                .getSelectedAlignment();
        newStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                hAlign);

        VerticalAlignmentEnum vAlign = this.verticalAlignmentPicker
                .getSelectedAlignment();
        newStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT,
                vAlign);

        return newStyle;
    }
}
