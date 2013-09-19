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
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;

public interface SelectionStyleLabels {
    
    public static final String SELECTION_ANCHOR_STYLE = "selectionAnchor"; //$NON-NLS-1$
    
    public static final String SELECTION_ANCHOR_GRID_LINE_STYLE = "selectionAnchorGridLine"; //$NON-NLS-1$

    public static final String COLUMN_FULLY_SELECTED_STYLE = GridRegion.COLUMN_HEADER + "_FULL"; //$NON-NLS-1$
    
    public static final String ROW_FULLY_SELECTED_STYLE = GridRegion.ROW_HEADER + "_FULL"; //$NON-NLS-1$

}
