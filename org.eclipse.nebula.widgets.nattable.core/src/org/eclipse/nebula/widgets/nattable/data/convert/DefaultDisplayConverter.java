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

public class DefaultDisplayConverter extends DisplayConverter {

    @Override
    public Object canonicalToDisplayValue(Object sourceValue) {
        return sourceValue != null ? sourceValue.toString() : ""; //$NON-NLS-1$
    }

    @Override
    public Object displayToCanonicalValue(Object destinationValue) {
        if (destinationValue == null
                || destinationValue.toString().length() == 0) {
            return null;
        } else {
            return destinationValue.toString();
        }
    }
}
