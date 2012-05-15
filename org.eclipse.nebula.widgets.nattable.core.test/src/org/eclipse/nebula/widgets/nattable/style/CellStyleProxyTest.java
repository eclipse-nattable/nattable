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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.Arrays;


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.StyleProxy;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.junit.Assert;
import org.junit.Test;

public class CellStyleProxyTest {

	private static final String TEST_CONFIG_LABEL1 = "testConfigLabel1";

	@Test
	public void proxyShouldRetreiveConfigAttributeUsingTheDisplayModeOrdering() throws Exception {
		ConfigRegistry configRegistry = new ConfigRegistry();
		
		Style testCellStyle1 = new Style();
		testCellStyle1.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);

		Style testCellStyle2 = new Style();
		testCellStyle2.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		testCellStyle2.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		
		//NORMAL mode has an horizontal align attribute registered
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, testCellStyle1, DisplayMode.NORMAL, TEST_CONFIG_LABEL1);
		
		//SELECT mode has a 'default' horizontal align attribute registered
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, testCellStyle2, DisplayMode.SELECT);

		//The 'default' from SELECT gets picked up
		StyleProxy cellStyleProxy = new CellStyleProxy(configRegistry, DisplayMode.SELECT, Arrays.asList(TEST_CONFIG_LABEL1));
		HorizontalAlignmentEnum alignmentFromProxy = cellStyleProxy.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
		
		Assert.assertEquals(HorizontalAlignmentEnum.CENTER, alignmentFromProxy);
	}
}
