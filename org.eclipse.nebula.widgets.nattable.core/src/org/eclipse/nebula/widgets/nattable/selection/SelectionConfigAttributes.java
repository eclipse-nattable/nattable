/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * This interface contains {@link ConfigAttribute}s that can be used to
 * configure selection rendering, like the border style of the active selection.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 1.4
 */
public interface SelectionConfigAttributes {

    /**
     * ConfigAttribute to configure the line style used to render the selection
     * border around selected cells. This is the line that surrounds an active
     * selection. By default this is the black dotted one pixel line.
     */
    ConfigAttribute<BorderStyle> SELECTION_GRID_LINE_STYLE = new ConfigAttribute<BorderStyle>();
}
