/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 454909
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.sort.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;

/**
 * Command to trigger sorting.
 *
 * @see SortHeaderLayer
 * @see SortCommandHandler
 */
public class SortColumnCommand extends AbstractColumnCommand {

    private boolean accumulate;
    private SortDirectionEnum sortDirection;

    /**
     * Create a {@link SortColumnCommand} that triggers sorting for replacing an
     * existing sort state (no accumulate) without specifying a sort direction.
     *
     * @param layer
     *            The layer to which the column position belongs.
     * @param columnPosition
     *            The position of the column for which the sorting should be
     *            applied.
     */
    public SortColumnCommand(ILayer layer, int columnPosition) {
        this(layer, columnPosition, false, null);
    }

    /**
     * Create a {@link SortColumnCommand} that triggers sorting without
     * specifying a sort direction.
     *
     * @param layer
     *            The layer to which the column position belongs.
     * @param columnPosition
     *            The position of the column for which the sorting should be
     *            applied.
     * @param accumulate
     *            <code>true</code> if the sorting should be accumulated to an
     *            already applied sorting, <code>false</code> if the sorting
     *            should replace an existing sorting.
     */
    public SortColumnCommand(ILayer layer, int columnPosition, boolean accumulate) {
        this(layer, columnPosition, accumulate, null);
    }

    /**
     * Create a {@link SortColumnCommand} that triggers sorting for replacing an
     * existing sort state (no accumulate).
     *
     * @param layer
     *            The layer to which the column position belongs.
     * @param columnPosition
     *            The position of the column for which the sorting should be
     *            applied.
     */
    public SortColumnCommand(ILayer layer, int columnPosition, SortDirectionEnum sortDirection) {
        this(layer, columnPosition, false, sortDirection);
    }

    /**
     *
     * @param layer
     *            The layer to which the column position belongs.
     * @param columnPosition
     *            The position of the column for which the sorting should be
     *            applied.
     * @param accumulate
     *            <code>true</code> if the sorting should be accumulated to an
     *            already applied sorting, <code>false</code> if the sorting
     *            should replace an existing sorting.
     * @param sortDirection
     *            The sort direction that should be used for sorting. Can be
     *            <code>null</code> which means that the sort direction will be
     *            used that is calculated by the ISortModel for the given
     *            column.
     */
    public SortColumnCommand(ILayer layer, int columnPosition, boolean accumulate, SortDirectionEnum sortDirection) {
        super(layer, columnPosition);
        this.accumulate = accumulate;
        this.sortDirection = sortDirection;
    }

    /**
     * Constructor that is used to clone a command.
     * 
     * @param command
     *            The command instance that should be cloned.
     */
    protected SortColumnCommand(SortColumnCommand command) {
        super(command);
        this.accumulate = command.accumulate;
        this.sortDirection = command.sortDirection;
    }

    /**
     *
     * @return <code>true</code> if the sorting should be accumulated to an
     *         already applied sorting, <code>false</code> if the sorting should
     *         replace an existing sorting.
     */
    public boolean isAccumulate() {
        return this.accumulate;
    }

    /**
     *
     * @return The sort direction that should be used for sorting. Can be
     *         <code>null</code> which means that the sort direction will be
     *         used that is calculated by the ISortModel for the given column.
     */
    public SortDirectionEnum getSortDirection() {
        return this.sortDirection;
    }

    @Override
    public SortColumnCommand cloneCommand() {
        return new SortColumnCommand(this);
    }

}
