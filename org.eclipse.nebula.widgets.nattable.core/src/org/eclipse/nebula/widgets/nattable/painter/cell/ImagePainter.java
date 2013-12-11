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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Paints an image. If no image is provided, it will attempt to look up an image from the cell style.
 */
public class ImagePainter extends BackgroundPainter {

	private Image image;
	private final boolean paintBg;

	public ImagePainter() {
		this(null);
	}

	public ImagePainter(Image image) {
		this(image, true);
	}

	public ImagePainter(Image image, boolean paintBg) {
		this.image = image;
		this.paintBg = paintBg;
	}

	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		Image image = getImage(cell, configRegistry);
		if (image != null) {
			return image.getBounds().width;
		} else {
			return 0;
		}
	}

	@Override
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		Image image = getImage(cell, configRegistry);
		if (image != null) {
			return image.getBounds().height;
		} else {
			return 0;
		}
	}

	@Override
	public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		Image image = getImage(cell, configRegistry);
		if (image != null) {
			Rectangle imageBounds = image.getBounds();
			IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
			int x0 = bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width);
			int y0 = bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height);
			if (	x >= x0 &&
					x < x0 + imageBounds.width &&
					y >= y0 &&
					y < y0 + imageBounds.height) {
				return super.getCellPainterAt(x, y, cell, gc, bounds, configRegistry);
			}
		}
		return null;
	}
	
	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		if (paintBg) {
			super.paintCell(cell, gc, bounds, configRegistry);
		}

		Image image = getImage(cell, configRegistry);
		if (image != null) {
			Rectangle imageBounds = image.getBounds();
			IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
			gc.drawImage(
					image,
					bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width),
					bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height));
		}
	}
	
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		return image != null ? image : CellStyleUtil.getCellStyle(cell, configRegistry).getAttributeValue(CellStyleAttributes.IMAGE);
	}

}
