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

import org.eclipse.nebula.widgets.nattable.columnRename.DisplayColumnRenameDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHeaderCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHelper;
import org.eclipse.nebula.widgets.nattable.columnRename.event.RenameColumnHeaderEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;

/**
 * Responsible for rendering, event handling etc on the column headers.
 */
public class ColumnHeaderLayer extends DimensionallyDependentLayer {

	private final SelectionLayer selectionLayer;
	private final ILayerPainter layerPainter = new CellLayerPainter();
	protected RenameColumnHelper renameColumnHelper;

	/**
	 * @param baseLayer data provider for the column header layer
	 * @param horizontalLayerDependency typically the body layer
	 * @param selectionLayer required to respond to selection events
	 */
	public ColumnHeaderLayer(IUniqueIndexLayer baseLayer, ILayer horizontalLayerDependency, SelectionLayer selectionLayer) {
		this(baseLayer, horizontalLayerDependency, selectionLayer, true);
	}

	public ColumnHeaderLayer(IUniqueIndexLayer baseLayer, ILayer horizontalLayerDependency, SelectionLayer selectionLayer, boolean useDefaultConfiguration) {
		super(baseLayer, horizontalLayerDependency, baseLayer);
		this.selectionLayer = selectionLayer;
		this.renameColumnHelper = new RenameColumnHelper(this);
		registerPersistable(renameColumnHelper);

		selectionLayer.addLayerListener(new ColumnHeaderSelectionListener(this));
		registerCommandHandlers();

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultColumnHeaderLayerConfiguration());
		}
	}

	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		int selectionLayerColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, selectionLayer);
		if (selectionLayer.isColumnPositionSelected(selectionLayerColumnPosition)) {
			return DisplayMode.SELECT;
		} else {
			return super.getDisplayModeByPosition(columnPosition, rowPosition);
		}
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack labelStack = super.getConfigLabelsByPosition(columnPosition, rowPosition);

		final int selectionLayerColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, selectionLayer);
		if (selectionLayer.isColumnPositionFullySelected(selectionLayerColumnPosition)) {
			labelStack.addLabel(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
		}

		return labelStack;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}

	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		int columnIndex = getColumnIndexByPosition(columnPosition);
		if (isColumnRenamed(columnIndex)) {
			return getRenamedColumnLabelByIndex(columnIndex);
		}
		return super.getDataValueByPosition(columnPosition, rowPosition);
	}
	
	// Configuration
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new RenameColumnHeaderCommandHandler(this));
		registerCommandHandler(new DisplayColumnRenameDialogCommandHandler(this));
	}

	// Column header renaming

	/**
	 * @return column header as defined by the data source
	 */
	public String getOriginalColumnLabel(int columnPosition) {
		return super.getDataValueByPosition(columnPosition, 0).toString();
	}

	/**
	 * @return renamed column header if the column has been renamed, NULL otherwise
	 */
	public String getRenamedColumnLabel(int columnPosition) {
		int index = getColumnIndexByPosition(columnPosition);
		return getRenamedColumnLabelByIndex(index);
	}

	/**
	 * @return renamed column header if the column has been renamed, NULL otherwise
	 */
	public String getRenamedColumnLabelByIndex(int columnIndex) {
		return renameColumnHelper.getRenamedColumnLabel(columnIndex);
	}

	/**
	 * @return TRUE if the column at the given index has been given a custom name by the user.
	 */
	public boolean isColumnRenamed(int columnIndex) {
		return renameColumnHelper.isColumnRenamed(columnIndex);
	}

    public boolean renameColumnPosition(int columnPosition, String customColumnName) {
        boolean renamed = renameColumnHelper.renameColumnPosition(columnPosition, customColumnName);
        if (renamed) {
            fireLayerEvent(new RenameColumnHeaderEvent(this, columnPosition));
        }
        return renamed;
    }

    public boolean renameColumnIndex(int columnIndex, String customColumnName) {
        boolean renamed = renameColumnHelper.renameColumnIndex(columnIndex, customColumnName);
        if (renamed) {
            fireLayerEvent(new RenameColumnHeaderEvent(this, columnIndex));
        }
        return renamed;
    }
    
}
