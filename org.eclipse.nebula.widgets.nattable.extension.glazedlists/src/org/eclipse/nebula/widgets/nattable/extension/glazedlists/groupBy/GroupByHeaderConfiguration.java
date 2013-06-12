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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;


import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.action.GroupByDragMode;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class GroupByHeaderConfiguration extends AbstractRegistryConfiguration {
	
	private final GroupByHeaderPainter groupByHeaderPainter;

	public GroupByHeaderConfiguration(GroupByModel groupByModel, IDataProvider columnHeaderDataProvider) {
		groupByHeaderPainter = new GroupByHeaderPainter(groupByModel, columnHeaderDataProvider);
	}
	
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, groupByHeaderPainter, DisplayMode.NORMAL, GroupByHeaderLayer.GROUP_BY_REGION);
	}
	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerFirstMouseDragMode(
				MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
				new AggregateDragMode(
						new CellDragMode(),
						new ColumnReorderDragMode() {
							@Override
							protected boolean isValidTargetColumnPosition(
									ILayer natLayer,
									int dragFromGridColumnPosition,
									int dragToGridColumnPosition) {
								// Suppress reorder if cursor is over the group by region
								LabelStack regionLabels = natLayer.getRegionLabelsByXY(this.currentEvent.x, this.currentEvent.y);
								if (regionLabels != null && !regionLabels.hasLabel(GroupByHeaderLayer.GROUP_BY_REGION)) {
									return super.isValidTargetColumnPosition(natLayer, dragFromGridColumnPosition,
											dragToGridColumnPosition);
								}
								return false;
							}
						},
						new GroupByDragMode()));
	}
	
	public GroupByHeaderPainter getGroupByHeaderPainter() {
		return groupByHeaderPainter;
	}
	
}
