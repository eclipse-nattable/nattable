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
package org.eclipse.nebula.widgets.nattable.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectRowGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;

/**
 * Adds the Row grouping functionality to the row headers.
 * Also persists the state of the row groups when {@link NatTable#saveState(String, Properties)} is invoked.
 * 
 * Internally uses the {@link IRowGroupModel} to track the row groups.
 * <p>
 * See RowGroupGridExample
 */
public class RowGroupHeaderLayer<T> extends AbstractLayerTransform {

	private final SizeConfig columnWidthConfig = new SizeConfig(DataLayer.DEFAULT_COLUMN_WIDTH);
	private final IRowGroupModel<T> model;
	private final SelectionLayer selectionLayer;
	private final ILayer rowHeaderLayer;
	private final ILayerPainter layerPainter = new CellLayerPainter();
		
	public RowGroupHeaderLayer(ILayer rowHeaderLayer, SelectionLayer selectionLayer, IRowGroupModel<T> rowGroupModel) {
		this(rowHeaderLayer, selectionLayer, rowGroupModel, true);
	}
		
	public RowGroupHeaderLayer(ILayer rowHeaderLayer, SelectionLayer selectionLayer, IRowGroupModel<T> rowGroupModel, boolean useDefaultConfiguration) {
		super(rowHeaderLayer);
		this.rowHeaderLayer = rowHeaderLayer;
		this.selectionLayer = selectionLayer;
		this.model = rowGroupModel;
		
		registerCommandHandlers();
		
		if( useDefaultConfiguration ) {
			addConfiguration( new DefaultRowGroupHeaderLayerConfiguration<T>(rowGroupModel) );
		}
	}
	
	public IRowGroupModel<T> getModel() {
		return this.model;
	}
	
	// Persistence
	
	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		model.loadState(prefix, properties);
		fireLayerEvent(new RowStructuralRefreshEvent(this));
	}
	
	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		model.saveState(prefix, properties);
	}
	
	// Configuration
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new SelectRowGroupCommandHandler<T>(this.model, this.selectionLayer, this));
	}
	
	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}
	
	// Horizontal features

	// Columns
	
	@Override
	public int getColumnCount() {
		return rowHeaderLayer.getColumnCount() + 1;
	}
	
	@Override
	public int getPreferredColumnCount() {
		return rowHeaderLayer.getPreferredColumnCount() + 1;
	}
	
	@Override
	public int getColumnIndexByPosition(int columnPosition) {
		if( columnPosition == 0 ) {
			return columnPosition;
		} else {
			return rowHeaderLayer.getColumnIndexByPosition(columnPosition - 1);
		}
	}
	
	// Width
	
	@Override
	public int getWidth() {
		return columnWidthConfig.getAggregateSize(1) + rowHeaderLayer.getWidth();
	}
	
	@Override
	public int getPreferredWidth() {		
		return columnWidthConfig.getAggregateSize(1) + rowHeaderLayer.getPreferredWidth();
	}
	
	@Override
	public int getColumnWidthByPosition(int columnPosition) {
		if( columnPosition == 0 ) {
			return columnWidthConfig.getSize(columnPosition);
		} else {
			return rowHeaderLayer.getColumnWidthByPosition(columnPosition - 1);
		}
	}
	
	public void setColumnWidth(int columnWidth) {
		this.columnWidthConfig.setSize(0, columnWidth);
	}
	
	// Column resize
	
	@Override
	public boolean isColumnPositionResizable(int columnPosition) {
		if( columnPosition == 0 ) {
			return columnWidthConfig.isPositionResizable(columnPosition);
		} else {
			return rowHeaderLayer.isRowPositionResizable(columnPosition - 1);
		}
	}
	
	// X
	
	@Override
	public int getColumnPositionByX(int x) {
		int col0Width = getColumnWidthByPosition(0);
		if( x < col0Width ) {
			return 0;
		} else {
			return 1 + rowHeaderLayer.getColumnPositionByX(x - col0Width);
		}
	}
	
	@Override
	public int getStartXOfColumnPosition(int columnPosition) {
		if( columnPosition == 0 ) {
			return columnWidthConfig.getAggregateSize(columnPosition);
		} else {
			return getColumnWidthByPosition(0) + rowHeaderLayer.getStartXOfColumnPosition(columnPosition - 1);
		}
	}
	
	// Cell features
	
	/**
	 * If a cell belongs to a column group:
	 * 	 column position - set to the start position of the group
	 * 	 span - set to the width/size of the row group
	 *
	 * NOTE: gc.setClip() is used in the CompositeLayerPainter to ensure that partially visible
	 * Column group header cells are rendered properly.
	 */
	@Override
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {		
		int bodyRowIndex = getRowIndexByPosition(rowPosition);
		
		// Row group header cell
		if (RowGroupUtils.isPartOfAGroup(model, bodyRowIndex)) {
			if (columnPosition == 0) {
				return new LayerCell(
						this,
						columnPosition, getStartPositionOfGroup(rowPosition),
						columnPosition, rowPosition,
						1, getRowSpan(rowPosition)
				);
			}
		}
		
		return new LayerCell(this, columnPosition, rowPosition);
	}
	
	/**
	 * Calculates the span of a cell in a Row Group.
	 * Takes into account collapsing and hidden rows in the group.
	 *
	 * @param rowPosition position of any row belonging to the group
	 */
	protected int getRowSpan(int rowPosition) {
		int rowIndex = getRowIndexByPosition(rowPosition);
		
		// Get the row and the group from our cache and model.
		final IRowGroup<T> rowGroup = RowGroupUtils.getRowGroupForRowIndex(model, rowIndex);
		
		if (RowGroupUtils.isCollapsed(model, rowGroup)) {
			return rowGroup.getOwnStaticMemberRows().size();			
		} else {
			int startPositionOfGroup = getStartPositionOfGroup(rowPosition);
			int sizeOfGroup = RowGroupUtils.sizeOfGroup(model, rowIndex);
			int endPositionOfGroup = startPositionOfGroup + sizeOfGroup;
			List<Integer> rowIndexesInGroup = RowGroupUtils.getRowIndexesInGroup(model, rowIndex);

			for (int i = startPositionOfGroup; i < endPositionOfGroup; i++) {
				int index = getRowIndexByPosition(i);
				if (!rowIndexesInGroup.contains(Integer.valueOf(index))) {
					sizeOfGroup--;
				}
			}
			
			return Math.max(1, sizeOfGroup);
		}
	}
	
	/**
	 * Figures out the start position of the group.
	 *
	 * @param selectionLayerColumnPosition of any column belonging to the group
	 * @return first position of the column group
	 */
	private int getStartPositionOfGroup(int rowPosition) {
		int bodyRowIndex = getRowIndexByPosition(rowPosition);
		int leastPossibleStartPositionOfGroup = Math.max(0, (rowPosition - RowGroupUtils.sizeOfGroup(model, bodyRowIndex)));
		int i = 0;
		for (i = leastPossibleStartPositionOfGroup; i < rowPosition; i++) {
			if (RowGroupUtils.isInTheSameGroup(getRowIndexByPosition(i), bodyRowIndex, model)) {
				break;
			}
		}
		return i;
	}
	
	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int rowIndex = getRowIndexByPosition(rowPosition);
		if( columnPosition == 0 && RowGroupUtils.isPartOfAGroup(model, rowIndex) ) {
			return DisplayMode.NORMAL;
		} else {
			return rowHeaderLayer.getDisplayModeByPosition(columnPosition - 1, rowPosition);
		}
	}
	
	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		int rowIndex = getRowIndexByPosition(rowPosition);
		if( columnPosition == 0 && RowGroupUtils.isPartOfAGroup(model, rowIndex) ) {
			List<Integer> selectedRowIndexes = convertToRowIndexes(selectionLayer.getFullySelectedRowPositions());
			if (selectedRowIndexes.contains(rowIndex)) {
				return new LabelStack(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE, GridRegion.ROW_GROUP_HEADER);
			}
			return new LabelStack(GridRegion.ROW_GROUP_HEADER);
		} else {
			return rowHeaderLayer.getConfigLabelsByPosition(columnPosition - 1, rowPosition);
		}
	}
	
	private List<Integer> convertToRowIndexes(final int[] rowPositions) {
		final List <Integer> rowIndexes = new ArrayList<Integer>();
		for(final Integer rowPosition : rowPositions){
			rowIndexes.add(this.selectionLayer.getRowIndexByPosition(rowPosition));
		}
		return rowIndexes;
	}
	
	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int rowIndex = getRowIndexByPosition(rowPosition);
		if( columnPosition == 0 ) {
			if( RowGroupUtils.isPartOfAGroup(model, rowIndex) ) {		
				return RowGroupUtils.getRowGroupNameForIndex(model, rowIndex);
			} else {
				return null;
			}
		} else {
			return rowHeaderLayer.getDataValueByPosition(columnPosition - 1, rowPosition);
		}
	}
	
	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		int rowIndex = getRowIndexByPosition(getRowPositionByY(y));
		if (RowGroupUtils.isPartOfAGroup(model, rowIndex) && x < getColumnWidthByPosition(0)) {
			return new LabelStack(GridRegion.ROW_GROUP_HEADER);
		} else {
			return rowHeaderLayer.getRegionLabelsByXY(x - getColumnWidthByPosition(0), y);
		}
	}

	public void collapseRowGroupByIndex(int rowIndex) {
		RowGroupUtils.getRowGroupForRowIndex(model, rowIndex).collapse();
	}

	public void clearAllGroups(){
		model.clear();
	}
}
