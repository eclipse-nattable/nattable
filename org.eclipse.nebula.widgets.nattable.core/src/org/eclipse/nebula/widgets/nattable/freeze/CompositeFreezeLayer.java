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
package org.eclipse.nebula.widgets.nattable.freeze;

import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeCommandHandler;
import org.eclipse.nebula.widgets.nattable.freeze.config.DefaultFreezeGridBindings;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommandHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


public class CompositeFreezeLayer extends CompositeLayer implements IUniqueIndexLayer {

	private final FreezeLayer freezeLayer;
	private final ViewportLayer viewportLayer;
	private final SelectionLayer selectionLayer;
	private final ILayerPainter layerPainter = new FreezableLayerPainter();
	
	
	public CompositeFreezeLayer(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
		this(freezeLayer, viewportLayer, selectionLayer, true);
	}
	
	public CompositeFreezeLayer(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer,
			boolean useDefaultConfiguration) {
		super(2, 2);
		this.freezeLayer = freezeLayer;
		this.viewportLayer = viewportLayer;
		this.selectionLayer = selectionLayer;
		
		setChildLayer("FROZEN_REGION", freezeLayer, 0, 0); //$NON-NLS-1$
		setChildLayer("FROZEN_ROW_REGION", new DimensionallyDependentIndexLayer(viewportLayer.getScrollableLayer(), viewportLayer, freezeLayer), 1, 0); //$NON-NLS-1$
		setChildLayer("FROZEN_COLUMN_REGION", new DimensionallyDependentIndexLayer(viewportLayer.getScrollableLayer(), freezeLayer, viewportLayer), 0, 1); //$NON-NLS-1$
		setChildLayer("NONFROZEN_REGION", viewportLayer, 1, 1); //$NON-NLS-1$
		
		registerCommandHandlers();
		
		if (useDefaultConfiguration) {
			addConfiguration(new DefaultFreezeGridBindings());
		}
	}
	
	public boolean isFrozen() {
		return freezeLayer.isFrozen();
	}
	
	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new FreezeCommandHandler(freezeLayer, viewportLayer, selectionLayer));
		
		final DimensionallyDependentIndexLayer frozenRowLayer = (DimensionallyDependentIndexLayer) getChildLayerByLayoutCoordinate(1, 0);
		frozenRowLayer.registerCommandHandler(new ViewportSelectRowCommandHandler(frozenRowLayer));
		
		final DimensionallyDependentIndexLayer frozenColumnLayer = (DimensionallyDependentIndexLayer) getChildLayerByLayoutCoordinate(0, 1);
		frozenColumnLayer.registerCommandHandler(new ViewportSelectColumnCommandHandler(frozenColumnLayer));
	}
	
	
	@Override
	public boolean doCommand(ILayerCommand command) {
		//if this layer should handle a ClientAreaResizeCommand we have to ensure that
		//it is only called on the ViewportLayer, as otherwise an undefined behaviour
		//could occur because the ViewportLayer isn't informed about potential refreshes
		if (command instanceof ClientAreaResizeCommand) {
			this.viewportLayer.doCommand(command);
		}
		return super.doCommand(command);
	}

	public int getColumnPositionByIndex(int columnIndex) {
		int columnPosition = freezeLayer.getColumnPositionByIndex(columnIndex);
		if (columnPosition >= 0) {
			return columnPosition;
		}
		return freezeLayer.getColumnCount() + viewportLayer.getColumnPositionByIndex(columnIndex);
	}

	public int getRowPositionByIndex(int rowIndex) {
		int rowPosition = freezeLayer.getRowPositionByIndex(rowIndex);
		if (rowPosition >= 0) {
			return rowPosition;
		}
		return freezeLayer.getRowCount() + viewportLayer.getRowPositionByIndex(rowIndex);
	}


	// Persistence
	
	@Override
	public void saveState(String prefix, Properties properties) {
		PositionCoordinate coord = freezeLayer.getTopLeftPosition();
		properties.setProperty(prefix + FreezeLayer.PERSISTENCE_TOP_LEFT_POSITION, 
				coord.columnPosition + IPersistable.VALUE_SEPARATOR + coord.rowPosition);
		
		coord = freezeLayer.getBottomRightPosition();
		properties.setProperty(prefix + FreezeLayer.PERSISTENCE_BOTTOM_RIGHT_POSITION, 
				coord.columnPosition + IPersistable.VALUE_SEPARATOR + coord.rowPosition);
		
		super.saveState(prefix, properties);
	}
	
	@Override
	public void loadState(String prefix, Properties properties) {
		String property = properties.getProperty(prefix + FreezeLayer.PERSISTENCE_TOP_LEFT_POSITION);
		PositionCoordinate topLeftPosition = null;
		if (property != null) {
			StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
			String columnPosition = tok.nextToken();
			String rowPosition = tok.nextToken();
			topLeftPosition = new PositionCoordinate(this.freezeLayer, 
					Integer.valueOf(columnPosition), Integer.valueOf(rowPosition));
		}
		
		property = properties.getProperty(prefix + FreezeLayer.PERSISTENCE_BOTTOM_RIGHT_POSITION);
		PositionCoordinate bottomRightPosition = null;
		if (property != null) {
			StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
			String columnPosition = tok.nextToken();
			String rowPosition = tok.nextToken();
			bottomRightPosition = new PositionCoordinate(this.freezeLayer, 
					Integer.valueOf(columnPosition), Integer.valueOf(rowPosition));
		}

		//only restore a freeze state if there is one persisted
		if (topLeftPosition != null && bottomRightPosition != null) {
			if (topLeftPosition.columnPosition == -1 && topLeftPosition.rowPosition == -1
					&& bottomRightPosition.columnPosition == -1 && bottomRightPosition.rowPosition == -1) {
				FreezeHelper.unfreeze(this.freezeLayer, this.viewportLayer);
			} else {
				FreezeHelper.freeze(this.freezeLayer, this.viewportLayer, topLeftPosition, bottomRightPosition);
			}
		}
		
		super.loadState(prefix, properties);
	}

	
	class FreezableLayerPainter extends CompositeLayerPainter {
		
		public FreezableLayerPainter() {
		}
		
		@Override
		public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configRegistry) {
			super.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);
			
			Color separatorColor = configRegistry.getConfigAttribute(IFreezeConfigAttributes.SEPARATOR_COLOR, DisplayMode.NORMAL);
			if (separatorColor == null) {
				separatorColor = GUIHelper.COLOR_BLUE;
			}
			
			gc.setClipping(rectangle);
			Color oldFg = gc.getForeground();
			gc.setForeground(separatorColor);
			final int freezeWidth = freezeLayer.getWidth() - 1;
			if (freezeWidth > 0) {
				gc.drawLine(xOffset + freezeWidth, yOffset, xOffset + freezeWidth, yOffset + getHeight() - 1);
			}
			final int freezeHeight = freezeLayer.getHeight() - 1;
			if (freezeHeight > 0) {
				gc.drawLine(xOffset, yOffset + freezeHeight, xOffset + getWidth() - 1, yOffset + freezeHeight);
			}
			gc.setForeground(oldFg);
		}
		
	}

}
