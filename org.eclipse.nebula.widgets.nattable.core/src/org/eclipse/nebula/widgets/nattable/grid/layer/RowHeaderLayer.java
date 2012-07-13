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
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;


/**
 * Layer for the row headers of the grid layer
 */
public class RowHeaderLayer extends DimensionallyDependentLayer {

	private final SelectionLayer selectionLayer;


	/**
	 * Creates a row header layer using the default configuration and painter
	 * 
	 * @param baseLayer
	 *            The data provider for this layer
	 * @param verticalLayerDependency
	 *            The layer to link the vertical dimension to, typically the body layer
	 * @param selectionLayer
	 *            The selection layer required to respond to selection events
	 */
	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer) {
		this(baseLayer, verticalLayerDependency, selectionLayer, true);
	}
	
	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer,
			boolean useDefaultConfiguration) {
		this(baseLayer, verticalLayerDependency, selectionLayer, useDefaultConfiguration, null);
	}

	/**
	 * @param baseLayer
	 *            The data provider for this layer
	 * @param verticalLayerDependency
	 *            The layer to link the vertical dimension to, typically the body layer
	 * @param selectionLayer
	 *            The selection layer required to respond to selection events
	 * @param useDefaultConfiguration
	 *            If default configuration should be applied to this layer
	 * @param layerPainter
	 *            The painter for this layer or <code>null</code> to use the painter of the base layer
	 */
	public RowHeaderLayer(IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency,
			SelectionLayer selectionLayer, boolean useDefaultConfiguration, ILayerPainter layerPainter) {
		super(baseLayer, baseLayer, verticalLayerDependency);
		if (selectionLayer == null) {
			throw new NullPointerException("selectionLayer"); //$NON-NLS-1$
		}

		this.selectionLayer = selectionLayer;
		this.layerPainter = layerPainter;
		
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
