/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * Command to reset the row size configurations. It will cause a reset of all
 * customizations done with regards to row sizing, e.g. resized rows will be
 * reset to the initial default size and all rows are share the same default
 * resizable behavior.
 *
 * @since 1.6
 */
public class ColumnSizeResetCommand extends AbstractContextFreeCommand {

    /**
     * Flag to indicate whether a refresh event should be triggered or not.
     * Should be set to <code>false</code> in case additional actions should be
     * executed before the repainting of the table should be triggered.
     */
    public final boolean fireEvent;

    /**
     * Creates a {@link ColumnSizeResetCommand} that triggers a refresh after
     * the command is handled.
     */
    public ColumnSizeResetCommand() {
        this(true);
    }

    /**
     * Creates a {@link ColumnSizeResetCommand}.
     *
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the repainting of the table
     *            should be triggered.
     */
    public ColumnSizeResetCommand(boolean fireEvent) {
        this.fireEvent = fireEvent;
    }

}
