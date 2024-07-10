/*******************************************************************************
 * Copyright (c) 2018, 2024 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.datachange.IdIndexKeyHandler;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;

public class DataChangeLayerIntegrationTest {

    private List<Person> dataModel;
    private FilterList<Person> filterList;
    private IRowDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;

    private LayerListenerFixture listenerFixture;

    @BeforeEach
    public void setup() {
        this.dataModel = PersonService.getFixedPersons();
        EventList<Person> eventList = GlazedLists.eventList(this.dataModel);
        SortedList<Person> sortedList = new SortedList<>(eventList, null);
        this.filterList = new FilterList<>(sortedList);

        this.dataProvider = new ListDataProvider<>(
                this.filterList,
                new ReflectiveColumnPropertyAccessor<>(new String[] {
                        "firstName",
                        "lastName",
                        "gender",
                        "married",
                        "birthday" }));
        this.dataLayer = new DataLayer(this.dataProvider);

        GlazedListsEventLayer<Person> glazedListsEventLayer =
                new GlazedListsEventLayer<>(this.dataLayer, this.filterList);
        glazedListsEventLayer.setTestMode(true);

        this.dataChangeLayer = new DataChangeLayer(glazedListsEventLayer,
                new IdIndexKeyHandler<>(
                        this.dataProvider,
                        new IRowIdAccessor<Person>() {

                            @Override
                            public Serializable getRowId(Person rowObject) {
                                return rowObject.getId();
                            }
                        }),
                false);

        this.listenerFixture = new LayerListenerFixture();
        this.dataChangeLayer.addLayerListener(this.listenerFixture);
    }

    @AfterEach
    public void tearDown() {
        this.dataChangeLayer.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldUpdateDataInDataLayer() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");
    }

    @Test
    public void shouldKeepChangeOnFilter() throws InterruptedException {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);

        this.filterList.setMatcher(new Matcher<Person>() {

            @Override
            public boolean matches(Person item) {
                return item.getLastName().equals("Simpson");
            }
        });

        // give the GlazedListsEventLayer some time to trigger the
        // RowStructuralRefreshEvent
        boolean completed = countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(9, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");

        countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);

        this.filterList.setMatcher(null);

        // give the GlazedListsEventLayer some time to trigger the
        // RowStructuralRefreshEvent
        completed = countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(18, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
    }

    @Test
    public void shouldNotThrowAnExceptionOnResize() throws InterruptedException {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        this.filterList.setMatcher(new Matcher<Person>() {

            @Override
            public boolean matches(Person item) {
                return item.getLastName().equals("Simpson");
            }
        });

        this.dataLayer.setColumnWidthByPosition(2, 75);

        assertEquals(9, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");

        CountDownLatch countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);

        this.filterList.setMatcher(null);

        // give the GlazedListsEventLayer some time to trigger the
        // RowStructuralRefreshEvent
        boolean completed = countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(18, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
    }
}
