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
package org.eclipse.nebula.widgets.nattable.test.fixture;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class NatTableFixture extends NatTable {

	private static final int DEFAULT_HEIGHT = 400;
	private static final int DEFAULT_WIDTH = 600;

	private int eventCount = 0;
	private boolean updated = false;
	private ColumnOverrideLabelAccumulator columnLabelAccumulator;

	public NatTableFixture() {
		super(new Shell(), new DummyGridLayerStack(), true);
		initClientArea();
	}

	public NatTableFixture(ILayer underlyingLayer) {
		this(underlyingLayer, true);
	}

	public NatTableFixture(Shell shell, ILayer underlyingLayer) {
		super(shell, underlyingLayer, true);
		initClientArea();
	}

	public NatTableFixture(Shell shell, ILayer underlyingLayer, int width, int height) {
		super(shell, underlyingLayer, true);
		initClientArea(width, height);
	}

	public NatTableFixture(ILayer underlyingLayer, boolean autoconfigure) {
		super(new Shell(Display.getDefault()), underlyingLayer, autoconfigure);
		initClientArea();
	}


	public NatTableFixture(ILayer underlyingLayer, int width, int height, boolean autoconfigure) {
		super(new Shell(Display.getDefault()), underlyingLayer, autoconfigure);
		initClientArea(width, height);
	}

	private void initClientArea() {
		initClientArea(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	private void initClientArea(int width, int height) {
		setSize(width, height);
		doCommand(new InitializeClientAreaCommandFixture());
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		super.handleLayerEvent(event);
		eventCount++;
	}

	@Override
	public void updateResize() {
		updated = true;
	}

	public int getEventCount() {
		return eventCount;
	}

	public boolean isUpdated() {
		return updated;
	}

	// Convenience methods for tests

	public void registerLabelOnColumn(DataLayer bodyDataLayer, int columnIndex, String columnLabel) {
		getColumnLabelAccumulator(bodyDataLayer).registerColumnOverrides(columnIndex, columnLabel);
	}

	public void registerLabelOnColumnHeader(DataLayer columnHeaderDataLayer, int columnIndex, String columnLabel) {
		getColumnLabelAccumulator(columnHeaderDataLayer).registerColumnOverrides(columnIndex, columnLabel);
	}

	private ColumnOverrideLabelAccumulator getColumnLabelAccumulator(DataLayer dataLayer) {
		if (columnLabelAccumulator == null) {
			columnLabelAccumulator = new ColumnOverrideLabelAccumulator(dataLayer);
			dataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		}
		return columnLabelAccumulator;
	}

	public void scrollToColumn(int gridColumnPosition) {
		DummyGridLayerStack gridLayer = (DummyGridLayerStack) getUnderlyingLayerByPosition(1, 1);
		ViewportLayer viewportLayer = gridLayer.getBodyLayer().getViewportLayer();
		viewportLayer.invalidateHorizontalStructure();
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(gridColumnPosition));
	}

	public void scrollToRow(int gridRowPosition) {
		DummyGridLayerStack gridLayer = (DummyGridLayerStack) getUnderlyingLayerByPosition(1, 1);
		ViewportLayer viewportLayer = gridLayer.getBodyLayer().getViewportLayer();
		viewportLayer.invalidateVerticalStructure();
		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(gridRowPosition));
	}

	public void enableEditingOnAllCells() {
		getConfigRegistry().registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT);
	}

	@Override
	public ConfigRegistry getConfigRegistry() {
		return (ConfigRegistry) super.getConfigRegistry();
	}

}
