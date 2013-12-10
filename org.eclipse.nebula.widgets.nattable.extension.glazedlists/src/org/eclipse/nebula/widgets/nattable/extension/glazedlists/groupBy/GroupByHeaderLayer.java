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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.GroupByColumnIndexCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.UngroupByColumnIndexCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

public class GroupByHeaderLayer extends DimensionallyDependentLayer {

	public static final String GROUP_BY_REGION = "GROUP_BY_REGION"; //$NON-NLS-1$
	
	private static DataLayer baseLayer = new DataLayer(new IDataProvider() {
		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			return null;
		}
		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		}
		@Override
		public int getRowCount() {
			return 1;
		}
		@Override
		public int getColumnCount() {
			return 1;
		}
	});
	
	private final GroupByModel groupByModel;

	private GroupByHeaderPainter groupByHeaderPainter;

	private boolean visible = true;
	
	public GroupByHeaderLayer(GroupByModel groupByModel, ILayer gridLayer, IDataProvider columnHeaderDataProvider) {
		super(baseLayer, gridLayer, baseLayer);
		
		this.groupByModel = groupByModel;
		registerPersistable(this.groupByModel);
		
		registerCommandHandler(new GroupByColumnCommandHandler());
		registerCommandHandler(new UngroupByColumnCommandHandler());
		
		GroupByHeaderConfiguration configuration = new GroupByHeaderConfiguration(groupByModel, columnHeaderDataProvider);
		addConfiguration(configuration);
		
		groupByHeaderPainter = configuration.getGroupByHeaderPainter();
		baseLayer.setRowHeightByPosition(0, groupByHeaderPainter.getPreferredHeight());
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		fireLayerEvent(new RowStructuralRefreshEvent(baseLayer));
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public int getHeight() {
		if (visible) {
			return super.getHeight();
		}
		return 0;
	}
	
	@Override
	public int getRowHeightByPosition(int rowPosition) {
		if (visible) {
			return super.getRowHeightByPosition(rowPosition);
		}
		return 0;
	}
	
	@Override
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		return new LayerCell(this, 0, 0, 0, 0, getColumnCount(), 1);
	}
	
	
	class GroupByColumnCommandHandler extends AbstractLayerCommandHandler<GroupByColumnIndexCommand> {

		@Override
		public Class<GroupByColumnIndexCommand> getCommandClass() {
			return GroupByColumnIndexCommand.class;
		}

		@Override
		protected boolean doCommand(GroupByColumnIndexCommand command) {
			int columnIndex = command.getGroupByColumnIndex();
			if (groupByModel.addGroupByColumnIndex(columnIndex)) {
				fireLayerEvent(new VisualRefreshEvent(GroupByHeaderLayer.this));
			}
			return true;
		}

	}

	class UngroupByColumnCommandHandler extends AbstractLayerCommandHandler<UngroupByColumnIndexCommand> {

		@Override
		public Class<UngroupByColumnIndexCommand> getCommandClass() {
			return UngroupByColumnIndexCommand.class;
		}

		@Override
		protected boolean doCommand(UngroupByColumnIndexCommand command) {
			int columnIndex = command.getGroupByColumnIndex();
			if (groupByModel.removeGroupByColumnIndex(columnIndex)) {
				fireLayerEvent(new VisualRefreshEvent(GroupByHeaderLayer.this));
			}
			return true;
		}

	}

	public int getGroupByColumnIndexAtXY(int x, int y) {
		return groupByHeaderPainter.getGroupByColumnIndexAtXY(x, y);
	}
	
}
