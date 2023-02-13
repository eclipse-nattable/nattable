/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HideRowCommandTest {

    private RowHideShowLayer rowHideShowLayer;

    @BeforeEach
    public void setup() {
        this.rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture());
    }

    @Test
    public void shouldHideRow() {
        ILayerCommand hideRowCommand = new MultiRowHideCommand(this.rowHideShowLayer, 2);

        assertEquals(7, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.doCommand(hideRowCommand);

        assertAll("row hidden",
                () -> assertEquals(6, this.rowHideShowLayer.getRowCount()),
                () -> assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0)),
                () -> assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1)),
                () -> assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2)),
                () -> assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3)),
                () -> assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4)),
                () -> assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(5)));
    }

    @Test
    public void shouldHideRowByIndex() {
        ILayerCommand hideRowCommand = new HideRowByIndexCommand(2);

        assertEquals(7, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.doCommand(hideRowCommand);

        assertAll("row hidden",
                () -> assertEquals(6, this.rowHideShowLayer.getRowCount()),
                () -> assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0)),
                () -> assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1)),
                () -> assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2)),
                () -> assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3)),
                () -> assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4)),
                () -> assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(5)));
    }

    @Test
    public void shouldNotConvertOnInvalidPosition() {
        RowReorderLayer layer = new RowReorderLayer(this.rowHideShowLayer);
        ILayerCommand hideRowCommand = new MultiRowHideCommand(layer, 10);

        assertFalse(hideRowCommand.convertToTargetLayer(this.rowHideShowLayer));
    }

    @Test
    public void shouldNotFireEventIfRowIndexIsAlreadyHidden() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.rowHideShowLayer.addLayerListener(listener);

        shouldHideRowByIndex();

        assertAll("first event fired",
                () -> assertEquals(1, listener.getEventsCount()),
                () -> assertTrue(listener.containsInstanceOf(HideRowPositionsEvent.class)),
                () -> {
                    HideRowPositionsEvent event = (HideRowPositionsEvent) listener.getReceivedEvents().get(0);
                    assertEquals(1, event.getRowIndexes().length);
                    assertEquals(2, event.getRowIndexes()[0]);
                    assertEquals(1, event.getRowPositionRanges().size());
                    assertEquals(new Range(2, 3), event.getRowPositionRanges().iterator().next());
                });

        listener.clearReceivedEvents();

        // try to hide the already hidden row again
        this.rowHideShowLayer.doCommand(new HideRowByIndexCommand(2));

        assertEquals(0, listener.getEventsCount());
    }

    @Test
    public void shouldNotContainAlreadyHiddenRowIndexInEvent() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.rowHideShowLayer.addLayerListener(listener);

        shouldHideRowByIndex();

        listener.clearReceivedEvents();

        // try to hide multiple rows with the already hidden row 2
        this.rowHideShowLayer.doCommand(new HideRowByIndexCommand(1, 2, 3));

        assertAll("event fired with correct data",
                () -> assertEquals(1, listener.getEventsCount()),
                () -> assertTrue(listener.containsInstanceOf(HideRowPositionsEvent.class)),
                () -> {
                    HideRowPositionsEvent event = (HideRowPositionsEvent) listener.getReceivedEvents().get(0);
                    assertEquals(2, event.getRowIndexes().length);
                    assertEquals(1, event.getRowIndexes()[0]);
                    assertEquals(3, event.getRowIndexes()[1]);
                    // there is only one range [1,3] because column 2 is already
                    // hidden
                    assertEquals(1, event.getRowPositionRanges().size());
                    Iterator<Range> it = event.getRowPositionRanges().iterator();
                    assertEquals(new Range(1, 3), it.next());
                });
    }
}
