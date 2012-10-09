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
package org.eclipse.nebula.widgets.nattable.layer.stack;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * A pre-configured layer stack which includes the following layers (in that order):<br/>
 * <ol>
 * <li>ColumnReorderLayer</li>
 * <li>ColumnGroupReorderLayer</li>
 * <li>ColumnHideShowLayer</li>
 * <li>ColumnGroupExpandCollapseLayer</li>
 * <li>SelectionLayer</li>
 * <li>ViewportLayer</li>
 * </ol>
 */
public class ColumnGroupBodyLayerStack extends AbstractIndexLayerTransform {

	private ColumnReorderLayer columnReorderLayer;
	private ColumnGroupReorderLayer columnGroupReorderLayer;
	private ColumnHideShowLayer columnHideShowLayer;
	private ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
	private SelectionLayer selectionLayer;
	private ViewportLayer viewportLayer;

	public ColumnGroupBodyLayerStack(IUniqueIndexLayer underlyingLayer, ColumnGroupModel columnGroupModel) {
		columnReorderLayer = new ColumnReorderLayer(underlyingLayer);
		columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
		columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
		columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
		selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		setUnderlyingLayer(viewportLayer);

		registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
	}

	public ColumnReorderLayer getColumnReorderLayer() {
		return columnReorderLayer;
	}

	public ColumnGroupReorderLayer getColumnGroupReorderLayer() {
		return columnGroupReorderLayer;
	}

	public ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
		return columnGroupExpandCollapseLayer;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

}
