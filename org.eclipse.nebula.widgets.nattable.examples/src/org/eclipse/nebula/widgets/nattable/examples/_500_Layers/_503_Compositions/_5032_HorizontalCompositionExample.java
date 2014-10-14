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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._503_Compositions;

import java.util.List;

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
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example showing a NatTable that contains a row header and a body layer.
 * 
 * @author Dirk Fauth
 *
 */
public class _5032_HorizontalCompositionExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400,
                new _5032_HorizontalCompositionExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to assemble a table that consists of a row header and a body layer.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday" };

        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Person>(
                propertyNames);

        final List<Person> data = PersonService.getPersons(10);

        IRowDataProvider<Person> bodyDataProvider = new ListDataProvider<Person>(
                data, columnPropertyAccessor);
        final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        final SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DefaultRowHeaderDataLayer(new DefaultRowHeaderDataProvider(
                        bodyDataProvider)), viewportLayer, selectionLayer);

        // set the region labels to make default configurations work, e.g.
        // selection
        CompositeLayer compositeLayer = new CompositeLayer(2, 1);
        compositeLayer.setChildLayer(GridRegion.ROW_HEADER, rowHeaderLayer, 0,
                0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 1, 0);

        return new NatTable(parent, compositeLayer);
    }

}
