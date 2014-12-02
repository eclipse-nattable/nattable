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
package org.eclipse.nebula.widgets.nattable.blink;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public abstract class BlinkingCellResolver implements IBlinkingCellResolver {

    @Override
    public abstract String[] resolve(Object oldValue, Object newValue);

    @Override
    public String[] resolve(ILayerCell cell, IConfigRegistry configRegistry,
            Object oldValue, Object newValue) {
        return resolve(oldValue, newValue);
    }

}
