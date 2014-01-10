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

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
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
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.nebula.widgets.nattable.util.CalculatedValueCache;
import org.eclipse.nebula.widgets.nattable.util.ICalculator;

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

	/**
	 * Label that gets attached to the LabelStack for every cell in the summary row.
	 */
	public static final String DEFAULT_SUMMARY_ROW_CONFIG_LABEL = "SummaryRow"; //$NON-NLS-1$
	/**
	 * Prefix of the labels that get attached to cells in the summary row. 
	 * The complete label will consist of this prefix and the column index at the end of
	 * the label. This way every cell in the summary row can be accessed directly via
	 * label mechanism.
	 */
	public static final String DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX = "SummaryColumn_"; //$NON-NLS-1$
	/**
	 * The ConfigRegistry for retrieving the ISummaryProvider per column.
	 */
	private final IConfigRegistry configRegistry;
	/**
	 * The row height of the summary row. As the summary row itself is not connected to a
	 * DataLayer, this layer needs to store the height for itself.
	 */
	private int summaryRowHeight = DataLayer.DEFAULT_ROW_HEIGHT;
	/**
	 * The value cache that contains the summary values and performs summary calculation in 
	 * background processes if necessary.
	 */
	private CalculatedValueCache valueCache;
	
	/**
	 * Creates a SummaryRowLayer on top of the given underlying layer. It uses smooth value updates
	 * as default.
	 * <p>
	 * Note: This constructor will create the SummaryRowLayer by using the default configuration.
	 * 		 The default configuration doesn't fit the needs so you usually will use your custom
	 * 		 summary row configuration.
	 * 
	 * @param underlyingDataLayer The underlying layer on which the SummaryRowLayer should be build.
	 * @param configRegistry The ConfigRegistry for retrieving the ISummaryProvider per column.
	 * 
	 * @see DefaultSummaryRowConfiguration
	 */
	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry) {
		this(underlyingDataLayer, configRegistry, true, true);
	}

	/**
	 * Creates a SummaryRowLayer on top of the given underlying layer. It uses smooth value updates
	 * as default.
	 * <p>
	 * Note: This constructor will create the SummaryRowLayer by using the default configuration
	 * 		 if the autoConfig parameter is set to <code>true</code>.
	 * 		 The default configuration doesn't fit the needs so you usually will use your custom
	 * 		 summary row configuration. When using a custom configuration you should use this
	 * 		 constructor setting autoConfig to <code>false</code>. Otherwise you might get strange
	 * 		 behaviour as the default configuration will be set additionally to your configuration.
	 * 
	 * @param underlyingDataLayer The underlying layer on which the SummaryRowLayer should be build.
	 * @param configRegistry The ConfigRegistry for retrieving the ISummaryProvider per column.
	 * @param autoConfigure <code>true</code> to use the DefaultSummaryRowConfiguration,
	 * 			<code>false</code> if a custom configuration will be set after the creation.
	 * 
	 * @see DefaultSummaryRowConfiguration
	 */
	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry, boolean autoConfigure) {
		this(underlyingDataLayer, configRegistry, true, autoConfigure);
	}

	/**
	 * Creates a SummaryRowLayer on top of the given underlying layer.
	 * <p>
	 * Note: This constructor will create the SummaryRowLayer by using the default configuration
	 * 		 if the autoConfig parameter is set to <code>true</code>.
	 * 		 The default configuration doesn't fit the needs so you usually will use your custom
	 * 		 summary row configuration. When using a custom configuration you should use this
	 * 		 constructor setting autoConfig to <code>false</code>. Otherwise you might get strange
	 * 		 behaviour as the default configuration will be set additionally to your configuration.
	 * 
	 * @param underlyingDataLayer The underlying layer on which the SummaryRowLayer should be build.
	 * @param configRegistry The ConfigRegistry for retrieving the ISummaryProvider per column.
	 * @param smoothUpdates <code>true</code> if the summary value updates should be performed smoothly,
	 * 			<code>false</code> if on re-calculation the value should be immediately shown as not calculated.
	 * @param autoConfigure <code>true</code> to use the DefaultSummaryRowConfiguration,
	 * 			<code>false</code> if a custom configuration will be set after the creation.
	 * 
	 * @see DefaultSummaryRowConfiguration
	 */
	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry, 
			boolean smoothUpdates, boolean autoConfigure) {
		
		super(underlyingDataLayer);
		this.configRegistry = configRegistry;
		
		this.valueCache = new CalculatedValueCache(this, true, false, smoothUpdates);
		
		if (autoConfigure){
			addConfiguration(new DefaultSummaryRowConfiguration());
		}
	}

	/**
	 * Calculates the summary for the column using the {@link ISummaryProvider} from the {@link IConfigRegistry}.
	 * In order to prevent the table from freezing (for large data sets), the summary is calculated in a separate 
	 * Thread. While summary is being calculated {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} is returned.
	 * <p>
	 * NOTE: Since this is a {@link IUniqueIndexLayer} sitting close to the {@link DataLayer}, 
	 * 		 columnPosition == columnIndex
	 */
	@Override
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) { 
		if (isSummaryRowPosition(rowPosition)) {
			return calculateNewSummaryValue(columnPosition, true);
		}
		return super.getDataValueByPosition(columnPosition, rowPosition);
	}

	/**
	 * Asks the local CalculatedValueCache for summary value. Is also used to trigger calculation
	 * prior printing or exporting to ensure that the values are calculated. This is necessary because
	 * usually the calculation will be done when the data value is requested the first time.
	 * @param columnPosition The column position of the requested summary value.
	 * @param calculateInBackground <code>true</code> if the summary value calculation should be
	 * 			processed in a background thread, <code>false</code> if the calculation should be
	 * 			processed in the current thread.
	 * @return The value that is calculated in the CalculatedValueCache or a temporary value
	 * 			if the calculation process is started in a background thread.
	 */
	private Object calculateNewSummaryValue(final int columnPosition, boolean calculateInBackground) {
		
		//as we only care about one row in the value cache, the real row position doesn't matter
		//in fact using the real summary row position would cause issues if rows are added or
		//removed because the value cache takes into account column and row position for caching.
		return this.valueCache.getCalculatedValue(columnPosition, getSummaryRowPosition(), calculateInBackground, 
				new ICalculator() {
			
			@Override
			public Object executeCalculation() {
				LabelStack labelStack = getConfigLabelsByPosition(columnPosition, getSummaryRowPosition());
				String[] configLabels = labelStack.getLabels().toArray(ArrayUtil.STRING_TYPE_ARRAY);
				
				final ISummaryProvider summaryProvider = configRegistry.getConfigAttribute(
						SummaryRowConfigAttributes.SUMMARY_PROVIDER, DisplayMode.NORMAL, configLabels);
				
				// If there is no Summary provider - skip processing
				if(summaryProvider == ISummaryProvider.NONE || summaryProvider == null){
					return null;
				}

				return summaryProvider.summarize(columnPosition);
			}
		});
	}
	
	/**
	 * @param rowPosition The row position to check.
	 * @return <code>true</code> if the given row position is the summary row position.
	 */
	private boolean isSummaryRowPosition(int rowPosition) {
		return rowPosition == getSummaryRowPosition();
	}

	/**
	 * @return The position of the summary row. 
	 * 			In most cases <code>rowCount - 1</code>. 
	 */
	private int getSummaryRowPosition() {
		return getRowCount() - 1;
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
		else if (command instanceof CalculateSummaryRowValuesCommand) {
			for (int i = 0; i < getColumnCount(); i++) {
				calculateNewSummaryValue(i, false);
			}
			//we do not return true here, as there might be other layers involved in 
			//the composition that also need to calculate the summary values immediately
		}
		else if (command instanceof DisposeResourcesCommand) {
			this.valueCache.dispose();
		}
		return super.doCommand(command);
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IVisualChangeEvent) {
			clearCache();
		}
		super.handleLayerEvent(event);
	}
	
	/**
	 * Clear the internal cache to trigger new calculations.
	 * <p>
	 * Usually it is not necessary to call this method manually. But for certain use cases
	 * it might be useful, e.g. changing the summary provider implementation at runtime.
	 * 
	 * @see CalculatedValueCache#clearCache()
	 */
	public void clearCache() {
		this.valueCache.clearCache();
	}
	
	/**
	 * Clears all values in the internal cache to trigger new calculations. This will also
	 * clear all values in the cache copy and will result in rendering like there was never
	 * a summary value calculated before.
	 * <p>
	 * Usually it is not necessary to call this method manually. But for certain use cases
	 * it might be useful, e.g. changing the summary provider implementation at runtime.
	 * 
	 * @see CalculatedValueCache#killCache()
	 */
	public void killCache() {
		this.valueCache.killCache();
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			//create a new LabelStack that takes the config labels into account
			LabelStack labelStack = new LabelStack();
			if (getConfigLabelAccumulator() != null) {
				getConfigLabelAccumulator().accumulateConfigLabels(labelStack, columnPosition, rowPosition);
			}
			labelStack.addLabelOnTop(DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
			labelStack.addLabelOnTop(DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + columnPosition);
			return labelStack;
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

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return summaryRowHeight;
		}
		return super.getRowHeightByPosition(rowPosition);
	}

	@Override
	public int getRowPositionByIndex(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			return rowIndex;
		} else {
			return -1;
		}
	}

	@Override
	public int getColumnPositionByIndex(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < getColumnCount()) {
			return columnIndex;
		} else {
			return -1;
		}
	}
}
