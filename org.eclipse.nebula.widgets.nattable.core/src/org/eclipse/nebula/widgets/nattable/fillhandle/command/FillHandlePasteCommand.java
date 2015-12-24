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
package org.eclipse.nebula.widgets.nattable.fillhandle.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command to trigger pasting data via fill handle drag operation.
 *
 * @since 1.4
 */
public class FillHandlePasteCommand extends AbstractContextFreeCommand {

    /**
     * The operation that should be triggered to fill the cells.
     */
    public enum FillHandleOperation {
        /**
         * Copy the current selected values to the cells in the fill handle
         * area.
         */
        COPY,
        /**
         * Fill the cells in the fill handle area by creating a series based on
         * the current selected values.
         */
        SERIES
    }

    /**
     * The {@link FillHandleOperation} that should be triggered.
     */
    public final FillHandleOperation operation;
    /**
     * The direction in which the fill handle was dragged. Necessary for the
     * series operation to calculate the values.
     */
    public final MoveDirectionEnum direction;
    /**
     * The {@link IConfigRegistry} needed to dynamically read configurations on
     * command handling, e.g. editable state.
     */
    public final IConfigRegistry configRegistry;

    /**
     * Create a FillHandlePasteCommand that triggers a copy operation.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to dynamically read
     *            configurations on command handling, e.g. editable state.
     */
    public FillHandlePasteCommand(IConfigRegistry configRegistry) {
        this(FillHandleOperation.COPY, null, configRegistry);
    }

    /**
     * Create a FillHandlePasteCommand.
     *
     * @param operation
     *            The {@link FillHandleOperation} that should be triggered.
     * @param direction
     *            The direction in which the fill handle was dragged. Necessary
     *            for the series operation to calculate the values.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to dynamically read
     *            configurations on command handling, e.g. editable state.
     */
    public FillHandlePasteCommand(
            FillHandleOperation operation, MoveDirectionEnum direction, IConfigRegistry configRegistry) {
        this.operation = operation;
        this.direction = direction;
        this.configRegistry = configRegistry;
    }
}
