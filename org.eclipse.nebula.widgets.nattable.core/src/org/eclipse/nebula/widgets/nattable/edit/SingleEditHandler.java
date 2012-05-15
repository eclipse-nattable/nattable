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
package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

public class SingleEditHandler implements ICellEditHandler {

	private final ICellEditor cellEditor;
	private final ILayer layer;
	private final int columnPosition;
	private final int rowPosition;

	public SingleEditHandler(ICellEditor cellEditor, ILayer layer, int columnPosition, int rowPosition) {
		this.cellEditor = cellEditor;
		this.layer = layer;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}
	
	/**
	 * {@inheritDoc}
 	 * Note: Assumes that the value is valid.<br/>
	 */
	public boolean commit(MoveDirectionEnum direction, boolean closeEditorAfterCommit) {
		Object canonicalValue = cellEditor.getCanonicalValue();
		boolean committed = layer.doCommand(new UpdateDataCommand(layer, columnPosition, rowPosition, canonicalValue));
		
		switch (direction) {
			case LEFT:
				layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, 1, false, false));
				break;
			case UP:
				layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, 1, false, false));
				break;
			case RIGHT:
				layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, 1, false, false));
				break;
			case DOWN:
				layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, 1, false, false));
				break;
		}
		
		if (committed && closeEditorAfterCommit) {
			cellEditor.close();
			return true;
		}
		
		return committed;
	}
	
}
