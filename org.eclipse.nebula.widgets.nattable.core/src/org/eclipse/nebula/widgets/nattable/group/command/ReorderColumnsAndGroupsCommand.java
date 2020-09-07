/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;

/**
 * Reorder single multiple columns and column groups in one shot. - Needed by
 * the column chooser
 */
public class ReorderColumnsAndGroupsCommand extends MultiColumnReorderCommand {

    /**
     * If any of the fromColumnPositions contain a group - the group will be
     * moved.
     */
    public ReorderColumnsAndGroupsCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPositions) {
        super(layer, fromColumnPositions, toColumnPositions);
    }
}
