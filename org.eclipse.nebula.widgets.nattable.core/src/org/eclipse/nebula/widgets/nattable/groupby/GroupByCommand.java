/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.groupby;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger a groupBy action.
 *
 * @since 2.6
 */
public class GroupByCommand implements ILayerCommand {

    public enum GroupByAction {
        /**
         * Action to add indexes an existing grouping.
         */
        ADD,
        /**
         * Action to clear the existing grouping.
         */
        CLEAR,
        /**
         * Action to remove indexes from an existing grouping.
         */
        REMOVE,
        /**
         * Action to set indexes as new grouping which overrides the existing
         * grouping.
         */
        SET
    };

    private final GroupByAction action;
    private final int[] indexes;

    /**
     * Performs the specified groupBy action for the given indexes.
     *
     * @param action
     *            The groupBy action to perform
     *            <ul>
     *            <li>{@link GroupByAction#ADD} - Add the given indexes to the
     *            existing grouping.</li>
     *            <li>{@link GroupByAction#CLEAR} - Clears the existing
     *            grouping. Given indexes will be ignored.</li>
     *            <li>{@link GroupByAction#REMOVE} - Removes the given indexes
     *            from the existing grouping.</li>
     *            <li>{@link GroupByAction#SET} - Set the given indexes which
     *            overrides the existing grouping.</li>
     *            </ul>
     * @param indexes
     *            The indexes of the columns/rows that should be grouped.
     */
    public GroupByCommand(GroupByAction action, int... indexes) {
        this.action = action;
        this.indexes = indexes;
    }

    /**
     *
     * @return The groupBy action to perform.
     */
    public GroupByAction getAction() {
        return this.action;
    }

    /**
     *
     * @return The indexes of the columns/rows that should be grouped.
     */
    public int[] getIndexes() {
        return this.indexes;
    }

    @Override
    public GroupByCommand cloneCommand() {
        return this;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

}
