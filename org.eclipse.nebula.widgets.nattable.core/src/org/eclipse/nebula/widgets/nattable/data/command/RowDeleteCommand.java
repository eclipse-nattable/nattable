/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.data.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to delete a row.
 *
 * @since 1.6
 */
public class RowDeleteCommand extends AbstractMultiRowCommand {

    /**
     * Creates a {@link RowDeleteCommand}.
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param rowPositions
     *            The row positions to delete.
     */
    public RowDeleteCommand(ILayer layer, int... rowPositions) {
        super(layer, rowPositions);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowDeleteCommand(RowDeleteCommand command) {
        super(command);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new RowDeleteCommand(this);
    }

}
