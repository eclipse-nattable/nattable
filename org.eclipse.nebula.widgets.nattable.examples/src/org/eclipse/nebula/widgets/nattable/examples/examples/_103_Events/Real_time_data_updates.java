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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.getRandomNumber;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.GlazedListsGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * Example to demonstrate rows being added/deleted as the backing data source is
 * updated.
 */
public class Real_time_data_updates extends AbstractNatExample {

    private int updatesPerSecond = 1;
    private final int defaultDatasetSize = 20;

    private ScheduledExecutorService scheduledThreadPool;
    private NatTable nattable;
    private EventList<RowDataFixture> eventList;

    public static void main(String[] args) {
        StandaloneNatExampleRunner.run(500, 700, new Real_time_data_updates());
    }

    @Override
    public String getDescription() {
        return "Grid demonstrates data being added/removed. You can experiment with different data set sizes and update speeds.\n"
                + "Select row by clicking on row header. Row selection is preserved when data is updated and sorted.";
    }

    /**
     * @see GlazedListsGridLayer to see the required stack setup. Basically the
     *      {@link DataLayer} needs to be wrapped up with a
     *      {@link GlazedListsEventLayer} and the backing list needs to be an
     *      {@link EventList}
     */
    @Override
    public Control createExampleControl(Composite parent) {
        this.eventList = GlazedLists.eventList(RowDataListFixture
                .getList(this.defaultDatasetSize));

        ConfigRegistry configRegistry = new ConfigRegistry();
        GlazedListsGridLayer<RowDataFixture> glazedListsGridLayer = new GlazedListsGridLayer<>(
                this.eventList, RowDataListFixture.getPropertyNames(),
                RowDataListFixture.getPropertyToLabelMap(), configRegistry);

        this.nattable = new NatTable(parent, glazedListsGridLayer, false);

        this.nattable.setConfigRegistry(configRegistry);
        this.nattable.addConfiguration(new DefaultNatTableStyleConfiguration());
        this.nattable.addConfiguration(new SingleClickSortConfiguration());

        SelectionLayer selectionLayer = glazedListsGridLayer
                .getBodyLayerStack()
                .getSelectionLayer();
        ListDataProvider<RowDataFixture> bodyDataProvider = glazedListsGridLayer
                .getBodyDataProvider();

        // Select complete rows
        RowOnlySelectionConfiguration<RowDataFixture> selectionConfig = new RowOnlySelectionConfiguration<>();
        selectionLayer.addConfiguration(selectionConfig);
        this.nattable.addConfiguration(new RowOnlySelectionBindings());

        // Preserve selection on updates and sort
        selectionLayer.setSelectionModel(new RowSelectionModel<>(
                selectionLayer, bodyDataProvider,
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));
        this.nattable.configure();

        // Layout widgets
        parent.setLayout(new GridLayout(1, true));
        this.nattable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                true));

        setupTextArea(parent);
        setupButtons(parent);

        return this.nattable;
    }

    @Override
    public void onStart() {
        this.scheduledThreadPool = Executors.newScheduledThreadPool(1);
        this.scheduledThreadPool.scheduleAtFixedRate(new ListEventsPublisher(),
                100L, 1000L / this.updatesPerSecond, MILLISECONDS);
    }

    @Override
    public void onStop() {
        this.scheduledThreadPool.shutdown();
        this.nattable.dispose();
    }

    /**
     * Utility class to make periodic modifications to the EventList. Each time
     * its run, it randomly either removes an element or adds one.
     */
    class ListEventsPublisher implements Runnable {
        @Override
        public void run() {

            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        Real_time_data_updates.this.eventList.getReadWriteLock().writeLock().lock();

                        // Delete a random entry
                        int elementIndexToRemove = getRandomNumber(Real_time_data_updates.this.eventList
                                .size() - 1);
                        elementIndexToRemove = elementIndexToRemove < 0 ? 0
                                : elementIndexToRemove;

                        // Random decision
                        boolean removeElements = (getRandomNumber(1000) % 2) == 0;

                        if (removeElements) {
                            final RowDataFixture remove = Real_time_data_updates.this.eventList
                                    .remove(elementIndexToRemove);
                            log("Removed record: " + remove.getSecurity_id());
                        } else {
                            final RowDataFixture added = RowDataFixture
                                    .getInstance("Added by test", ("AAA"));
                            Real_time_data_updates.this.eventList.add(added);
                            log("Added record: " + added.getSecurity_id());
                        }
                    } catch (Exception e) {
                        log("Ignoring exception: " + e.getMessage());
                    } finally {
                        Real_time_data_updates.this.eventList.getReadWriteLock().writeLock().unlock();
                    }
                }
            });
        }
    }

    /**
     * Adds the 'clear' button at the bottom
     */
    private void setupButtons(Composite parent) {
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, false));
        buttonComposite.setLayoutData(new GridData(GridData.FILL,
                GridData.FILL, true, false));

        new Label(buttonComposite, SWT.NONE).setText("Clear and add records");

        final Text clearText = new Text(buttonComposite, SWT.BORDER);
        clearText.setText(Integer.toString(this.defaultDatasetSize));
        clearText.setLayoutData(new GridData(100, 14));
        clearText.setSize(100, 20);

        Button clearButton = new Button(buttonComposite, SWT.PUSH);
        clearButton.setText("Apply");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final Integer newRecordCount = Integer.valueOf(clearText
                        .getText());
                try {
                    Real_time_data_updates.this.eventList.getReadWriteLock().writeLock().lock();
                    Real_time_data_updates.this.eventList.clear();
                    Real_time_data_updates.this.eventList.addAll(RowDataListFixture.getList(newRecordCount));
                } finally {
                    Real_time_data_updates.this.eventList.getReadWriteLock().writeLock().unlock();
                    log(">> List cleared. Added " + newRecordCount
                            + " new records.");
                }
            }
        });

        new Label(buttonComposite, SWT.PUSH).setText("Updates/sec");

        final Text updateSpeedText = new Text(buttonComposite, SWT.BORDER);
        updateSpeedText.setLayoutData(new GridData(100, 14));
        updateSpeedText.setText(Integer.toString(this.updatesPerSecond));

        Button updateSpeedButton = new Button(buttonComposite, SWT.PUSH);
        updateSpeedButton.setText("Apply");
        updateSpeedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Real_time_data_updates.this.updatesPerSecond = Integer.parseInt(updateSpeedText.getText());
                log(">> Update speed: " + Real_time_data_updates.this.updatesPerSecond);
                Real_time_data_updates.this.scheduledThreadPool.shutdownNow();
                onStart();
            }
        });

        final Button pauseButton = new Button(buttonComposite, SWT.PUSH);
        pauseButton.setLayoutData(new GridData(100, 20));
        pauseButton.setText("Pause updates");
        pauseButton.addSelectionListener(new SelectionAdapter() {
            boolean updatesPaused = false;

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (this.updatesPaused) {
                    log(">> Starting updates");
                    this.updatesPaused = false;
                    pauseButton.setText("Pause updates");
                    onStart();
                } else {
                    log(">> Pausing updates");
                    this.updatesPaused = true;
                    pauseButton.setText("Start updates");
                    Real_time_data_updates.this.scheduledThreadPool.shutdownNow();
                }
            }
        });

    }
}
