/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRegionCommand;

/**
 * Command to reset the column width configurations. It will cause a reset of
 * all customizations done with regards to column width sizing, e.g. resized
 * columns will be reset to the initial default size and all columns are share
 * the same default resizable behavior.
 *
 * @since 1.6
 */
public class ColumnWidthResetCommand extends AbstractRegionCommand {

    /**
     * Flag to indicate whether a refresh event should be triggered or not.
     * Should be set to <code>false</code> in case additional actions should be
     * executed before the repainting of the table should be triggered.
     */
    public final boolean fireEvent;

    /**
     * Creates a {@link ColumnWidthResetCommand} to reset the column width
     * configuration of all regions, that triggers a refresh after the command
     * is handled.
     */
    public ColumnWidthResetCommand() {
        this(null, true);
    }

    /**
     * Creates a {@link ColumnWidthResetCommand} to reset the column width
     * configuration of all regions.
     *
     * @param fireEvent
     *            Flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the repainting of the table
     *            should be triggered.
     */
    public ColumnWidthResetCommand(boolean fireEvent) {
        this(null, fireEvent);
    }

    /**
     * Creates a {@link ColumnWidthResetCommand} to reset the column width
     * configuration of the region with the given label. Triggers a refresh
     * after the command is handled.
     *
     * @param label
     *            The region label of the region on which the command should be
     *            processed. If the label is <code>null</code> the command will
     *            be processed by all regions or until the first layer in the
     *            composition consumes the command.
     */
    public ColumnWidthResetCommand(String label) {
        this(label, true);
    }

    /**
     * Creates a {@link ColumnWidthResetCommand} to reset the column width
     * configuration of the region with the given label.
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
    public ColumnWidthResetCommand(String label, boolean fireEvent) {
        super(label);
        this.fireEvent = fireEvent;
    }

    @Override
    public ColumnWidthResetCommand cloneForRegion() {
        return new ColumnWidthResetCommand(null, this.fireEvent);
    }

}
