/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.FixedSummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummationSummaryProvider;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TransformedList;

public class GroupByDataLayerSummaryRowConcurrencyTest {

    private FixedSummaryRowLayer summaryRowLayer;
    private int calcCount = 0;

    class Value {
        int value;

        Value(int value) {
            this.value = value;
        }
    }

    @Before
    public void setup() {
        List<Value> values = new ArrayList<Value>();
        values.add(new Value(1));
        values.add(new Value(2));
        values.add(new Value(3));
        values.add(new Value(4));
        values.add(new Value(5));
        values.add(new Value(6));
        values.add(new Value(7));
        values.add(new Value(8));
        values.add(new Value(9));
        values.add(new Value(10));

        IColumnAccessor<Value> columnAccessor = new IColumnAccessor<Value>() {

            @Override
            public Object getDataValue(Value rowObject, int columnIndex) {
                if (columnIndex % 2 == 0) {
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return rowObject.value;
            }

            @Override
            public void setDataValue(Value rowObject, int columnIndex, Object newValue) {}

            @Override
            public int getColumnCount() {
                return 10;
            }
        };

        EventList<Value> eventList = GlazedLists.eventList(values);
        TransformedList<Value, Value> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

        ConfigRegistry configRegistry = new ConfigRegistry();

        final GroupByDataLayer<Value> dataLayer = new GroupByDataLayer<Value>(new GroupByModel(), eventList, columnAccessor);
        // DataLayer dataLayer = new DataLayer(dataProvider);
        GlazedListsEventLayer<Value> glazedListsEventLayer = new GlazedListsEventLayer<Value>(dataLayer, rowObjectsGlazedList);
        DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(glazedListsEventLayer);

        this.summaryRowLayer = new FixedSummaryRowLayer(dataLayer, bodyLayerStack, configRegistry, false);
        this.summaryRowLayer.setHorizontalCompositeDependency(false);

        CompositeLayer composite = new CompositeLayer(1, 2);
        composite.setChildLayer("SUMMARY", this.summaryRowLayer, 0, 0);
        composite.setChildLayer(GridRegion.BODY, bodyLayerStack, 0, 1);

        NatTable natTable = new NatTableFixture(composite, false);
        natTable.addConfiguration(new DefaultSummaryRowConfiguration() {
            @Override
            protected void addSummaryProviderConfig(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        SummaryRowConfigAttributes.SUMMARY_PROVIDER,
                        new SummationSummaryProvider(dataLayer.getDataProvider(), false),
                        DisplayMode.NORMAL,
                        SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
            }
        });
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.setConfigRegistry(configRegistry);
        natTable.configure();
    }

    // summary value == 55

    @Test
    public void shouldCorrectlyCalculateSummaryValues() {

        this.summaryRowLayer.addLayerListener(new ILayerListener() {

            @Override
            public synchronized void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellVisualChangeEvent) {
                    GroupByDataLayerSummaryRowConcurrencyTest.this.calcCount++;
                }
            }
        });

        assertNull(this.summaryRowLayer.getDataValueByPosition(0, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(1, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(2, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(3, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(4, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(5, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(6, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(7, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(8, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(9, 0));
        assertNull(this.summaryRowLayer.getDataValueByPosition(10, 0));

        while (this.calcCount < 11) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(0, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(1, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(2, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(3, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(4, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(5, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(6, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(7, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(8, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(9, 0));
        assertEquals(55.0, this.summaryRowLayer.getDataValueByPosition(10, 0));
    }
}
