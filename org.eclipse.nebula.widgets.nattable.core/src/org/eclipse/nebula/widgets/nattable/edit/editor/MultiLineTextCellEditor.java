/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.edit.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A specialization of {@link TextCellEditor} that uses a multi line text editor as
 * editor control. To support multi line editing correctly, the behaviour to commit
 * on pressing the enter key is disabled.
 * <p>
 * A multi line editor usually needs some space. Therefore it might be a good decision
 * to set the configuration attribute {@link EditConfigAttributes#OPEN_IN_DIALOG} to
 * <code>true</code> for this editor, so the editor always opens in a subdialog.
 * </p>
 * <p>
 * As some table layouts may support enough space for an inline cell editor, this editor
 * does not specify {@link ICellEditor#openInline(org.eclipse.nebula.widgets.nattable.config.IConfigRegistry, 
 * java.util.List)} to always return <code>false</code>.
 * </p>
 * @author Dirk Fauth
 *
 */
public class MultiLineTextCellEditor extends TextCellEditor {

	/**
	 * Create a new multi line text editor that ensures to not commit the editor
	 * value in case enter is typed.
	 */
	public MultiLineTextCellEditor() {
		this.commitOnEnter = false;
	}
	
	@Override
	public Text createEditorControl(Composite parent) {
		final Text textControl = super.createEditorControl(parent, 
				HorizontalAlignmentEnum.getSWTStyle(this.cellStyle) | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		
		if (!openInline(this.configRegistry, this.labelStack.getLabels())) {
			//add the layout data directly so it will not be layouted by the CellEditDialog
			GridDataFactory.fillDefaults().grab(true, true).hint(100, 50).applyTo(textControl);
		}
		
		return textControl;
	}
}
