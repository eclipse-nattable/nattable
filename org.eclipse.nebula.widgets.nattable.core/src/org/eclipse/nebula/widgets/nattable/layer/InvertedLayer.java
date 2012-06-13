package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.InvertedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class InvertedLayer implements ILayer {
	
	private ILayer underlyingLayer;
	
	public InvertedLayer(ILayer underlyingLayer) {
		this.underlyingLayer = underlyingLayer;
	}
	
	// ILayerListener
	
	public void handleLayerEvent(ILayerEvent event) {
		underlyingLayer.handleLayerEvent(event);
	}
	
	// IPersistable
	
	public void saveState(String prefix, Properties properties) {
		underlyingLayer.saveState(prefix, properties);
	}
	
	public void loadState(String prefix, Properties properties) {
		underlyingLayer.loadState(prefix, properties);
	}
	
	// Persistence
	
	public void registerPersistable(IPersistable persistable) {
		underlyingLayer.registerPersistable(persistable);
	}
	
	public void unregisterPersistable(IPersistable persistable) {
		underlyingLayer.unregisterPersistable(persistable);
	}
	
	// Configuration
	
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		underlyingLayer.configure(configRegistry, uiBindingRegistry);
	}
	
	// Region
	
	public LabelStack getRegionLabelsByXY(int x, int y) {
		return underlyingLayer.getRegionLabelsByXY(y, x);
	}
	
	// Commands
	
	public boolean doCommand(ILayerCommand command) {
		return underlyingLayer.doCommand(command);
	}
	
	// Events
	
	public void fireLayerEvent(ILayerEvent event) {
		underlyingLayer.fireLayerEvent(event);
	}
	
	public void addLayerListener(ILayerListener listener) {
		underlyingLayer.addLayerListener(listener);
	}
	
	public void removeLayerListener(ILayerListener listener) {
		underlyingLayer.removeLayerListener(listener);
	}
	
	public ILayerPainter getLayerPainter() {
		return new ILayerPainter() {

			public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configuration) {
				underlyingLayer.getLayerPainter().paintLayer(natLayer, gc, xOffset, yOffset, InvertUtil.invertRectangle(rectangle), configuration);
			}

			public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle cellBounds) {
				return underlyingLayer.getLayerPainter().adjustCellBounds(rowPosition, columnPosition, InvertUtil.invertRectangle(cellBounds));
			}
			
		};
	}
	
	// Client area

	public IClientAreaProvider getClientAreaProvider() {
		return underlyingLayer.getClientAreaProvider();
	}
	
	public void setClientAreaProvider(final IClientAreaProvider clientAreaProvider) {
		underlyingLayer.setClientAreaProvider(new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return InvertUtil.invertRectangle(clientAreaProvider.getClientArea());
			}
		});
	}
	
	// Horizontal features
	
	// Columns
	
	public int getColumnCount() {
		return underlyingLayer.getRowCount();
	}
	
	public int getPreferredColumnCount() {
		return underlyingLayer.getPreferredRowCount();
	}
	
	public int getColumnIndexByPosition(int columnPosition) {
		return underlyingLayer.getRowIndexByPosition(columnPosition);
	}
	
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return underlyingLayer.localToUnderlyingRowPosition(localColumnPosition);
	}
	
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		return underlyingLayer.underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingColumnPosition);
	}
	
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		return underlyingLayer.underlyingToLocalRowPositions(sourceUnderlyingLayer, underlyingColumnPositionRanges);
	}
	
	// Width
	
	public int getWidth() {
		return underlyingLayer.getHeight();
	}
	
	public int getPreferredWidth() {
		return underlyingLayer.getPreferredHeight();
	}
	
	public int getColumnWidthByPosition(int columnPosition) {
		return underlyingLayer.getRowHeightByPosition(columnPosition);
	}
	
	// Column resize
	
	public boolean isColumnPositionResizable(int columnPosition) {
		return underlyingLayer.isRowPositionResizable(columnPosition);
	}
	
	// X
	
	public int getColumnPositionByX(int x) {
		return underlyingLayer.getRowPositionByY(x);
	}
	
	public int getStartXOfColumnPosition(int columnPosition) {
		return underlyingLayer.getStartYOfRowPosition(columnPosition);
	}
	
	// Underlying
	
	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		return underlyingLayer.getUnderlyingLayersByRowPosition(columnPosition);
	}
	
	// Vertical features
	
	// Rows
	
	public int getRowCount() {
		return underlyingLayer.getColumnCount();
	}
	
	public int getPreferredRowCount() {
		return underlyingLayer.getPreferredColumnCount();
	}
	
	public int getRowIndexByPosition(int rowPosition) {
		return underlyingLayer.getColumnIndexByPosition(rowPosition);
	}
	
	public int localToUnderlyingRowPosition(int localRowPosition) {
		return underlyingLayer.localToUnderlyingColumnPosition(localRowPosition);
	}
	
	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		return underlyingLayer.underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingRowPosition);
	}
	
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		return underlyingLayer.underlyingToLocalColumnPositions(sourceUnderlyingLayer, underlyingRowPositionRanges);
	}
	
	// Height
	
	public int getHeight() {
		return underlyingLayer.getWidth();
	}
	
	public int getPreferredHeight() {
		return underlyingLayer.getPreferredWidth();
	}
	
	public int getRowHeightByPosition(int rowPosition) {
		return underlyingLayer.getColumnWidthByPosition(rowPosition);
	}
	
	// Row resize
	
	public boolean isRowPositionResizable(int rowPosition) {
		return underlyingLayer.isColumnPositionResizable(rowPosition);
	}
	
	// Y
	
	public int getRowPositionByY(int y) {
		return underlyingLayer.getColumnPositionByX(y);
	}
	
	public int getStartYOfRowPosition(int rowPosition) {
		return underlyingLayer.getStartXOfColumnPosition(rowPosition);
	}
	
	// Underlying
	
	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		return underlyingLayer.getUnderlyingLayersByColumnPosition(rowPosition);
	}
	
	// Cell features
	
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		ILayerCell cell = underlyingLayer.getCellByPosition(rowPosition, columnPosition);
		if (cell != null)
			return new InvertedLayerCell(cell);
		else
			return null;
	}
	
	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getBoundsByPosition(rowPosition, columnPosition);
	}
	
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getDisplayModeByPosition(rowPosition, columnPosition);
	}
	
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getConfigLabelsByPosition(rowPosition, columnPosition);
	}
	
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getDataValueByPosition(rowPosition, columnPosition);
	}
	
	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getUnderlyingLayerByPosition(rowPosition, columnPosition);
	}
	
	public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		return underlyingLayer.getCellPainter(rowPosition, columnPosition, cell, configRegistry);
	}
	
}
