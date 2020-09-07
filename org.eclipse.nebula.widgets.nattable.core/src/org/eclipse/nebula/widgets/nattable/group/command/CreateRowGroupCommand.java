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
 * Command to create a row group.
 *
 * @since 1.6
 */
public class CreateRowGroupCommand extends AbstractContextFreeCommand implements IRowGroupCommand {

    private final String rowGroupName;

    /**
     *
     * @param rowGroupName
     *            The name that should be used for the new row group.
     */
    public CreateRowGroupCommand(String rowGroupName) {
        this.rowGroupName = rowGroupName;
    }

    /**
     *
     * @return The name that should be used for the new row group.
     */
    public String getRowGroupName() {
        return this.rowGroupName;
    }

}
