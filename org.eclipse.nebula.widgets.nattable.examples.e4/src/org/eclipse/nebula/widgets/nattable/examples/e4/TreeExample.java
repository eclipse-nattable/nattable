/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.examples.e4;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
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
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommand;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.TreeList;

public class TreeExample {

    public static final String GENDER_LABEL = "genderLabel";
    public static final String MARRIED_LABEL = "marriedLabel";
    public static final String DATE_LABEL = "dateLabel";

    @PostConstruct
    public void postConstruct(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

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

        final BodyLayerStack bodyLayerStack =
                new BodyLayerStack(
                        PersonService.getPersonsWithAddress(5),
                        columnPropertyAccessor,
                        new PersonWithAddressTwoLevelTreeFormat());
        // new PersonWithAddressTreeFormat());

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
        final NatTable natTable = new NatTable(container, gridLayer);

        natTable.setData("org.eclipse.e4.ui.css.CssClassName", "modern");

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

        Button expandToLevelButton = new Button(buttonPanel, SWT.PUSH);
        expandToLevelButton.setText("Expand First Level");
        expandToLevelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(new TreeExpandToLevelCommand(1));
            }
        });
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <PersonWithAddress>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    class BodyLayerStack extends AbstractLayerTransform {

        private final TreeList treeList;

        private final IRowDataProvider bodyDataProvider;

        private final SelectionLayer selectionLayer;

        public BodyLayerStack(
                List<PersonWithAddress> values,
                IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor,
                TreeList.Format treeFormat) {
            // wrapping of the list to show into GlazedLists
            // see http://publicobject.com/glazedlists/ for further information
            EventList<PersonWithAddress> eventList = GlazedLists.eventList(values);
            TransformedList<PersonWithAddress, PersonWithAddress> rowObjectsGlazedList =
                    GlazedLists.threadSafeList(eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator
            // will be set by configuration
            SortedList<PersonWithAddress> sortedList =
                    new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the TreeList
            treeList =
                    new TreeList(sortedList, treeFormat, TreeList.nodesStartExpanded());

            bodyDataProvider =
                    new ListDataProvider<Object>(
                            treeList, new PersonWithAddressTreeColumnPropertyAccessor(columnPropertyAccessor));
            DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

            // only apply labels in case it is a real row object and not a tree
            // structure object
            bodyDataLayer.setConfigLabelAccumulator(new AbstractOverrider() {

                @Override
                public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                    Object rowObject = bodyDataProvider.getRowObject(rowPosition);
                    if (rowObject instanceof PersonWithAddress) {
                        if (columnPosition == 2) {
                            configLabels.addLabel(GENDER_LABEL);
                        } else if (columnPosition == 3) {
                            configLabels.addLabel(MARRIED_LABEL);
                        } else if (columnPosition == 4) {
                            configLabels.addLabel(DATE_LABEL);
                        }
                    }
                }

                @Override
                public Collection<String> getProvidedLabels() {
                    // make the custom labels available for the CSS engine
                    Collection<String> result = super.getProvidedLabels();
                    result.add(GENDER_LABEL);
                    result.add(MARRIED_LABEL);
                    result.add(DATE_LABEL);
                    return result;
                }
            });

            // layer for event handling of GlazedLists and PropertyChanges
            GlazedListsEventLayer<PersonWithAddress> glazedListsEventLayer =
                    new GlazedListsEventLayer<PersonWithAddress>(bodyDataLayer, treeList);

            GlazedListTreeData<Object> treeData = new GlazedListTreeData<Object>(treeList);
            ITreeRowModel<Object> treeRowModel = new GlazedListTreeRowModel<>(treeData);

            // ITreeRowModel<Object> treeRowModel = new
            // TreeRowModel<Object>(treeData);

            selectionLayer = new SelectionLayer(glazedListsEventLayer);

            TreeLayer treeLayer = new TreeLayer(selectionLayer, treeRowModel);
            ViewportLayer viewportLayer = new ViewportLayer(treeLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return selectionLayer;
        }

        public TreeList<PersonWithAddress> getTreeList() {
            return treeList;
        }

        public IDataProvider getBodyDataProvider() {
            return bodyDataProvider;
        }
    }

    private class PersonWithAddressTreeColumnPropertyAccessor implements IColumnPropertyAccessor<Object> {

        private IColumnPropertyAccessor<PersonWithAddress> cpa;

        public PersonWithAddressTreeColumnPropertyAccessor(IColumnPropertyAccessor<PersonWithAddress> cpa) {
            this.cpa = cpa;
        }

        @Override
        public Object getDataValue(Object rowObject, int columnIndex) {
            if (rowObject instanceof PersonWithAddress) {
                return cpa.getDataValue((PersonWithAddress) rowObject, columnIndex);
            } else if (columnIndex == 0) {
                return rowObject;
            }
            return null;
        }

        @Override
        public void setDataValue(Object rowObject, int columnIndex, Object newValue) {
            if (rowObject instanceof PersonWithAddress) {
                cpa.setDataValue((PersonWithAddress) rowObject,
                        columnIndex, newValue);
            }
        }

        @Override
        public int getColumnCount() {
            return cpa.getColumnCount();
        }

        @Override
        public String getColumnProperty(int columnIndex) {
            return cpa.getColumnProperty(columnIndex);
        }

        @Override
        public int getColumnIndex(String propertyName) {
            return cpa.getColumnIndex(propertyName);
        }

    }

    @SuppressWarnings("unused")
    private class PersonWithAddressTreeFormat implements TreeList.Format<Object> {

        @Override
        public void getPath(List<Object> path, Object element) {
            if (element instanceof PersonWithAddress) {
                PersonWithAddress ele = (PersonWithAddress) element;
                path.add(new LastNameGroup(ele.getId(), ele.getLastName()));
            }
            path.add(element);
        }

        @Override
        public boolean allowsChildren(Object element) {
            return true;
        }

        @Override
        public Comparator<? super Object> getComparator(int depth) {
            return new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    String e1 = (o1 instanceof PersonWithAddress) ? ((PersonWithAddress) o1).getLastName() : o1.toString();
                    String e2 = (o2 instanceof PersonWithAddress) ? ((PersonWithAddress) o2).getLastName() : o2.toString();
                    return e1.compareTo(e2);
                }

            };
        }
    }

    private class PersonWithAddressTwoLevelTreeFormat implements TreeList.Format<Object> {

        AtomicInteger counter = new AtomicInteger();
        Map<String, LastNameGroup> lastNames = new HashMap<>();
        Map<String, FirstNameGroup> firstNames = new HashMap<>();

        @Override
        public void getPath(List<Object> path, Object element) {
            if (element instanceof PersonWithAddress) {
                PersonWithAddress ele = (PersonWithAddress) element;
                if (!lastNames.containsKey(ele.getLastName())) {
                    lastNames.put(ele.getLastName(), new LastNameGroup(counter.incrementAndGet(), ele.getLastName()));
                }
                path.add(lastNames.get(ele.getLastName()));

                String firstNameKey = ele.getLastName() + "_" + ele.getFirstName();
                if (!firstNames.containsKey(firstNameKey)) {
                    firstNames.put(firstNameKey, new FirstNameGroup(ele.getLastName(), ele.getFirstName()));
                }
                path.add(firstNames.get(firstNameKey));
            }
            path.add(element);
        }

        @Override
        public boolean allowsChildren(Object element) {
            return true;
        }

        @Override
        public Comparator<? super Object> getComparator(final int depth) {
            return new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    String e1 = (o1 instanceof PersonWithAddress)
                            ? (depth == 0 ? ((PersonWithAddress) o1).getLastName() : ((PersonWithAddress) o1).getFirstName()) : o1.toString();
                    String e2 = (o2 instanceof PersonWithAddress)
                            ? (depth == 0 ? ((PersonWithAddress) o2).getLastName() : ((PersonWithAddress) o2).getFirstName()) : o2.toString();
                    return e1.compareTo(e2);
                }

            };
        }
    }

    // To make expand/collapse work correctly with the TreeRowModel, the
    // elements in the TreeList needs to be identifiable. This is necessary so
    // List#indexOf() returns the correct positions
    class LastNameGroup implements Comparable<LastNameGroup> {
        int id;
        String lastName;

        public LastNameGroup(int id, String lastName) {
            this.id = id;
            this.lastName = lastName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LastNameGroup other = (LastNameGroup) obj;
            if (id != other.id)
                return false;
            if (lastName == null) {
                if (other.lastName != null)
                    return false;
            } else if (!lastName.equals(other.lastName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return lastName;
        }

        @Override
        public int compareTo(LastNameGroup o) {
            return lastName.compareTo(o.lastName);
        }
    }

    // firstname group is unique within a lastname group
    class FirstNameGroup implements Comparable<FirstNameGroup> {
        String lastName;
        String firstName;

        public FirstNameGroup(String lastName, String firstName) {
            this.lastName = lastName;
            this.firstName = firstName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
            result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FirstNameGroup other = (FirstNameGroup) obj;
            if (firstName == null) {
                if (other.firstName != null)
                    return false;
            } else if (!firstName.equals(other.firstName))
                return false;
            if (lastName == null) {
                if (other.lastName != null)
                    return false;
            } else if (!lastName.equals(other.lastName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return firstName;
        }

        @Override
        public int compareTo(FirstNameGroup o) {
            return firstName.compareTo(o.firstName);
        }
    }
}
