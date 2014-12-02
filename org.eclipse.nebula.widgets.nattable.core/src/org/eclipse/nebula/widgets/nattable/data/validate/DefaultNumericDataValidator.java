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
package org.eclipse.nebula.widgets.nattable.data.validate;

public class DefaultNumericDataValidator extends DataValidator {

    @Override
    public boolean validate(int columnIndex, int rowIndex, Object newValue) {
        try {
            if (newValue != null)
                new Double(newValue.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
