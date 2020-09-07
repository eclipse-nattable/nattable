/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.data;

import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;

/**
 * Using this specialization of {@link RowHeaderLayer} adds support for
 * configuring a fixed {@link SummaryRowLayer} in the body region of a grid.
 */
public class FixedSummaryRowHeaderLayer extends RowHeaderLayer {

    public static final String DEFAULT_SUMMARY_ROW_LABEL = "Summary"; //$NON-NLS-1$
    protected String summaryRowLabel = DEFAULT_SUMMARY_ROW_LABEL;

    public FixedSummaryRowHeaderLayer(
            IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency, SelectionLayer selectionLayer) {
        super(baseLayer, verticalLayerDependency, selectionLayer);
    }

    public FixedSummaryRowHeaderLayer(
            IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency,
            SelectionLayer selectionLayer, boolean useDefaultConfiguration) {
        super(baseLayer, verticalLayerDependency, selectionLayer, useDefaultConfiguration);
    }

    public FixedSummaryRowHeaderLayer(
            IUniqueIndexLayer baseLayer, ILayer verticalLayerDependency,
            SelectionLayer selectionLayer, boolean useDefaultConfiguration,
            ILayerPainter layerPainter) {
        super(baseLayer, verticalLayerDependency, selectionLayer, useDefaultConfiguration, layerPainter);
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        if (rowPosition == 0) {
            return this.summaryRowLabel;
        }
        return super.getDataValueByPosition(columnPosition, rowPosition);
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        if (rowPosition == 0) {
            // for the summary row we need the same implementation as the
            // DimensionallyDependentLayer and not the super implementation of
            // the RowHeaderLayer
            int baseColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, getBaseLayer());
            int baseRowPosition = LayerUtil.convertRowPosition(this, rowPosition, getBaseLayer());
            return getBaseLayer().getDisplayModeByPosition(baseColumnPosition, baseRowPosition);
        }
        return super.getDisplayModeByPosition(columnPosition, rowPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (rowPosition == 0) {
            // for the summary row we need the same implementation as the
            // DimensionallyDependentLayer and not the super implementation of
            // the RowHeaderLayer
            int baseColumnPosition = LayerUtil.convertColumnPosition(this, columnPosition, getBaseLayer());
            int baseRowPosition = LayerUtil.convertRowPosition(this, rowPosition, getBaseLayer());
            LabelStack labelStack = getBaseLayer().getConfigLabelsByPosition(baseColumnPosition, baseRowPosition);

            IConfigLabelAccumulator configLabelAccumulator = getConfigLabelAccumulator();
            if (configLabelAccumulator != null) {
                configLabelAccumulator.accumulateConfigLabels(labelStack, columnPosition, rowPosition);
            }

            // add a label to the row header summary row cell, so it can be
            // styled differently too in this case it will simply use the same
            // styling as the summary row in the body
            labelStack.addLabelOnTop(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
            return labelStack;

        }
        return super.getConfigLabelsByPosition(columnPosition, rowPosition);
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

}
