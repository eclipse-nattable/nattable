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


import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class DefaultBigIntegerDisplayConverterTest {

	private DefaultBigIntegerDisplayConverter bigIntConverter = new DefaultBigIntegerDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("123", bigIntConverter.canonicalToDisplayValue(new BigInteger("123")));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals(null, bigIntConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(new BigInteger("123"), bigIntConverter.displayToCanonicalValue("123"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(null, bigIntConverter.displayToCanonicalValue(""));
	}

	@Test(expected=ConversionFailedException.class)
	public void testConversionException() {
		bigIntConverter.displayToCanonicalValue("abc");
	}
}
