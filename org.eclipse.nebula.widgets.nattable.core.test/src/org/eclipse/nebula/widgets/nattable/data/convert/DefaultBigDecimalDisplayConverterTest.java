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


import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class DefaultBigDecimalDisplayConverterTest {

	private DefaultBigDecimalDisplayConverter bigDecConverter = new DefaultBigDecimalDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("123", bigDecConverter.canonicalToDisplayValue(BigDecimal.valueOf(123)));
		Assert.assertEquals("23.5", bigDecConverter.canonicalToDisplayValue(BigDecimal.valueOf(23.5)));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals(null, bigDecConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(BigDecimal.valueOf(123), bigDecConverter.displayToCanonicalValue("123"));
		Assert.assertEquals(BigDecimal.valueOf(23.5), bigDecConverter.displayToCanonicalValue("23.5"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(null, bigDecConverter.displayToCanonicalValue(""));
	}

	@Test(expected=ConversionFailedException.class)
	public void testConversionException() {
		bigDecConverter.displayToCanonicalValue("abc");
	}
}
