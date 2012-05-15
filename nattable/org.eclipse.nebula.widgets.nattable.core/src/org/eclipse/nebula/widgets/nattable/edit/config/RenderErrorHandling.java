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
package org.eclipse.nebula.widgets.nattable.edit.config;

import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.ControlDecorationProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;


public class RenderErrorHandling extends AbstractEditErrorHandler {

	private Color originalColor;
	protected final ControlDecorationProvider decorationProvider;
	
	public RenderErrorHandling() {
		this(null, new ControlDecorationProvider());
	}
	
	public RenderErrorHandling(ControlDecorationProvider decorationProvider) {
        this(null, decorationProvider);
	}
	
    public RenderErrorHandling(IEditErrorHandler underlyingErrorHandler, ControlDecorationProvider decorationProvider) {
		super(underlyingErrorHandler);
        this.decorationProvider = decorationProvider;
	}
	
	@Override
	public void removeError(ICellEditor cellEditor) {
		super.removeError(cellEditor);
		if (cellEditor instanceof TextCellEditor) {
			TextCellEditor textCellEditor = (TextCellEditor) cellEditor;
			Text textControl = textCellEditor.getTextControl();

			if (originalColor != null) {
				textControl.setForeground(originalColor);
				originalColor = null;
			}
			decorationProvider.hideDecoration();
		}
	}
	
	@Override
	public void displayError(ICellEditor cellEditor, Exception e) {
		super.displayError(cellEditor, e);
		if (cellEditor instanceof TextCellEditor) {
			TextCellEditor textCellEditor = (TextCellEditor) cellEditor;
			Text textControl = textCellEditor.getTextControl();
			
			if (originalColor == null) {
				originalColor = textControl.getForeground();
			}
			textControl.setForeground(GUIHelper.COLOR_RED);
			decorationProvider.showDecoration();
		}
	}

}
