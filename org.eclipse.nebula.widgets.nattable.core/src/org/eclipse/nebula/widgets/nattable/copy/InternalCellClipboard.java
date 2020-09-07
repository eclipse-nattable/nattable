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
package org.eclipse.nebula.widgets.nattable.copy;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * The internal clipboard that is used for copy+paste within NatTable.
 *
 * @since 1.4
 */
public class InternalCellClipboard {

    private ILayerCell[][] copiedCells;

    /**
     * @return the copiedCells
     */
    public ILayerCell[][] getCopiedCells() {
        return this.copiedCells;
    }

    /**
     * @param copiedCells
     *            the copiedCells to set
     */
    public void setCopiedCells(ILayerCell[][] copiedCells) {
        this.copiedCells = copiedCells;
    }

    /**
     * Clears the clipboard.
     */
    public void clear() {
        setCopiedCells(null);
    }
}
