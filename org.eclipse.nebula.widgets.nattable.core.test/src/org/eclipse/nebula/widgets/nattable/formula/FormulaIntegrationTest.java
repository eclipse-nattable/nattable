/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaEvaluationCommand;
import org.eclipse.nebula.widgets.nattable.formula.config.DefaultFormulaConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormulaIntegrationTest {

    IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new Object[10][10]);
    FormulaDataProvider formulaDataProvider = new FormulaDataProvider(this.dataProvider);

    DummyGridLayerStack gridLayerStack = new DummyGridLayerStack(this.formulaDataProvider);
    NatTableFixture natTable = new NatTableFixture(this.gridLayerStack, false);

    LayerListenerFixture listenerFixture = new LayerListenerFixture();

    @BeforeEach
    public void setup() {
        this.natTable.addConfiguration(new DefaultFormulaConfiguration(
                this.formulaDataProvider,
                this.gridLayerStack.getBodyLayer().getSelectionLayer(),
                new InternalCellClipboard()));
        this.natTable.configure();

        this.natTable.addLayerListener(this.listenerFixture);

        // enable caching
        this.formulaDataProvider.configureCaching(this.gridLayerStack.getBodyDataLayer());

        this.dataProvider.setDataValue(0, 0, "5");
        this.dataProvider.setDataValue(1, 0, "3");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        this.dataProvider.setDataValue(0, 2, "5");
        this.dataProvider.setDataValue(0, 3, "5");
        this.dataProvider.setDataValue(0, 4, "=SUM(A3:A4)");
        this.dataProvider.setDataValue(1, 2, "3");
        this.dataProvider.setDataValue(1, 3, "3");
        this.dataProvider.setDataValue(1, 4, "=SUM(B3:B4)");

        this.dataProvider.setDataValue(2, 4, "=SUM(A5;B5)");
    }

    @Test
    public void testResultCaching() throws InterruptedException {
        assertNull(this.natTable.getDataValueByPosition(3, 1));

        // wait until calculation is processed
        Thread.sleep(50);

        assertEquals(new BigDecimal("15"), this.natTable.getDataValueByPosition(3, 1));

        ILayerEvent receivedEvent = this.listenerFixture.getReceivedEvent(CellVisualChangeEvent.class);
        assertNotNull(receivedEvent);
    }

    @Test
    public void testResultCachingDisabled() throws InterruptedException {
        this.natTable.doCommand(new DisableFormulaCachingCommand());

        assertNotNull(this.natTable.getDataValueByPosition(3, 1));
        assertEquals(new BigDecimal("15"), this.natTable.getDataValueByPosition(3, 1));

        ILayerEvent receivedEvent = this.listenerFixture.getReceivedEvent(CellVisualChangeEvent.class);
        assertNull(receivedEvent);

        this.natTable.doCommand(new EnableFormulaCachingCommand());

        assertNull(this.natTable.getDataValueByPosition(3, 1));

        // wait until calculation is processed
        Thread.sleep(50);

        assertEquals(new BigDecimal("15"), this.natTable.getDataValueByPosition(3, 1));

        receivedEvent = this.listenerFixture.getReceivedEvent(CellVisualChangeEvent.class);
        assertNotNull(receivedEvent);
    }

    @Test
    public void testCacheUpdate() throws InterruptedException {
        assertNull(this.natTable.getDataValueByPosition(3, 5));

        // wait until calculation is processed
        Thread.sleep(50);

        assertEquals(new BigDecimal("16"), this.natTable.getDataValueByPosition(3, 5));

        ILayerEvent receivedEvent = this.listenerFixture.getReceivedEvent(CellVisualChangeEvent.class);
        assertNotNull(receivedEvent);

        // update a value in the first sum
        ((DataLayer) this.gridLayerStack.getBodyDataLayer()).setDataValue(0, 2, 20);

        // right after the update we still get the old value from the cache
        assertEquals(new BigDecimal("16"), this.natTable.getDataValueByPosition(3, 5));

        // wait until calculation is processed
        Thread.sleep(50);

        assertEquals(new BigDecimal("31"), this.natTable.getDataValueByPosition(3, 5));

        assertEquals(2, this.listenerFixture.getEventsCount());
    }

    @Test
    public void testDisableFormulaResolution() throws InterruptedException {
        this.natTable.doCommand(new DisableFormulaEvaluationCommand());

        assertNotNull(this.natTable.getDataValueByPosition(3, 1));
        assertEquals("=A1*B1", this.natTable.getDataValueByPosition(3, 1));

        this.natTable.doCommand(new EnableFormulaEvaluationCommand());

        assertNull(this.natTable.getDataValueByPosition(3, 1));

        // wait until calculation is processed
        Thread.sleep(50);

        assertEquals(new BigDecimal("15"), this.natTable.getDataValueByPosition(3, 1));
    }
}
