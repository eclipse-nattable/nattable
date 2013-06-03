/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.blink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.nebula.widgets.nattable.blink.command.BlinkTimerEnableCommandHandler;
import org.eclipse.nebula.widgets.nattable.blink.event.BlinkEvent;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyResolver;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.widgets.Display;

/**
 * Blinks cells when they are updated.
 * Returns blinking cell styles for the cells which have been updated.
 *
 * Every time its asked for config labels:
 * 	 Checks the UpdateEventsCache for changes to the cell
 * 	 If a cell is updated
 * 		The cell is tracked as 'blinking' and blinking config labels are returned
 *		A TimerTask is started which will stop the blinking after the blink period is over
 *
 * @param <T> Type of the Bean in the backing {@linkplain IDataProvider}
 */
public class BlinkLayer<T> extends AbstractLayerTransform implements IUniqueIndexLayer {

    private static class DefaultSchedulerLazyInitializer {
        private final static ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    }
    
	private final IUniqueIndexLayer dataLayer;
	private final IRowDataProvider<T> rowDataProvider;
	private final IConfigRegistry configRegistry;
	private final IRowIdAccessor<T> rowIdAccessor;
	private final IColumnPropertyResolver columnPropertyResolver;
	private final ScheduledExecutorService scheduler;

	protected boolean blinkingEnabled = true;

	/** Cache all the update events allowing the layer to track what got updated */
	private final UpdateEventsCache<T> updateEventsCache;

	/** Duration of a single blink */
	private int blinkDurationInMilis = 1000;

	/** Track the updates which are currently blinking */
	Map<String, PropertyUpdateEvent<T>> blinkingUpdates = new HashMap<String, PropertyUpdateEvent<T>>();

	/** Track the blinking tasks which are currently running*/
	Map<String, ScheduledFuture<?>> blinkingTasks = new HashMap<String, ScheduledFuture<?>>();

	public BlinkLayer(IUniqueIndexLayer dataLayer,
			IRowDataProvider<T> listDataProvider,
			IRowIdAccessor<T> rowIdAccessor,
			IColumnPropertyResolver columnPropertyResolver,
			IConfigRegistry configRegistry) {
		this(dataLayer, listDataProvider, rowIdAccessor, columnPropertyResolver, configRegistry, false);
	}

	public BlinkLayer(IUniqueIndexLayer dataLayer,
			IRowDataProvider<T> listDataProvider,
			IRowIdAccessor<T> rowIdAccessor,
			IColumnPropertyResolver columnPropertyResolver,
			IConfigRegistry configRegistry,
			boolean triggerBlinkOnRowUpdate) {
	    this(dataLayer, listDataProvider, rowIdAccessor, columnPropertyResolver, configRegistry, triggerBlinkOnRowUpdate, DefaultSchedulerLazyInitializer.SCHEDULER);
	}
	
    public BlinkLayer(IUniqueIndexLayer dataLayer,
            IRowDataProvider<T> listDataProvider,
            IRowIdAccessor<T> rowIdAccessor,
            IColumnPropertyResolver columnPropertyResolver,
            IConfigRegistry configRegistry,
            boolean triggerBlinkOnRowUpdate,
            ScheduledExecutorService scheduler) {
		super(dataLayer);
		this.dataLayer = dataLayer;
		this.rowDataProvider = listDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		this.columnPropertyResolver = columnPropertyResolver;
		this.configRegistry = configRegistry;
		this.scheduler = scheduler;
		this.updateEventsCache = new UpdateEventsCache<T>(rowIdAccessor,
				triggerBlinkOnRowUpdate ? new RowKeyStrategyImpl() : new CellKeyStrategyImpl(),
		        scheduler);
		
		registerCommandHandler(new BlinkTimerEnableCommandHandler(this));
	}
    
    @Override
    public void dispose() {
    	super.dispose();
    	
    	scheduler.shutdown();
    }
	
	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		if (!blinkingEnabled) {
			return getUnderlyingLayer().getConfigLabelsByPosition(columnPosition, rowPosition);
		}
		
		ILayerCell cell = underlyingLayer.getCellByPosition(columnPosition, rowPosition);

		int columnIndex = getUnderlyingLayer().getColumnIndexByPosition(columnPosition);
		String columnProperty = columnPropertyResolver.getColumnProperty(columnIndex);

		int rowIndex = getUnderlyingLayer().getRowIndexByPosition(rowPosition);
		String rowId = rowIdAccessor.getRowId(rowDataProvider.getRowObject(rowIndex)).toString();
		
		String key = updateEventsCache.getKey(columnProperty, rowId);

		LabelStack underlyingLabelStack = getUnderlyingLayer().getConfigLabelsByPosition(columnPosition, rowPosition);

		// Cell has been updated
		if (updateEventsCache.isUpdated(key)) {
			PropertyUpdateEvent<T> event = updateEventsCache.getEvent(key);

			// Old update in middle of a blink - cancel it
			ScheduledFuture<?> scheduledFuture = blinkingTasks.remove(key);
			blinkingUpdates.remove(key);
			if (scheduledFuture != null) {
				scheduledFuture.cancel(true);
			}

			LabelStack blinkingConfigTypes = resolveConfigTypes(cell, event.getOldValue(), event.getNewValue());

			// start blinking cell
			if (blinkingConfigTypes != null) {
				Runnable stopBlinkTask = getStopBlinkTask(key, this);
				blinkingUpdates.put(key, event);
				updateEventsCache.remove(key);
				blinkingTasks.put(key, scheduler.schedule(stopBlinkTask, blinkDurationInMilis, TimeUnit.MILLISECONDS));
				return blinkingConfigTypes;
			} else {
				return new LabelStack();
			}
		}
		// Previous blink timer is still running
		if (blinkingUpdates.containsKey(key)) {
			PropertyUpdateEvent<T> event = blinkingUpdates.get(key);
			return resolveConfigTypes(cell, event.getOldValue(), event.getNewValue());

		}
		return underlyingLabelStack;
	}

	private IBlinkingCellResolver getBlinkingCellResolver(List<String> configTypes) {
		return configRegistry.getConfigAttribute(BlinkConfigAttributes.BLINK_RESOLVER, DisplayMode.NORMAL, configTypes);
	}

	/**
	 * Find the {@link IBlinkingCellResolver} from the {@link ConfigRegistry}.
	 * Use this to find the config types associated with a blinking cell.
	 * @param cell the cell
	 * @param oldValue the old value
	 * @param newValue the new value
	 * @return a LabelStack containing resolved config types associated with the cell
	 */
	public LabelStack resolveConfigTypes(ILayerCell cell, Object oldValue, Object newValue) {
		// Acquire default config types for the coordinate. Use these to search for the associated resolver.
		LabelStack underlyingLabelStack = underlyingLayer.getConfigLabelsByPosition(cell.getColumnIndex(), cell.getRowIndex());

		String[] blinkConfigTypes = null;
		IBlinkingCellResolver resolver = getBlinkingCellResolver(underlyingLabelStack.getLabels());
		if (resolver != null) {
		    blinkConfigTypes = resolver.resolve(cell, configRegistry, oldValue, newValue);
		}
		if (!ArrayUtils.isEmpty(blinkConfigTypes)) { //blinkConfigTypes != null && blinkConfigTypes.length > 0
			return new LabelStack(blinkConfigTypes);
		}
		return underlyingLabelStack;
	}

	/**
	 * Stops the cell from blinking at the end of the blinking period.
	 */
    private Runnable getStopBlinkTask(final String key, final ILayer layer) {

		return new Runnable() {
			public void run() {
				
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						
						blinkingUpdates.remove(key);
						blinkingTasks.remove(key);
						fireLayerEvent(new BlinkEvent(layer));
					}
				});
			}
		};

	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleLayerEvent(ILayerEvent event) {
	    if (blinkingEnabled) {
    		if (event instanceof PropertyUpdateEvent) {
    			updateEventsCache.put((PropertyUpdateEvent<T>) event);
    		}
	    }
		super.handleLayerEvent(event);
	}

	public void setBlinkingEnabled(boolean enabled) {
		this.blinkingEnabled = enabled;
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return dataLayer.getColumnPositionByIndex(columnIndex);
	}

	public int getRowPositionByIndex(int rowIndex) {
		return dataLayer.getRowPositionByIndex(rowIndex);
	}

	public void setBlinkDurationInMilis(int blinkDurationInMilis) {
		this.blinkDurationInMilis = blinkDurationInMilis;
	}

}
