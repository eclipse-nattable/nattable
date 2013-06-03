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

import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

public class ComboBoxPainter extends CellPainterWrapper {

	/**
	 * Create a new {@link ComboBoxPainter} with the default image.
	 */
	public ComboBoxPainter() {
		this(GUIHelper.getImage("down_2")); //$NON-NLS-1$
	}
	
	/**
	 * Create a new {@link ComboBoxPainter} with the given {@link Image} as the image 
	 * marking the cell as a combo control.
	 * @param comboImage The image marking the cell as a combo control
	 */
	public ComboBoxPainter(Image comboImage) {
		setWrappedPainter(
				new CellPainterDecorator(
						new TextPainter(), CellEdgeEnum.RIGHT, 
						new ImagePainter(comboImage)));
	}
}
