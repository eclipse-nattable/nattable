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
package org.eclipse.nebula.widgets.nattable.resize.command;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;


public class MultiRowResizeCommand extends AbstractMultiRowCommand {

	private int commonRowHeight = -1;
	protected Map<RowPositionCoordinate, Integer> rowPositionToHeight = new HashMap<RowPositionCoordinate, Integer>();

	/**
	 * All rows are being resized to the same height e.g. during a drag resize
	 */
	public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int commonRowHeight) {
		super(layer, rowPositions);
		this.commonRowHeight = commonRowHeight;
	}

	/**
	 * Each row is being resized to a different size e.g. during auto resize
	 */
	public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int[] rowHeights) {
		super(layer, rowPositions);
		for (int i = 0; i < rowPositions.length; i++) {
			rowPositionToHeight.put(new RowPositionCoordinate(layer, rowPositions[i]), Integer.valueOf(rowHeights[i]));
		}
	}
	
	protected MultiRowResizeCommand(MultiRowResizeCommand command) {
		super(command);
		this.commonRowHeight = command.commonRowHeight;
		this.rowPositionToHeight = new HashMap<RowPositionCoordinate, Integer>(command.rowPositionToHeight);
	}

	public int getCommonRowHeight() {
		return commonRowHeight;
	}
	
	public int getRowHeight(int rowPosition) {
		for (RowPositionCoordinate rowPositionCoordinate : rowPositionToHeight.keySet()) {
			if (rowPositionCoordinate.getRowPosition() == rowPosition) {
				return rowPositionToHeight.get(rowPositionCoordinate).intValue();
			}
		}
		return commonRowHeight;
	}
	
	@Override
	public boolean convertToTargetLayer(ILayer targetLayer) {
		Map<RowPositionCoordinate, Integer> newRowPositionToHeight = new HashMap<RowPositionCoordinate, Integer>();
		
		for (RowPositionCoordinate rowPositionCoordinate : rowPositionToHeight.keySet()) {
			RowPositionCoordinate convertedRowPositionCoordinate = LayerCommandUtil.convertRowPositionToTargetContext(rowPositionCoordinate, targetLayer);
			if (convertedRowPositionCoordinate != null) {
				newRowPositionToHeight.put(convertedRowPositionCoordinate, rowPositionToHeight.get(rowPositionCoordinate));
			}
		}
		
		if (super.convertToTargetLayer(targetLayer)) {
			rowPositionToHeight = newRowPositionToHeight;
			return true;
		} else {
			return false;
		}
	}
	
	public MultiRowResizeCommand cloneCommand() {
		return new MultiRowResizeCommand(this);
	}
	
}
