/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.gui;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
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
        cell.setBounds(new Rectangle(100, 100, 100, 20));
        cell.setConfigLabels(new LabelStack("Cell_Edit"));
        cell.setDataValue("123");
        cell.setDisplayMode(DisplayMode.NORMAL);

        TextCellEditor cellEditor = new TextCellEditor();

        IDisplayConverter dataTypeConverter = new DisplayConverter() {

            @Override
            public Object canonicalToDisplayValue(Object canonicalValue) {
                return canonicalValue;
            }

            @Override
            public Object displayToCanonicalValue(Object displayValue) {
                return displayValue;
            }

        };

        final Character newValue = Character.valueOf('4');
        IDataValidator dataValidator = new DataValidator() {

            @Override
            public boolean validate(int columnIndex, int rowIndex,
                    Object newValue) {
                Assert.assertEquals(newValue, newValue);
                return false;
            }

        };

        Shell shell = new Shell(Display.getDefault(), SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.RESIZE);

        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                dataTypeConverter);
        configRegistry.registerConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                dataValidator);

        final CellEditDialog dialog = new CellEditDialog(shell,
                newValue, cell, cellEditor, configRegistry);

        if (!this.interactive) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        dialog.close();
                    }
                }
            });
        }
        dialog.open();
    }

    public static void main(String[] args) {
        MultiCellEditDialogRunner test = new MultiCellEditDialogRunner();
        test.interactive = true;
        test.shouldOpenDefaultDialogWithoutIncrementDecrementBox();
    }
}
