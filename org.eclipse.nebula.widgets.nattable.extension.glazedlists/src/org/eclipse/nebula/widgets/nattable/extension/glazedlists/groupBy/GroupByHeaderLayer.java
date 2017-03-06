/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453177
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.GroupByColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.UngroupByColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

/**
 * The layer that is used to render the groupBy region where columns can be
 * dragged from to perform grouping and ungrouping actions.
 */
public class GroupByHeaderLayer extends DimensionallyDependentLayer {

    public static final String GROUP_BY_REGION = "GROUP_BY_REGION"; //$NON-NLS-1$

    private final GroupByModel groupByModel;

    private GroupByHeaderPainter groupByHeaderPainter;

    private boolean visible = true;

    /**
     * Create a {@link GroupByHeaderLayer} that uses the default
     * {@link GroupByHeaderConfiguration}.
     *
     * @param groupByModel
     *            The {@link GroupByModel} needed for grouping operations.
     * @param horizontalLayerDependency
     *            The {@link ILayer} to which this header layer is horizontally
     *            dependent.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header which is needed
     *            to create the {@link GroupByHeaderConfiguration}
     */
    public GroupByHeaderLayer(
            GroupByModel groupByModel, ILayer horizontalLayerDependency,
            IDataProvider columnHeaderDataProvider) {
        this(groupByModel, horizontalLayerDependency, columnHeaderDataProvider, null, null);
    }

    /**
     * Create a {@link GroupByHeaderLayer} that uses the default
     * {@link GroupByHeaderConfiguration}.
     *
     * @param groupByModel
     *            The {@link GroupByModel} needed for grouping operations.
     * @param horizontalLayerDependency
     *            The {@link ILayer} to which this header layer is horizontally
     *            dependent.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header which is needed
     *            to create the {@link GroupByHeaderConfiguration}
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} which should be used to create
     *            the {@link GroupByHeaderConfiguration} if it should support
     *            showing the renamed column headers. Can be <code>null</code>
     *            which results in using either the given
     *            columnHeaderDataProvider or the given
     *            {@link GroupByHeaderConfiguration}.
     *
     * @since 1.5
     */
    public GroupByHeaderLayer(
            GroupByModel groupByModel, ILayer horizontalLayerDependency,
            IDataProvider columnHeaderDataProvider, ColumnHeaderLayer columnHeaderLayer) {
        this(groupByModel, horizontalLayerDependency, columnHeaderDataProvider, columnHeaderLayer, null);
    }

    /**
     * Create a {@link GroupByHeaderLayer} that uses the given
     * {@link GroupByHeaderConfiguration}.
     *
     * @param groupByModel
     *            The {@link GroupByModel} needed for grouping operations.
     * @param horizontalLayerDependency
     *            The {@link ILayer} to which this header layer is horizontally
     *            dependent.
     * @param groupByHeaderConfiguration
     *            The {@link GroupByHeaderConfiguration} that should be added to
     *            this {@link GroupByHeaderLayer}. Needs to be a
     *            {@link GroupByHeaderConfiguration} because we retrieve the
     *            necessary {@link GroupByHeaderPainter} out of the
     *            configuration to setup this layer accordingly.
     */
    public GroupByHeaderLayer(
            GroupByModel groupByModel, ILayer horizontalLayerDependency,
            GroupByHeaderConfiguration groupByHeaderConfiguration) {
        this(groupByModel, horizontalLayerDependency, null, groupByHeaderConfiguration);
    }

    /**
     * Create a {@link GroupByHeaderLayer} by either using the given
     * {@link GroupByHeaderConfiguration} or creating a new
     * {@link GroupByHeaderConfiguration} using the given column header
     * {@link IDataProvider}. Note that either the {@link IDataProvider} or the
     * {@link GroupByHeaderConfiguration} parameter must be set. If both are
     * <code>null</code> an {@link IllegalArgumentException} will be thrown.
     *
     * @param groupByModel
     *            The {@link GroupByModel} needed for grouping operations.
     * @param horizontalLayerDependency
     *            The {@link ILayer} to which this header layer is horizontally
     *            dependent.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header which is needed
     *            to create the {@link GroupByHeaderConfiguration}. Can be
     *            <code>null</code> if groupByHeaderConfiguration is not
     *            <code>null</code>.
     * @param groupByHeaderConfiguration
     *            The {@link GroupByHeaderConfiguration} that should be added to
     *            this {@link GroupByHeaderLayer}. Needs to be a
     *            {@link GroupByHeaderConfiguration} because we retrieve the
     *            necessary {@link GroupByHeaderPainter} out of the
     *            configuration to setup this layer accordingly. Can be
     *            <code>null</code> if columnHeaderDataProvider is not
     *            <code>null</code>.
     */
    public GroupByHeaderLayer(
            GroupByModel groupByModel, ILayer horizontalLayerDependency,
            IDataProvider columnHeaderDataProvider,
            GroupByHeaderConfiguration groupByHeaderConfiguration) {
        this(groupByModel, horizontalLayerDependency, columnHeaderDataProvider, null, groupByHeaderConfiguration);
    }

    /**
     * Create a {@link GroupByHeaderLayer} by either using the given
     * {@link GroupByHeaderConfiguration} or creating a new
     * {@link GroupByHeaderConfiguration} using the given column header
     * {@link IDataProvider}. Note that either the {@link IDataProvider} or the
     * {@link GroupByHeaderConfiguration} parameter must be set. If both are
     * <code>null</code> an {@link IllegalArgumentException} will be thrown.
     *
     * @param groupByModel
     *            The {@link GroupByModel} needed for grouping operations.
     * @param horizontalLayerDependency
     *            The {@link ILayer} to which this header layer is horizontally
     *            dependent.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header which is needed
     *            to create the {@link GroupByHeaderConfiguration}. Can be
     *            <code>null</code> if groupByHeaderConfiguration is not
     *            <code>null</code>.
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} which should be used to create
     *            the {@link GroupByHeaderConfiguration} if it should support
     *            showing the renamed column headers. Can be <code>null</code>
     *            which results in using either the given
     *            columnHeaderDataProvider or the given
     *            {@link GroupByHeaderConfiguration}.
     * @param groupByHeaderConfiguration
     *            The {@link GroupByHeaderConfiguration} that should be added to
     *            this {@link GroupByHeaderLayer}. Needs to be a
     *            {@link GroupByHeaderConfiguration} because we retrieve the
     *            necessary {@link GroupByHeaderPainter} out of the
     *            configuration to setup this layer accordingly. Can be
     *            <code>null</code> if columnHeaderDataProvider is not
     *            <code>null</code>.
     *
     * @since 1.5
     */
    public GroupByHeaderLayer(
            GroupByModel groupByModel, ILayer horizontalLayerDependency,
            IDataProvider columnHeaderDataProvider, ColumnHeaderLayer columnHeaderLayer,
            GroupByHeaderConfiguration groupByHeaderConfiguration) {

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

        if (groupByModel == null) {
            throw new IllegalArgumentException("GroupByModel can not be null!"); //$NON-NLS-1$
        }
        if (columnHeaderDataProvider == null && groupByHeaderConfiguration == null) {
            throw new IllegalArgumentException("You must either specify a GroupByHeaderConfiguration " //$NON-NLS-1$
                    + "or a columnHeaderDataProvider to be able to create an internal GroupByHeaderConfiguration!"); //$NON-NLS-1$
        }

        setHorizontalLayerDependency(horizontalLayerDependency);
        setVerticalLayerDependency(getBaseLayer());

        this.groupByModel = groupByModel;
        registerPersistable(this.groupByModel);

        registerCommandHandler(new GroupByColumnCommandHandler(this));
        registerCommandHandler(new UngroupByColumnCommandHandler(this));

        GroupByHeaderConfiguration configuration = null;
        if (groupByHeaderConfiguration != null) {
            configuration = groupByHeaderConfiguration;
        } else {
            configuration = new GroupByHeaderConfiguration(groupByModel, columnHeaderDataProvider, columnHeaderLayer);
        }
        addConfiguration(configuration);

        this.groupByHeaderPainter = configuration.getGroupByHeaderPainter();
        ((DataLayer) getBaseLayer()).setRowHeightByPosition(0, this.groupByHeaderPainter.getPreferredHeight());
    }

    public GroupByModel getGroupByModel() {
        return this.groupByModel;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        fireLayerEvent(new RowStructuralRefreshEvent(getBaseLayer()));
    }

    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public int getHeight() {
        if (this.visible) {
            return super.getHeight();
        }
        return 0;
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        if (this.visible) {
            return super.getRowHeightByPosition(rowPosition);
        }
        return 0;
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        return new LayerCell(this, 0, 0, 0, 0, getColumnCount(), 1);
    }

    public int getGroupByColumnIndexAtXY(int x, int y) {
        return this.groupByHeaderPainter.getGroupByColumnIndexAtXY(x, y);
    }

}
