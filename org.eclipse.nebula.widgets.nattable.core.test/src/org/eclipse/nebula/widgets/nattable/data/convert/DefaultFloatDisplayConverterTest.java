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

public class DefaultFloatDisplayConverterTest {

	private DefaultFloatDisplayConverter floatConverter = new DefaultFloatDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("123.0", floatConverter.canonicalToDisplayValue(Float.valueOf("123")));
		Assert.assertEquals("23.5", floatConverter.canonicalToDisplayValue(Float.valueOf("23.5")));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals(null, floatConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(Float.valueOf("123"), floatConverter.displayToCanonicalValue("123"));
		Assert.assertEquals(Float.valueOf("23.5"), floatConverter.displayToCanonicalValue("23.5"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(null, floatConverter.displayToCanonicalValue(""));
	}

	@Test(expected=ConversionFailedException.class)
	public void testConversionException() {
		floatConverter.displayToCanonicalValue("abc");
	}
}
