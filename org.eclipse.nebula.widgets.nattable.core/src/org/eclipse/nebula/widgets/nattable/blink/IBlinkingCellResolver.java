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
package org.eclipse.nebula.widgets.nattable.blink;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * This interface is used to determine whether a change requires a blink. This
 * is a way to add thresholds to blinking.
 */
public interface IBlinkingCellResolver {

    /**
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     * @return Possibly the config type associated with the blinking style.
     */
    public String[] resolve(Object oldValue, Object newValue);

    public String[] resolve(ILayerCell cell, IConfigRegistry configRegistry,
            Object oldValue, Object newValue);

}
