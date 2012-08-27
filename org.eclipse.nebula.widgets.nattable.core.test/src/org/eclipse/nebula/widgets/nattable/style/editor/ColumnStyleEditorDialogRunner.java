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


import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.editor.ColumnStyleEditorDialog;
import org.eclipse.swt.widgets.Shell;

public class ColumnStyleEditorDialogRunner {

	public static void main(String[] args) throws Exception {
		Shell shell = new Shell();
		ColumnStyleEditorDialog dialog = new ColumnStyleEditorDialog(shell, new Style());
		dialog.open();
		System.out.println("Style: " + dialog.getNewColumnCellStyle());
	}

}
