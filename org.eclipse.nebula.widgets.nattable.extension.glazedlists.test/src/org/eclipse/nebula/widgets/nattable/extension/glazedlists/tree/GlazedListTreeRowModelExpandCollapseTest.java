/*******************************************************************************
 * Copyright (c) 2013, 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.nebula.widgets.nattable.test.data.Person;
import org.eclipse.nebula.widgets.nattable.test.data.PersonService;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GlazedListTreeRowModelExpandCollapseTest {

    private TreeList treeList;
    private GlazedListTreeData treeData;
    private GlazedListTreeRowModel treeRowModel;

    @Before
    public void setup() {
        EventList<Person> eventList = GlazedLists.eventList(PersonService.getFixedPersons());

        this.treeList = new TreeList(eventList, new PersonTreeFormat(), TreeList.nodesStartExpanded());

        this.treeData = new GlazedListTreeData(this.treeList);
        this.treeRowModel = new GlazedListTreeRowModel(this.treeData);
    }

    @Test
    public void testInitialExpanded() {
        for (int i = 0; i < this.treeList.size(); i++) {
            assertFalse("Node is not expanded", this.treeRowModel.isCollapsed(i));
        }
    }

    @Test
    public void testCollapseAllStepByStep() {
        for (int i = this.treeList.size() - 1; i >= 0; i--) {
            this.treeRowModel.collapse(i);
        }

        for (int i = 0; i < this.treeList.size(); i++) {
            assertTrue(
                    MessageFormat.format("Node at index {0} is expanded", i),
                    this.treeRowModel.isCollapsed(i));
        }
    }

    @Test
    public void testCollapseAll() {
        this.treeRowModel.collapseAll();

        for (int i = 0; i < this.treeList.size(); i++) {
            assertTrue(
                    MessageFormat.format("Node at index {0} is expanded", i),
                    this.treeRowModel.isCollapsed(i));
        }
    }

    @Test
    public void testCollapseExpandAll() {
        this.treeRowModel.collapseAll();
        this.treeRowModel.expandAll();

        for (int i = 0; i < this.treeList.size(); i++) {
            assertFalse(
                    MessageFormat.format("Node at index {0} is collapsed", i),
                    this.treeRowModel.isCollapsed(i));
        }
    }

    @Test
    public void testCollapseAllNonUnique() {
        this.treeList.addAll(PersonService.getFixedMixedPersons());

        this.treeRowModel.collapseAll();

        for (int i = 0; i < this.treeList.size(); i++) {
            assertTrue(
                    MessageFormat.format("Node at index {0} is expanded", i),
                    this.treeRowModel.isCollapsed(i));
        }
    }

    @Test
    public void testCollapseExpandAllNonUnique() {
        this.treeList.addAll(PersonService.getFixedMixedPersons());

        this.treeRowModel.collapseAll();
        this.treeRowModel.expandAll();

        for (int i = 0; i < this.treeList.size(); i++) {
            assertFalse(
                    MessageFormat.format("Node at index {0} is collapsed", i),
                    this.treeRowModel.isCollapsed(i));
        }
    }

    @Test
    public void testGetChildrenByIndex() {
        List flandersChildren = this.treeData.getChildren(0);

        assertEquals(4, flandersChildren.size());
    }

    @Test
    public void testExpandCollapseByIndex() {
        assertFalse("Flanders is not expanded", this.treeRowModel.isCollapsed(0));

        // collapse Flanders
        this.treeRowModel.collapse(0);

        assertTrue("Flanders is not expanded", this.treeRowModel.isCollapsed(0));

        // expand Flanders
        this.treeRowModel.expand(0);

        assertFalse("Flanders is not expanded", this.treeRowModel.isCollapsed(0));
    }

    @Test
    public void testExpandCollapseByObject() {
        LastNameGroup flanders = new LastNameGroup(2, "Flanders");
        assertFalse("Flanders is not expanded", this.treeRowModel.isCollapsed(flanders));

        // collapse Flanders
        this.treeRowModel.collapse(flanders);

        assertTrue("Flanders is not expanded", this.treeRowModel.isCollapsed(flanders));

        // expand Flanders
        this.treeRowModel.expand(flanders);

        assertFalse("Flanders is not expanded", this.treeRowModel.isCollapsed(flanders));
    }

    @Test
    public void testExpandToLevel() {
        // collapse all and expand to level 1
        // lastnames should be expanded, firstnames still collapsed
        this.treeRowModel.collapseAll();

        for (int i = 0; i < this.treeList.size(); i++) {
            assertTrue(
                    MessageFormat.format("Node at index {0} is expanded", i),
                    this.treeRowModel.isCollapsed(i));
        }

        this.treeRowModel.expandToLevel(1);
        for (int i = 0; i < this.treeList.size(); i++) {
            if (this.treeList.get(i) instanceof LastNameGroup) {
                assertFalse(
                        MessageFormat.format("Node at index {0} is collapsed", i),
                        this.treeRowModel.isCollapsed(i));
            }
            else if (this.treeList.get(i) instanceof FirstNameGroup) {
                assertTrue(
                        MessageFormat.format("Node at index {0} is expanded", i),
                        this.treeRowModel.isCollapsed(i));
            }
            else {
                // there should be no other values visible right now
                fail("Another object than LastNameGroup and FirstNameGroup is visible");
            }
        }
    }

    private class PersonTreeFormat implements TreeList.Format<Object> {

        AtomicInteger counter = new AtomicInteger();
        Map<String, LastNameGroup> lastNames = new HashMap<String, LastNameGroup>();
        {
            this.lastNames.put("Simpsons", new LastNameGroup(1, "Simpsons"));
            this.lastNames.put("Flanders", new LastNameGroup(2, "Flanders"));
        }
        Map<String, FirstNameGroup> firstNames = new HashMap<String, FirstNameGroup>();

        @Override
        public void getPath(List<Object> path, Object element) {
            if (element instanceof Person) {
                Person ele = (Person) element;
                if (!this.lastNames.containsKey(ele.getLastName())) {
                    this.lastNames.put(ele.getLastName(), new LastNameGroup(this.counter.incrementAndGet(), ele.getLastName()));
                }
                path.add(this.lastNames.get(ele.getLastName()));

                String firstNameKey = ele.getLastName() + "_" + ele.getFirstName();
                if (!this.firstNames.containsKey(firstNameKey)) {
                    this.firstNames.put(firstNameKey, new FirstNameGroup(ele.getLastName(), ele.getFirstName()));
                }
                path.add(this.firstNames.get(firstNameKey));
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
                    String e1 = (o1 instanceof Person) ? (depth == 0 ? ((Person) o1)
                            .getLastName() : ((Person) o1).getFirstName())
                            : o1.toString();
                    String e2 = (o2 instanceof Person) ? (depth == 0 ? ((Person) o2)
                            .getLastName() : ((Person) o2).getFirstName())
                            : o2.toString();
                    return e1.compareTo(e2);
                }

            };
        }
    }

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
            result = prime * result + this.id;
            result = prime * result + ((this.lastName == null) ? 0 : this.lastName.hashCode());
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
            if (this.id != other.id)
                return false;
            if (this.lastName == null) {
                if (other.lastName != null)
                    return false;
            } else if (!this.lastName.equals(other.lastName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.lastName;
        }

        @Override
        public int compareTo(LastNameGroup o) {
            return this.lastName.compareTo(o.lastName);
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
            result = prime * result + ((this.firstName == null) ? 0 : this.firstName.hashCode());
            result = prime * result + ((this.lastName == null) ? 0 : this.lastName.hashCode());
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
            if (this.firstName == null) {
                if (other.firstName != null)
                    return false;
            } else if (!this.firstName.equals(other.firstName))
                return false;
            if (this.lastName == null) {
                if (other.lastName != null)
                    return false;
            } else if (!this.lastName.equals(other.lastName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.firstName;
        }

        @Override
        public int compareTo(FirstNameGroup o) {
            return this.firstName.compareTo(o.firstName);
        }
    }
}
