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

import static org.eclipse.swt.SWT.CHECK;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * EditorPanel for editing a border style.
 */
public class BorderStyleEditorPanel extends AbstractEditorPanel<BorderStyle> {

    private BorderThicknessPicker thicknessPicker;
    private LineStylePicker lineStylePicker;
    private ColorPicker colorPicker;
    private Button noBordersCheckBox;

    @Override
    public String getEditorName() {
        return Messages.getString("BorderStyleEditorPanel.editorName"); //$NON-NLS-1$
    }

    public BorderStyleEditorPanel(Composite parent, int style) {
        super(parent, style);
        initComponents();
    }

    public void initComponents() {
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginLeft = 10;
		setLayout(gridLayout);

        new Label(this, NONE).setText(Messages.getString("BorderStyleEditorPanel.noBorder")); //$NON-NLS-1$

        noBordersCheckBox = new Button(this, CHECK);
        noBordersCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                    boolean noBorder = noBordersCheckBox.getSelection();
                    colorPicker.setEnabled(!noBorder);
                    thicknessPicker.setEnabled(!noBorder);
                    lineStylePicker.setEnabled(!noBorder);
            }
        });

        new Label(this, NONE).setText(Messages.getString("BorderStyleEditorPanel.color")); //$NON-NLS-1$
        colorPicker = new ColorPicker(this, GUIHelper.COLOR_WIDGET_BORDER);

        new Label(this, NONE).setText(Messages.getString("BorderStyleEditorPanel.lineStyle")); //$NON-NLS-1$
        lineStylePicker = new LineStylePicker(this);

        new Label(this, NONE).setText(Messages.getString("BorderStyleEditorPanel.thickness")); //$NON-NLS-1$
        thicknessPicker = new BorderThicknessPicker(this);

        // By default, no border is selected and all controls are disabled
        noBordersCheckBox.setSelection(true);
        colorPicker.setEnabled(false);
        thicknessPicker.setEnabled(false);
        lineStylePicker.setEnabled(false);
    }

    private void disableEditing() {
        colorPicker.setEnabled(false);
        thicknessPicker.setEnabled(false);
        lineStylePicker.setEnabled(false);
    }

    public void edit(BorderStyle bstyle) throws Exception {
        if (bstyle != null) {
            noBordersCheckBox.setSelection(false);
            colorPicker.setSelectedColor(bstyle.getColor());
            lineStylePicker.setSelectedLineStyle(bstyle.getLineStyle());
            thicknessPicker.setSelectedThickness(bstyle.getThickness());
        } else {
            noBordersCheckBox.setSelection(true);
            disableEditing();
        }
    }

    public BorderStyle getNewValue() {
        if (!noBordersCheckBox.getSelection()) {
            Color borderColor = colorPicker.getSelectedColor();
            LineStyleEnum lineStyle = lineStylePicker.getSelectedLineStyle();
            int borderThickness = thicknessPicker.getSelectedThickness();
            return new BorderStyle(borderThickness, borderColor, lineStyle);
        }
        return null;
    }
}
