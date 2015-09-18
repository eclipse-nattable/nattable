/*******************************************************************************
 * Copyright (c) 2013, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._501_Data;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing the DataLayer.
 */
public class _5011_DataLayerExample extends AbstractNatExample {

    private boolean showDefaultColumnWidth = true;
    private boolean showDefaultRowHeight = true;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5011_DataLayerExample());
    }

    @Override
    public String getDescription() {
        return "This example shows a simple NatTable that is only able to show data and "
                + "supports different column/row sizes.\n"
                + "By pressing the buttons 'Toggle column width' and 'Toggle row height' you can see "
                + "how to change column/row sizes programmatically.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        parent.setLayout(new GridLayout());

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday" };

        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Person>(
                propertyNames);

        IDataProvider bodyDataProvider = new ListDataProvider<Person>(
                PersonService.getPersons(10), columnPropertyAccessor);
        final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        // use different style bits to avoid rendering of inactive scrollbars
        // for small table
        // Note: The enabling/disabling and showing of the scrollbars is handled
        // by the ViewportLayer.
        // Without the ViewportLayer the scrollbars will always be visible with
        // the default
        // style bits of NatTable.
        final NatTable natTable = new NatTable(parent, SWT.NO_BACKGROUND
                | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, bodyDataLayer);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button b1 = new Button(parent, SWT.PUSH);
        b1.setText("Toggle column width");
        b1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _5011_DataLayerExample.this.showDefaultColumnWidth = !_5011_DataLayerExample.this.showDefaultColumnWidth;
                if (_5011_DataLayerExample.this.showDefaultColumnWidth) {
                    // reset to default
                    bodyDataLayer.setColumnWidthByPosition(0,
                            DataLayer.DEFAULT_COLUMN_WIDTH, false);
                    bodyDataLayer.setColumnWidthByPosition(1,
                            DataLayer.DEFAULT_COLUMN_WIDTH, false);
                    bodyDataLayer.setColumnWidthByPosition(2,
                            DataLayer.DEFAULT_COLUMN_WIDTH, false);
                    bodyDataLayer.setColumnWidthByPosition(3,
                            DataLayer.DEFAULT_COLUMN_WIDTH, false);
                    // this one will trigger the refresh
                    bodyDataLayer.setColumnWidthByPosition(4,
                            DataLayer.DEFAULT_COLUMN_WIDTH, true);
                } else {
                    bodyDataLayer.setColumnWidthByPosition(0, 70, false);
                    bodyDataLayer.setColumnWidthByPosition(1, 70, false);
                    bodyDataLayer.setColumnWidthByPosition(2, 50, false);
                    bodyDataLayer.setColumnWidthByPosition(3, 30, false);
                    // this one will trigger the refresh
                    bodyDataLayer.setColumnWidthByPosition(4, 200, true);
                }
            }
        });

        Button b2 = new Button(parent, SWT.PUSH);
        b2.setText("Toggle row height");
        b2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _5011_DataLayerExample.this.showDefaultRowHeight = !_5011_DataLayerExample.this.showDefaultRowHeight;
                if (_5011_DataLayerExample.this.showDefaultRowHeight) {
                    // reset to default
                    bodyDataLayer
                            .setDefaultRowHeight(DataLayer.DEFAULT_ROW_HEIGHT);
                } else {
                    bodyDataLayer.setDefaultRowHeight(50);
                }

                // repaint the table, as setting the default height is not
                // triggering a refresh automatically
                // this is because setting the default usually should be done
                // prior rendering
                natTable.doCommand(new VisualRefreshCommand());
            }
        });

        return natTable;
    }

}
