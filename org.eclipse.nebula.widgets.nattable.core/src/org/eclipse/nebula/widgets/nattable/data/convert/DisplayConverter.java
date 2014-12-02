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

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public abstract class DisplayConverter implements IDisplayConverter {

    @Override
    public abstract Object canonicalToDisplayValue(Object canonicalValue);

    @Override
    public abstract Object displayToCanonicalValue(Object displayValue);

    @Override
    public Object canonicalToDisplayValue(ILayerCell cell,
            IConfigRegistry configRegistry, Object canonicalValue) {
        return canonicalToDisplayValue(canonicalValue);
    }

    @Override
    public Object displayToCanonicalValue(ILayerCell cell,
            IConfigRegistry configRegistry, Object displayValue) {
        return displayToCanonicalValue(displayValue);
    }

}
