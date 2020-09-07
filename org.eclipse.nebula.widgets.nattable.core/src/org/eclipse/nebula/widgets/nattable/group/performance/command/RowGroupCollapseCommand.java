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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;

/**
 * Command that is used to collapse row groups.
 *
 * @since 1.6
 */
public class RowGroupCollapseCommand extends AbstractContextFreeCommand {

    private final GroupModel groupModel;
    private final List<Group> groups;

    public RowGroupCollapseCommand(GroupModel groupModel, Group... groups) {
        this.groupModel = groupModel;
        this.groups = Arrays.asList(groups);
    }

    public RowGroupCollapseCommand(GroupModel groupModel, Collection<Group> groups) {
        this.groupModel = groupModel;
        this.groups = new ArrayList<Group>(groups);
    }

    protected RowGroupCollapseCommand(RowGroupCollapseCommand command) {
        this.groupModel = command.groupModel;
        this.groups = new ArrayList<Group>(command.groups);
    }

    @Override
    public RowGroupCollapseCommand cloneCommand() {
        return new RowGroupCollapseCommand(this);
    }

    public GroupModel getGroupModel() {
        return this.groupModel;
    }

    public List<Group> getGroups() {
        return this.groups;
    }

}
