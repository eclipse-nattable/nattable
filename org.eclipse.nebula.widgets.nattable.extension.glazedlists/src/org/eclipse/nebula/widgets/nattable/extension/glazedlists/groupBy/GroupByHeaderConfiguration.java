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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.action.GroupByDragMode;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.action.ColumnGroupHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

/**
 * The configuration for the GroupByHeaderLayer. Basically creates and registers
 * the {@link GroupByHeaderPainter} and the corresponding bindings for managing
 * groups.
 */
public class GroupByHeaderConfiguration extends AbstractRegistryConfiguration {

    private final GroupByHeaderPainter groupByHeaderPainter;
    private ColumnGroupHeaderLayer columnGroupHeaderLayer;

    /**
     *
     * @param groupByModel
     *            The {@link GroupByModel} needed to retrieve the groupBy state.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} needed to retrieve the column label.
     */
    public GroupByHeaderConfiguration(GroupByModel groupByModel, IDataProvider columnHeaderDataProvider) {
        this.groupByHeaderPainter = new GroupByHeaderPainter(groupByModel, columnHeaderDataProvider);
    }

    /**
     * @param groupByModel
     *            The {@link GroupByModel} needed to retrieve the groupBy state.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} needed to retrieve the column label.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} needed to retrieve the column
     *            label in case a user renamed a column.
     *
     * @since 1.5
     */
    public GroupByHeaderConfiguration(GroupByModel groupByModel, IDataProvider columnHeaderDataProvider, ColumnHeaderLayer columnHeaderLayer) {
        this.groupByHeaderPainter = new GroupByHeaderPainter(groupByModel, columnHeaderDataProvider, columnHeaderLayer);
    }

    /**
     * @param groupByModel
     *            The {@link GroupByModel} needed to retrieve the groupBy state.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} needed to retrieve the column label.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} needed to retrieve the column
     *            label in case a user renamed a column.
     * @param columnGroupHeaderLayer
     *            The new performance {@link ColumnGroupHeaderLayer}, needed in
     *            case column grouping is used with unbreakable groups, to avoid
     *            reordering can break the groups.
     *
     * @since 1.6
     */
    public GroupByHeaderConfiguration(
            GroupByModel groupByModel,
            IDataProvider columnHeaderDataProvider,
            ColumnHeaderLayer columnHeaderLayer,
            ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.groupByHeaderPainter = new GroupByHeaderPainter(groupByModel, columnHeaderDataProvider, columnHeaderLayer);
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                this.groupByHeaderPainter,
                DisplayMode.NORMAL,
                GroupByHeaderLayer.GROUP_BY_REGION);
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        if (this.columnGroupHeaderLayer == null) {
            uiBindingRegistry.registerFirstMouseDragMode(
                    MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                    new AggregateDragMode(
                            new CellDragMode(),
                            new GroupByColumnReorderDragMode(),
                            new GroupByDragMode()));
        } else {
            uiBindingRegistry.registerFirstMouseDragMode(
                    MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                    new AggregateDragMode(
                            new CellDragMode(),
                            new GroupByColumnGroupReorderDragMode(this.columnGroupHeaderLayer),
                            new GroupByDragMode()));

            // Need to register the ColumnGroupHeaderReorderDragMode again at
            // the first position in the registry. Necessary because the column
            // group header is part of the column header region. To ensure the
            // column group reorder handling is picked up first, it needs to be
            // first in the list of registered DragModes.

            // TODO Create class GroupByColumnGroupHeaderReorderDragMode
            // we use anonymous overriding here to get this fixed in a bugfix
            // release, but for consistency this should be a concrete class
            uiBindingRegistry.registerFirstMouseDragMode(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                    new AggregateDragMode(new CellDragMode(),
                            new ColumnGroupHeaderReorderDragMode(this.columnGroupHeaderLayer) {

                                @Override
                                public boolean isValidTargetColumnPosition(ILayer natLayer, int dragFromGridColumnPosition, int dragToGridColumnPosition) {
                                    // Suppress reorder if cursor is over the
                                    // group by region
                                    LabelStack regionLabels = natLayer.getRegionLabelsByXY(this.currentEvent.x, this.currentEvent.y);
                                    if (regionLabels != null && !regionLabels.hasLabel(GroupByHeaderLayer.GROUP_BY_REGION)) {
                                        return super.isValidTargetColumnPosition(natLayer, dragFromGridColumnPosition, dragToGridColumnPosition);
                                    }
                                    return false;
                                }

                            }));
        }
    }

    public GroupByHeaderPainter getGroupByHeaderPainter() {
        return this.groupByHeaderPainter;
    }

}
