/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.nebula.widgets.nattable.copy.command.InternalCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalPasteDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleLayerPainter;
import org.eclipse.nebula.widgets.nattable.formula.CopySelectionLayerPainter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;

/**
 * Interface that contains labels that are used to style selection related
 * components.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface SelectionStyleLabels {

    /**
     * Label that is applied to the cell which is currently holding the
     * selection anchor.
     */
    String SELECTION_ANCHOR_STYLE = "selectionAnchor"; //$NON-NLS-1$

    /**
     * Label that is used to configure the line style of the selection grid
     * line. This is the line that surrounds an active selection. By default
     * this is the black dotted one pixel line.
     */
    String SELECTION_ANCHOR_GRID_LINE_STYLE = "selectionAnchorGridLine"; //$NON-NLS-1$

    /**
     * Label that is applied to the column header cell of the column that is
     * fully selected.
     */
    String COLUMN_FULLY_SELECTED_STYLE = GridRegion.COLUMN_HEADER + "_FULL"; //$NON-NLS-1$

    /**
     * Label that is applied to the row header cell of the row that is fully
     * selected.
     */
    String ROW_FULLY_SELECTED_STYLE = GridRegion.ROW_HEADER + "_FULL"; //$NON-NLS-1$

    /**
     * Label that is used to mark cells as part of the fill handle region. This
     * is the region that is <i>selected</i> via fill handle to trigger a fill
     * action (copy/series) by dragging a current selection.
     *
     * @since 1.4
     */
    String FILL_HANDLE_REGION = "FILL_HANDLE_REGION"; //$NON-NLS-1$

    /**
     * Label that is added to the bottom right cell of a contiguous selection.
     * Used to mark the cell for rendering the fill handle.
     *
     * @since 1.4
     */
    String FILL_HANDLE_CELL = "selectionHandleCell"; //$NON-NLS-1$

    /**
     * Style label for configuring the copy border.
     *
     * @see CopySelectionLayerPainter
     * @see FillHandleLayerPainter
     * @see InternalCopyDataCommandHandler
     * @see InternalPasteDataCommandHandler
     *
     * @since 1.4
     */
    String COPY_BORDER_STYLE = "copyBorderStyle"; //$NON-NLS-1$
}
