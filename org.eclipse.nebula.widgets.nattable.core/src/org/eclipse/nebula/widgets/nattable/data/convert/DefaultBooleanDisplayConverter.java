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
 * Data type converter for a Check Box. Assumes that the data value is stored as
 * a boolean.
 */
public class DefaultBooleanDisplayConverter extends DisplayConverter {

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        return Boolean.valueOf(displayValue.toString());
    }

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        if (canonicalValue == null) {
            return null;
        } else {
            return canonicalValue.toString();
        }
    }

}
