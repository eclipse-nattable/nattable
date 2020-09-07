/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.convert;

/**
 * Converts the display value to a byte and vice versa.
 */
public class DefaultByteDisplayConverter extends NumericDisplayConverter {

    @Override
    protected Object convertToNumericValue(String value) {
        return Byte.valueOf(value);
    }

}
