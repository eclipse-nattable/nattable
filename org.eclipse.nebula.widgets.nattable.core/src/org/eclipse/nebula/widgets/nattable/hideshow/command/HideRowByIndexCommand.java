/*******************************************************************************
 * Copyright (c) 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * Hide a row by index instead of position. Intended to be used to
 * programmatically hide rows in complex compositions.
 *
 * @since 2.0
 */
public class HideRowByIndexCommand extends AbstractContextFreeCommand {

    private int[] rowIndexes;

    /**
     *
     * @param rowIndexes
     *            The row indexes that should be hidden.
     */
    public HideRowByIndexCommand(int... rowIndexes) {
        this.rowIndexes = rowIndexes;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected HideRowByIndexCommand(HideRowByIndexCommand command) {
        this.rowIndexes = command.rowIndexes;
    }

    @Override
    public HideRowByIndexCommand cloneCommand() {
        return new HideRowByIndexCommand(this);
    }

    /**
     *
     * @return The row indexes that should be hidden.
     */
    public int[] getRowIndexes() {
        return this.rowIndexes;
    }
}
