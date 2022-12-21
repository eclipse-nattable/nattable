/*******************************************************************************
 * Copyright (c) 2018, 2022 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowObjectDeleteEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RowDeleteCommandTest {

    private List<Person> dataModel;
    private ListDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private LayerListenerFixture listener;

    @BeforeEach
    public void setup() {
        this.dataModel = PersonService.getFixedPersons();
        this.dataProvider = new ListDataProvider<>(
                this.dataModel,
                new ReflectiveColumnPropertyAccessor<>(new String[] {
                        "firstName",
                        "lastName",
                        "gender",
                        "married",
                        "birthday" }));

        this.dataLayer = new DataLayer(this.dataProvider);
        this.dataLayer.registerCommandHandler(new RowDeleteCommandHandler<>(this.dataModel));

        this.listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.listener);
    }

    @Test
    public void shouldDeleteSingleRow() {
        assertEquals(18, this.dataModel.size());

        Person toDelete = this.dataModel.get(10);

        this.dataLayer.doCommand(new RowDeleteCommand(this.dataLayer, 10));

        // test that the object is deleted
        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowObjectDeleteEvent.class));
        RowObjectDeleteEvent event = (RowObjectDeleteEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(1, event.getDeletedRowIndexes().size());
        assertEquals(10, event.getDeletedRowIndexes().iterator().next().intValue());
        assertEquals(1, event.getDeletedObjects().size());
        assertEquals(toDelete, event.getDeletedObjects().get(10));
        assertEquals(new Range(10, 11), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldDeleteMultipleContiguousRows() {
        assertEquals(18, this.dataModel.size());

        Person toDelete1 = this.dataModel.get(10);
        Person toDelete2 = this.dataModel.get(11);
        Person toDelete3 = this.dataModel.get(12);

        this.dataLayer.doCommand(new RowDeleteCommand(this.dataLayer, 10, 11, 12));

        // test that the object is deleted
        assertEquals(15, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete1));
        assertFalse(this.dataModel.contains(toDelete2));
        assertFalse(this.dataModel.contains(toDelete3));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowObjectDeleteEvent.class));
        RowObjectDeleteEvent event = (RowObjectDeleteEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(3, event.getDeletedRowIndexes().size());
        assertTrue(event.getDeletedRowIndexes().contains(10), "Index 10 not included");
        assertTrue(event.getDeletedRowIndexes().contains(11), "Index 11 not included");
        assertTrue(event.getDeletedRowIndexes().contains(12), "Index 12 not included");
        assertEquals(3, event.getDeletedObjects().size());
        assertEquals(toDelete1, event.getDeletedObjects().get(10));
        assertEquals(toDelete2, event.getDeletedObjects().get(11));
        assertEquals(toDelete3, event.getDeletedObjects().get(12));
        assertEquals(new Range(10, 13), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldDeleteMultipleNotContiguousRows() {
        assertEquals(18, this.dataModel.size());

        Person toDelete1 = this.dataModel.get(5);
        Person toDelete2 = this.dataModel.get(6);
        Person toDelete3 = this.dataModel.get(10);
        Person toDelete4 = this.dataModel.get(12);

        this.dataLayer.doCommand(new RowDeleteCommand(this.dataLayer, 12, 5, 6, 10));

        // test that the object is deleted
        assertEquals(14, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete1));
        assertFalse(this.dataModel.contains(toDelete2));
        assertFalse(this.dataModel.contains(toDelete3));
        assertFalse(this.dataModel.contains(toDelete4));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowObjectDeleteEvent.class));
        RowObjectDeleteEvent event = (RowObjectDeleteEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(4, event.getDeletedRowIndexes().size());
        assertTrue(event.getDeletedRowIndexes().contains(5), "Index 5 not included");
        assertTrue(event.getDeletedRowIndexes().contains(6), "Index 6 not included");
        assertTrue(event.getDeletedRowIndexes().contains(10), "Index 10 not included");
        assertTrue(event.getDeletedRowIndexes().contains(12), "Index 12 not included");
        assertEquals(4, event.getDeletedObjects().size());
        assertEquals(toDelete1, event.getDeletedObjects().get(5));
        assertEquals(toDelete2, event.getDeletedObjects().get(6));
        assertEquals(toDelete3, event.getDeletedObjects().get(10));
        assertEquals(toDelete4, event.getDeletedObjects().get(12));
        assertEquals(3, event.getRowPositionRanges().size());
        Iterator<Range> iterator = event.getRowPositionRanges().iterator();
        assertEquals(new Range(5, 7), iterator.next());
        assertEquals(new Range(10, 11), iterator.next());
        assertEquals(new Range(12, 13), iterator.next());
    }
}
