/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.swt.graphics.GC;

public class GroupByDataLayerConfiguration extends AbstractRegistryConfiguration {

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		//register a TextPainter to ensure that the GroupBy objects are rendered as text
		//even if in the first column by default another painter is registered
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER,
				new BackgroundPainter(new TextPainter() {
					@Override
					protected String getTextToDisplay(ILayerCell cell, GC gc, int availableLength, String text) {
						if (cell.getConfigLabels().hasLabel(TreeLayer.TREE_COLUMN_CELL) 
								|| cell.getConfigLabels().hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY)) {
							return super.getTextToDisplay(cell, gc, availableLength, text);
						} else {
							return ""; //$NON-NLS-1$
						}
					}
				}),
				DisplayMode.NORMAL,
				GroupByDataLayer.GROUP_BY_OBJECT
		);
		
		//register a converter for group by summary values that renders ... in case there is
		//no summary value calculated yet
		DisplayConverter converter = new DefaultDisplayConverter() {
			@Override
			public Object canonicalToDisplayValue(Object canonicalValue) {
				if (canonicalValue == null) {
					return IGroupBySummaryProvider.DEFAULT_SUMMARY_VALUE;
				}
				return super.canonicalToDisplayValue(canonicalValue);
			}
		};
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, converter, 
				DisplayMode.NORMAL, GroupByDataLayer.GROUP_BY_SUMMARY);

	}
	
}
