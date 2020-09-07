/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
