/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 454909
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.sort.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.event.SortColumnEvent;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Command handler that handles {@link SortColumnCommand}s. Applies sorting via
 * the {@link ISortModel} corresponding to the information that are send via the
 * {@link SortColumnCommand}.
 *
 * @see SortHeaderLayer
 * @see SortColumnCommand
 */
public class SortCommandHandler<T> extends AbstractLayerCommandHandler<SortColumnCommand> {

    private final ISortModel sortModel;
    private final SortHeaderLayer<T> sortHeaderLayer;

    public SortCommandHandler(ISortModel sortModel, SortHeaderLayer<T> sortHeaderLayer) {
        this.sortModel = sortModel;
        this.sortHeaderLayer = sortHeaderLayer;
    }

    @Override
    public boolean doCommand(final SortColumnCommand command) {
        final int columnIndex = command.getLayer().getColumnIndexByPosition(command.getColumnPosition());
        if (columnIndex >= 0) {
            final SortDirectionEnum newSortDirection = (command.getSortDirection() != null)
                    ? command.getSortDirection()
                    : getSortDirectionFromSortModel(columnIndex, command.isAccumulate());

            // Fire command - with busy indicator
            Runnable sortRunner = () -> SortCommandHandler.this.sortModel.sort(columnIndex, newSortDirection, command.isAccumulate());
            BusyIndicator.showWhile(null, sortRunner);

            // Fire event
            SortColumnEvent sortEvent = new SortColumnEvent(this.sortHeaderLayer, command.getColumnPosition());
            this.sortHeaderLayer.fireLayerEvent(sortEvent);
        }

        return true;
    }

    private SortDirectionEnum getSortDirectionFromSortModel(int columnIndex, boolean accumulate) {
        SortDirectionEnum sortDirection = this.sortModel.getSortDirection(columnIndex);
        // if there is a sorting applied for multiple columns and the sorting
        // should be accumulated and not the last column should be sorted,
        // only update the sort direction ASC vs. DESC and avoid to remove
        // the sorting
        if (accumulate
                && sortDirection != SortDirectionEnum.NONE
                && this.sortModel.getSortedColumnIndexes().size() > 1
                && this.sortModel.getSortOrder(columnIndex) < this.sortModel.getSortedColumnIndexes().size() - 1) {
            return (sortDirection == SortDirectionEnum.ASC) ? SortDirectionEnum.DESC : SortDirectionEnum.ASC;
        }
        return sortDirection.getNextSortDirection();
    }

    @Override
    public Class<SortColumnCommand> getCommandClass() {
        return SortColumnCommand.class;
    }

}
