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

public class DefaultIntegerDisplayConverterTest {

	private DefaultIntegerDisplayConverter intConverter = new DefaultIntegerDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("123", intConverter.canonicalToDisplayValue(Integer.valueOf("123")));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals(null, intConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(Integer.valueOf("123"), intConverter.displayToCanonicalValue("123"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(null, intConverter.displayToCanonicalValue(""));
	}

	@Test(expected=ConversionFailedException.class)
	public void testConversionException() {
		intConverter.displayToCanonicalValue("abc");
	}
}
