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
package org.eclipse.nebula.widgets.nattable.group.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

public class ColumnGroupHeaderTextPainter extends CellPainterWrapper {

	/** Needed to query column group cell expand/collapse state */
	private final ColumnGroupModel columnGroupModel;

	public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel) {
		this.columnGroupModel = columnGroupModel;

		setWrappedPainter(new CellPainterDecorator( new TextPainter(), CellEdgeEnum.RIGHT, new ExpandCollapseImagePainter()));
	}

	/**
	 * @param columnGroupModel Column group model used by the grid
	 * @param interiorPainter for painting the text portion
	 * @param imagePainter for painting the icon image on the right
	 */
	public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel, ICellPainter interiorPainter) {
		this.columnGroupModel = columnGroupModel;

		setWrappedPainter(new CellPainterDecorator(interiorPainter, CellEdgeEnum.RIGHT, new ExpandCollapseImagePainter()));
	}

	/**
	 * Preferred width is used during auto resize.
	 * Column groups do not participate in auto resize, since auto resizing is
	 * done by the column width. Hence, always return 0
	 */
	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}

	@Override
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 25;
	}
	
	private class ExpandCollapseImagePainter extends ImagePainter {
		final Image rightImg = GUIHelper.getImage("right"); //$NON-NLS-1$
		final Image leftImg = GUIHelper.getImage("left"); //$NON-NLS-1$

		@Override
		protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
			Object dataValue = cell.getDataValue();
			if (dataValue != null) {
				String cellValue = dataValue.toString();
				ColumnGroup columnGroup = columnGroupModel.getColumnGroupByName(cellValue);
				
				if (columnGroup != null && columnGroup.isCollapseable()) {
					return columnGroup.isCollapsed() ? rightImg : leftImg;
				}
			}
			return null;
		}

	}

}
