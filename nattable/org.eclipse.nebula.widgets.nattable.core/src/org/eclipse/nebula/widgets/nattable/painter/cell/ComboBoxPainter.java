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

import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

public class ComboBoxPainter extends CellPainterWrapper {

	public ComboBoxPainter() {
		setWrappedPainter(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, new ImagePainter(GUIHelper.getImage("down_2")))); //$NON-NLS-1$
	}
	
}
