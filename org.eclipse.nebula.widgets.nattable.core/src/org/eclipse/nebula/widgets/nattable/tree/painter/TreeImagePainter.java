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
package org.eclipse.nebula.widgets.nattable.tree.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

public class TreeImagePainter extends ImagePainter {

	private final ITreeRowModel<?> treeRowModel;
	private final Image plusImage;
	private final Image minusImage;
	private final Image leafImage;
	
	public TreeImagePainter(ITreeRowModel<?> treeRowModel) {
		this(treeRowModel, GUIHelper.getImage("plus"), GUIHelper.getImage("minus"), GUIHelper.getImage("leaf")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public TreeImagePainter(ITreeRowModel<?> treeRowModel, Image plusImage, Image minusImage, Image leafImage) {
		super(null, false);
		this.treeRowModel = treeRowModel;
		this.plusImage = plusImage;
		this.minusImage = minusImage;
		this.leafImage = leafImage;
	}
	
	public Image getPlusImage() {
		return plusImage;
	}
	
	public Image getMinusImage() {
		return minusImage;
	}
	
	public Image getLeafImage() {
		return leafImage;
	}

	@Override
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		int index = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());
		return !this.treeRowModel.hasChildren(index) ? this.leafImage : this.treeRowModel.isCollapsed(index) ? this.plusImage : this.minusImage;
	}
	
}
