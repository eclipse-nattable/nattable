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
 * Converts the display value to a {@link Character} and vice versa.
 */
public class DefaultCharacterDisplayConverter extends DisplayConverter {

    @Override
    public Object canonicalToDisplayValue(Object sourceValue) {
        return sourceValue != null ? sourceValue.toString() : ""; //$NON-NLS-1$
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        if (isNotNull(displayValue) && isNotEmpty(displayValue.toString())) {
            if (displayValue.toString().length() > 1) {
                throw new ConversionFailedException(
                        Messages.getString("DefaultCharacterDisplayConverter.failure", //$NON-NLS-1$
                                displayValue));
            } else {
                return displayValue.toString().charAt(0);
            }
        }
        return null;
    }
}
