/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.config;

import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;

/**
 * Configuration attributes for configuring the visualization of a tree
 * representation.
 */
public final class TreeConfigAttributes {

    private TreeConfigAttributes() {
        // private default constructor for constants class
    }

    /**
     * Configuration attribute to specify the painter that should be used to
     * render the tree structure. It needs to be an
     * {@link IndentedTreeImagePainter} that can be wrapped with several
     * {@link CellPainterWrapper}. If there is no
     * {@link IndentedTreeImagePainter} in the painter hierarchy, this
     * configuration attribute will be ignored by the TreeLayer.
     */
    public static final ConfigAttribute<ICellPainter> TREE_STRUCTURE_PAINTER = new ConfigAttribute<>();

}
