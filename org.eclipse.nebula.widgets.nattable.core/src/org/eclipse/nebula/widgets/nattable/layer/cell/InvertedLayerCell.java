package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.InvertUtil;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.graphics.Rectangle;

public class InvertedLayerCell implements ILayerCell {

	private final ILayerCell layerCell;

	public InvertedLayerCell(ILayerCell layerCell) {
		this.layerCell = layerCell;
	}

	public void updateLayer(ILayer layer) {
		layerCell.updateLayer(layer);
	}

	public void updatePosition(ILayer layer, int originColumnPosition, int originRowPosition, int columnPosition, int rowPosition) {
		layerCell.updatePosition(layer, originRowPosition, originColumnPosition, rowPosition, columnPosition);
	}

	public void updateColumnSpan(int columnSpan) {
		layerCell.updateColumnSpan(columnSpan);
	}

	public void updateRowSpan(int rowSpan) {
		layerCell.updateRowSpan(rowSpan);
	}

	public ILayer getSourceLayer() {
		return layerCell.getSourceLayer();
	}

	public int getOriginColumnPosition() {
		return layerCell.getOriginColumnPosition();
	}

	public int getOriginRowPosition() {
		return layerCell.getOriginRowPosition();
	}

	public ILayer getLayer() {
		return layerCell.getLayer();
	}

	public int getColumnPosition() {
		return layerCell.getColumnIndex();
	}

	public int getRowPosition() {
		return layerCell.getRowPosition();
	}

	public int getColumnIndex() {
		return layerCell.getColumnIndex();
	}

	public int getRowIndex() {
		return layerCell.getRowIndex();
	}

	public int getColumnSpan() {
		return layerCell.getColumnSpan();
	}

	public int getRowSpan() {
		return layerCell.getRowSpan();
	}

	public boolean isSpannedCell() {
		return layerCell.isSpannedCell();
	}

	public String getDisplayMode() {
		return layerCell.getDisplayMode();
	}

	public LabelStack getConfigLabels() {
		return layerCell.getConfigLabels();
	}

	public Object getDataValue() {
		return layerCell.getDataValue();
	}

	public Rectangle getBounds() {
		return InvertUtil.invertRectangle(layerCell.getBounds());
	}
	
}
