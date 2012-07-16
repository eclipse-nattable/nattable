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

public class DefaultBooleanDisplayConverterTest {

	private DefaultBooleanDisplayConverter booleanConverter = new DefaultBooleanDisplayConverter();
	
	@Test
	public void testNonNullDataToDisplay() {
		Assert.assertEquals("true", booleanConverter.canonicalToDisplayValue(Boolean.TRUE));
		Assert.assertEquals("false", booleanConverter.canonicalToDisplayValue(Boolean.FALSE));
	}
	
	@Test
	public void testNullDataToDisplay() {
		Assert.assertEquals(null, booleanConverter.canonicalToDisplayValue(null));
	}
	
	@Test
	public void testNonNullDisplayToData() {
		Assert.assertEquals(Boolean.TRUE, booleanConverter.displayToCanonicalValue("true"));
		Assert.assertEquals(Boolean.FALSE, booleanConverter.displayToCanonicalValue("false"));
		Assert.assertEquals(Boolean.FALSE, booleanConverter.displayToCanonicalValue("123"));
	}
	
	@Test
	public void testNullDisplayToData() {
		Assert.assertEquals(Boolean.FALSE, booleanConverter.displayToCanonicalValue(""));
	}
	
}
