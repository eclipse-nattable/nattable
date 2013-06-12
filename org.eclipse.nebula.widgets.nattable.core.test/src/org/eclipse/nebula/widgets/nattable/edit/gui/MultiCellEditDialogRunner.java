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
package org.eclipse.nebula.widgets.nattable.edit.gui;


import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.CellFixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;

public class MultiCellEditDialogRunner {

	private boolean interactive = false;

	public void shouldOpenDefaultDialogWithoutIncrementDecrementBox() {
		CellFixture cell = new CellFixture();
		cell.setBounds(new Rectangle(100,100,100,20));
		cell.setConfigLabels(new LabelStack("Cell_Edit"));
		cell.setDataValue("123");
		cell.setDisplayMode(DisplayMode.NORMAL);

		TextCellEditor cellEditor = new TextCellEditor();

		IDisplayConverter dataTypeConverter = new DisplayConverter() {

			public Object canonicalToDisplayValue(Object canonicalValue) {
				return canonicalValue;
			}

			public Object displayToCanonicalValue(Object displayValue) {
				return displayValue;
			}

		};

		final Character newValue = Character.valueOf('4');
		IDataValidator dataValidator = new DataValidator() {

			public boolean validate(int columnIndex, int rowIndex, Object newValue) {
				Assert.assertEquals(newValue, newValue);
				return false;
			}

		};

		Shell shell = new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.RESIZE);
		
		//FIXME correct test cases
//		final MultiCellEditDialog dialog = new MultiCellEditDialog(shell, cellEditor, dataTypeConverter, new Style(), dataValidator, cell.getDataValue(), newValue, true);
//
//		if (!interactive) {
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					} finally {
//						dialog.close();
//					}
//				}
//			});
//		}
//		dialog.open();
	}

	public static void main(String[] args) {
		MultiCellEditDialogRunner test = new MultiCellEditDialogRunner();
		test.interactive = true;
		test.shouldOpenDefaultDialogWithoutIncrementDecrementBox();
	}
}
