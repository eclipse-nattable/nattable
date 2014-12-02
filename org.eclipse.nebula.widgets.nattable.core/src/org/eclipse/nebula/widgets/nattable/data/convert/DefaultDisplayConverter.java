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
