/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

/**
 * Interface that specifies a handler that creates and tracks data changes.
 *
 * @since 1.6
 */
public interface DataChangeHandler {

    /**
     * Disable tracking of data changes to avoid tracking of changes that are
     * caused by save or discard operations.
     */
    void disableTracking();

    /**
     * Enable tracking of data changes to keep track of all changes.
     */
    void enableTracking();

    /**
     * Trigger handling of {@link IStructuralChangeEvent}s to update locally
     * stored changes, e.g. update the row index of a key in case a row above
     * was inserted or deleted.
     * 
     * @param event
     *            The event to handle.
     */
    void handleStructuralChange(IStructuralChangeEvent event);

    /**
     * Clear the locally stored changes.
     */
    void clearDataChanges();

    /**
     * Checks if the column with the given position contains cells in a dirty
     * state.
     *
     * @param columnPosition
     *            The position of the column whose dirty state should be
     *            checked.
     * @return <code>true</code> if the column contains cells that are marked as
     *         dirty (data has changed and not saved yet), <code>false</code> if
     *         not.
     */
    boolean isColumnDirty(int columnPosition);

    /**
     * Checks if the row with the given position contains cells in a dirty
     * state.
     *
     * @param rowPosition
     *            The position of the row whose dirty state should be checked.
     * @return <code>true</code> if the row contains cells that are marked as
     *         dirty (data has changed and not saved yet), <code>false</code> if
     *         not.
     */
    boolean isRowDirty(int rowPosition);

    /**
     * Checks if the cell at the given position is dirty.
     *
     * @param columnPosition
     *            The column position of the cell whose dirty state should be
     *            checked.
     * @param rowPosition
     *            The row position of the cell whose dirty state should be
     *            checked.
     * @return <code>true</code> if the cell is dirty (data has changed and not
     *         saved yet), <code>false</code> if not.
     */
    boolean isCellDirty(int columnPosition, int rowPosition);

}
