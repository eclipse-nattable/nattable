/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * Configuration attributes for the freeze separator visualization.
 */
public interface IFreezeConfigAttributes {

    /**
     * Configuration attribute to configure the color of the separator line.
     */
    ConfigAttribute<Color> SEPARATOR_COLOR = new ConfigAttribute<Color>();

    /**
     * Configuration attribute to configure the width of the separator line.
     *
     * @since 1.6
     */
    ConfigAttribute<Integer> SEPARATOR_WIDTH = new ConfigAttribute<Integer>();

}
