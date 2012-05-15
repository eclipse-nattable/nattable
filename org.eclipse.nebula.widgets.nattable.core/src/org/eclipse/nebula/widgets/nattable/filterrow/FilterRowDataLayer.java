/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

/**
 * {@link DataLayer} with a custom {@link IDataProvider} which stores/tracks the filter text on columns.<br/>
 * Applies region name of {@link FilterRowHeaderComposite#FILTER_ROW} to the filter row.<br/>
 * Persists the filter text to the properties file.<br/>
 *
 * @param <T> type of the underlying row object
 */
public class FilterRowDataLayer<T> extends DataLayer {

	/** Prefix of the column label applied to each column in the filter row */
	public static final String FILTER_ROW_COLUMN_LABEL_PREFIX = "FILTER_COLUMN_"; //$NON-NLS-1$

	/** Prefix for the persistence key in the properties file */
	public static final String PERSISTENCE_KEY_FILTER_ROW_TOKENS = ".filterTokens"; //$NON-NLS-1$

	private final FilterRowDataProvider<T> dataProvider;

	public FilterRowDataLayer(IFilterStrategy<T> filterStrategy, ILayer columnHeaderLayer, IDataProvider columnHeaderDataProvider, IConfigRegistry configRegistry) {
		this.dataProvider = new FilterRowDataProvider<T>(filterStrategy, columnHeaderLayer, columnHeaderDataProvider, configRegistry);
		registerPersistable(dataProvider);

		setDataProvider(dataProvider);

		addConfiguration(new DefaultFilterRowConfiguration());
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		boolean handled = false;
		if (command instanceof ClearFilterCommand && command.convertToTargetLayer(this)) {
			int columnPosition = ((ClearFilterCommand) command).getColumnPosition();
			dataProvider.setDataValue(columnPosition, 0, null);
			handled = true;
		} else if (command instanceof ClearAllFiltersCommand) {
			dataProvider.clearAllFilters();
			handled = true;
		}

		if (handled) {
			fireLayerEvent(new RowStructuralRefreshEvent(this));
			return true;
		} else {
			return super.doCommand(command);
		}
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		// At the data layer level position == index
		final LabelStack labels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
		labels.addLabel(FILTER_ROW_COLUMN_LABEL_PREFIX + columnPosition);
		labels.addLabel(GridRegion.FILTER_ROW);
		return labels;
	}
}
