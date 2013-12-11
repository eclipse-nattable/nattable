/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.painter;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * IOverlayPainter that renders the left and the top border of a NatTable.
 * <p>
 * This is necessary because by default the left and the top borders are not
 * rendered so it is possible to create composite stacks that do not create
 * double sized borders (e.g. between row header and body). Mainly the issue 
 * results because of several offset calculations all over the NatTable
 * rendering code.
 * <p>
 * For completeness this painter is also able to render the right and the bottom border
 * to ensure that the border has the same color around the NatTable without changing
 * the grid line color.
 * 
 * @author Dirk Fauth
 *
 */
public class NatTableBorderOverlayPainter implements IOverlayPainter {

	/**
	 * The color that should be used to render the border around the NatTable.
	 */
	private final Color borderColor;
	
	/**
	 * Flag to specify whether to render all border lines or only the left and the top border.
	 * By default only the left and the top border lines are rendered as the right and the
	 * bottom border lines are already rendered by existing painters.
	 */
	private final boolean renderAllBorderLines;
	
	/**
	 * Creates a NatTableBorderOverlayPainter that paints gray border lines to the top and to 
	 * the left.
	 */
	public NatTableBorderOverlayPainter() {
		this(GUIHelper.COLOR_GRAY);
	}

	/**
	 * Creates a NatTableBorderOverlayPainter that paints gray border lines.
	 * @param renderAllBorderLines <code>true</code> if all border lines should be rendered,
	 * 			<code>false</code> if only the left and the top border line need to be rendered.
	 */
	public NatTableBorderOverlayPainter(final boolean renderAllBorderLines) {
		this(GUIHelper.COLOR_GRAY, renderAllBorderLines);
	}

	/**
	 * Creates a NatTableBorderOverlayPainter that paints border lines to the top and to the left.
	 * @param borderColor The color that should be used to render the border lines.
	 */
	public NatTableBorderOverlayPainter(final Color borderColor) {
		this(borderColor, false);
	}

	/**
	 * Creates a NatTableBorderOverlayPainter that paints border lines.
	 * @param borderColor The color that should be used to render the border lines.
	 * @param renderAllBorderLines <code>true</code> if all border lines should be rendered,
	 * 			<code>false</code> if only the left and the top border line need to be rendered.
	 */
	public NatTableBorderOverlayPainter(final Color borderColor, final boolean renderAllBorderLines) {
		this.borderColor = borderColor;
		this.renderAllBorderLines = renderAllBorderLines;
	}

	@Override
	public void paintOverlay(GC gc, ILayer layer) {
		Color beforeColor = gc.getForeground();
		
		gc.setForeground(this.borderColor);
		
		gc.drawLine(0, 0, 0, layer.getHeight()-1);
		gc.drawLine(0, 0, layer.getWidth()-1, 0);

		if (this.renderAllBorderLines) {
			gc.drawLine(layer.getWidth()-1, 0, layer.getWidth()-1, layer.getHeight()-1);
			gc.drawLine(0, layer.getHeight()-1, layer.getWidth()-1, layer.getHeight()-1);
		}
		
		gc.setForeground(beforeColor);
	}

}
