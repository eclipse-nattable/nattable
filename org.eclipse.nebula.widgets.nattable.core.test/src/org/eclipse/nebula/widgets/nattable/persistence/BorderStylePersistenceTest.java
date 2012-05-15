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
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.junit.Test;

public class BorderStylePersistenceTest {

	@Test
	public void toStringCreatesAPersistableString() throws Exception {
		BorderStyle borderStyle = new BorderStyle();
		assertEquals("1|0,0,0|SOLID", borderStyle.toString());
	}
	
	@Test
	public void canRecreateInstanceFromAPersistedString() throws Exception {
		BorderStyle borderStyle = new BorderStyle("2|100,110,120|DOTTED");
		
		assertEquals(2, borderStyle.getThickness());
		assertEquals(100, borderStyle.getColor().getRed());
		assertEquals(110, borderStyle.getColor().getGreen());
		assertEquals(120, borderStyle.getColor().getBlue());
		assertEquals(LineStyleEnum.DOTTED, borderStyle.getLineStyle());
	}
}
