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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.CheckBoxStateEnum;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class TreeCheckBoxPainter extends ImagePainter {
	
	private final Image checkedImg;
	private final Image semicheckedImg;
	private final Image uncheckedImg;

	public TreeCheckBoxPainter() {
		this(
				GUIHelper.getImage("checked"), //$NON-NLS-1$
				GUIHelper.getImage("semichecked"), //$NON-NLS-1$
				GUIHelper.getImage("unchecked") //$NON-NLS-1$
		);
	}

	public TreeCheckBoxPainter(Image checkedImg, Image semicheckedImage, Image uncheckedImg) {
		this.checkedImg = checkedImg;
		this.semicheckedImg = semicheckedImage;
		this.uncheckedImg = uncheckedImg;
	}

	public int getPreferredWidth(boolean checked) {
		return checked ? checkedImg.getBounds().width : uncheckedImg.getBounds().width;
	}

	public int getPreferredHeight(boolean checked) {
		return checked ? checkedImg.getBounds().height : uncheckedImg.getBounds().height;
	}

	public void paintIconImage(GC gc, Rectangle rectangle, int yOffset, boolean checked) {
		Image checkBoxImage = checked ? checkedImg : uncheckedImg;

		// Center image
		int x = rectangle.x + (rectangle.width / 2) - (checkBoxImage.getBounds().width/2);

		gc.drawImage(checkBoxImage, x, rectangle.y + yOffset);
	}

	@Override
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		switch (getCheckBoxState(cell)) {
		case CHECKED:
			return checkedImg;
		case SEMICHECKED:
			return semicheckedImg;
		default:
			return uncheckedImg;
		}
	}
	
	protected abstract CheckBoxStateEnum getCheckBoxState(ILayerCell cell);

}
