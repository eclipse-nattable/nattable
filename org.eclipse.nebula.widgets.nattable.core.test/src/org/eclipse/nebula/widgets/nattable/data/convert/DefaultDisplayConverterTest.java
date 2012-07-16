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


import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.junit.Assert;
import org.junit.Test;

public class DefaultDisplayConverterTest {

	private DefaultDisplayConverter defaultDisplayTypeConverter = new DefaultDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("abc", defaultDisplayTypeConverter.canonicalToDisplayValue("abc"));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals("", defaultDisplayTypeConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals("abc", defaultDisplayTypeConverter.displayToCanonicalValue("abc"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(null, defaultDisplayTypeConverter.displayToCanonicalValue(""));
	}
	
}
