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

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public abstract class ContextualDisplayConverter implements IDisplayConverter {

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        throw new UnsupportedOperationException(
                this.getClass().getName()
                        + " is a ContextualDisplayConverter and has therefore to be called with context informations."); //$NON-NLS-1$
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        throw new UnsupportedOperationException(
                this.getClass().getName()
                        + " is a ContextualDisplayConverter and has therefore to be called with context informations."); //$NON-NLS-1$
    }

    @Override
    public abstract Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue);

    @Override
    public abstract Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue);

}
