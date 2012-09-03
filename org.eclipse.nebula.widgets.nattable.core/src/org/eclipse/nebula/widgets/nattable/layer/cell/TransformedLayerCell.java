/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class TransformedLayerCell extends AbstractLayerCell {

	private ILayerCell cell;
	
	public TransformedLayerCell(ILayerCell cell) {
		this.cell = cell;
	}
	
	public int getOriginColumnPosition() {
		return cell.getOriginColumnPosition();
	}

	public int getOriginRowPosition() {
		return cell.getOriginRowPosition();
	}

	public ILayer getLayer() {
		return cell.getLayer();
	}

	public int getColumnPosition() {
		return cell.getColumnPosition();
	}

	public int getRowPosition() {
		return cell.getRowPosition();
	}

	public int getColumnIndex() {
		return cell.getColumnIndex();
	}

	public int getRowIndex() {
		return cell.getRowIndex();
	}

	public int getColumnSpan() {
		return cell.getColumnSpan();
	}

	public int getRowSpan() {
		return cell.getRowSpan();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransformedLayerCell other = (TransformedLayerCell) obj;
		if (cell == null) {
			if (other.cell != null)
				return false;
		} else if (!cell.equals(other.cell))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
		return result;
	}

}
