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
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class GridColorsEditorPanel extends AbstractEditorPanel<GridStyleParameterObject> {
	private FontPicker fontPicker;
	private ColorPicker evenRowColorPicker;
	private ColorPicker oddRowColorPicker;
	private ColorPicker selectionColorPicker;
	private IConfigRegistry configRegistry;

	public GridColorsEditorPanel(Composite parent, GridStyleParameterObject currentStyle) {
		super(parent, SWT.NONE);
	}

	@Override
	public String getEditorName() {
		return Messages.getString("GridColorsEditorPanel.editorName"); //$NON-NLS-1$
	}

	@Override
	public GridStyleParameterObject getNewValue() {
		GridStyleParameterObject newStyle = new GridStyleParameterObject(configRegistry);
		newStyle.tableFont = fontPicker.getSelectedFont();
		newStyle.evenRowColor = evenRowColorPicker.getSelectedColor();
		newStyle.oddRowColor = oddRowColorPicker.getSelectedColor();
		newStyle.selectionColor = selectionColorPicker.getSelectedColor();
		return newStyle;
	}

	@Override
	public void edit(GridStyleParameterObject currentStyle) throws Exception {
		configRegistry = currentStyle.getConfigRegistry();
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 10;
		setLayout(layout);

		new Label(this, SWT.NONE).setText(Messages.getString("GridColorsEditorPanel.font")); //$NON-NLS-1$
		fontPicker = new FontPicker(this, currentStyle.tableFont);
		fontPicker.setLayoutData(new GridData(100, 22));

		new Label(this, SWT.NONE).setText(Messages.getString("GridColorsEditorPanel.evenRowColor")); //$NON-NLS-1$
        evenRowColorPicker = new ColorPicker(this, currentStyle.evenRowColor);

        new Label(this, SWT.NONE).setText(Messages.getString("GridColorsEditorPanel.oddRowColor")); //$NON-NLS-1$
        oddRowColorPicker = new ColorPicker(this, currentStyle.oddRowColor);

        new Label(this, SWT.NONE).setText(Messages.getString("GridColorsEditorPanel.selectionColor")); //$NON-NLS-1$
        selectionColorPicker = new ColorPicker(this, currentStyle.selectionColor);
	}

}
