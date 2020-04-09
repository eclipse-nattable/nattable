/*****************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.test.performance;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;
import org.junit.Test;

public class TreeLayerPerformanceTest {

    @Test
    public void performanceCollapseExpandAll() {
        List<Person> values = PersonService.getFixedPersons();
        for (int i = 42; i < 100_042; i++) {
            values.add(new Person(i, "Ralph", "Wiggum", Gender.MALE, false, new Date()));
        }

        String[] propertyNames = { "lastName", "firstName", "gender", "married", "birthday" };
        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(propertyNames);
        ListDataProvider<Person> bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        ITreeRowModel<Person> treeRowModel = new TreeRowModel<>(new PersonTreeData(values));

        RowHideShowLayer hideShowLayer = new RowHideShowLayer(bodyDataLayer);

        SelectionLayer selectionLayer = new SelectionLayer(hideShowLayer);

        TreeLayer treeLayer = new TreeLayer(selectionLayer, treeRowModel);

        long start = System.currentTimeMillis();
        treeLayer.collapseAll();
        long end = System.currentTimeMillis();

        assertEquals(3, treeLayer.getRowCount());
        System.out.println("collapseAll() " + (end - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        start = System.currentTimeMillis();
        treeLayer.expandAll();
        end = System.currentTimeMillis();

        assertEquals(100_018, treeLayer.getRowCount());
        System.out.println("expandAll() " + (end - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void performanceCollapseAllExpandOneBigParent() {
        List<Person> values = PersonService.getFixedPersons();
        for (int i = 42; i < 100_042; i++) {
            values.add(new Person(i, "Ralph", "Wiggum", Gender.MALE, false, new Date()));
        }

        String[] propertyNames = { "lastName", "firstName", "gender", "married", "birthday" };
        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(propertyNames);
        ListDataProvider<Person> bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        ITreeRowModel<Person> treeRowModel = new TreeRowModel<>(new PersonTreeData(values));

        RowHideShowLayer hideShowLayer = new RowHideShowLayer(bodyDataLayer);

        SelectionLayer selectionLayer = new SelectionLayer(hideShowLayer);

        TreeLayer treeLayer = new TreeLayer(selectionLayer, treeRowModel);

        long start = System.currentTimeMillis();
        treeLayer.collapseAll();
        long end = System.currentTimeMillis();

        assertEquals(3, treeLayer.getRowCount());
        System.out.println("collapseAll() " + (end - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        start = System.currentTimeMillis();
        treeLayer.expandTreeRow(18);
        end = System.currentTimeMillis();

        assertEquals(100_002, treeLayer.getRowCount());
        System.out.println("expandRow() " + (end - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void performanceBuildCache() {
        List<Person> values = PersonService.getFixedPersons();
        for (int i = 42; i < 1_000_042; i++) {
            values.add(new Person(i, "Ralph", "Wiggum", Gender.MALE, false, new Date()));
        }

        String[] propertyNames = { "lastName", "firstName", "gender", "married", "birthday" };
        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(propertyNames);
        ListDataProvider<Person> bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        ITreeRowModel<Person> treeRowModel = new TreeRowModel<>(new PersonTreeData(values));

        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);

        TreeLayer treeLayer = new TreeLayer(selectionLayer, treeRowModel);

        treeLayer.collapseTreeRow(0);

        long start = System.currentTimeMillis();
        assertEquals(1_000_011, treeLayer.getRowCount());
        long end = System.currentTimeMillis();

        System.out.println("buildCache() " + (end - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static class PersonTreeData implements ITreeData<Person> {

        private List<Person> values;

        private Map<String, List<Person>> parentMapping;

        private Map<String, Person> firstElementMapping = new HashMap<>();

        public PersonTreeData(List<Person> values) {
            this.values = values;

            // first we need to sort by lastname to ensure all elements with the
            // same lastname are grouped together
            this.values.sort(Comparator.comparing(Person::getLastName));

            // then we build up the mapping from lastname to all child elements
            this.parentMapping = values.stream().collect(Collectors.groupingBy(Person::getLastName));

            // identify the parent node element
            String current = null;
            for (Person p : this.values) {
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
        public int getDepthOfData(Person object) {
            Person firstElement = this.firstElementMapping.get(object.getLastName());
            return firstElement.equals(object) ? 0 : 1;
        }

        @Override
        public int getDepthOfData(int index) {
            return getDepthOfData(getDataAtIndex(index));
        }

        @Override
        public Person getDataAtIndex(int index) {
            if (!isValidIndex(index)) {
                return null;
            }
            return this.values.get(index);
        }

        @Override
        public int indexOf(Person child) {
            return this.values.indexOf(child);
        }

        @Override
        public boolean hasChildren(Person object) {
            if (object != null && getDepthOfData(object) == 0) {
                List<Person> children = this.parentMapping.get(object.getLastName());
                return children != null && !children.isEmpty();
            }
            return false;
        }

        @Override
        public boolean hasChildren(int index) {
            return hasChildren(getDataAtIndex(index));
        }

        @Override
        public List<Person> getChildren(Person object) {
            if (object != null && getDepthOfData(object) == 0) {
                return this.parentMapping.get(object.getLastName());
            }
            return new ArrayList<>(0);
        }

        @Override
        public List<Person> getChildren(Person object, boolean fullDepth) {
            // since we only support one level here it is the same as
            // getChildren(PersonWithAddress)
            return getChildren(object);
        }

        @Override
        public List<Person> getChildren(int index) {
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
