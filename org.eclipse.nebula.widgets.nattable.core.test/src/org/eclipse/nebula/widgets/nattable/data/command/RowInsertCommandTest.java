/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class RowInsertCommandTest {

    private List<Person> dataModel;
    private ListDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private LayerListenerFixture listener;

    @Before
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
        this.dataLayer.registerCommandHandler(new RowInsertCommandHandler<>(this.dataModel));

        this.listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.listener);
    }

    @Test
    public void shouldAppendSingleRow() {
        assertEquals(18, this.dataModel.size());

        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, ralph));

        // test that the object is inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(18, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(18, 19), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertSingleRow() {
        assertEquals(18, this.dataModel.size());

        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, 10, ralph));

        // test that the objects are inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(10, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(10, 11), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldAppendMultipleRows() {
        assertEquals(18, this.dataModel.size());

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());

        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, -1, clancy, sarah, ralph));

        // test that the objects are inserted
        assertEquals(21, this.dataModel.size());
        assertEquals(18, this.dataModel.indexOf(clancy));
        assertEquals(19, this.dataModel.indexOf(sarah));
        assertEquals(20, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(18, 21), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertMultipleRows() {
        assertEquals(18, this.dataModel.size());

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());

        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, 10, clancy, sarah, ralph));

        // test that the objects are inserted
        assertEquals(21, this.dataModel.size());
        assertEquals(10, this.dataModel.indexOf(clancy));
        assertEquals(11, this.dataModel.indexOf(sarah));
        assertEquals(12, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(10, 13), event.getRowPositionRanges().iterator().next());
    }
}
