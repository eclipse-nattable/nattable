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
package org.eclipse.nebula.widgets.nattable.blink;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public abstract class BlinkingCellResolver implements IBlinkingCellResolver {

    @Override
    public abstract String[] resolve(Object oldValue, Object newValue);

    @Override
    public String[] resolve(ILayerCell cell, IConfigRegistry configRegistry, Object oldValue, Object newValue) {
        return resolve(oldValue, newValue);
    }

}
