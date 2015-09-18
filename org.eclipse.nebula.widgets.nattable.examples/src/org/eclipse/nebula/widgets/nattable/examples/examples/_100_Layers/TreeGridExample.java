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
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.DetailGlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.SortableTreeComparator;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public class TreeGridExample extends AbstractNatExample {

    protected static final String NO_SORT_LABEL = "noSortLabel";

    public static void main(String[] args) {
        StandaloneNatExampleRunner.run(800, 400, new TreeGridExample());
    }

    @Override
    public Control createExampleControl(Composite parent) {
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR, DefaultComparator.getInstance());

        // Underlying data source
        createDatums();
        EventList<Datum> eventList = GlazedLists.eventList(this.datums.values());
        SortedList<Datum> sortedList = new SortedList<Datum>(eventList, null);

        String[] propertyNames = new String[] { "foo", "bar" };
        IColumnPropertyAccessor<Datum> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Datum>(
                propertyNames);

        // Column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                propertyNames);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(
                columnHeaderDataProvider);

        ISortModel sortModel = new GlazedListsSortModel<Datum>(sortedList,
                columnPropertyAccessor, configRegistry, columnHeaderDataLayer);

        final TreeList<Datum> treeList = new TreeList<Datum>(sortedList,
                new DatumTreeFormat(sortModel), new DatumExpansionModel());
        GlazedListTreeData<Datum> treeData = new GlazedListTreeData<Datum>(treeList);

        ListDataProvider<Datum> bodyDataProvider = new ListDataProvider<Datum>(
                treeList, columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        // GlazedListsEventLayer<Datum> glazedListsEventLayer =
        // new GlazedListsEventLayer<Datum>(bodyDataLayer, treeList);
        DetailGlazedListsEventLayer<Datum> glazedListsEventLayer = new DetailGlazedListsEventLayer<Datum>(
                bodyDataLayer, treeList);

        // Body layer
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(
                glazedListsEventLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(
                columnReorderLayer);

        RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(
                columnHideShowLayer);

        // Switch the ITreeRowModel implementation between using native grid
        // Hide/Show or GlazedList TreeList Hide/Show
        // TreeLayer treeLayer = new TreeLayer(rowHideShowLayer, new
        // TreeRowModel<Datum>(treeData), true);
        TreeLayer treeLayer = new TreeLayer(rowHideShowLayer,
                new GlazedListTreeRowModel<Datum>(treeData), false);

        SelectionLayer selectionLayer = new SelectionLayer(treeLayer);

        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(
                columnHeaderDataLayer, viewportLayer, selectionLayer);
        // Note: The column header layer is wrapped in a filter row composite.
        // This plugs in the filter row functionality

        ColumnOverrideLabelAccumulator labelAccumulator = new ColumnOverrideLabelAccumulator(
                columnHeaderDataLayer);
        columnHeaderDataLayer.setConfigLabelAccumulator(labelAccumulator);

        // Register labels
        SortHeaderLayer<Datum> sortHeaderLayer = new SortHeaderLayer<Datum>(
                columnHeaderLayer, sortModel, false);

        // Row header layer
        DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                bodyDataProvider);
        DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(
                rowHeaderDataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                viewportLayer, selectionLayer);

        // Corner layer
        DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        CornerLayer cornerLayer = new CornerLayer(cornerDataLayer,
                rowHeaderLayer, sortHeaderLayer);

        // Grid
        GridLayer gridLayer = new GridLayer(viewportLayer, sortHeaderLayer,
                rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(parent, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
                return super.createRowHeaderMenu(natTable)
                        .withHideRowMenuItem().withShowAllRowsMenuItem();
            }
        });
        natTable.addConfiguration(new DefaultTreeLayerConfiguration(treeLayer));
        natTable.addConfiguration(new SingleClickSortConfiguration());

        // Uncomment to see the native tree list printed to stout.
        // printTree(treeList, treeData);

        natTable.configure();
        return natTable;
    }

    private static class DatumTreeFormat implements TreeList.Format<Datum> {

        private final ISortModel sortModel;

        public DatumTreeFormat(ISortModel sortModel) {
            this.sortModel = sortModel;
        }

        @Override
        public void getPath(List<Datum> path, Datum element) {
            path.add(element);
            Datum parent = element.getParent();
            while (parent != null) {
                path.add(parent);
                parent = parent.getParent();
            }
            Collections.reverse(path);
        }

        @Override
        public boolean allowsChildren(Datum element) {
            return true;
        }

        @Override
        public Comparator<Datum> getComparator(int depth) {
            final Comparator<Datum> dComp = GlazedLists.beanPropertyComparator(Datum.class, "foo");
            Comparator<Datum> test = new Comparator<TreeGridExample.Datum>() {

                @Override
                public int compare(Datum o1, Datum o2) {
                    int result = 0;
                    if (o1.getParent() != null && o2.getParent() != null) {
                        // test the depth
                        Integer o1Depth = depth(o1);
                        Integer o2Depth = depth(o2);

                        // result = o1Depth.compareTo(o2Depth);
                        // if (result == 0) {
                        // result = dComp.compare(o1.getParent(),
                        // o2.getParent());
                        // }
                        // else {
                        // FIXME correct order for different levels
                        if (o1Depth > o2Depth) {
                            result = dComp.compare(o1.getParent(), o2);
                            if (result == 0) {
                                // we have a parent-child relationship
                                return 1;
                            }
                        }
                        else {
                            result = dComp.compare(o1, o2.getParent());
                            if (result == 0) {
                                // we have a parent-child relationship
                                return -1;
                            }
                        }
                        // }
                    }
                    else if (!isRoot(o1) && isRoot(o2)) {
                        result = dComp.compare(getRoot(o1), o2);
                        if (result == 0) {
                            // the given root and the root of the child are
                            // equal
                            // therefore the child is "lower" than the root
                            result = -1;
                        }
                    }
                    else if (isRoot(o1) && !isRoot(o2)) {
                        result = dComp.compare(o1, getRoot(o2));
                        if (result == 0) {
                            // the given root and the root of the child are
                            // equal
                            // therefore the child is "bigger" than the root
                            result = 1;
                        }
                    }
                    else if (isRoot(o1) && isRoot(o2)) {
                        return dComp.compare(o1, o2);
                    }
                    return result;
                }

                int depth(Datum datum) {
                    int d = 0;
                    while (datum.getParent() != null) {
                        d++;
                        datum = datum.getParent();
                    }
                    return d;
                }

                boolean isRoot(Datum datum) {
                    return datum.getParent() == null;
                }

                Datum getRoot(Datum datum) {
                    Datum result = datum;

                    while (!isRoot(result)) {
                        result = result.getParent();
                    }

                    return result;
                }
            };

            // return test;
            return new SortableTreeComparator<Datum>(
                    test,
                    // GlazedLists.beanPropertyComparator(Datum.class, "foo"),
                    this.sortModel);
        }
    }

    private static class DatumExpansionModel implements
            TreeList.ExpansionModel<Datum> {
        @Override
        public boolean isExpanded(Datum element, List<Datum> path) {
            return true;
        }

        @Override
        public void setExpanded(Datum element, List<Datum> path,
                boolean expanded) {}
    }

    protected void printTree(TreeList<Datum> treeList, ITreeData<Datum> treeData) {
        System.out.println(treeList.size());
        for (int i = 0; i < treeList.size(); i++) {
            final Datum location = treeList.get(i);
            final int depth = treeList.depth(i);
            final boolean hasChildren = treeList.hasChildren(i);
            final boolean isExpanded = treeList.isExpanded(i);

            for (int j = 0; j < depth; j++)
                System.out.print("\t");

            if (hasChildren)
                System.out.print(isExpanded ? "- " : "+ ");
            else
                System.out.print("  ");

            System.out.println(location.getFoo());
        }
    }

    public class Datum {
        /**
		 *
		 */
        private final Datum parent;
        private String foo;
        private int bar;

        public Datum(Datum parent, String foo, int bar) {
            this.parent = parent;
            this.foo = foo;
            this.bar = bar;
        }

        public Datum getParent() {
            return this.parent;
        }

        public String getFoo() {
            return this.foo;
        }

        public int getBar() {
            return this.bar;
        }

        @Override
        public String toString() {
            return "[" + "parent=" + this.parent + ", foo=" + this.foo + ", bar=" + this.bar
                    + "]";
        }

    }

    private Map<String, Datum> datums = new HashMap<String, Datum>();

    private void createDatum(String parent, String foo, int bar) {
        Datum datum = new Datum(this.datums.get(parent), foo, bar);
        this.datums.put(foo, datum);
    }

    private void createDatums() {
        createDatum(null, "root", 2);
        createDatum("root", "A", 10);
        createDatum("A", "A.1", 100);
        createDatum("A", "A.2", 110);
        createDatum("A", "A.3", 120);
        createDatum("root", "B", 20);
        createDatum("B", "B.1", 200);
        createDatum("B", "B.2", 210);
        createDatum("root", "C", 30);
        createDatum("C", "C.1", 330);
        createDatum("C", "C.2", 370);
        createDatum("C", "C.3", 322);
        createDatum("C", "C.4", 310);
        createDatum("C", "C.5", 315);
        createDatum(null, "root2", 2);
        createDatum("root2", "X", 70);
        createDatum("X", "X.1", 700);
        createDatum("X", "X.2", 710);
        createDatum("X", "X.3", 720);
        createDatum("root2", "Y", 80);
        createDatum("Y", "Y.1", 800);
        createDatum("Y", "Y.2", 810);
        createDatum("root2", "Z", 90);
        createDatum("Z", "Z.1", 900);
        createDatum("Z", "Z.2", 910);
        createDatum("Z", "Z.3", 920);
        createDatum("Z", "Z.4", 930);
        createDatum("Z", "Z.5", 940);
    }

}
