/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public ReorderColumnsAndGroupsCommand(ILayer layer,
            List<Integer> fromColumnPositions, int toColumnPositions) {
        super(layer, fromColumnPositions, toColumnPositions);
    }
}
