/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.examples.examples._103_Events;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.ColumnHeaders;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.PricingDataBean;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.PricingDataBeanGenerator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.ColumnStructureUpdatesExampleGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class ColumnStructureUpdatesGridExample extends AbstractNatExample {

    public static void main(String[] args) {
        StandaloneNatExampleRunner.run(new ColumnStructureUpdatesGridExample());
    }

    private EventList<PricingDataBean> rowObjectsGlazedList;

    /**
     * NOTE - Glazed {@link EventList} class is thread ready but not thread
     * safe.
     */
    @Override
    public Control createExampleControl(Composite parent) {

        EventList<PricingDataBean> eventList = GlazedLists
                .eventList(PricingDataBeanGenerator.getData(10));
        this.rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
        Map<String, String> propertyToLabelMap = populateColHeaderPropertiesToLabelsMap();
        String[] propertyNames = propertyToLabelMap.keySet().toArray(
                ArrayUtil.STRING_TYPE_ARRAY);

        ConfigRegistry configRegistry = new ConfigRegistry();
        final ColumnStructureUpdatesExampleGridLayer<PricingDataBean> glazedListsGridLayer = new ColumnStructureUpdatesExampleGridLayer<>(
                this.rowObjectsGlazedList, propertyNames, propertyToLabelMap,
                configRegistry, true);
        final NatTable natTable = new NatTable(parent, glazedListsGridLayer,
                false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.setConfigRegistry(configRegistry);
        natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, "ODD_BODY");
        natTable.getConfigRegistry().registerConfigAttribute(
                EditConfigAttributes.CELL_EDITABLE_RULE,
                IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, "EVEN_BODY");

        natTable.configure();
        glazedListsGridLayer.bodyDataProvider.setColumnCount(2);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        Button button = new Button(composite, SWT.PUSH);
        button.setText("Clear list, add 6 items, Change column count");
        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.getReadWriteLock().writeLock().lock();
                try {
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.clear();
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    glazedListsGridLayer.bodyDataProvider.setColumnCount(8);
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.add(new PricingDataBean());
                } finally {
                    ColumnStructureUpdatesGridExample.this.rowObjectsGlazedList.getReadWriteLock().writeLock()
                            .unlock();
                }
            }
        });
        return natTable;
    }

    @Override
    public String getDescription() {
        return "Column structure updates";
    }

    private Map<String, String> populateColHeaderPropertiesToLabelsMap() {
        Map<String, String> propertyToLabelMap = new HashMap<>();
        ColumnHeaders[] columnHeaders = ColumnHeaders.values();
        for (int i = 0; i < columnHeaders.length; i++) {
            propertyToLabelMap.put(columnHeaders[i].getProperty(),
                    columnHeaders[i].getLabel());
        }
        return propertyToLabelMap;
    }
}
