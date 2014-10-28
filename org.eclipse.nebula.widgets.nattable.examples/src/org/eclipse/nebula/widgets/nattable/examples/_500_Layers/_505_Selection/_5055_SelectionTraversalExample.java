/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._505_Selection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _5055_SelectionTraversalExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 600,
                new _5055_SelectionTraversalExample());
    }

    @Override
    public String getDescription() {
        return "This example shows different traversal strategy configurations.\n\n"
                + "1. AXIS traversal - traversal happens on one axis without cycle, default\n"
                + "2. AXIS CYCLE traversal - traversal happens on one axis where moving over a "
                + "border means to move to the beginning of the same row/column\n"
                + "3. TABLE traversal - traversal happens on table basis where moving over a "
                + "border means to move to the beginning of the next/previous row/column\n"
                + "4. TABLE CYCLE traversal - traversal happens on table basis where moving over "
                + "a border means to move to the beginning of the next/previous row/column, but "
                + "moving over the table end/beginning moves to the opposite\n"
                + "5. mixed - this shows how to mix traversal strategies for left/right and up/down movements";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };
        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<Person>(propertyNames);
        IRowDataProvider<Person> bodyDataProvider =
                new ListDataProvider<Person>(PersonService.getPersons(3), columnPropertyAccessor);

        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        // 1. AXIS traversal - NatTable default
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        NatTable natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 2. AXIS CYCLE traversal
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // AXIS_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 3. TABLE traversal
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 4. TABLE CYCLE traversal
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // 5. mixed traversal
        // on left/right we will use TABLE CYCLE
        // on up/down we will use AXIS CYCLE
        bodyDataLayer = new DataLayer(bodyDataProvider);
        selectionLayer = new SelectionLayer(bodyDataLayer);
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        // register a MoveCellSelectionCommandHandler with
        // TABLE_CYCLE_TRAVERSAL_STRATEGY for horizontal traversal
        // and AXIS_CYCLE_TRAVERSAL_STRATEGY for vertical traversal
        // NOTE:
        // You could achieve the same by registering a command handler
        // with TABLE_CYCLE_TRAVERSAL_STRATEGY and registering
        // MoveSelectionActions with a customized ITraversalStrategy, e.g.
        // AXIS_CYCLE_TRAVERSAL_STRATEGY
        viewportLayer.registerCommandHandler(
                new MoveCellSelectionCommandHandler(selectionLayer,
                        ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY));

        natTable = new NatTable(panel, viewportLayer);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        return panel;
    }
}
