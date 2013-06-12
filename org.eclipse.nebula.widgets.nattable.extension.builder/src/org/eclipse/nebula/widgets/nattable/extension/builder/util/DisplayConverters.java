/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.builder.util;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;


/**
 * Converts between data source and display values for numeric data.
 */
public class DisplayConverters {

	/**
	 * Convert using the FormatterFactory. (Typically Broker and Book columns)
	 *
	 * @see BrokerFormatter
	 * @see BookFormatter
	 */
	public static IDisplayConverter getDoubleDisplayConverter(final String pattern) {
		return new DisplayConverter() {
			private final DecimalFormat formatter = isNotNull(pattern) ? new DecimalFormat(pattern) : new DecimalFormat();

			public Object canonicalToDisplayValue(Object canonicalValue) {
				if (isNotNull(canonicalValue)){
					return formatter.format(canonicalValue);
				}
				return canonicalValue;
			}

			public Object displayToCanonicalValue(Object displayValue) {
				try {
					if (isNotNull(displayValue)) {
						return formatter.parse(displayValue.toString()).doubleValue();
					}
				} catch (ParseException e) {
					System.err.println("Error while converting the followiung to canonical value: " + displayValue);
					e.printStackTrace(System.err);
				}
				return displayValue;
			}
		};
	}
}
