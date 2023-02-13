/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HideColumnCommandTest {

    private ColumnHideShowLayer columnHideShowLayer;

    @BeforeEach
    public void setup() {
        this.columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture());
    }

    @Test
    public void shouldHideColumn() {
        ILayerCommand hideColumnCommand =
                new MultiColumnHideCommand(this.columnHideShowLayer, 2);

        assertEquals(5, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.doCommand(hideColumnCommand);

        assertAll("column hidden",
                () -> assertEquals(4, this.columnHideShowLayer.getColumnCount()),
                () -> assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0)),
                () -> assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1)),
                () -> assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2)),
                () -> assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3)));
    }

    @Test
    public void shouldHideColumnByIndex() {
        ILayerCommand hideColumnCommand = new HideColumnByIndexCommand(2);

        assertEquals(5, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.doCommand(hideColumnCommand);

        assertAll("column hidden",
                () -> assertEquals(4, this.columnHideShowLayer.getColumnCount()),
                () -> assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0)),
                () -> assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1)),
                () -> assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2)),
                () -> assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3)));
    }

    @Test
    public void shouldNotFireEventIfColumnIndexIsAlreadyHidden() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHideShowLayer.addLayerListener(listener);

        shouldHideColumnByIndex();

        assertAll("first event fired",
                () -> assertEquals(1, listener.getEventsCount()),
                () -> assertTrue(listener.containsInstanceOf(HideColumnPositionsEvent.class)),
                () -> {
                    HideColumnPositionsEvent event = (HideColumnPositionsEvent) listener.getReceivedEvents().get(0);
                    assertEquals(1, event.getColumnIndexes().length);
                    assertEquals(2, event.getColumnIndexes()[0]);
                    assertEquals(1, event.getColumnPositionRanges().size());
                    assertEquals(new Range(2, 3), event.getColumnPositionRanges().iterator().next());
                });

        listener.clearReceivedEvents();

        // try to hide the already hidden column again
        this.columnHideShowLayer.doCommand(new HideColumnByIndexCommand(2));

        assertEquals(0, listener.getEventsCount());
    }

    @Test
    public void shouldNotContainAlreadyHiddenColumnIndexInEvent() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHideShowLayer.addLayerListener(listener);

        shouldHideColumnByIndex();

        listener.clearReceivedEvents();

        // try to hide multiple columns with the already hidden column 2
        this.columnHideShowLayer.doCommand(new HideColumnByIndexCommand(1, 2, 3));

        assertAll("event fired with correct data",
                () -> assertEquals(1, listener.getEventsCount()),
                () -> assertTrue(listener.containsInstanceOf(HideColumnPositionsEvent.class)),
                () -> {
                    HideColumnPositionsEvent event = (HideColumnPositionsEvent) listener.getReceivedEvents().get(0);
                    assertEquals(2, event.getColumnIndexes().length);
                    assertEquals(1, event.getColumnIndexes()[0]);
                    assertEquals(3, event.getColumnIndexes()[1]);
                    // there is only one range [1,3] because column 2 is already
                    // hidden
                    assertEquals(1, event.getColumnPositionRanges().size());
                    Iterator<Range> it = event.getColumnPositionRanges().iterator();
                    assertEquals(new Range(1, 3), it.next());
                });
    }

}
