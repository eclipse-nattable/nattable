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
package org.eclipse.nebula.widgets.nattable.group;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
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

public class RowGroupHeaderTextPainter<T> extends CellPainterWrapper {

	/** Needed to query row group cell expand/collapse state */
	private final IRowGroupModel<T> rowGroupModel;
	
	public RowGroupHeaderTextPainter(IRowGroupModel<T> rowGroupModel) {
		this.rowGroupModel = rowGroupModel;
				
		setWrappedPainter(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.BOTTOM, new ExpandCollapseImagePainter()));
	}
	
	/**
	 * @param rowGroupModel Column group model used by the grid
	 * @param interiorPainter for painting the text portion
	 * @param imagePainter for painting the icon image on the right
	 */
	public RowGroupHeaderTextPainter(IRowGroupModel<T> rowGroupModel, ICellPainter interiorPainter) {
		this.rowGroupModel = rowGroupModel;
		setWrappedPainter(new CellPainterDecorator(interiorPainter, CellEdgeEnum.BOTTOM, new ExpandCollapseImagePainter()));
	}
	
	@Override
	public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}

	public class ExpandCollapseImagePainter extends ImagePainter {
		final Image downImg = GUIHelper.getImage("down_0"); //$NON-NLS-1$
		final Image upImg = GUIHelper.getImage("up_0"); //$NON-NLS-1$

		public ExpandCollapseImagePainter() {
			super(null, true);
		}

		@Override
		protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
			final IRowGroup<T> rowGroup = RowGroupUtils.getTopMostParentGroup(RowGroupUtils.getRowGroupForRowIndex(RowGroupHeaderTextPainter.this.rowGroupModel, cell.getRowIndex()));
			
			if (rowGroup != null) {
				return rowGroup.isCollapsed() ? downImg : upImg;
			} else {
				return null;
			}
		}
	}
}
