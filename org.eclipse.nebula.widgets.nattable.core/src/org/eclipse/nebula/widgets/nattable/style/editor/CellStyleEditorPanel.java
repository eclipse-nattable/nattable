/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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

    public CellStyleEditorPanel(Composite parent, int style) {
        super(parent, style);
        initComponents();
    }

    private void initComponents() {
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginLeft = 10;
		setLayout(gridLayout);

        new Label(this, SWT.NONE).setText(Messages.getString("CellStyleEditorPanel.backgroundColor")); //$NON-NLS-1$
        backgroundColorPicker = new ColorPicker(this, DEFAULT_BG_COLOR);

        new Label(this, SWT.NONE).setText(Messages.getString("CellStyleEditorPanel.foregroundColor")); //$NON-NLS-1$
        foregroundColorPicker = new ColorPicker(this, DEFAULT_FG_COLOR);

        new Label(this, SWT.NONE).setText(Messages.getString("CellStyleEditorPanel.font")); //$NON-NLS-1$
        fontPicker = new FontPicker(this, GUIHelper.DEFAULT_FONT);
        fontPicker.setLayoutData(new GridData(80, 20));

        new Label(this, SWT.NONE).setText(Messages.getString("CellStyleEditorPanel.horizontalAlignment")); //$NON-NLS-1$
        horizontalAlignmentPicker = new HorizontalAlignmentPicker(this, HorizontalAlignmentEnum.CENTER);

        new Label(this, SWT.NONE).setText(Messages.getString("CellStyleEditorPanel.verticalAlignment")); //$NON-NLS-1$
        verticalAlignmentPicker = new VerticalAlignmentPicker(this, VerticalAlignmentEnum.MIDDLE);
    }

    @Override
    public String getEditorName() {
        return Messages.getString("CellStyleEditorPanel.editorName"); //$NON-NLS-1$
    }

    @Override
    public void edit(Style style) throws Exception {
        Color bgColor = style.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		backgroundColorPicker.setSelectedColor(bgColor != null ? bgColor : GUIHelper.COLOR_WHITE);

        Color fgColor = style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		foregroundColorPicker.setSelectedColor(fgColor != null ? fgColor : GUIHelper.COLOR_BLACK);

		fontPicker.setFont(style.getAttributeValue(CellStyleAttributes.FONT));

        HorizontalAlignmentEnum hAlign = style.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
		horizontalAlignmentPicker.setSelectedAlignment(hAlign != null ? hAlign : HorizontalAlignmentEnum.CENTER);

        VerticalAlignmentEnum vAlign = style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
		verticalAlignmentPicker.setSelectedAlignment(vAlign != null ? vAlign : VerticalAlignmentEnum.MIDDLE);
    }

    @Override
    public Style getNewValue() {
    	Style newStyle = new Style();
    	newStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, backgroundColorPicker.getSelectedColor());
    	newStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, foregroundColorPicker.getSelectedColor());
    	newStyle.setAttributeValue(CellStyleAttributes.FONT, fontPicker.getSelectedFont());
    	newStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, horizontalAlignmentPicker.getSelectedAlignment());
    	newStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, verticalAlignmentPicker.getSelectedAlignment());
    	return newStyle;
    }
}
