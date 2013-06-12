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
package org.eclipse.nebula.widgets.nattable.summaryrow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowUpdateEvent;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Adds a summary row at the end. Uses {@link ISummaryProvider} to calculate the summaries for all columns.
 * <p>This layer also adds the following labels:
 * <ol>
 *  <li>{@link SummaryRowLayer#DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX} + column index</li>
 * 	<li>{@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL} to all cells in the row</li>
 * </ol>
 *
 * Example: column with index 1 will have the DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 1 label applied.
 * Styling and {@link ISummaryProvider} can be hooked up to these labels.
 *
 * @see DefaultSummaryRowConfiguration
 */
public class SummaryRowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	public static final String DEFAULT_SUMMARY_ROW_CONFIG_LABEL = "SummaryRow"; //$NON-NLS-1$
	public static final String DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX = "SummaryColumn_"; //$NON-NLS-1$

	private final IConfigRegistry configRegistry;
	private int summaryRowHeight = DataLayer.DEFAULT_ROW_HEIGHT;

	/** Cache the calculated summary value, since its CPU intensive */
	protected Map<Integer, Object> summaryCache = new HashMap<Integer, Object>();
	/** Use a cache-copy which does not get cleared, as using an Entry type object with stale flag per instance 
	 *  would require traversal of full set of entries in <code>clearSummaryCache()</code>
	 */
	protected Map<Integer, Object> summaryCacheIncludingStaleValues = new HashMap<Integer, Object>();
	
	
	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry) {
		this(underlyingDataLayer, configRegistry, true);
	}

	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry, boolean autoConfigure) {
		super(underlyingDataLayer);
		this.configRegistry = configRegistry;
		if(autoConfigure){
			addConfiguration(new DefaultSummaryRowConfiguration());
		}
	}

	/**
	 * Calculates the summary for the column using the {@link ISummaryProvider} from the {@link IConfigRegistry}.
	 * In order to prevent the table from freezing (for large data sets), the summary is calculated in a separate Thread. While
	 * summary is being calculated {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} is returned.
	 * <p>
	 * NOTE: Since this is a {@link IUniqueIndexLayer} sitting close to the {@link DataLayer}, columnPosition == columnIndex
	 */
	@Override
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
		
		if (isSummaryRowPosition(rowPosition)) {
			
			final Object potentiallyStaleSummaryValue = getPotentiallyStaleSummaryFromCache(columnPosition);
			
			if (potentiallyStaleSummaryValue == null || !hasNonStaleSummaryFor(columnPosition)) {
				
				calculateNewSummaryValue(potentiallyStaleSummaryValue, columnPosition, rowPosition);
				
			}
			
			if (potentiallyStaleSummaryValue != null) {
				return potentiallyStaleSummaryValue;
			}
			
			return null;
		}
		return super.getDataValueByPosition(columnPosition, rowPosition);
	}

	private void calculateNewSummaryValue(final Object potentiallyStaleSummaryValue, final int columnPosition, final int rowPosition) {
		// Get the summary provider from the configuration registry
		LabelStack labelStack = getConfigLabelsByPosition(columnPosition, rowPosition);
		String[] configLabels = labelStack.getLabels().toArray(ArrayUtil.STRING_TYPE_ARRAY);
		
		final ISummaryProvider summaryProvider = configRegistry.getConfigAttribute(
				SummaryRowConfigAttributes.SUMMARY_PROVIDER, DisplayMode.NORMAL, configLabels);
		
		// If there is no Summary provider - skip processing
		if(summaryProvider == ISummaryProvider.NONE){
			return;
		}

		// Start thread to calculate summary
		new Thread() {
			@Override
			public void run() {
				Object summaryValue = calculateColumnSummary(columnPosition, summaryProvider);
				addToCache(columnPosition, summaryValue);
				if (!ObjectUtils.equals(potentiallyStaleSummaryValue, summaryValue)) {
					fireLayerEvent(new RowUpdateEvent(SummaryRowLayer.this, rowPosition));
				}
			}
		}.start();
	}

	private Object calculateColumnSummary(int columnIndex, ISummaryProvider summaryProvider) {
		Object summaryValue = null;
		if (summaryProvider != null) {
			summaryValue = summaryProvider.summarize(columnIndex);
		}
		return summaryValue;
	}
	
	public Object getSummaryFromCache(Integer columnIndex) {
		return summaryCache.get(columnIndex);
	}

	public Object getPotentiallyStaleSummaryFromCache(Integer columnIndex) {
		return summaryCacheIncludingStaleValues.get(columnIndex);
	}
	
	public boolean hasNonStaleSummaryFor(Integer columnIndex)
	{
		return summaryCache.containsKey(columnIndex);
	}
	
	protected boolean addToCache(Integer columnIndex, Object summaryValue) {
		Object oldSummaryValue = summaryCache.put(columnIndex, summaryValue);
		summaryCacheIncludingStaleValues.put(columnIndex,summaryValue);
		return !ObjectUtils.equals(oldSummaryValue, summaryValue);
	}

	protected void clearSummaryCache() {
		summaryCache.clear();
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof RowResizeCommand) {
			RowResizeCommand rowResizeCommand = (RowResizeCommand) command;
			if (isSummaryRowPosition(rowResizeCommand.getRowPosition())) {
				summaryRowHeight = rowResizeCommand.getNewHeight();
				return true;
			}
		}
		return super.doCommand(command);
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IVisualChangeEvent) {
			clearSummaryCache();
		}
		super.handleLayerEvent(event);
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return new LabelStack(
					DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + columnPosition, 
					DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
		}
		return super.getConfigLabelsByPosition(columnPosition, rowPosition);
	}

	@Override
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return new LayerCell(this, columnPosition, rowPosition);
		}
		return super.getCellByPosition(columnPosition, rowPosition);
	}

	@Override
	public int getHeight() {
		return super.getHeight() + getRowHeightByPosition(getSummaryRowPosition());
	}
	
	@Override
	public int getPreferredHeight() {
		return super.getPreferredHeight() + getRowHeightByPosition(getSummaryRowPosition());
	}
	
	@Override
	public int getRowCount() {
		return super.getRowCount() + 1;
	}

	@Override
	public int getPreferredRowCount() {
		return getRowCount();
	}

	@Override
	public int getRowIndexByPosition(int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return rowPosition;
		}
		return super.getRowIndexByPosition(rowPosition);
	}

	@Override
	public int getRowPositionByY(int y) {
		return LayerUtil.getRowPositionByY(this, y);
	}

	private boolean isSummaryRowPosition(int rowPosition) {
		return rowPosition == getSummaryRowPosition();
	}

	/**
	 * @return the position of the summary row. In most 
	 * cases <code>rowCount - 1</code>. 
	 */
	private int getSummaryRowPosition() {
		return getRowCount() - 1;
	}

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return summaryRowHeight;
		}
		return super.getRowHeightByPosition(rowPosition);
	}

	public int getRowPositionByIndex(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			return rowIndex;
		} else {
			return -1;
		}
	}

	public int getColumnPositionByIndex(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < getColumnCount()) {
			return columnIndex;
		} else {
			return -1;
		}
	}
}
