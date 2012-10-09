/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation (DimensionallyDependentLayer)
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.layer;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;


/**
 * <p>A DimensionallyDependentIndexLayer is a layer whose horizontal and vertical dimensions are dependent on the 
 * horizontal and vertical dimensions of other layers. A DimensionallyDependentIndexLayer takes three constructor 
 * parameters: the horizontal layer that the DimensionallyDependentIndexLayer's horizontal dimension is linked to, the 
 * vertical layer that the DimensionallyDependentIndexLayer is linked to, and a base layer to which all 
 * non-dimensionally related ILayer method calls will be delegated to (e.g. command, event methods)
 * </p>
 * <p>Prime examples of dimensionally dependent layers are the column header and row header layers. For example, the
 * column header layer's horizontal dimension is linked to the body layer's horizontal dimension. This means that
 * whatever columns are shown in the body area will also be shown in the column header area, and vice versa. Note that
 * the column header layer maintains its own vertical dimension, however, so it's vertical layer dependency would be a
 * separate data layer. The same is true for the row header layer, only with the vertical instead of the horizontal
 * dimension. The constructors for the column header and row header layers would therefore look something like this:
 * </p>
 * <pre>
 * ILayer columnHeaderLayer = new DimensionallyDependentIndexLayer(columnHeaderRowDataLayer, bodyLayer, columnHeaderRowDataLayer);
 * ILayer rowHeaderLayer = new DimensionallyDependentIndexLayer(rowHeaderColumnDataLayer, bodyLayer, rowHeaderColumnDataLayer);
 * </pre>
 * <p>In contrast to {@link DimensionallyDependentLayer}, this class:
 * </p>
 * <ul>
 *     <li>implements {@link IUniqueIndexLayer};</li>
 *     <li>provides conversion of local and underlying positions based on the unique index (reliable);</li>
 *     <li>provides an implementation of {@link #getCellByPosition(int, int)} which e.g. fully supports spanned cells;</li>
 *     <li>requires that the layers, the horizontal and vertical dimension are linked to, implements
 *         {@link IUniqueIndexLayer} too.</li>
 * </ul>
 */
public class DimensionallyDependentIndexLayer extends AbstractIndexLayerTransform {


	private IUniqueIndexLayer horizontalLayerDependency;
	private IUniqueIndexLayer verticalLayerDependency;


	/**
	 * Creates a new DimensionallyDependentIndexLayer.
	 * 
	 * @param baseLayer the underlying base layer
	 * @param horizontalLayerDependency the layer, the horizontal dimension is linked to
	 * @param verticalLayerDependency the layer, the vertical dimension is linked to
	 */
	public DimensionallyDependentIndexLayer(IUniqueIndexLayer baseLayer,
			IUniqueIndexLayer horizontalLayerDependency, IUniqueIndexLayer verticalLayerDependency) {
		super(baseLayer);
		
		setHorizontalLayerDependency(horizontalLayerDependency);
		setVerticalLayerDependency(verticalLayerDependency);
	}

	/**
	 * Creates a new DimensionallyDependentIndexLayer. The horizontal and vertical layer dependency must be set
	 * by calling {@link #init(IUniqueIndexLayer, IUniqueIndexLayer)} before the layer is used.
	 * 
	 * @param baseLayer the underlying base layer
	 */
	protected DimensionallyDependentIndexLayer(IUniqueIndexLayer baseLayer) {
		super(baseLayer);
	}


	// Dependent layer accessors

	protected void setHorizontalLayerDependency(IUniqueIndexLayer horizontalLayerDependency) {
		this.horizontalLayerDependency = horizontalLayerDependency;

//		horizontalLayerDependency.addLayerListener(new ILayerListener() {
//
//			public void handleLayerEvent(ILayerEvent event) {
//				if (event instanceof IStructuralChangeEvent) {
//					// TODO refresh horizontal structure
//				}
//			}
//
//		});
	}

	protected void setVerticalLayerDependency(IUniqueIndexLayer verticalLayerDependency) {
		this.verticalLayerDependency = verticalLayerDependency;

//		verticalLayerDependency.addLayerListener(new ILayerListener() {
//
//			public void handleLayerEvent(ILayerEvent event) {
//				if (event instanceof IStructuralChangeEvent) {
//					// TODO refresh vertical structure
//				}
//			}
//
//		});
	}

	public ILayer getHorizontalLayerDependency() {
		return horizontalLayerDependency;
	}

	public ILayer getVerticalLayerDependency() {
		return verticalLayerDependency;
	}

	public IUniqueIndexLayer getBaseLayer() {
		return getUnderlyingLayer();
	}

	// Commands

	@Override
	public boolean doCommand(ILayerCommand command) {
		// Invoke command handler(s) on the Dimensionally dependent layer
		ILayerCommand clonedCommand = command.cloneCommand();
		if (super.doCommand(command)) {
			return true;
		}

		clonedCommand = command.cloneCommand();
		if (horizontalLayerDependency.doCommand(clonedCommand)) {
			return true;
		}

		clonedCommand = command.cloneCommand();
		if (verticalLayerDependency.doCommand(clonedCommand)) {
			return true;
		}

		return false;
	}

	// Horizontal features

	// Columns

	public int getColumnCount() {
		return horizontalLayerDependency.getColumnCount();
	}

	public int getPreferredColumnCount() {
		return horizontalLayerDependency.getPreferredColumnCount();
	}

	public int getColumnIndexByPosition(int columnPosition) {
		return horizontalLayerDependency.getColumnIndexByPosition(columnPosition);
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return horizontalLayerDependency.getColumnPositionByIndex(columnIndex);
	}

	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return LayerUtil.convertColumnPosition(this, localColumnPosition, getUnderlyingLayer());
	}

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		return LayerUtil.convertColumnPosition(sourceUnderlyingLayer, underlyingColumnPosition, this);
	}
	
	// Width

	public int getWidth() {
		return horizontalLayerDependency.getWidth();
	}

	public int getPreferredWidth() {
		return horizontalLayerDependency.getPreferredWidth();
	}

	public int getColumnWidthByPosition(int columnPosition) {
		return horizontalLayerDependency.getColumnWidthByPosition(columnPosition);
	}

	// Column resize

	public boolean isColumnPositionResizable(int columnPosition) {
		return horizontalLayerDependency.isColumnPositionResizable(columnPosition);
	}

	// X

	public int getColumnPositionByX(int x) {
		return horizontalLayerDependency.getColumnPositionByX(x);
	}

	public int getStartXOfColumnPosition(int columnPosition) {
		return horizontalLayerDependency.getStartXOfColumnPosition(columnPosition);
	}

	// Vertical features

	// Rows

	public int getRowCount() {
		return verticalLayerDependency.getRowCount();
	}

	public int getPreferredRowCount() {
		return verticalLayerDependency.getPreferredRowCount();
	}

	public int getRowIndexByPosition(int rowPosition) {
		return verticalLayerDependency.getRowIndexByPosition(rowPosition);
	}

	public int getRowPositionByIndex(int rowIndex) {
		return verticalLayerDependency.getRowPositionByIndex(rowIndex);
	}

	public int localToUnderlyingRowPosition(int localRowPosition) {
		return LayerUtil.convertRowPosition(this, localRowPosition, getUnderlyingLayer());
	}

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		return LayerUtil.convertRowPosition(sourceUnderlyingLayer, underlyingRowPosition, this);
	}

	// Height

	public int getHeight() {
		return verticalLayerDependency.getHeight();
	}

	public int getPreferredHeight() {
		return verticalLayerDependency.getPreferredHeight();
	}

	public int getRowHeightByPosition(int rowPosition) {
		return verticalLayerDependency.getRowHeightByPosition(rowPosition);
	}

	// Row resize

	public boolean isRowPositionResizable(int rowPosition) {
		return verticalLayerDependency.isRowPositionResizable(rowPosition);
	}

	// Y

	public int getRowPositionByY(int y) {
		return verticalLayerDependency.getRowPositionByY(y);
	}

	public int getStartYOfRowPosition(int rowPosition) {
		return verticalLayerDependency.getStartYOfRowPosition(rowPosition);
	}

}
