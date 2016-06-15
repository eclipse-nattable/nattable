/*****************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4.part;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.e4.AbstractE4NatExamplePart;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class SelectionListenerExample extends AbstractE4NatExamplePart {

    @Inject
    ESelectionService service;

    private Text outputArea;

    @PostConstruct
    public void postConstruct(Composite parent) {
        // property names of the Person class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        final List<Person> data = PersonService.getPersons(10);

        // create the body layer stack
        final IRowDataProvider<Person> bodyDataProvider =
                new ListDataProvider<>(data, columnPropertyAccessor);
        final DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);
        final SelectionLayer selectionLayer =
                new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // create a E4SelectionListener and configure it for providing selection
        // on cell selection
        E4SelectionListener<Person> esl = new E4SelectionListener<>(service, selectionLayer, bodyDataProvider);
        esl.setFullySelectedRowsOnly(false);
        esl.setHandleSameRowSelection(false);

        selectionLayer.addLayerListener(esl);

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        new DataLayer(columnHeaderDataProvider),
                        viewportLayer,
                        selectionLayer);

        // create the row header layer stack
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DefaultRowHeaderDataLayer(
                        new DefaultRowHeaderDataProvider(bodyDataProvider)),
                viewportLayer,
                selectionLayer);

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(
                new DataLayer(
                        new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                rowHeaderLayer,
                columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        columnHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        final NatTable natTable = new NatTable(parent, gridLayer);
        natTable.setData("org.eclipse.e4.ui.css.CssClassName", "modern");

        parent.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        outputArea = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        outputArea.setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false).hint(0, 100).align(SWT.FILL, SWT.BEGINNING).applyTo(outputArea);

        showSourceLinks(parent, getClass().getName());
    }

    @Inject
    @Optional
    void handleSelection(@Named(IServiceConstants.ACTIVE_SELECTION) List<Person> selected) {
        if (selected != null) {
            String msg = "";
            for (Person p : selected) {
                msg += p.getFirstName() + " " + p.getLastName() + " selected\n";
            }
            outputArea.append(msg);
        }
    }
}