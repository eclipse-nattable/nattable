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
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TranslatedLayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * A composite layer is a layer that is made up of a number of underlying child layers. This class assumes that the child
 * layers are laid out in a regular grid pattern where the child layers in each composite row all have the same number of
 * rows and the same height, and the child layers in each composite column each have the same number of columns and
 * the same width.
 */
public class CompositeLayer extends AbstractLayer {

	private final int layoutXCount;

	private final int layoutYCount;

	private final Map<ILayer, String> childLayerToRegionNameMap = new HashMap<ILayer, String>();

	private final Map<String, IConfigLabelAccumulator> regionNameToConfigLabelAccumulatorMap = new HashMap<String, IConfigLabelAccumulator>();

	private final Map<ILayer, LayoutCoordinate> childLayerToLayoutCoordinateMap = new HashMap<ILayer, LayoutCoordinate>();

	/** Data struct. for child Layers */
	private final ILayer[][] childLayerLayout;

	private final CompositeLayerPainter compositeLayerPainter = new CompositeLayerPainter();

	public CompositeLayer(int layoutXCount, int layoutYCount) {
		this.layoutXCount = layoutXCount;
		this.layoutYCount = layoutYCount;
		childLayerLayout = new ILayer[layoutXCount][layoutYCount];
		
		setLayerPainter(compositeLayerPainter);
	}

	// Dispose

	@Override
	public void dispose() {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				ILayer childLayer = childLayerLayout[layoutX][layoutY];
				if (childLayer != null) {
					childLayer.dispose();
				}
			}
		}
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				ILayer childLayer = childLayerLayout[layoutX][layoutY];
				if (childLayer != null) {
					String regionName = childLayerToRegionNameMap.get(childLayer);
					childLayer.saveState(prefix + "." + regionName, properties); //$NON-NLS-1$
				}
			}
		}
		super.saveState(prefix, properties);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				ILayer childLayer = childLayerLayout[layoutX][layoutY];
				if (childLayer != null) {
					String regionName = childLayerToRegionNameMap.get(childLayer);
					childLayer.loadState(prefix + "." + regionName, properties); //$NON-NLS-1$
				}
			}
		}
		super.loadState(prefix, properties);
	}

	// Configuration

	@Override
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				childLayerLayout[layoutX][layoutY].configure(configRegistry, uiBindingRegistry);
			}
		}

		super.configure(configRegistry, uiBindingRegistry);
	}

	/**
	 * {@inheritDoc}
	 * Handle commands
	 */
	@Override
	public boolean doCommand(ILayerCommand command) {
		if (super.doCommand(command)) {
			return true;
		}
		return doCommandOnChildLayers(command);
	}

	protected boolean doCommandOnChildLayers(ILayerCommand command) {
		for (ILayer childLayer : childLayerToLayoutCoordinateMap.keySet()) {
			ILayerCommand childCommand = command.cloneCommand();
			if (childLayer.doCommand(childCommand)) {
				return true;
			}
		}
		return false;
	}

	// Horizontal features

	// Columns

	/**
	 * @return total number of columns being displayed
	 *    Note: Works off the header layers.
	 */
	@Override
	public int getColumnCount() {
		int columnCount = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			columnCount += childLayerLayout[layoutX][0].getColumnCount();
		}
		return columnCount;
	}

	@Override
	public int getPreferredColumnCount() {
		int preferredColumnCount = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			preferredColumnCount += childLayerLayout[layoutX][0].getPreferredColumnCount();
		}
		return preferredColumnCount;
	}

	/**
	 * @param compositeColumnPosition Column position in the {@link CompositeLayer}
	 * @return column index in the underlying layer.
	 */
	@Override
	public int getColumnIndexByPosition(int compositeColumnPosition) {
    	int layoutX = getLayoutXByColumnPosition(compositeColumnPosition);
		if (layoutX < 0) {
			return -1;
		}
    	ILayer childLayer = childLayerLayout[layoutX][0];
		int childColumnPosition = compositeColumnPosition - getColumnPositionOffset(layoutX);
		return childLayer.getColumnIndexByPosition(childColumnPosition);
	}

	@Override
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
    	int layoutX = getLayoutXByColumnPosition(localColumnPosition);
		if (layoutX < 0) {
			return -1;
		}
		return localColumnPosition - getColumnPositionOffset(layoutX);
	}

	@Override
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		Point layoutCoordinate = getLayoutXYByChildLayer(sourceUnderlyingLayer);
		if (layoutCoordinate == null) {
			return -1;
		}
		return getColumnPositionOffset(layoutCoordinate.x) + underlyingColumnPosition;
	}

	@Override
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		Point layoutCoordinate = getLayoutXYByChildLayer(sourceUnderlyingLayer);
		if (layoutCoordinate == null) {
			return null;
		}

		Collection<Range> localColumnPositionRanges = new ArrayList<Range>();

		int offset = getColumnPositionOffset(layoutCoordinate.x);
		for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
			localColumnPositionRanges.add(new Range(offset + underlyingColumnPositionRange.start, offset + underlyingColumnPositionRange.end));
		}

		return localColumnPositionRanges;
	}

	// Width

	@Override
	public int getWidth() {
		int width = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			width += childLayerLayout[layoutX][0].getWidth();
		}
		return width;
	}

	@Override
	public int getPreferredWidth() {
		int preferredWidth = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			preferredWidth += childLayerLayout[layoutX][0].getPreferredWidth();
		}
		return preferredWidth;
	}

	@Override
	public int getColumnWidthByPosition(int column) {
    	int layoutX = getLayoutXByColumnPosition(column);
		if (layoutX < 0) {
			return 0;
		}
		ILayer childLayer = childLayerLayout[layoutX][0];
		return childLayer.getColumnWidthByPosition(
				column - getColumnPositionOffset(layoutX) );
	}

	// Column resize

    @Override
	public boolean isColumnPositionResizable(int compositeColumnPosition) {
    	//Only looks at the header
    	int layoutX = getLayoutXByColumnPosition(compositeColumnPosition);
    	if (layoutX < 0) {
    		return false;
    	}
    	ILayer childLayer = childLayerLayout[layoutX][0];
		int childColumnPosition = compositeColumnPosition - getColumnPositionOffset(layoutX);
		return childLayer.isColumnPositionResizable(childColumnPosition);
    }

    // X

    /**
     * @param x pixel position - starts from 0
     */
    @Override
	public int getColumnPositionByX(int x) {
    	int layoutX = getLayoutXByPixelX(x);
    	if (layoutX < 0) {
    		return -1;
    	}
		ILayer childLayer = childLayerLayout[layoutX][0];
		int childX = x - getWidthOffset(layoutX);
		int childColumnPosition = childLayer.getColumnPositionByX(childX);
		return getColumnPositionOffset(layoutX) + childColumnPosition;
	}

    @Override
	public int getStartXOfColumnPosition(int columnPosition) {
    	int layoutX = getLayoutXByColumnPosition(columnPosition);
		if (layoutX < 0) {
			return -1;
		}
		ILayer childLayer = childLayerLayout[layoutX][0];
		int childColumnPosition = columnPosition - getColumnPositionOffset(layoutX);
		return getWidthOffset(layoutX) + childLayer.getStartXOfColumnPosition(childColumnPosition);
	}
    
    // Underlying

    @Override
	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();

		for (int layoutX = 0; layoutX < childLayerLayout.length; layoutX++) {
			int columnPositionOffset = getColumnPositionOffset(layoutX);
			if (columnPosition >= columnPositionOffset && columnPosition < columnPositionOffset + childLayerLayout[layoutX][0].getColumnCount()) {
				for (int layoutY = 0; layoutY < childLayerLayout[layoutX].length; layoutY++) {
					underlyingLayers.add(childLayerLayout[layoutX][layoutY]);
				}
				break;
			}
		}

		return underlyingLayers;
	}

	// Vertical features

	// Rows

	@Override
	public int getRowCount() {
		int rowCount = 0;
		for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
			rowCount += childLayerLayout[0][layoutY].getRowCount();
		}
		return rowCount;
	}

	@Override
	public int getPreferredRowCount() {
		int preferredRowCount = 0;
		for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
			preferredRowCount += childLayerLayout[0][layoutY].getPreferredRowCount();
		}
		return preferredRowCount;
	}

	@Override
	public int getRowIndexByPosition(int compositeRowPosition) {
		int layoutY = getLayoutYByRowPosition(compositeRowPosition);
		if (layoutY < 0) {
			return -1;
		}
		ILayer childLayer = childLayerLayout[0][layoutY];
		int childRowPosition = compositeRowPosition - getRowPositionOffset(layoutY);
		return childLayer.getRowIndexByPosition(childRowPosition);

	}

	@Override
	public int localToUnderlyingRowPosition(int localRowPosition) {
		int layoutY = getLayoutYByRowPosition(localRowPosition);
		if (layoutY < 0) {
			return -1;
		}
		return localRowPosition - getRowPositionOffset(layoutY);
	}

	@Override
	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		Point layoutCoordinate = getLayoutXYByChildLayer(sourceUnderlyingLayer);
		if (layoutCoordinate == null) {
			return -1;
		}
		return getRowPositionOffset(layoutCoordinate.y) + underlyingRowPosition;
	}

	@Override
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		Point layoutCoordinate = getLayoutXYByChildLayer(sourceUnderlyingLayer);
		if (layoutCoordinate == null) {
			return null;
		}

		Collection<Range> localRowPositionRanges = new ArrayList<Range>();

		int offset = getRowPositionOffset(layoutCoordinate.y);
		for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
			localRowPositionRanges.add(new Range(offset + underlyingRowPositionRange.start, offset + underlyingRowPositionRange.end));
		}

		return localRowPositionRanges;
	}

	// Height

	@Override
	public int getHeight() {
		int height = 0;
		for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
			height += childLayerLayout[0][layoutY].getHeight();
		}
		return height;
	}

	@Override
	public int getPreferredHeight() {
		int preferredHeight = 0;
		for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
			preferredHeight += childLayerLayout[0][layoutY].getPreferredHeight();
		}
		return preferredHeight;
	}

	@Override
	public int getRowHeightByPosition(int row) {
		int layoutY = getLayoutYByRowPosition(row);
		if (layoutY < 0) {
			return 0;
		}
		ILayer childLayer = childLayerLayout[0][layoutY];
		return childLayer.getRowHeightByPosition(
				row - getRowPositionOffset(layoutY) );
	}

    // Row resize

    /**
     * @return false if the row position is out of bounds
     */
    @Override
	public boolean isRowPositionResizable(int compositeRowPosition) {
    	int layoutY = getLayoutYByRowPosition(compositeRowPosition);
		if (layoutY < 0) {
			return false;
		}
		ILayer childLayer = childLayerLayout[0][layoutY];
    	int childRowPosition = compositeRowPosition - getRowPositionOffset(layoutY);
		return childLayer.isRowPositionResizable(childRowPosition);
    }

	// Y

	/**
	 * Get the <i>row</i> position relative to the layer the containing coordinate y.
	 * @param y Mouse event Y position.
	 */
    @Override
	public int getRowPositionByY(int y) {
    	int layoutY = getLayoutYByPixelY(y);
		if (layoutY < 0) {
			return -1;
		}
		ILayer childLayer = childLayerLayout[0][layoutY];
		int childY = y - getHeightOffset(layoutY);
		int childRowPosition = childLayer.getRowPositionByY(childY);
		return getRowPositionOffset(layoutY) + childRowPosition;
	}

    @Override
	public int getStartYOfRowPosition(int rowPosition) {
    	int layoutY = getLayoutYByRowPosition(rowPosition);
		if (layoutY < 0) {
			return -1;
		}
		ILayer childLayer = childLayerLayout[0][layoutY];
		int childRowPosition = rowPosition - getRowPositionOffset(layoutY);
		return getHeightOffset(layoutY) + childLayer.getStartYOfRowPosition(childRowPosition);
	}
    
    @Override
	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();

		for (int layoutY = 0; layoutY < childLayerLayout[0].length; layoutY++) {
			int rowPositionOffset = getRowPositionOffset(layoutY);
			if (rowPosition >= rowPositionOffset && rowPosition < rowPositionOffset + childLayerLayout[0][layoutY].getRowCount()) {
				for (int layoutX = 0; layoutX < childLayerLayout.length; layoutX++) {
					underlyingLayers.add(childLayerLayout[layoutX][layoutY]);
				}
				break;
			}
		}

		return underlyingLayers;
    }

	// Cell features

	@Override
	public ILayerCell getCellByPosition(int compositeColumnPosition, int compositeRowPosition) {
		Point layoutCoordinate = getLayoutXYByPosition(compositeColumnPosition, compositeRowPosition);

		if (layoutCoordinate == null) {
			return null;
		}

		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
		int childColumnPosition = compositeColumnPosition - getColumnPositionOffset(layoutCoordinate.x);
		int childRowPosition = compositeRowPosition - getRowPositionOffset(layoutCoordinate.y);

		ILayerCell cell = childLayer.getCellByPosition(childColumnPosition, childRowPosition);

		if (cell != null) {
			cell = new TranslatedLayerCell(
					cell,
					this,
					underlyingToLocalColumnPosition(childLayer, cell.getOriginColumnPosition()),
					underlyingToLocalRowPosition(childLayer, cell.getOriginRowPosition()),
					underlyingToLocalColumnPosition(childLayer, cell.getColumnPosition()),
					underlyingToLocalRowPosition(childLayer, cell.getRowPosition())
			);
		}

		return cell;
	}

	@Override
	public Rectangle getBoundsByPosition(int compositeColumnPosition, int compositeRowPosition) {
		Point layoutCoordinate = getLayoutXYByPosition(compositeColumnPosition, compositeRowPosition);

		if (layoutCoordinate == null) {
			return null;
		}

		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
		int childColumnPosition = compositeColumnPosition - getColumnPositionOffset(layoutCoordinate.x);
		int childRowPosition = compositeRowPosition - getRowPositionOffset(layoutCoordinate.y);

		final Rectangle bounds = childLayer.getBoundsByPosition(childColumnPosition, childRowPosition);

		if (bounds != null) {
			bounds.x += getWidthOffset(layoutCoordinate.x);
			bounds.y += getHeightOffset(layoutCoordinate.y);
		}

		return bounds;
	}

	@Override
	public String getDisplayModeByPosition(int compositeColumnPosition, int compositeRowPosition) {
		Point layoutCoordinate = getLayoutXYByPosition(compositeColumnPosition, compositeRowPosition);
		if (layoutCoordinate == null) {
			return super.getDisplayModeByPosition(compositeColumnPosition, compositeRowPosition);
		}
		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];

		return childLayer.getDisplayModeByPosition(
				compositeColumnPosition - getColumnPositionOffset(layoutCoordinate.x),
				compositeRowPosition - getRowPositionOffset(layoutCoordinate.y));
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int compositeColumnPosition, int compositeRowPosition) {
		Point layoutCoordinate = getLayoutXYByPosition(compositeColumnPosition, compositeRowPosition);
		if (layoutCoordinate == null) {
			return new LabelStack();
		}
		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];

		int childColumnPosition = compositeColumnPosition - getColumnPositionOffset(layoutCoordinate.x);
		int childRowPosition = compositeRowPosition - getRowPositionOffset(layoutCoordinate.y);
		LabelStack configLabels = childLayer.getConfigLabelsByPosition(childColumnPosition, childRowPosition);

		String regionName = childLayerToRegionNameMap.get(childLayer);
		IConfigLabelAccumulator configLabelAccumulator = regionNameToConfigLabelAccumulatorMap.get(regionName);
		if (configLabelAccumulator != null) {
			configLabelAccumulator.accumulateConfigLabels(configLabels, childColumnPosition, childRowPosition);
		}
		configLabels.addLabel(regionName);

		return configLabels;
	}

	@Override
	public Object getDataValueByPosition(int compositeColumnPosition, int compositeRowPosition) {
		Point layoutCoordinate = getLayoutXYByPosition(compositeColumnPosition, compositeRowPosition);
		if (layoutCoordinate == null) {
			return null;
		}

		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
		return childLayer.getDataValueByPosition(
				compositeColumnPosition - getColumnPositionOffset(layoutCoordinate.x),
				compositeRowPosition - getRowPositionOffset(layoutCoordinate.y));
	}

	@Override
	public ICellPainter getCellPainter(int compositeColumnPosition, int compositeRowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		Point layoutCoordinate = getLayoutXYByPosition(compositeColumnPosition, compositeRowPosition);
		if (layoutCoordinate == null) {
			return null;
		}
		
		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
		return childLayer.getCellPainter(
				compositeColumnPosition - getColumnPositionOffset(layoutCoordinate.x),
				compositeRowPosition - getRowPositionOffset(layoutCoordinate.y),
				cell,
				configRegistry);
	}
	
	// Child layer stuff

	public void setChildLayer(String regionName, ILayer childLayer, final int layoutX, final int layoutY) {
		if (childLayer == null) {
			throw new IllegalArgumentException("Cannot set null child layer"); //$NON-NLS-1$
		}

		childLayerToRegionNameMap.put(childLayer, regionName);

		childLayer.addLayerListener(this);
		childLayerToLayoutCoordinateMap.put(childLayer, new LayoutCoordinate(layoutX, layoutY));
		childLayerLayout[layoutX][layoutY] = childLayer;

		childLayer.setClientAreaProvider(new IClientAreaProvider() {
			@Override
			public Rectangle getClientArea() {
				return getChildClientArea(layoutX, layoutY);
			}
		});
	}

	public IConfigLabelAccumulator getConfigLabelAccumulatorByRegionName(String regionName) {
		return regionNameToConfigLabelAccumulatorMap.get(regionName);
	}

	/**
	 * Sets the IConfigLabelAccumulator for the given named region. Replaces any existing IConfigLabelAccumulator.
	 */
	public void setConfigLabelAccumulatorForRegion(String regionName, IConfigLabelAccumulator configLabelAccumulator) {
		regionNameToConfigLabelAccumulatorMap.put(regionName, configLabelAccumulator);
	}

	/**
	 *  Adds the configLabelAccumulator to the existing label accumulators.
	 */
	public void addConfigLabelAccumulatorForRegion(String regionName, IConfigLabelAccumulator configLabelAccumulator) {
		IConfigLabelAccumulator existingConfigLabelAccumulator = regionNameToConfigLabelAccumulatorMap.get(regionName);
		AggregrateConfigLabelAccumulator aggregateAccumulator;
		if (existingConfigLabelAccumulator instanceof AggregrateConfigLabelAccumulator) {
			aggregateAccumulator = (AggregrateConfigLabelAccumulator) existingConfigLabelAccumulator;
		} else {
			aggregateAccumulator = new AggregrateConfigLabelAccumulator();
			aggregateAccumulator.add(existingConfigLabelAccumulator);
			regionNameToConfigLabelAccumulatorMap.put(regionName, aggregateAccumulator);
		}
		aggregateAccumulator.add(configLabelAccumulator);
	}

	private Rectangle getChildClientArea(final int layoutX, final int layoutY) {
		final ILayer childLayer = childLayerLayout[layoutX][layoutY];

		final Rectangle compositeClientArea = getClientAreaProvider().getClientArea();

		final Rectangle childClientArea = new Rectangle(
				compositeClientArea.x + getWidthOffset(layoutX),
				compositeClientArea.y + getHeightOffset(layoutY),
				childLayer.getPreferredWidth(),
				childLayer.getPreferredHeight());

		final Rectangle intersection = compositeClientArea.intersection(childClientArea);

		return intersection;
	}

	/**
	 * @param layoutX col position in the CompositeLayer
	 * @param layoutY row position in the CompositeLayer
	 * @return child layer according to the Composite Layer Layout
	 */
	public ILayer getChildLayerByLayoutCoordinate(int layoutX, int layoutY) {
		if (layoutX < 0 || layoutX >= layoutXCount || layoutY < 0 || layoutY >= layoutYCount) {
			return null;
		} else {
			return childLayerLayout[layoutX][layoutY];
		}
	}

	/**
	 * @param x pixel position
	 * @param y pixel position
	 * @return Region which the given position is in
	 */
	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		Point layoutCoordinate = getLayoutXYByPixelXY(x, y);
		if (layoutCoordinate == null) {
			return null;
		}

		ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
		int childX = x - getWidthOffset(layoutCoordinate.x);
	    int childY = y - getHeightOffset(layoutCoordinate.y);
		LabelStack regionLabels = childLayer.getRegionLabelsByXY(childX, childY);

		String regionName = childLayerToRegionNameMap.get(childLayer);
		regionLabels.addLabel(regionName);

		return regionLabels;
	}

	@Override
	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		Point layoutCoordinate = getLayoutXYByPosition(columnPosition, rowPosition);
		return childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
	}

	// Layout coordinate accessors

	protected Point getLayoutXYByChildLayer(ILayer childLayer) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				if (childLayerLayout[layoutX][layoutY] == childLayer) {
					return new Point(layoutX, layoutY);
				}
			}
		}
		return null;
	}
	
	protected int getLayoutXByPixelX(int x) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ILayer childLayer = childLayerLayout[layoutX][0];
			if (childLayer == null) {
				return -1;
			}
			int widthOffset = getWidthOffset(layoutX);
			if (x >= widthOffset && x < widthOffset + childLayer.getWidth()) {
				return layoutX;
			}

			layoutX++;
		}
		
		return -1;
	}
	
	protected int getLayoutYByPixelY(int y) {
		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ILayer childLayer = getChildLayerByLayoutCoordinate(0, layoutY);
			if (childLayer == null) {
				return -1;
			}
			int heightOffset = getHeightOffset(layoutY);
			if (y >= heightOffset && y < heightOffset + childLayer.getHeight()) {
				return layoutY;
			}

			layoutY++;
		}
		
		return -1;
	}
	
	protected Point getLayoutXYByPixelXY(int x, int y) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ILayer childLayer = childLayerLayout[layoutX][0];
			if (childLayer == null) {
				return null;
			}
			int widthOffset = getWidthOffset(layoutX);
			if (x >= widthOffset && x < widthOffset + childLayer.getWidth()) {
				break;
			}

			layoutX++;
		}

		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ILayer childLayer = getChildLayerByLayoutCoordinate(layoutX, layoutY);
			if (childLayer == null) {
				return null;
			}
			int heightOffset = getHeightOffset(layoutY);
			if (y >= heightOffset && y < heightOffset + childLayer.getHeight()) {
				return new Point(layoutX, layoutY);
			}

			layoutY++;
		}

		return null;
	}

	protected int getLayoutXByColumnPosition(int compositeColumnPosition) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ILayer childLayer = childLayerLayout[layoutX][0];
			int columnPositionOffset = getColumnPositionOffset(layoutX);
			if (compositeColumnPosition >= columnPositionOffset && compositeColumnPosition < columnPositionOffset + childLayer.getColumnCount()) {
				return layoutX;
			}

			layoutX++;
		}
		return -1;
	}

	protected int getLayoutYByRowPosition(int compositeRowPosition) {
		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ILayer childLayer = childLayerLayout[0][layoutY];
			int rowPositionOffset = getRowPositionOffset(layoutY);
			if (compositeRowPosition >= rowPositionOffset && compositeRowPosition < rowPositionOffset + childLayer.getRowCount()) {
				return layoutY;
			}

			layoutY++;
		}

		return -1;
	}

	protected Point getLayoutXYByPosition(int compositeColumnPosition, int compositeRowPosition) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ILayer childLayer = childLayerLayout[layoutX][0];
			int columnPositionOffset = getColumnPositionOffset(layoutX);
			if (compositeColumnPosition >= columnPositionOffset && compositeColumnPosition < columnPositionOffset + childLayer.getColumnCount()) {
				break;
			}

			layoutX++;
		}

		if (layoutX >= layoutXCount) {
			return null;
		}

		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ILayer childLayer = childLayerLayout[layoutX][layoutY];
			int rowPositionOffset = getRowPositionOffset(layoutY);
			if (compositeRowPosition >= rowPositionOffset && compositeRowPosition < rowPositionOffset + childLayer.getRowCount()) {
				return new Point(layoutX, layoutY);
			}

			layoutY++;
		}

		return null;
	}
	
	// Offsets
	
	protected int getColumnPositionOffset(int layoutX) {
		int offset = 0;
		for (int x = 0; x < layoutX; x++) {
			offset += childLayerLayout[x][0].getColumnCount();
		}
		return offset;
	}
	
	protected int getWidthOffset(int layoutX) {
		int offset = 0;
		for (int x = 0; x < layoutX; x++) {
			offset += childLayerLayout[x][0].getWidth();
		}
		return offset;
	}
	
	protected int getRowPositionOffset(int layoutY) {
		int offset = 0;
		for (int y = 0; y < layoutY; y++) {
			offset += childLayerLayout[0][y].getRowCount();
		}
		return offset;
	}
	
	protected int getHeightOffset(int layoutY) {
		int offset = 0;
		for (int y = 0; y < layoutY; y++) {
			offset += childLayerLayout[0][y].getHeight();
		}
		return offset;
	}

	protected class CompositeLayerPainter implements ILayerPainter {

		@Override
		public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configuration) {
			int x = xOffset;
			for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
				int y = yOffset;
				for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
					ILayer childLayer = childLayerLayout[layoutX][layoutY];

					Rectangle childLayerRectangle = new Rectangle(x, y, childLayer.getWidth(), childLayer.getHeight());

					childLayerRectangle = rectangle.intersection(childLayerRectangle);

					Rectangle originalClipping = gc.getClipping();
					gc.setClipping(childLayerRectangle);

					childLayer.getLayerPainter().paintLayer(natLayer, gc, x, y, childLayerRectangle, configuration);

					gc.setClipping(originalClipping);
					y += childLayer.getHeight();
				}

				x += childLayerLayout[layoutX][0].getWidth();
			}
		}

		@Override
		public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle cellBounds) {
			Point layoutCoordinate = getLayoutXYByPosition(columnPosition, rowPosition);
			if (layoutCoordinate == null) {
				return null;
			}
			
			ILayer childLayer = childLayerLayout[layoutCoordinate.x][layoutCoordinate.y];
			if (childLayer == null) {
				return null;
			}
			
			int widthOffset = getWidthOffset(layoutCoordinate.x);
			int heightOffset = getHeightOffset(layoutCoordinate.y);

			cellBounds.x -= widthOffset;
			cellBounds.y -= heightOffset;
			
			ILayerPainter childLayerPainter = childLayer.getLayerPainter();
			int childColumnPosition = columnPosition - getColumnPositionOffset(layoutCoordinate.x);
			int childRowPosition = rowPosition - getRowPositionOffset(layoutCoordinate.y);
			Rectangle adjustedChildCellBounds = childLayerPainter.adjustCellBounds(childColumnPosition, childRowPosition, cellBounds);

			adjustedChildCellBounds.x += widthOffset;
			adjustedChildCellBounds.y += heightOffset;

			return adjustedChildCellBounds;
		}

	}

}
