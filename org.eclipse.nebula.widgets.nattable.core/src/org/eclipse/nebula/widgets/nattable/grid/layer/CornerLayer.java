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
package org.eclipse.nebula.widgets.nattable.grid.layer;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;

public class CornerLayer extends DimensionallyDependentLayer {

	private ILayerPainter layerPainter = new CellLayerPainter();
	
	public CornerLayer(IUniqueIndexLayer baseLayer, ILayer horizontalLayerDependency, ILayer verticalLayerDependency) {
		super(baseLayer, horizontalLayerDependency, verticalLayerDependency);
	}
	
	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}

	@Override
	public LayerCell getCellByPosition(int columnPosition, int rowPosition) {
		return new LayerCell(this, 0, 0, columnPosition, rowPosition, getHorizontalLayerDependency().getColumnCount(), getVerticalLayerDependency().getRowCount());
	}
	
}
