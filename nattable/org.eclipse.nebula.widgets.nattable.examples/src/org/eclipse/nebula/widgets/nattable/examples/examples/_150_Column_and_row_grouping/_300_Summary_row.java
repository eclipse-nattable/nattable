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
package org.eclipse.nebula.widgets.nattable.examples.examples._150_Column_and_row_grouping;

import static org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture.ASK_PRICE_PROP_NAME;
import static org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture.getColumnIndexOfProperty;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummationSummaryProvider;

public class _300_Summary_row<T> extends DefaultSummaryRowConfiguration implements IConfiguration {

	private IRowDataProvider<T> dataProvider;
	
	public _300_Summary_row(IRowDataProvider<T> dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public void addSummaryProviderConfig(IConfigRegistry configRegistry) {
		// Add summaries to ask price column
		configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER,
				new SummationSummaryProvider(dataProvider),
				DisplayMode.NORMAL,
				SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + getColumnIndexOfProperty(ASK_PRICE_PROP_NAME));

		// No Summary by default
		configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER,
				ISummaryProvider.NONE,
				DisplayMode.NORMAL,
				SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
	}
}
