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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Rectangle;

public class CellFixture extends LayerCell {

	public static final Rectangle TEST_BOUNDS = new Rectangle(0,0,10,10);
	public static final String TEST_CONFIG_TYPE = "default";
	public static final String TEST_CELL_DATA = "Test cell data";
	
	private String displayMode;
	private LabelStack configLabels;
	private Object dataValue;
	private Rectangle bounds;

	public CellFixture() {
		this(TEST_CELL_DATA);
	}
	
	public CellFixture(Object dataValue) {
		super(null, 0, 0);
		
		this.dataValue = dataValue;
		
		displayMode = DisplayMode.NORMAL;
		configLabels = new LabelStack(TEST_CONFIG_TYPE);
		bounds = TEST_BOUNDS;
	}
	
	@Override
	public String getDisplayMode() {
		return displayMode;
	}
	
	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}
	
	@Override
	public LabelStack getConfigLabels() {
		return configLabels;
	}
	
	public void setConfigLabels(LabelStack configLabels) {
		this.configLabels = configLabels;
	}

	public void addConfigLabels(String... additionalConfigLabels) {
		for (String configLabel : additionalConfigLabels) {
			configLabels.addLabel(configLabel);
		}
	}
	
	@Override
	public Object getDataValue() {
		return dataValue;
	}
	
	public void setDataValue(Object dataValue) {
		this.dataValue = dataValue;
	}
	
	@Override
	public Rectangle getBounds() {
		return bounds;
	}
	
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}
	
}
