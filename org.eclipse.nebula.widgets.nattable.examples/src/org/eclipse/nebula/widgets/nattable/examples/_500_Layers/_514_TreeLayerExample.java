/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Roman Flueckiger <roman.flueckiger@mac.com> - added expand/collapse key bindings
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeLayerExpandCollapseKeyBindings;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing how to create a tree within a grid.
 */
public class _514_TreeLayerExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _514_TreeLayerExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to create a tree within a grid."
                + " It will use a child as the parent node for tree structuring.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the Person class
        String[] propertyNames = { "lastName", "firstName", "gender", "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        List<PersonWithAddress> personsWithAddress = PersonService.getPersonsWithAddress(50);
        final BodyLayerStack<PersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        personsWithAddress,
                        columnPropertyAccessor,
                        new PersonWithAddressTreeData(personsWithAddress));

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        final NatTable natTable = new NatTable(container, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                // register a CheckBoxPainter as CellPainter for the married
                // information
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new CheckBoxPainter(),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        new DefaultDateDisplayConverter("MM/dd/yyyy"),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);

            }
        });

        // adds the key bindings that allows pressing space bar to
        // expand/collapse tree nodes
        natTable.addConfiguration(
                new TreeLayerExpandCollapseKeyBindings(
                        bodyLayerStack.getTreeLayer(),
                        bodyLayerStack.getSelectionLayer()));

        natTable.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Composite buttonPanel = new Composite(container, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        Button collapseAllButton = new Button(buttonPanel, SWT.PUSH);
        collapseAllButton.setText("Collapse All");
        collapseAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(new TreeCollapseAllCommand());
            }
        });

        Button expandAllButton = new Button(buttonPanel, SWT.PUSH);
        expandAllButton.setText("Expand All");
        expandAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(new TreeExpandAllCommand());
            }
        });

        return container;
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <T>
     */
    class BodyLayerStack<T> extends AbstractLayerTransform {

        private final IDataProvider bodyDataProvider;

        private final SelectionLayer selectionLayer;

        private final TreeLayer treeLayer;

        public BodyLayerStack(List<T> values,
                IColumnPropertyAccessor<T> columnPropertyAccessor,
                ITreeData<T> treeData) {

            this.bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
            DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

            // simply apply labels for every column by index
            bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

            ITreeRowModel<T> treeRowModel = new TreeRowModel<>(treeData);

            this.selectionLayer = new SelectionLayer(bodyDataLayer);

            this.treeLayer = new TreeLayer(this.selectionLayer, treeRowModel);
            ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public TreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }
    }

    /**
     * Simple ITreeData implementation that uses the lastname of the
     * PersonWithAddress object as tree item.
     * <p>
     * Using a String directly as the tree item has the possible disadvantage of
     * haven non-unique items in the tree within subtrees.
     */
    private static class PersonWithAddressTreeData implements ITreeData<PersonWithAddress> {

        private List<PersonWithAddress> values;

        private Map<String, List<PersonWithAddress>> parentMapping;

        private Map<String, PersonWithAddress> firstElementMapping = new HashMap<>();

        public PersonWithAddressTreeData(List<PersonWithAddress> values) {
            this.values = values;

            // first we need to sort by lastname to ensure all elements with the
            // same lastname are grouped together
            this.values.sort(Comparator.comparing(PersonWithAddress::getLastName));

            // then we build up the mapping from lastname to all child elements
            this.parentMapping = values.stream().collect(Collectors.groupingBy(PersonWithAddress::getLastName));

            // identify the parent node element
            String current = null;
            for (PersonWithAddress p : this.values) {
                if (p.getLastName() != current) {
                    this.firstElementMapping.put(p.getLastName(), p);
                    current = p.getLastName();
                }
            }

            // remove the parent node element from the children list
            this.firstElementMapping.forEach((lastname, parent) -> {
                this.parentMapping.get(lastname).remove(parent);
            });
        }

        @Override
        public int getDepthOfData(PersonWithAddress object) {
            PersonWithAddress firstElement = this.firstElementMapping.get(object.getLastName());
            return firstElement.equals(object) ? 0 : 1;
        }

        @Override
        public int getDepthOfData(int index) {
            return getDepthOfData(getDataAtIndex(index));
        }

        @Override
        public PersonWithAddress getDataAtIndex(int index) {
            if (!isValidIndex(index)) {
                return null;
            }
            return this.values.get(index);
        }

        @Override
        public int indexOf(PersonWithAddress child) {
            return this.values.indexOf(child);
        }

        @Override
        public boolean hasChildren(PersonWithAddress object) {
            if (object != null && getDepthOfData(object) == 0) {
                List<PersonWithAddress> children = this.parentMapping.get(object.getLastName());
                return children != null && !children.isEmpty();
            }
            return false;
        }

        @Override
        public boolean hasChildren(int index) {
            return hasChildren(getDataAtIndex(index));
        }

        @Override
        public List<PersonWithAddress> getChildren(PersonWithAddress object) {
            if (object != null && getDepthOfData(object) == 0) {
                return this.parentMapping.get(object.getLastName());
            }
            return new ArrayList<>(0);
        }

        @Override
        public List<PersonWithAddress> getChildren(PersonWithAddress object, boolean fullDepth) {
            // since we only support one level here it is the same as
            // getChildren(PersonWithAddress)
            return getChildren(object);
        }

        @Override
        public List<PersonWithAddress> getChildren(int index) {
            return getChildren(getDataAtIndex(index));
        }

        @Override
        public int getElementCount() {
            return this.values.size();
        }

        @Override
        public boolean isValidIndex(int index) {
            return (!(index < 0) && index < this.values.size());
        }

    }

}
