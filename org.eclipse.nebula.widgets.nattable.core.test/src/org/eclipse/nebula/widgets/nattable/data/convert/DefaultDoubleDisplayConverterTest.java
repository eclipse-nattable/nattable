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
package org.eclipse.nebula.widgets.nattable.data.convert;


import org.junit.Assert;
import org.junit.Test;

public class DefaultDoubleDisplayConverterTest {

	private DefaultDoubleDisplayConverter doubleConverter = new DefaultDoubleDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("123.0", doubleConverter.canonicalToDisplayValue(Double.valueOf("123")));
		Assert.assertEquals("23.5", doubleConverter.canonicalToDisplayValue(Double.valueOf("23.5")));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals(null, doubleConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(Double.valueOf("123"), doubleConverter.displayToCanonicalValue("123"));
		Assert.assertEquals(Double.valueOf("23.5"), doubleConverter.displayToCanonicalValue("23.5"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(null, doubleConverter.displayToCanonicalValue(""));
	}

	@Test(expected=ConversionFailedException.class)
	public void testConversionException() {
		doubleConverter.displayToCanonicalValue("abc");
	}
}
