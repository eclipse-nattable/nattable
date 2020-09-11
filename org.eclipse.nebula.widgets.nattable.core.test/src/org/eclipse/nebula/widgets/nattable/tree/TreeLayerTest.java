/*****************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;
import org.junit.Before;
import org.junit.Test;

public class TreeLayerTest {

    private TreeLayer treeLayer;
    private LayerListenerFixture listener;

    @Before
    public void setup() {
        String[] propertyNames = { "lastName", "firstName", "gender", "married", "birthday" };
        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(propertyNames);

        List<Person> values = PersonService.getFixedPersons();

        ListDataProvider<Person> bodyDataProvider = new ListDataProvider<>(values, columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        ITreeRowModel<Person> treeRowModel = new TreeRowModel<>(new PersonTreeData(values));

        RowHideShowLayer hideShowLayer = new RowHideShowLayer(bodyDataLayer);

        SelectionLayer selectionLayer = new SelectionLayer(hideShowLayer);

        this.treeLayer = new TreeLayer(selectionLayer, treeRowModel);
        this.listener = new LayerListenerFixture();
        this.treeLayer.addLayerListener(this.listener);
    }

    @Test
    public void shouldHaveTreeNodes() {
        assertTrue(this.treeLayer.isTreeColumn(0));
        assertTrue(this.treeLayer.getModel().hasChildren(0));
        assertTrue(this.treeLayer.getModel().hasChildren(8));
    }

    @Test
    public void shouldCollapseAll() {
        this.treeLayer.collapseAll();
        assertEquals(2, this.treeLayer.getRowCount());

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(HideRowPositionsEvent.class));
        HideRowPositionsEvent event = (HideRowPositionsEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(2, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(1, 8), iterator.next());
        assertEquals(new Range(9, 18), iterator.next());
    }

    @Test
    public void shouldHideAndCollapse() {
        this.treeLayer.doCommand(new RowHideCommand(this.treeLayer, 2));

        assertEquals(17, this.treeLayer.getRowCount());

        this.listener.clearReceivedEvents();

        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0));

        // 10 Simpsons + 1 Flanders parent node
        assertEquals(11, this.treeLayer.getRowCount());

        assertTrue(this.treeLayer.hasHiddenRows());

        int[] hiddenRowIndexes = this.treeLayer.getHiddenRowIndexesArray();
        assertEquals(7, hiddenRowIndexes.length);
        assertEquals(1, hiddenRowIndexes[0]);
        assertEquals(2, hiddenRowIndexes[1]);
        assertEquals(3, hiddenRowIndexes[2]);
        assertEquals(4, hiddenRowIndexes[3]);
        assertEquals(5, hiddenRowIndexes[4]);
        assertEquals(6, hiddenRowIndexes[5]);
        assertEquals(7, hiddenRowIndexes[6]);

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(HideRowPositionsEvent.class));

        HideRowPositionsEvent receivedEvent = (HideRowPositionsEvent) this.listener.getReceivedEvent(HideRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = receivedEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 7), rowPositionRanges.iterator().next());

        int[] rowIndexes = receivedEvent.getRowIndexes();
        // only 6 indexes hidden as 1 index was already hidden in an underlying
        // layer before
        assertEquals(6, rowIndexes.length);
        assertEquals(1, rowIndexes[0]);
        assertEquals(3, rowIndexes[1]);
        assertEquals(4, rowIndexes[2]);
        assertEquals(5, rowIndexes[3]);
        assertEquals(6, rowIndexes[4]);
        assertEquals(7, rowIndexes[5]);
    }

    @Test
    public void shouldCollapseAllWithHidden() {
        this.treeLayer.doCommand(new RowHideCommand(this.treeLayer, 2));

        assertEquals(17, this.treeLayer.getRowCount());

        this.listener.clearReceivedEvents();

        this.treeLayer.doCommand(new TreeCollapseAllCommand());

        // 1 Simpsons parent node + 1 Flanders parent node
        assertEquals(2, this.treeLayer.getRowCount());

        int[] hiddenRowIndexes = this.treeLayer.getHiddenRowIndexesArray();
        assertEquals(16, hiddenRowIndexes.length);

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(HideRowPositionsEvent.class));

        HideRowPositionsEvent receivedEvent = (HideRowPositionsEvent) this.listener.getReceivedEvent(HideRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = receivedEvent.getRowPositionRanges();
        assertEquals(2, rowPositionRanges.size());
        Iterator<Range> iterator = rowPositionRanges.iterator();
        assertEquals(new Range(1, 7), iterator.next());
        assertEquals(new Range(8, 17), iterator.next());

        int[] rowIndexes = receivedEvent.getRowIndexes();
        // 15 because 1 row was already hidden
        assertEquals(15, rowIndexes.length);
        assertEquals(1, rowIndexes[0]);
        assertEquals(3, rowIndexes[1]);
        assertEquals(4, rowIndexes[2]);
        assertEquals(5, rowIndexes[3]);
        assertEquals(6, rowIndexes[4]);
        assertEquals(7, rowIndexes[5]);
        assertEquals(9, rowIndexes[6]);
        assertEquals(10, rowIndexes[7]);
        assertEquals(11, rowIndexes[8]);
        assertEquals(12, rowIndexes[9]);
        assertEquals(13, rowIndexes[10]);
        assertEquals(14, rowIndexes[11]);
        assertEquals(15, rowIndexes[12]);
        assertEquals(16, rowIndexes[13]);
        assertEquals(17, rowIndexes[14]);
    }

    @Test
    public void shouldExpandWithHidden() {
        this.treeLayer.doCommand(new RowHideCommand(this.treeLayer, 2));

        // collapse
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0));

        this.listener.clearReceivedEvents();

        // expand again
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0));

        // 10 Simpsons + 7 Flanders (1 Flanders still hidden)
        assertEquals(17, this.treeLayer.getRowCount());

        assertFalse(this.treeLayer.hasHiddenRows());

        assertTrue(this.treeLayer.isRowIndexHidden(2));

        int[] hiddenRowIndexes = this.treeLayer.getHiddenRowIndexesArray();
        assertEquals(0, hiddenRowIndexes.length);

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(ShowRowPositionsEvent.class));

        ShowRowPositionsEvent receivedEvent = (ShowRowPositionsEvent) this.listener.getReceivedEvent(ShowRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = receivedEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 7), rowPositionRanges.iterator().next());

        int[] rowIndexes = receivedEvent.getRowIndexes();
        // only 6 indexes shown again as 1 index is still hidden in an
        // underlying layer
        assertEquals(6, rowIndexes.length);
        assertEquals(1, rowIndexes[0]);
        assertEquals(3, rowIndexes[1]);
        assertEquals(4, rowIndexes[2]);
        assertEquals(5, rowIndexes[3]);
        assertEquals(6, rowIndexes[4]);
        assertEquals(7, rowIndexes[5]);
    }

    @Test
    public void shouldExpandAllWithHidden() {
        this.treeLayer.doCommand(new RowHideCommand(this.treeLayer, 2));

        // collapse
        this.treeLayer.doCommand(new TreeCollapseAllCommand());

        this.listener.clearReceivedEvents();

        // expand again
        this.treeLayer.doCommand(new TreeExpandAllCommand());

        // 10 Simpsons + 7 Flanders (1 Flanders still hidden)
        assertEquals(17, this.treeLayer.getRowCount());

        assertTrue(this.treeLayer.isRowIndexHidden(2));

        int[] hiddenRowIndexes = this.treeLayer.getHiddenRowIndexesArray();
        assertEquals(0, hiddenRowIndexes.length);

        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(ShowRowPositionsEvent.class));

        ShowRowPositionsEvent receivedEvent = (ShowRowPositionsEvent) this.listener.getReceivedEvent(ShowRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = receivedEvent.getRowPositionRanges();
        Iterator<Range> iterator = rowPositionRanges.iterator();
        assertEquals(new Range(1, 7), iterator.next());
        assertEquals(new Range(8, 17), iterator.next());

        int[] rowIndexes = receivedEvent.getRowIndexes();
        // 15 because 1 row was already hidden
        assertEquals(15, rowIndexes.length);
        assertEquals(1, rowIndexes[0]);
        assertEquals(3, rowIndexes[1]);
        assertEquals(4, rowIndexes[2]);
        assertEquals(5, rowIndexes[3]);
        assertEquals(6, rowIndexes[4]);
        assertEquals(7, rowIndexes[5]);
        assertEquals(9, rowIndexes[6]);
        assertEquals(10, rowIndexes[7]);
        assertEquals(11, rowIndexes[8]);
        assertEquals(12, rowIndexes[9]);
        assertEquals(13, rowIndexes[10]);
        assertEquals(14, rowIndexes[11]);
        assertEquals(15, rowIndexes[12]);
        assertEquals(16, rowIndexes[13]);
        assertEquals(17, rowIndexes[14]);
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
                if (!p.getLastName().equals(current)) {
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
