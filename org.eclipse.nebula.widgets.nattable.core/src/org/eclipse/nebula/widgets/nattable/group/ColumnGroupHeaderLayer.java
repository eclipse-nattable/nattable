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
package org.eclipse.nebula.widgets.nattable.group;

import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupsCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TransformedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;


/**
 * Adds the Column grouping functionality to the column headers.<br/>
 * Also persists the state of the column groups when {@link NatTable#saveState()} is invoked.<br/>
 * <br/>
 * Internally uses the {@link ColumnGroupModel} to track the column groups.<br/>
 * @see ColumnGroupGridExample
 */
public class ColumnGroupHeaderLayer extends AbstractLayerTransform {

	private final SizeConfig rowHeightConfig = new SizeConfig(DataLayer.DEFAULT_ROW_HEIGHT);
	private final ColumnGroupModel model;
	private final ILayer columnHeaderLayer;
	private final ILayerPainter layerPainter = new CellLayerPainter();
	
	/**
	 * Flag which is used to tell the ColumnGroupHeaderLayer whether to calculate the height of the layer
	 * dependent on column group configuration or not. If it is set to <code>true</code> the column header
	 * will check if column groups are configured and if not, the height of the column header will not
	 * show the double height for showing column groups.
	 */
	private boolean calculateHeight = false;
	
	/**
	 * Listener that will fire a RowStructuralRefreshEvent in case the ColumnGroupModel changes.
	 * Is only needed in case the dynamic height calculation is enabled.
	 */
	private IColumnGroupModelListener modelChangeListener;

	public ColumnGroupHeaderLayer(ILayer columnHeaderLayer, SelectionLayer selectionLayer, ColumnGroupModel columnGroupModel) {
		this(columnHeaderLayer, selectionLayer, columnGroupModel, true);
	}

	public ColumnGroupHeaderLayer(final ILayer columnHeaderLayer, SelectionLayer selectionLayer, ColumnGroupModel columnGroupModel, boolean useDefaultConfiguration) {
		super(columnHeaderLayer);
		
		this.columnHeaderLayer = columnHeaderLayer;
		this.model = columnGroupModel;
		
		registerCommandHandler(new ColumnGroupsCommandHandler(model, selectionLayer, this));

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(columnGroupModel));
		}

		modelChangeListener = new IColumnGroupModelListener() { 
			public void columnGroupModelChanged() { 
				fireLayerEvent(new RowStructuralRefreshEvent(columnHeaderLayer)); 
			} 
		};
		
		this.model.registerColumnGroupModelListener(modelChangeListener);
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		model.saveState(prefix, properties);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		model.loadState(prefix, properties);
		fireLayerEvent(new ColumnStructuralRefreshEvent(this));
	}

	// Configuration

	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}

	// Vertical features

	// Rows

	@Override
	public int getRowCount() {
		if (!calculateHeight 
				|| (this.model.getAllIndexesInGroups() != null 
						&& this.model.getAllIndexesInGroups().size() > 0)) { 
			return columnHeaderLayer.getRowCount() + 1;
		}
		return columnHeaderLayer.getRowCount();
	}

	@Override
	public int getPreferredRowCount() {
		return columnHeaderLayer.getPreferredRowCount() + 1;
	}

	@Override
	public int getRowIndexByPosition(int rowPosition) {
		if (rowPosition == 0) {
			return rowPosition;
		} else {
			return columnHeaderLayer.getRowIndexByPosition(rowPosition - 1);
		}
	}

	// Height

	@Override 
	public int getHeight() { 
		if (!calculateHeight 
				|| (this.model.getAllIndexesInGroups() != null 
						&& this.model.getAllIndexesInGroups().size() > 0)) { 
			return rowHeightConfig.getAggregateSize(1) + columnHeaderLayer.getHeight();
		} 
		return columnHeaderLayer.getHeight(); 
	} 
	

	
	@Override
	public int getPreferredHeight() {
		return rowHeightConfig.getAggregateSize(1) + columnHeaderLayer.getPreferredHeight();
	}

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		if (rowPosition == 0) {
			return rowHeightConfig.getSize(rowPosition);
		} else {
			return columnHeaderLayer.getRowHeightByPosition(rowPosition - 1);
		}
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeightConfig.setSize(0, rowHeight);
	}

	// Row resize

	@Override
	public boolean isRowPositionResizable(int rowPosition) {
		if (rowPosition == 0) {
			return rowHeightConfig.isPositionResizable(rowPosition);
		} else {
			return columnHeaderLayer.isRowPositionResizable(rowPosition - 1);
		}
	}

	// Y

	@Override
	public int getRowPositionByY(int y) {
		int row0Height = getRowHeightByPosition(0);
		if (y < row0Height) {
			return 0;
		} else {
			return 1 + columnHeaderLayer.getRowPositionByY(y - row0Height);
		}
	}

	@Override
	public int getStartYOfRowPosition(int rowPosition) {
		if (rowPosition == 0) {
			return rowHeightConfig.getAggregateSize(rowPosition);
		} else {
			return getRowHeightByPosition(0) + columnHeaderLayer.getStartYOfRowPosition(rowPosition - 1);
		}
	}

	// Cell features

	/**
	 * If a cell belongs to a column group:
	 * 	 column position - set to the start position of the group
	 * 	 span - set to the width/size of the column group
	 *
	 * NOTE: gc.setClip() is used in the CompositeLayerPainter to ensure that partially visible
	 * Column group header cells are rendered properly.
	 */
	@Override
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		int bodyColumnIndex = getColumnIndexByPosition(columnPosition);

		// Column group header cell
		if (model.isPartOfAGroup(bodyColumnIndex)) {
			if (rowPosition == 0) {
				return new LayerCell(
						this,
						getStartPositionOfGroup(columnPosition), rowPosition,
						columnPosition, rowPosition,
						getColumnSpan(columnPosition), 1
				);
			} else {
				return new LayerCell(this, columnPosition, rowPosition);
			}
		} else {
			// render column header w/ rowspan = 2
			// as in this case we ask the column header layer for the cell position
			// and the column header layer asks his data provider for the row count
			// which should always return 1, we ask for row position 0 instead of 
			// using getGroupHeaderRowPosition(), if we would use getGroupHeaderRowPosition()
			// the ColumnGroupGroupHeaderLayer wouldn't work anymore
			ILayerCell cell = columnHeaderLayer.getCellByPosition(columnPosition, 0);
			if (cell != null) {
				final int rowSpan;
				
				if (calculateHeight && model.size() == 0) {
					rowSpan = 1;
				} else {
					rowSpan = 2;
				}
				
				cell = new TransformedLayerCell(cell) {
					@Override
					public ILayer getLayer() {
						return ColumnGroupHeaderLayer.this;
					}
					
					@Override
					public int getRowSpan() {
						return rowSpan;
					}
				};
			}
			return cell;
		}
	}
	
	/**
	 * Calculates the span of a cell in a Column Group.
	 * Takes into account collapsing and hidden columns in the group.
	 *
	 * @param selectionLayerColumnPosition of any column belonging to the group
	 */
	protected int getColumnSpan(int columnPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		
		if (columnGroup.isCollapsed()) {
			int sizeOfStaticColumns = columnGroup.getStaticColumnIndexes().size();
			return sizeOfStaticColumns == 0 ? 1 : sizeOfStaticColumns;
		} else {
			int startPositionOfGroup = getStartPositionOfGroup(columnPosition);
			int sizeOfGroup = columnGroup.getSize();
			int endPositionOfGroup = startPositionOfGroup + sizeOfGroup;
			List<Integer> columnIndexesInGroup = columnGroup.getMembers();

			for (int i = startPositionOfGroup; i < endPositionOfGroup; i++) {
				int index = getColumnIndexByPosition(i);
				if (!columnIndexesInGroup.contains(Integer.valueOf(index))) {
					sizeOfGroup--;
				}
			}
			return sizeOfGroup;
		}
	}

	/**
	 * Figures out the start position of the group.
	 *
	 * @param selectionLayerColumnPosition of any column belonging to the group
	 * @return first position of the column group
	 */
	private int getStartPositionOfGroup(int columnPosition) {
		int bodyColumnIndex = getColumnIndexByPosition(columnPosition);
		ColumnGroup columnGroup = model.getColumnGroupByIndex(bodyColumnIndex);

		int leastPossibleStartPositionOfGroup = columnPosition - columnGroup.getSize();
		int i = 0;
		for (i = leastPossibleStartPositionOfGroup; i < columnPosition; i++) {
			if (ColumnGroupUtils.isInTheSameGroup(getColumnIndexByPosition(i), bodyColumnIndex, model)) {
				break;
			}
		}
		return i;
	}

	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return DisplayMode.NORMAL;
		} else {
			return columnHeaderLayer.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
		} else {
			return columnHeaderLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (rowPosition == 0 && model.isPartOfAGroup(columnIndex)) {
			return model.getColumnGroupByIndex(columnIndex).getName();
		} else {
			return columnHeaderLayer.getDataValueByPosition(columnPosition, 0);
		}
	}

	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		int columnIndex = getColumnIndexByPosition(getColumnPositionByX(x));
		if (model.isPartOfAGroup(columnIndex) && y < getRowHeightByPosition(0)) {
			return new LabelStack(GridRegion.COLUMN_GROUP_HEADER);
		} else {
			return columnHeaderLayer.getRegionLabelsByXY(x, y - getRowHeightByPosition(0));
		}
	}

	// ColumnGroupModel delegates

	public void addColumnsIndexesToGroup(String colGroupName, int... colIndexes) {
		model.addColumnsIndexesToGroup(colGroupName, colIndexes);
	}

	public void clearAllGroups(){
		model.clear();
	}
	
	public void setStaticColumnIndexesByGroup(String colGroupName, int... staticColumnIndexes) {
		model.setStaticColumnIndexesByGroup(colGroupName, staticColumnIndexes);
	}

	public boolean isColumnInGroup(int bodyColumnIndex) {
		return model.isPartOfAGroup(bodyColumnIndex);
	}

	/**
	 * @see ColumnGroupModel#setGroupUnBreakable(int)
	 */
	public void setGroupUnbreakable(int columnIndex){
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		columnGroup.setUnbreakable(true);
	}

	public void setGroupAsCollapsed(int columnIndex) {
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		columnGroup.setCollapsed(true);
	}
	
	public boolean isCalculateHeight() {
		return calculateHeight;
	}

	public void setCalculateHeight(boolean calculateHeight) {
		this.calculateHeight = calculateHeight;
		
		if (calculateHeight) {
			this.model.registerColumnGroupModelListener(modelChangeListener);
		} else {
			this.model.unregisterColumnGroupModelListener(modelChangeListener);
		}
	}

}
