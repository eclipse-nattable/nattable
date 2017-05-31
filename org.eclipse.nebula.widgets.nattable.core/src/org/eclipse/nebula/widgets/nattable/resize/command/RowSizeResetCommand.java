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

import org.eclipse.nebula.widgets.nattable.command.AbstractRegionCommand;

/**
 * Command to reset the row size configurations. It will cause a reset of all
 * customizations done with regards to row sizing, e.g. resized rows will be
 * reset to the initial default size and all rows are share the same default
 * resizable behavior.
 *
 * @since 1.6
 */
public class RowSizeResetCommand extends AbstractRegionCommand {

    /**
     * Flag to indicate whether a refresh event should be triggered or not.
     * Should be set to <code>false</code> in case additional actions should be
     * executed before the repainting of the table should be triggered.
     */
    public final boolean fireEvent;

    /**
     * Creates a {@link RowSizeResetCommand} to reset the size configuration of
     * all regions, that triggers a refresh after the command is handled.
     */
    public RowSizeResetCommand() {
        this(null, true);
    }

    /**
     * Creates a {@link RowSizeResetCommand} to reset the size configuration of
     * all regions.
     *
     * @param fireEvent
     *            Flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the repainting of the table
     *            should be triggered.
     */
    public RowSizeResetCommand(boolean fireEvent) {
        this(null, fireEvent);
    }

    /**
     * Creates a {@link RowSizeResetCommand} to reset the size configuration of
     * the region with the given label. Triggers a refresh after the command is
     * handled.
     *
     * @param label
     *            The region label of the region on which the command should be
     *            processed. If the label is <code>null</code> the command will
     *            be processed by all regions or until the first layer in the
     *            composition consumes the command.
     */
    public RowSizeResetCommand(String label) {
        this(label, true);
    }

    /**
     * Creates a {@link RowSizeResetCommand} to reset the size configuration of
     * the region with the given label.
     *
     * @param label
     *            The region label of the region on which the command should be
     *            processed. If the label is <code>null</code> the command will
     *            be processed by all regions or until the first layer in the
     *            composition consumes the command.
     * @param fireEvent
     *            Flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the repainting of the table
     *            should be triggered.
     */
    public RowSizeResetCommand(String label, boolean fireEvent) {
        super(label);
        this.fireEvent = fireEvent;
    }

    @Override
    public RowSizeResetCommand cloneForRegion() {
        return new RowSizeResetCommand(null, this.fireEvent);
    }
}
