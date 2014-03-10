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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultBigDecimalDisplayConverterTest {

	private DefaultBigDecimalDisplayConverter bigDecConverter = new DefaultBigDecimalDisplayConverter();
	
	private static Locale defaultLocale;
	
	@BeforeClass
	public static void setup() {
		defaultLocale = Locale.getDefault();
		Locale.setDefault(new Locale("en"));
	}
	
	@AfterClass
	public static void tearDown() {
		Locale.setDefault(defaultLocale);
	}
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("123", bigDecConverter.canonicalToDisplayValue(BigDecimal.valueOf(123)));
		Assert.assertEquals("23.5", bigDecConverter.canonicalToDisplayValue(BigDecimal.valueOf(23.5)));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertNull(bigDecConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(BigDecimal.valueOf(123), bigDecConverter.displayToCanonicalValue("123"));
		Assert.assertEquals(BigDecimal.valueOf(23.5), bigDecConverter.displayToCanonicalValue("23.5"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertNull(bigDecConverter.displayToCanonicalValue(""));
	}

	@Test(expected=ConversionFailedException.class)
	public void testConversionException() {
		bigDecConverter.displayToCanonicalValue("abc");
	}
	
	@Test
	public void testLocalizedDisplayConversion() {
		NumberFormat original = bigDecConverter.getNumberFormat();
		NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
		localized.setMinimumFractionDigits(0);
		localized.setMaximumFractionDigits(2);

		bigDecConverter.setNumberFormat(localized);
		Assert.assertEquals("123,5", bigDecConverter.canonicalToDisplayValue(new BigDecimal("123.5")));
		
		bigDecConverter.setNumberFormat(original);
	}
	
	@Test
	public void testLocalizedCanonicalConversion() {
		NumberFormat original = bigDecConverter.getNumberFormat();
		NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
		localized.setMinimumFractionDigits(0);
		localized.setMaximumFractionDigits(2);
		((DecimalFormat)localized).setParseBigDecimal(true);

		bigDecConverter.setNumberFormat(localized);
		Object result = bigDecConverter.displayToCanonicalValue("123,5");
		Assert.assertTrue(result instanceof BigDecimal);
		Assert.assertEquals(new BigDecimal("123.5"), result);
		
		bigDecConverter.setNumberFormat(original);
	}
}
