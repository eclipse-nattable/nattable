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
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;

public class RowHeaderLayer extends DimensionallyDependentLayer {

	private final SelectionLayer selectionLayer;

	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer) {
		this(baseLayer, verticalLayerDependency, selectionLayer, true);
	}
	
	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer, boolean useDefaultConfiguration) {
		super(baseLayer, baseLayer, verticalLayerDependency);
		this.selectionLayer = selectionLayer;
		
		if (useDefaultConfiguration) {
			addConfiguration(new DefaultRowHeaderLayerConfiguration());
		}
	}
	
	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int selectionLayerRowPosition = LayerUtil.convertRowPosition(this, rowPosition, selectionLayer);
		if (selectionLayer.isRowPositionSelected(selectionLayerRowPosition)) {
			return DisplayMode.SELECT;
		} else {
			return super.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}
	
	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack labelStack = super.getConfigLabelsByPosition(columnPosition, rowPosition);
		
		final int selectionLayerRowPosition = LayerUtil.convertRowPosition(this, rowPosition, selectionLayer);
		if (selectionLayer.isRowPositionFullySelected(selectionLayerRowPosition)) {
			labelStack.addLabel(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
		}
		
		return labelStack;
	}
	
}
