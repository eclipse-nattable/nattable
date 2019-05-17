/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * Command to remove a row group identified by a row index.
 *
 * @since 1.6
 */
public class RemoveRowGroupCommand extends AbstractContextFreeCommand implements IRowGroupCommand {

    private int rowIndex;

    /**
     *
     * @param rowIndex
     *            The index of the row whose parent row group should be removed.
     */
    public RemoveRowGroupCommand(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     *
     * @return The index of the row whose parent row group should be removed.
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

}
