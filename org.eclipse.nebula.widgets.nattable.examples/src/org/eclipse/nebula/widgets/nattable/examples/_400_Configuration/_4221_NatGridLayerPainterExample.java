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
package org.eclipse.nebula.widgets.nattable.examples._400_Configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing the NatGridLayerPainter which renders grid lines at
 * the remainder space.
 * 
 * @author Dirk Fauth
 *
 */
public class _4221_NatGridLayerPainterExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400,
                new _4221_NatGridLayerPainterExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the NatGridLayerPainter which renders grid lines"
                + " to the remainder space.";
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
        final NatTable natTable = new NatTable(parent, SWT.NO_REDRAW_RESIZE
                | SWT.DOUBLE_BUFFERED | SWT.BORDER, bodyDataLayer);
        natTable.setBackground(GUIHelper.COLOR_WHITE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        natTable.setLayerPainter(new NatGridLayerPainter(natTable,
                DataLayer.DEFAULT_ROW_HEIGHT));

        return natTable;
    }

}
