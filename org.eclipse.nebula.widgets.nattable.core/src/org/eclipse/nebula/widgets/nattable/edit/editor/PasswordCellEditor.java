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
package org.eclipse.nebula.widgets.nattable.edit.editor;

import org.eclipse.nebula.widgets.nattable.painter.cell.PasswordTextPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Specialized {@link TextCellEditor} that sets the echo char of the text control used by
 * this editor to a configured character. You can configure the echo character by setting
 * the attribute {@link CellStyleAttributes#PASSWORD_ECHO_CHAR} to the cell style to use. 
 * If there is no echo character configured, the bullet character will be used.
 * 
 * <p>As the anonymization of the inserted value only relates to the {@link Text} control,
 * ensure to also register the {@link PasswordTextPainter} so the value is not shown in clear text
 * after commit.
 * 
 * @author Dirk Fauth
 * @see PasswordTextPainter
 */
public class PasswordCellEditor extends TextCellEditor {

	public PasswordCellEditor() {
		this(false);
	}
	
	public PasswordCellEditor(boolean commitOnUpOrDownCursorKeyPress) {
		super(commitOnUpOrDownCursorKeyPress);
	}

	@Override
	protected Text createTextControl(Composite parent) {
		final Text textControl = super.createTextControl(parent);
		
		//search for the configured echo character within the ConfigRegistry
		IStyle cellStyle = getCellStyle();
		Character configEchoChar = cellStyle.getAttributeValue(CellStyleAttributes.PASSWORD_ECHO_CHAR);
		//set the echo char of the Text control to the configured one or if there is
		//none configured, set the bullet char
		textControl.setEchoChar(configEchoChar != null ? configEchoChar : '\u2022');
		
		return textControl;
	}

}
