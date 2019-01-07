/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.command;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;

/**
 * Command that is used to update the hidden indexes for a column group in the
 * ColumnGroupExpandCollapseLayer.
 *
 * @since 1.6
 */
public class UpdateColumnGroupCollapseCommand extends AbstractContextFreeCommand {

    private final GroupModel groupModel;
    private final Group group;
    private final Collection<Integer> indexesToHide = new TreeSet<Integer>();
    private final Collection<Integer> indexesToShow = new TreeSet<Integer>();

    public UpdateColumnGroupCollapseCommand(GroupModel groupModel, Group group) {
        this.groupModel = groupModel;
        this.group = group;
    }

    protected UpdateColumnGroupCollapseCommand(UpdateColumnGroupCollapseCommand command) {
        this.groupModel = command.groupModel;
        this.group = command.group;
        this.indexesToHide.addAll(command.indexesToHide);
        this.indexesToShow.addAll(command.indexesToShow);
    }

    @Override
    public UpdateColumnGroupCollapseCommand cloneCommand() {
        return new UpdateColumnGroupCollapseCommand(this);
    }

    public GroupModel getGroupModel() {
        return this.groupModel;
    }

    public Group getGroup() {
        return this.group;
    }

    public Collection<Integer> getIndexesToHide() {
        return this.indexesToHide;
    }

    public Collection<Integer> getIndexesToShow() {
        return this.indexesToShow;
    }

}
