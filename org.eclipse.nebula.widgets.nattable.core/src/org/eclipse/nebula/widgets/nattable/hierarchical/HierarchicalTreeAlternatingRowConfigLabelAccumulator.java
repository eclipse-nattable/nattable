/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * Specialization of the {@link AlternatingRowConfigLabelAccumulator} that
 * calculates the even/odd row labels in a hierarchical tree by inspecting the
 * row spanning of the first level node. For better performance the calculation
 * results are cached. As the cache needs to be cleared on structural changes,
 * this class also implements the {@link ILayerListener} to clear the cache
 * automatically on {@link RowStructuralChangeEvent}s if registered on the given
 * layer via {@link ILayer#addLayerListener(ILayerListener)}.
 *
 * @since 1.6
 */
public class HierarchicalTreeAlternatingRowConfigLabelAccumulator
        extends AlternatingRowConfigLabelAccumulator
        implements ILayerListener, ILayerCommandHandler<DisposeResourcesCommand> {

    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "HierarchicalTreeAlternatingRowConfigLabelAccumulator"); //$NON-NLS-1$
        }
    });
    private Future<Void> future = null;

    private Map<Integer, String> rowLabelCache = new ConcurrentHashMap<Integer, String>();

    /**
     *
     * @param layer
     *            The {@link ILayer} that is used to determine the row spanning
     *            in the first column. Should be the HierarchicalTreeLayer.
     */
    public HierarchicalTreeAlternatingRowConfigLabelAccumulator(ILayer layer) {
        super(layer);
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        String label = getRowLabel(rowPosition);
        if (label == null) {
            if (this.future == null || (this.future.isCancelled() || this.future.isDone())) {
                calculateLabels();
            }

            if (rowPosition < 100) {
                // for the first few rows we wait a moment to give the
                // background calculation time to proceed to avoid flickering at
                // the top of the table. we do not wait in any case as otherwise
                // sorting would show a bad performance in the middle or the end
                // of the table
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // nothing to do here
                }
                label = getRowLabel(rowPosition);

                if (label != null) {
                    configLabels.addLabel(label);
                }
            }
        } else {
            configLabels.addLabel(label);
        }
    }

    /**
     * Returns the row label for the given row position. Calculates the row
     * label based on the logically corresponding origin row dependent on the
     * row spanning.
     *
     * @param rowPosition
     *            The row position for which the row label is requested.
     * @return The row label for the given row position based on the origin row.
     */
    protected String getRowLabel(int rowPosition) {
        ILayerCell cell = this.layer.getCellByPosition(0, rowPosition);
        return this.rowLabelCache.get(cell.getOriginRowPosition());
    }

    /**
     * Triggers a new background thread for calculation of the row label cache.
     */
    public void calculateLabels() {
        // TODO Java 8 - use CompletableFuture
        this.future = this.executor.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                String lastKnownLabel = EVEN_ROW_CONFIG_TYPE;
                HierarchicalTreeAlternatingRowConfigLabelAccumulator.this.rowLabelCache.put(0, lastKnownLabel);

                for (int row = 0; row < HierarchicalTreeAlternatingRowConfigLabelAccumulator.this.layer.getRowCount();) {

                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    // determine the next row after the last known based
                    // on spanning
                    ILayerCell lastKnownCell = HierarchicalTreeAlternatingRowConfigLabelAccumulator.this.layer.getCellByPosition(0, row);
                    if (lastKnownCell != null) {
                        row = lastKnownCell.getOriginRowPosition() + lastKnownCell.getRowSpan();

                        lastKnownLabel = (lastKnownLabel == ODD_ROW_CONFIG_TYPE) ? EVEN_ROW_CONFIG_TYPE : ODD_ROW_CONFIG_TYPE;

                        HierarchicalTreeAlternatingRowConfigLabelAccumulator.this.rowLabelCache.put(row, lastKnownLabel);
                    }
                }

                // once the calculation is done we trigger a repaint to ensure
                // the correct alternate colors are rendered
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        HierarchicalTreeAlternatingRowConfigLabelAccumulator.this.layer.fireLayerEvent(
                                new VisualRefreshEvent(HierarchicalTreeAlternatingRowConfigLabelAccumulator.this.layer));
                    }
                });

                return null;
            }

        });
    }

    /**
     * Clears the local cache of calculated row position to label mappings.
     */
    public void clearCache() {
        if (this.future != null && (!this.future.isCancelled() || !this.future.isDone())) {
            // cancel a already running process
            this.future.cancel(true);
            // ensure to wait until the current running future is terminated
            // before starting a new calculation so there are no concurrent
            // write operations when starting the new calculation
            try {
                this.future.get();
            } catch (CancellationException e) {
                // nothing to do here
            } catch (InterruptedException e) {
                // nothing to do here
            } catch (ExecutionException e) {
                // nothing to do here
            }
        }
        this.rowLabelCache.clear();
        // trigger calculation
        calculateLabels();
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        // if there are structural changes to rows that are not related to
        // resizing, we need to clear the cache
        if ((event instanceof RowStructuralChangeEvent && !(event instanceof RowResizeEvent))
                || event instanceof RowStructuralRefreshEvent) {
            clearCache();
        }
    }

    @Override
    public boolean doCommand(ILayer targetLayer, DisposeResourcesCommand command) {
        if (!this.executor.isShutdown()) {
            // simply shutdown the executor, no need to await termination on
            // dispose
            this.executor.shutdownNow();
        }
        // the DisposeResourcesCommand should not be consumed
        return false;
    }

    @Override
    public Class<DisposeResourcesCommand> getCommandClass() {
        return DisposeResourcesCommand.class;
    }
}
