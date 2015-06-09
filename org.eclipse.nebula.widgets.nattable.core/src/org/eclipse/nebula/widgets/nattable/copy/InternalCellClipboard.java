/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
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
