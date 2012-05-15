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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotEmpty;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import org.eclipse.nebula.widgets.nattable.Messages;

/**
 * Converts the display value to a double and vice versa.
 */
public abstract class NumericDisplayConverter extends DisplayConverter {

	public Object canonicalToDisplayValue(Object canonicalValue) {
		try {
			if (isNotNull(canonicalValue)) {
				return canonicalValue.toString();
			}
			return null;
		} catch (Exception e) {
			return canonicalValue;
		}
	}

	public Object displayToCanonicalValue(Object displayValue) {
		try {
			if (isNotNull(displayValue) && isNotEmpty(displayValue.toString())) {
				return convertToNumericValue(displayValue.toString());
			}
			return null;
		} catch (Exception e) {
			throw new ConversionFailedException(
					Messages.getString("NumericDisplayConverter.failure", //$NON-NLS-1$
							new Object[] {displayValue}), e);
		}
	}
	
	protected abstract Object convertToNumericValue(String value);
}
