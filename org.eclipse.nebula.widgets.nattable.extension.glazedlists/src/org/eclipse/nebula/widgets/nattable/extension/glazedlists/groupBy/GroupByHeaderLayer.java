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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.GroupByColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.UngroupByColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

public class GroupByHeaderLayer extends DimensionallyDependentLayer {

    public static final String GROUP_BY_REGION = "GROUP_BY_REGION"; //$NON-NLS-1$

    private final GroupByModel groupByModel;

    private GroupByHeaderPainter groupByHeaderPainter;

    private boolean visible = true;

    public GroupByHeaderLayer(GroupByModel groupByModel, ILayer gridLayer,
            IDataProvider columnHeaderDataProvider) {
        super(new DataLayer(new IDataProvider() {
            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                return null;
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex,
                    Object newValue) {}

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount() {
                return 1;
            }
        }));

        setHorizontalLayerDependency(gridLayer);
        setVerticalLayerDependency(getBaseLayer());

        this.groupByModel = groupByModel;
        registerPersistable(this.groupByModel);

        registerCommandHandler(new GroupByColumnCommandHandler(this));
        registerCommandHandler(new UngroupByColumnCommandHandler(this));

        GroupByHeaderConfiguration configuration = new GroupByHeaderConfiguration(
                groupByModel, columnHeaderDataProvider);
        addConfiguration(configuration);

        groupByHeaderPainter = configuration.getGroupByHeaderPainter();
        ((DataLayer) getBaseLayer()).setRowHeightByPosition(0,
                groupByHeaderPainter.getPreferredHeight());
    }

    public GroupByModel getGroupByModel() {
        return this.groupByModel;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        fireLayerEvent(new RowStructuralRefreshEvent(getBaseLayer()));
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public int getHeight() {
        if (visible) {
            return super.getHeight();
        }
        return 0;
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        if (visible) {
            return super.getRowHeightByPosition(rowPosition);
        }
        return 0;
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        return new LayerCell(this, 0, 0, 0, 0, getColumnCount(), 1);
    }

    public int getGroupByColumnIndexAtXY(int x, int y) {
        return groupByHeaderPainter.getGroupByColumnIndexAtXY(x, y);
    }

}
