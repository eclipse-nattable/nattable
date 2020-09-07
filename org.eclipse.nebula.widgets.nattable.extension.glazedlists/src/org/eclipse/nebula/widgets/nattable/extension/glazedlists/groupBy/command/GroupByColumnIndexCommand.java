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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class GroupByColumnIndexCommand implements ILayerCommand {

    private final int groupByColumnIndex;

    public GroupByColumnIndexCommand(int groupByColumnIndex) {
        this.groupByColumnIndex = groupByColumnIndex;
    }

    public int getGroupByColumnIndex() {
        return this.groupByColumnIndex;
    }

    @Override
    public GroupByColumnIndexCommand cloneCommand() {
        return this;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

}
