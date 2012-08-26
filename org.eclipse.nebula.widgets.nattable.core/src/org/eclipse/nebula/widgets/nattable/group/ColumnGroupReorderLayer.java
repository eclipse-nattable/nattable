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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.command.GroupColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.GroupMultiColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnsAndGroupsCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;


/**
 * Adds functionality allowing the reordering of the the Column groups. 
 */
public class ColumnGroupReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	private IUniqueIndexLayer underlyingLayer;
	
	private final ColumnGroupModel model;

	private int reorderFromColumnPosition;
	
	public ColumnGroupReorderLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
		setUnderlyingLayer(underlyingLayer);
		this.underlyingLayer = underlyingLayer;
		this.model = model;
		
		registerCommandHandlers();
	}
	
	public boolean reorderColumnGroup(int fromColumnPosition, int toColumnPosition) {
		int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition);
		
		List<Integer> fromColumnPositions = getColumnGroupPositions(fromColumnIndex);
		return underlyingLayer.doCommand(new MultiColumnReorderCommand(this, fromColumnPositions, toColumnPosition));
	}
	
	public ColumnGroupModel getModel() {
		return model;
	}
	
	@Override
	public ILayer getUnderlyingLayer() {
		return super.getUnderlyingLayer();
	}
	
	// Configuration
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new ReorderColumnGroupCommandHandler(this));
		registerCommandHandler(new ReorderColumnGroupStartCommandHandler(this));
		registerCommandHandler(new ReorderColumnGroupEndCommandHandler(this));
		registerCommandHandler(new ReorderColumnsAndGroupsCommandHandler(this));
		registerCommandHandler(new GroupColumnReorderCommandHandler(this));
		registerCommandHandler(new GroupMultiColumnReorderCommandHandler(this));
	}
	
	// Horizontal features
	
	// Columns
	
	public int getColumnPositionByIndex(int columnIndex) {
		return underlyingLayer.getColumnPositionByIndex(columnIndex);
	}
	
	// Vertical features
	
	// Rows
	
	public int getRowPositionByIndex(int rowIndex) {
		return underlyingLayer.getRowPositionByIndex(rowIndex);
	}
	
	// Column Groups
	
	/**
	 * @return the column positions for all the columns in this group
	 */
	public List<Integer> getColumnGroupPositions(int fromColumnIndex) {
		List<Integer> fromColumnIndexes = model.getColumnGroupByIndex(fromColumnIndex).getMembers();
		List<Integer> fromColumnPositions = new ArrayList<Integer>();
		
		for (Integer columnIndex : fromColumnIndexes) {
			fromColumnPositions.add(
					Integer.valueOf(underlyingLayer.getColumnPositionByIndex(columnIndex.intValue())));
		}
		//These positions are actually consecutive but the Column Group does not know about the order 
		Collections.sort(fromColumnPositions);
		return fromColumnPositions;
	}
	
	public int getReorderFromColumnPosition() {
		return reorderFromColumnPosition;
	}

	public void setReorderFromColumnPosition(int fromColumnPosition) {
		this.reorderFromColumnPosition = fromColumnPosition;
	}
	
}
