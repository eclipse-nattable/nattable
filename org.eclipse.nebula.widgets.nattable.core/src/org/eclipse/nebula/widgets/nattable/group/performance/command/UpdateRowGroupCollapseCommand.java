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
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;

/**
 * Command that is used to update the hidden indexes for a row group in the
 * RowGroupExpandCollapseLayer. Internally used to sync grouping related layers.
 *
 * @since 1.6
 */
public class UpdateRowGroupCollapseCommand extends AbstractContextFreeCommand {

    private final GroupModel groupModel;
    private final Group group;

    private final MutableIntSet indexesToHide = IntSets.mutable.empty();
    private final MutableIntSet indexesToShow = IntSets.mutable.empty();

    /**
     *
     * @param groupModel
     *            The {@link GroupModel} to which the {@link Group} belongs that
     *            should be updated.
     * @param group
     *            The {@link Group} that should be updated.
     */
    public UpdateRowGroupCollapseCommand(GroupModel groupModel, Group group) {
        this.groupModel = groupModel;
        this.group = group;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected UpdateRowGroupCollapseCommand(UpdateRowGroupCollapseCommand command) {
        this.groupModel = command.groupModel;
        this.group = command.group;
        this.indexesToHide.addAll(command.indexesToHide);
        this.indexesToShow.addAll(command.indexesToShow);
    }

    @Override
    public UpdateRowGroupCollapseCommand cloneCommand() {
        return new UpdateRowGroupCollapseCommand(this);
    }

    /**
     *
     * @return The {@link GroupModel} to which the {@link Group} belongs that
     *         should be updated.
     */
    public GroupModel getGroupModel() {
        return this.groupModel;
    }

    /**
     *
     * @return The {@link Group} that should be updated.
     */
    public Group getGroup() {
        return this.group;
    }

    /**
     *
     * @return The indexes to hide.
     * @since 2.0
     */
    public int[] getIndexesToHide() {
        return this.indexesToHide.toSortedArray();
    }

    /**
     *
     * @return The indexes to show.
     * @since 2.0
     */
    public int[] getIndexesToShow() {
        return this.indexesToShow.toSortedArray();
    }

    /**
     * Add the given indexes to the indexes to hide.
     *
     * @param indexes
     *            The indexes that should be added to the indexes to hide.
     * @since 2.0
     */
    public void addIndexesToHide(int... indexes) {
        this.indexesToHide.addAll(indexes);
    }

    /**
     * Add the given indexes to the indexes to show.
     *
     * @param indexes
     *            The indexes that should be added to the indexes to show.
     * @since 2.0
     */
    public void addIndexesToShow(int... indexes) {
        this.indexesToShow.addAll(indexes);
    }

}
