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
package org.eclipse.nebula.widgets.nattable.examples.examples;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.SimplePerson;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _001_Getting_Started extends AbstractNatExample {

    private IDataProvider bodyDataProvider;
    private String[] propertyNames;
    private BodyLayerStack bodyLayer;
    private Map<String, String> propertyToLabels;

    public static void main(String[] args) {
        StandaloneNatExampleRunner.run(600, 400, new _001_Getting_Started());
    }

    @Override
    public Control createExampleControl(Composite parent) {
        this.bodyDataProvider = setupBodyDataProvider();
        DefaultColumnHeaderDataProvider colHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                this.propertyNames, this.propertyToLabels);
        DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                this.bodyDataProvider);

        this.bodyLayer = new BodyLayerStack(this.bodyDataProvider);
        ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(
                colHeaderDataProvider);
        RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack(
                rowHeaderDataProvider);
        DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                colHeaderDataProvider, rowHeaderDataProvider);
        CornerLayer cornerLayer = new CornerLayer(new DataLayer(
                cornerDataProvider), rowHeaderLayer, columnHeaderLayer);

        GridLayer gridLayer = new GridLayer(this.bodyLayer, columnHeaderLayer,
                rowHeaderLayer, cornerLayer);
        NatTable natTable = new NatTable(parent, gridLayer);

        return natTable;
    }

    private IDataProvider setupBodyDataProvider() {
        final List<SimplePerson> people = Arrays.asList(new SimplePerson(100,
                "Mickey Mouse", new Date(1000000)),
                new SimplePerson(110, "Batman",
                        new Date(2000000)),
                new SimplePerson(120, "Bender", new Date(3000000)), new SimplePerson(130,
                        "Cartman", new Date(4000000)),
                new SimplePerson(140,
                        "Dogbert", new Date(5000000)));

        this.propertyToLabels = new HashMap<>();
        this.propertyToLabels.put("id", "ID");
        this.propertyToLabels.put("name", "First Name");
        this.propertyToLabels.put("birthDate", "DOB");

        this.propertyNames = new String[] { "id", "name", "birthDate" };
        return new ListDataProvider<>(people,
                new ReflectiveColumnPropertyAccessor<SimplePerson>(this.propertyNames));

    }

    public class BodyLayerStack extends AbstractLayerTransform {

        private SelectionLayer selectionLayer;

        public BodyLayerStack(IDataProvider dataProvider) {
            DataLayer bodyDataLayer = new DataLayer(dataProvider);
            ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(
                    bodyDataLayer);
            ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(
                    columnReorderLayer);
            this.selectionLayer = new SelectionLayer(columnHideShowLayer);
            ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);
            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }
    }

    public class ColumnHeaderLayerStack extends AbstractLayerTransform {

        public ColumnHeaderLayerStack(IDataProvider dataProvider) {
            DataLayer dataLayer = new DataLayer(dataProvider);
            ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer,
                    _001_Getting_Started.this.bodyLayer, _001_Getting_Started.this.bodyLayer.getSelectionLayer());
            setUnderlyingLayer(colHeaderLayer);
        }
    }

    public class RowHeaderLayerStack extends AbstractLayerTransform {

        public RowHeaderLayerStack(IDataProvider dataProvider) {
            DataLayer dataLayer = new DataLayer(dataProvider, 50, 20);
            RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(dataLayer,
                    _001_Getting_Started.this.bodyLayer, _001_Getting_Started.this.bodyLayer.getSelectionLayer());
            setUnderlyingLayer(rowHeaderLayer);
        }
    }
}
