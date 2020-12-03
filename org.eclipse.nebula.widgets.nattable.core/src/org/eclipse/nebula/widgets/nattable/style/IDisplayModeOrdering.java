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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.List;

public interface IDisplayModeOrdering {

    /**
     *
     * @param targetDisplayMode
     *            The {@link DisplayMode} for which the ordering is requested.
     * @return An ordered list of {@link DisplayMode} descending from the target
     *         display mode.
     * @since 2.0
     */
    public List<DisplayMode> getDisplayModeOrdering(DisplayMode targetDisplayMode);

}
