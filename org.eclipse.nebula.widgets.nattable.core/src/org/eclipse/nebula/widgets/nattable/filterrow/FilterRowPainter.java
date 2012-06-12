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
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class FilterRowPainter extends CellPainterWrapper {

	private final FilterIconPainter filterIconPainter;
	private final int TOP_IMAGE_PADDING = 2;
	private final int RIGHT_IMAGE_PADDING = 0;
	
	public FilterRowPainter() {
		filterIconPainter = new FilterIconPainter();
		setWrappedPainter(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, new PaddingDecorator(
				filterIconPainter,
				TOP_IMAGE_PADDING,
				RIGHT_IMAGE_PADDING,
				0,
				0)));
	}
	
	public boolean containsRemoveFilterImage(int x, int y, ILayerCell cell, IConfigRegistry configRegistry) {
		Image image = filterIconPainter.getImage(cell, configRegistry);
		if (image == null) {
			return false;
		}
		int iconImageWidth = image.getImageData().width;
		int iconImageHeight = image.getImageData().height;
		Rectangle cellBounds = cell.getBounds();
		Rectangle imageBounds = new Rectangle(
				cellBounds.x + cellBounds.width - RIGHT_IMAGE_PADDING - iconImageWidth,
				cellBounds.y + TOP_IMAGE_PADDING,
				iconImageWidth,
				iconImageHeight).intersection(cellBounds);
		return imageBounds.contains(x, y);
	}
	
	static class FilterIconPainter extends ImagePainter {

		@Override
		protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
			// If a filter value is present draw the remove filter icon 
			if(ObjectUtils.isNotNull(cell.getDataValue())){
				return GUIHelper.getImage("remove_filter"); //$NON-NLS-1$
			}
			
			ICellEditor cellEditor = configRegistry.getConfigAttribute(
					EditConfigAttributes.CELL_EDITOR, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
			
			// If a comb box is specified as the editor, draw the combo box arrow 
			if (ObjectUtils.isNotNull(cellEditor) && cellEditor instanceof ComboBoxCellEditor) {
				return GUIHelper.getImage("down_2"); //$NON-NLS-1$
			}

			return null;
		}
	}
}
