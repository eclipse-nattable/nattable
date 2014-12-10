/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * This is a special kind of {@link IConfigLabelAccumulator} since it doesn't
 * add a label but remove one on a special condition. If the {@link LabelStack}
 * of a cell contains the {@link TreeLayer#TREE_COLUMN_CELL} label but there is
 * no active grouping, the label gets removed so there is no tree styling
 * (mainly left horizontal alignment) for the tree column.
 * <p>
 * Since the {@link TreeLayer#TREE_COLUMN_CELL} label will be removed by this
 * {@link IConfigLabelAccumulator}, it needs to be set to a label that is
 * located on top of the {@link TreeLayer} who adds the label. This for example
 * can be the ViewportLayer.
 * </p>
 */
public class GroupByConfigLabelModifier implements IConfigLabelAccumulator {

    private GroupByModel groupByModel;

    /**
     * Create a new {@link GroupByConfigLabelModifier} that removes the
     * {@link TreeLayer#TREE_COLUMN_CELL} label if no grouping is active.
     *
     * @param groupByModel
     *            The {@link GroupByModel} which is used to check whether a
     *            grouping is active or not.
     */
    public GroupByConfigLabelModifier(GroupByModel groupByModel) {
        this.groupByModel = groupByModel;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (configLabels.hasLabel(TreeLayer.TREE_COLUMN_CELL)
                && this.groupByModel.getGroupByColumnIndexes().isEmpty()) {
            configLabels.removeLabel(TreeLayer.TREE_COLUMN_CELL);
        }
    }

}
