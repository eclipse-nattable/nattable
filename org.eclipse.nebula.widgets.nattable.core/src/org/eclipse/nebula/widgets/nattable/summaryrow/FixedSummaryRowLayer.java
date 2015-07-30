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
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;

/**
 * This layer is a specialization of the {@link SummaryRowLayer} and is intended
 * to be used in a composition below a {@link GridLayer} or a vertical
 * composition like one with a column header and a body. It is horizontal
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
 * |      FixedSummaryRowLayer     |<br>
 * +-------------------------------+<br>
 * </pre>
 * <p>
 * It would be created by the following code:
 * </p>
 *
 * <pre>
 *      GridLayer gridLayer = new GridLayer(...);
 *      FixedSummaryRowLayer summaryRowLayer =
 *          new FixedSummaryRowLayer(bodyDataLayer, gridLayer, configRegistry);
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
public class FixedSummaryRowLayer extends SummaryRowLayer {

    public static final String DEFAULT_SUMMARY_ROW_LABEL = "Summary"; //$NON-NLS-1$
    protected String summaryRowLabel = DEFAULT_SUMMARY_ROW_LABEL;

    /**
     * The layer to which fixed summary row should be horizontally dependent.
     * Typically a {@link GridLayer} or the body layer stack in case of a simple
     * vertical composition.
     */
    protected ILayer horizontalLayerDependency;

    /**
     * Flag to tell whether the horizontal dependency is a composite or not. For
     * example, if the horizontal dependency is a {@link GridLayer} it contains
     * a row header, which means this {@link FixedSummaryRowLayer} needs to
     * handle an additional column at position 1.
     */
    private boolean horizontalCompositeDependency = true;

    /**
     * Creates a standalone {@link FixedSummaryRowLayer} that is horizontal
     * dependent to the given layer and calculates the summary values from the
     * given bodyDataLayer. It will register the default configurations and
     * perform smooth value updates.
     *
     * <p>
     * <b>Note:</b><br>
     * The {@link FixedSummaryRowLayer} constructor is setting a
     * {@link GridLineCellLayerPainter} that is configured for clipTop to the
     * given bodyDataLayer. This is necessary as otherwise the body region would
     * paint over the summary row. In case you want to use a different
     * {@link ILayerPainter} ensure to set it AFTER creating the
     * {@link FixedSummaryRowLayer}.
     * </p>
     *
     * @param bodyDataLayer
     *            The underlying layer on which this layer should be build.
     *            Typically the {@link DataLayer} of the body region.
     * @param horizontalLayerDependency
     *            The layer that is above this layer in the surrounding
     *            composition. Typically a {@link GridLayer}.
     * @param configRegistry
     *            The ConfigRegistry for retrieving the ISummaryProvider per
     *            column.
     */
    public FixedSummaryRowLayer(
            IUniqueIndexLayer bodyDataLayer, ILayer horizontalLayerDependency, IConfigRegistry configRegistry) {
        this(bodyDataLayer, horizontalLayerDependency, configRegistry, true, true);
    }

    /**
     * Creates a standalone {@link FixedSummaryRowLayer} that is horizontal
     * dependent to the given layer and calculates the summary values from the
     * given bodyDataLayer. It will perform smooth value updates.
     *
     * <p>
     * <b>Note:</b><br>
     * The {@link FixedSummaryRowLayer} constructor is setting a
     * {@link GridLineCellLayerPainter} that is configured for clipTop to the
     * given bodyDataLayer. This is necessary as otherwise the body region would
     * paint over the summary row. In case you want to use a different
     * {@link ILayerPainter} ensure to set it AFTER creating the
     * {@link FixedSummaryRowLayer}.
     * </p>
     *
     * @param bodyDataLayer
     *            The underlying layer on which this layer should be build.
     *            Typically the {@link DataLayer} of the body region.
     * @param horizontalLayerDependency
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
    public FixedSummaryRowLayer(
            IUniqueIndexLayer bodyDataLayer, ILayer horizontalLayerDependency, IConfigRegistry configRegistry,
            boolean autoConfigure) {
        this(bodyDataLayer, horizontalLayerDependency, configRegistry, true, autoConfigure);
    }

    /**
     * Creates a standalone {@link FixedSummaryRowLayer} that is horizontal
     * dependent to the given layer and calculates the summary values from the
     * given bodyDataLayer.
     *
     * <p>
     * <b>Note:</b><br>
     * The {@link FixedSummaryRowLayer} constructor is setting a
     * {@link GridLineCellLayerPainter} that is configured for clipTop to the
     * given bodyDataLayer. This is necessary as otherwise the body region would
     * paint over the summary row. In case you want to use a different
     * {@link ILayerPainter} ensure to set it AFTER creating the
     * {@link FixedSummaryRowLayer}.
     * </p>
     *
     * @param bodyDataLayer
     *            The underlying layer on which this layer should be build.
     *            Typically the {@link DataLayer} of the body region.
     *            <p>
     *            <b>Note</b>: When using a different layer than the DataLayer,
     *            e.g. the GlazedListsEventLayer to receive automatic updates,
     *            you need to ensure that the GridLineCellLayerPainter
     *            configured for clipping on top is set to the DataLayer for
     *            correct rendering of the fixed summary row.
     *            </p>
     * @param horizontalLayerDependency
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
    public FixedSummaryRowLayer(
            IUniqueIndexLayer bodyDataLayer, ILayer horizontalLayerDependency, IConfigRegistry configRegistry,
            boolean smoothUpdates, boolean autoConfigure) {
        super(bodyDataLayer, configRegistry, smoothUpdates, autoConfigure);
        this.horizontalLayerDependency = horizontalLayerDependency;
        setStandalone(true);

        // register a GridLineCellLayerPainter that is configured for clipping
        // on top
        if (bodyDataLayer instanceof AbstractLayer) {
            ((AbstractLayer) bodyDataLayer).setLayerPainter(new GridLineCellLayerPainter(false, true));
        }

        // if the layer we are dependent to has changed, we need to update
        if (this.horizontalLayerDependency != null) {
            horizontalLayerDependency.addLayerListener(new ILayerListener() {

                @Override
                public void handleLayerEvent(ILayerEvent event) {
                    FixedSummaryRowLayer.this.handleLayerEvent(event);
                }
            });
        }
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        if (!isBodyColumn(columnPosition)) {
            return getSummaryRowLabel();
        }

        int columnIndex = LayerUtil.convertColumnPosition(
                this.horizontalLayerDependency, columnPosition, (IUniqueIndexLayer) this.underlyingLayer);
        return super.getDataValueByPosition(columnIndex, rowPosition);
    }

    /**
     *
     * @param columnPosition
     *            The column position that should be checked.
     * @return <code>true</code> if the column at the given position is a column
     *         of the body, <code>false</code> if it is a column of another
     *         region, e.g. the row header in a grid.
     */
    protected boolean isBodyColumn(int columnPosition) {
        return !(this.horizontalCompositeDependency && (columnPosition == 0));
    }

    /**
     *
     * @return <code>true</code> if the horizontal dependency is itself a
     *         composite that has an additional column, e.g. a {@link GridLayer}
     *         with a row header. <code>false</code> if the horizontal
     *         dependency is not a composite, e.g. the body layer stack.
     */
    public boolean hasHorizontalCompositeDependency() {
        return this.horizontalCompositeDependency;
    }

    /**
     * Specify if the horizontal dependency is a {@link CompositeLayer} that
     * adds additional columns.
     *
     * @param compositeDependency
     *            <code>true</code> to specify that the horizontal dependency is
     *            itself a composite that has an additional column, e.g. a
     *            {@link GridLayer} with a row header. <code>false</code> if the
     *            horizontal dependency is not a composite, e.g. the body layer
     *            stack.
     */
    public void setHorizontalCompositeDependency(boolean compositeDependency) {
        this.horizontalCompositeDependency = compositeDependency;
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
        if (!isBodyColumn(columnPosition)) {
            LabelStack labelStack = this.horizontalLayerDependency.getConfigLabelsByPosition(
                    columnPosition, this.horizontalLayerDependency.getRowCount() - 1);

            labelStack.addLabelOnTop(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

            if (getConfigLabelAccumulator() != null) {
                getConfigLabelAccumulator().accumulateConfigLabels(
                        labelStack, columnPosition, rowPosition);
            }
            return labelStack;
        }

        int columnIndex = LayerUtil.convertColumnPosition(
                this.horizontalLayerDependency, columnPosition, (IUniqueIndexLayer) this.underlyingLayer);
        return super.getConfigLabelsByPosition(columnIndex, rowPosition);
    }

    // Columns

    @Override
    public int getColumnCount() {
        return this.horizontalLayerDependency.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.horizontalLayerDependency.getPreferredColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return this.horizontalLayerDependency.getColumnIndexByPosition(columnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return this.horizontalLayerDependency.localToUnderlyingColumnPosition(localColumnPosition);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {
        if (sourceUnderlyingLayer == this.horizontalLayerDependency) {
            return underlyingColumnPosition;
        }
        return this.horizontalLayerDependency.underlyingToLocalColumnPosition(
                sourceUnderlyingLayer, underlyingColumnPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        if (sourceUnderlyingLayer == this.horizontalLayerDependency) {
            return underlyingColumnPositionRanges;
        }
        return this.horizontalLayerDependency.underlyingToLocalColumnPositions(
                sourceUnderlyingLayer, underlyingColumnPositionRanges);
    }

    // Width

    @Override
    public int getWidth() {
        return this.horizontalLayerDependency.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.horizontalLayerDependency.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.horizontalLayerDependency.getColumnWidthByPosition(columnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.horizontalLayerDependency.isColumnPositionResizable(columnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.horizontalLayerDependency.getColumnPositionByX(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.horizontalLayerDependency.getStartXOfColumnPosition(columnPosition);
    }

}
