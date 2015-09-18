/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._120_Selection;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.SelectionExampleGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Selection_events extends AbstractNatExample {

    private NatTable nattable;
    private SelectionExampleGridLayer gridLayer;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new Selection_events());
    }

    @Override
    public String getDescription() {
        return "Row, row header, column, column header and cell selection is built into the table by default. "
                + "Events are fired by the tables when any selection occurs. These can be hooked up "
                + "to trigger business actions as required.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        this.gridLayer = new SelectionExampleGridLayer();
        this.nattable = new NatTable(parent, this.gridLayer, false);

        this.nattable.addConfiguration(new DefaultNatTableStyleConfiguration());
        this.nattable.addConfiguration(new HeaderMenuConfiguration(this.nattable));
        this.nattable.addConfiguration(new DefaultSelectionStyleConfiguration());

        // Custom selection configuration
        SelectionLayer selectionLayer = this.gridLayer.getSelectionLayer();
        selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(
                selectionLayer, this.gridLayer.getBodyDataProvider(),
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));

        selectionLayer
                .addConfiguration(new RowOnlySelectionConfiguration<RowDataFixture>());
        this.nattable.addConfiguration(new RowOnlySelectionBindings());

        this.nattable.configure();

        addCustomSelectionBehaviour();

        // Layout widgets
        parent.setLayout(new GridLayout(1, true));
        this.nattable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                true));
        setupTextArea(parent);

        return this.nattable;
    }

    private void addCustomSelectionBehaviour() {
        this.nattable.addLayerListener(new ILayerListener() {

            // Default selection behavior selects cells by default.
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellSelectionEvent) {
                    CellSelectionEvent cellEvent = (CellSelectionEvent) event;
                    log("Selected cell: ["
                            + cellEvent.getRowPosition()
                            + ", "
                            + cellEvent.getColumnPosition()
                            + "], "
                            + Selection_events.this.nattable.getDataValueByPosition(
                                    cellEvent.getColumnPosition(),
                                    cellEvent.getRowPosition()));
                }
            }
        });

        // Events are fired whenever selection occurs. These can be use to
        // trigger
        // external actions as required. Also you can use this data to pull out
        // the backing data from the IRowDataProvider. Example:
        // rowDataProvider.getRowObject(natTable.getRowIndexByPosition(selectedRowPosition));
        this.nattable.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof RowSelectionEvent) {
                    RowSelectionEvent rowEvent = (RowSelectionEvent) event;
                    log("Selected Row: "
                            + ObjectUtils.toString(rowEvent
                                    .getRowPositionRanges()));
                }
            }
        });

        this.nattable.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof ColumnSelectionEvent) {
                    ColumnSelectionEvent columnEvent = (ColumnSelectionEvent) event;
                    log("Selected Column: "
                            + columnEvent.getColumnPositionRanges());
                }
            }
        });
    }

}
