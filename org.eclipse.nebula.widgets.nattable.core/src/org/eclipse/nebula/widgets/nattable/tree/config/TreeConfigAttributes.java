/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.config;

import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;

/**
 * Configuration attributes for configuring the visualization of a tree
 * representation.
 *
 * @author Dirk Fauth
 *
 */
public interface TreeConfigAttributes {

    /**
     * Configuration attribute to specify the painter that should be used to
     * render the tree structure. It needs to be an
     * {@link IndentedTreeImagePainter} that can be wrapped with several
     * {@link CellPainterWrapper}. If there is no
     * {@link IndentedTreeImagePainter} in the painter hierarchy, this
     * configuration attribute will be ignored by the TreeLayer.
     */
    ConfigAttribute<ICellPainter> TREE_STRUCTURE_PAINTER = new ConfigAttribute<ICellPainter>();

}
