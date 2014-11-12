/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.summaryrow;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;

/**
 * This layer is a specialization of the {@link SummaryRowLayer} and is intended
 * to be used in a composition below a {@link GridLayer}. It is horizontal
 * dependent to the layer above and configured as a standalone summary row,
 * which means that only the summary row is rendered.
 * <p>
 * A typical composition to use this layer could look like this:<br>
 *
 * <pre>
 * +---------------+---------------+<br>
 * |         CompositeLayer        |<br>
 * |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+<br>
 * | +---------------------------+ |<br>
 * | |          GridLayer        | |<br>
 * | +~~~~~~~~~~~~~~~~~~~~~~~~~~~+ |<br>
 * | |  Corner   |   ColHeader   | |<br>
 * | +-----------+---------------+ |<br>
 * | | RowHeader |     Body      | |<br>
 * | +-----------+---------------+ |<br>
 * +-------------------------------+<br>
 * |    FixedGridSummaryRowLayer   |<br>
 * +-------------------------------+<br>
 * </pre>
 * <p>
 * It would be created by the following code:
 * </p>
 *
 * <pre>
 *      GridLayer gridLayer = new GridLayer(...);
 *      FixedGridSummaryRowLayer summaryRowLayer =
 *          new FixedGridSummaryRowLayer(bodyDataLayer, gridLayer, configRegistry);
 * 
 *      CompositeLayer composite = new CompositeLayer(1, 2);
 *      composite.setChildLayer("GRID", gridLayer, 0, 0);
 *      composite.setChildLayer(SUMMARY_REGION, summaryRowLayer, 0, 1);
 * 
 *      NatTable natTable = new NatTable(panel, composite);
 * </pre>
 * <p>
 * Note that the <i>bodyDataLayer</i> needs to be accessible somehow and the
 * creation of the {@link GridLayer} is not specified in detail in the above
 * example.
 * </p>
 * <p>
 * Using this layer in a composition as shown above will result in a fixed
 * summary row that doesn't scroll if the viewport is scrolled. This is
 * different to the typical approach of adding the {@link SummaryRowLayer} on
 * top of the body {@link DataLayer} where the summary row will scroll as part
 * of the body region.
 * </p>
 */
public class FixedGridSummaryRowLayer extends SummaryRowLayer {

    public static final String DEFAULT_SUMMARY_ROW_LABEL = "Summary"; //$NON-NLS-1$
    protected String summaryRowLabel = DEFAULT_SUMMARY_ROW_LABEL;

    /**
     * The grid layer to which fixed summary row should be horizontally
     * dependent.
     */
    protected ILayer gridLayer;

    /**
     * Creates a standalone {@link FixedGridSummaryRowLayer} that is horizontal
     * dependent to the given gridLayer and calculates the summary values from
     * the given bodyDataLayer. It will register the default configurations and
     * perform smooth value updates.
     *
     * <p>
     * <b>Note:</b><br>
     * The {@link FixedGridSummaryRowLayer} constructor is setting a
     * {@link GridLineCellLayerPainter} that is configured for clipTop to the
     * given bodyDataLayer. This is necessary as otherwise the body region would
     * paint over the summary row. In case you want to use a different
     * {@link ILayerPainter} ensure to set it AFTER creating the
     * {@link FixedGridSummaryRowLayer}.
     * </p>
     *
     * @param bodyDataLayer
     *            The underlying layer on which this layer should be build.
     *            Typically the {@link DataLayer} of the body region.
     * @param gridLayer
     *            The layer that is above this layer in the surrounding
     *            composition. Typically a {@link GridLayer}.
     * @param configRegistry
     *            The ConfigRegistry for retrieving the ISummaryProvider per
     *            column.
     */
    public FixedGridSummaryRowLayer(
            IUniqueIndexLayer bodyDataLayer, ILayer gridLayer, IConfigRegistry configRegistry) {
        this(bodyDataLayer, gridLayer, configRegistry, true, true);
    }

    /**
     * Creates a standalone {@link FixedGridSummaryRowLayer} that is horizontal
     * dependent to the given gridLayer and calculates the summary values from
     * the given bodyDataLayer. It will perform smooth value updates.
     *
     * <p>
     * <b>Note:</b><br>
     * The {@link FixedGridSummaryRowLayer} constructor is setting a
     * {@link GridLineCellLayerPainter} that is configured for clipTop to the
     * given bodyDataLayer. This is necessary as otherwise the body region would
     * paint over the summary row. In case you want to use a different
     * {@link ILayerPainter} ensure to set it AFTER creating the
     * {@link FixedGridSummaryRowLayer}.
     * </p>
     *
     * @param bodyDataLayer
     *            The underlying layer on which this layer should be build.
     *            Typically the {@link DataLayer} of the body region.
     * @param gridLayer
     *            The layer that is above this layer in the surrounding
     *            composition. Typically a {@link GridLayer}.
     * @param configRegistry
     *            The ConfigRegistry for retrieving the ISummaryProvider per
     *            column.
     * @param autoConfigure
     *            <code>true</code> to use the DefaultSummaryRowConfiguration,
     *            <code>false</code> if a custom configuration will be set after
     *            the creation.
     */
    public FixedGridSummaryRowLayer(
            IUniqueIndexLayer bodyDataLayer, ILayer gridLayer, IConfigRegistry configRegistry,
            boolean autoConfigure) {
        this(bodyDataLayer, gridLayer, configRegistry, true, autoConfigure);
    }

    /**
     * Creates a standalone {@link FixedGridSummaryRowLayer} that is horizontal
     * dependent to the given gridLayer and calculates the summary values from
     * the given bodyDataLayer.
     *
     * <p>
     * <b>Note:</b><br>
     * The {@link FixedGridSummaryRowLayer} constructor is setting a
     * {@link GridLineCellLayerPainter} that is configured for clipTop to the
     * given bodyDataLayer. This is necessary as otherwise the body region would
     * paint over the summary row. In case you want to use a different
     * {@link ILayerPainter} ensure to set it AFTER creating the
     * {@link FixedGridSummaryRowLayer}.
     * </p>
     *
     * @param bodyDataLayer
     *            The underlying layer on which this layer should be build.
     *            Typically the {@link DataLayer} of the body region.
     * @param gridLayer
     *            The layer that is above this layer in the surrounding
     *            composition. Typically a {@link GridLayer}.
     * @param configRegistry
     *            The ConfigRegistry for retrieving the ISummaryProvider per
     *            column.
     * @param smoothUpdates
     *            <code>true</code> if the summary value updates should be
     *            performed smoothly, <code>false</code> if on re-calculation
     *            the value should be immediately shown as not calculated.
     * @param autoConfigure
     *            <code>true</code> to use the DefaultSummaryRowConfiguration,
     *            <code>false</code> if a custom configuration will be set after
     *            the creation.
     */
    public FixedGridSummaryRowLayer(
            IUniqueIndexLayer bodyDataLayer, ILayer gridLayer, IConfigRegistry configRegistry,
            boolean smoothUpdates, boolean autoConfigure) {
        super(bodyDataLayer, configRegistry, smoothUpdates, autoConfigure);
        this.gridLayer = gridLayer;
        setStandalone(true);

        // register a GridLineCellLayerPainter that is configured for clipping
        // on top
        if (bodyDataLayer instanceof AbstractLayer) {
            ((AbstractLayer) bodyDataLayer).setLayerPainter(new GridLineCellLayerPainter(false, true));
        }
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        if (columnPosition == 0) {
            return getSummaryRowLabel();
        }

        int columnIndex = LayerUtil.convertColumnPosition(
                this.gridLayer, columnPosition, (IUniqueIndexLayer) this.underlyingLayer);
        return super.getDataValueByPosition(columnIndex, rowPosition);
    }

    /**
     *
     * @return The label that is used as data value for the horizontal dependent
     *         cell to the row header column.
     */
    public String getSummaryRowLabel() {
        return this.summaryRowLabel;
    }

    /**
     *
     * @param summaryRowLabel
     *            The label that should be used as data value for the horizontal
     *            dependent cell to the row header column.
     */
    public void setSummaryRowLabel(String summaryRowLabel) {
        this.summaryRowLabel = summaryRowLabel;
    }

    /**
     * This implementation directly calls the super implementation. This is done
     * to skip the column position-index transformation since it was done
     * already.
     */
    @Override
    protected LabelStack getConfigLabelsByPositionWithoutTransformation(
            int columnPosition, int rowPosition) {
        return super.getConfigLabelsByPosition(columnPosition, rowPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (columnPosition == 0) {
            LabelStack labelStack = this.gridLayer.getConfigLabelsByPosition(
                    columnPosition, this.gridLayer.getRowCount() - 1);
            labelStack.addLabelOnTop(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
            return labelStack;
        }

        int columnIndex = LayerUtil.convertColumnPosition(
                this.gridLayer, columnPosition, (IUniqueIndexLayer) this.underlyingLayer);
        return super.getConfigLabelsByPosition(columnIndex, rowPosition);
    }

    // Columns

    @Override
    public int getColumnCount() {
        return this.gridLayer.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.gridLayer.getPreferredColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return this.gridLayer.getColumnIndexByPosition(columnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return this.gridLayer.localToUnderlyingColumnPosition(localColumnPosition);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {
        if (sourceUnderlyingLayer == this.gridLayer) {
            return underlyingColumnPosition;
        }
        return this.gridLayer.underlyingToLocalColumnPosition(
                sourceUnderlyingLayer, underlyingColumnPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        if (sourceUnderlyingLayer == this.gridLayer) {
            return underlyingColumnPositionRanges;
        }
        return this.gridLayer.underlyingToLocalColumnPositions(
                sourceUnderlyingLayer, underlyingColumnPositionRanges);
    }

    // Width

    @Override
    public int getWidth() {
        return this.gridLayer.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.gridLayer.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.gridLayer.getColumnWidthByPosition(columnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.gridLayer.isColumnPositionResizable(columnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.gridLayer.getColumnPositionByX(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.gridLayer.getStartXOfColumnPosition(columnPosition);
    }

}
