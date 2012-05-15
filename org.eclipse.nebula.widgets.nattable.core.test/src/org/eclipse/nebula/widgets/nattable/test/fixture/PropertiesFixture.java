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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import java.util.Properties;

public class PropertiesFixture extends Properties {

	private static final long serialVersionUID = 1L;

	public static final String VALUE_1 = "value_1";
	public static final String VALUE_2 = "value_2";
	public static final String VALUE_3 = "value_3";
	public static final String PROPERY_3 = "propery_3";
	public static final String PROPERY_2 = "propery_2";
	public static final String PROPERY_1 = "propery_1";

	public PropertiesFixture() {
		super();
		setProperty(PROPERY_1, VALUE_1);
		setProperty(PROPERY_2, VALUE_2);
		setProperty(PROPERY_3, VALUE_3);
	}

	public PropertiesFixture addStyleProperties(String testPrefix) {
		setProperty(testPrefix + ".style.bg.color", "200,210,220");
		setProperty(testPrefix + ".style.fg.color", "100,110,120");

		setProperty(testPrefix + ".style.horizontalAlignment", "LEFT");
		setProperty(testPrefix + ".style.verticalAlignment", "TOP");

		setProperty(testPrefix + ".style.font", "1|Tahoma|8.25|0|WINDOWS|1|-11|0|0|0|400|0|0|0|1|0|0|0|0|Tahoma");

		setProperty(testPrefix + ".style.border", "2|100,110,120|DASHDOTDOT");

		return this;
	}

}
