/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

/**
 * Specialization of the {@link AlternatingRowConfigLabelAccumulator} that
 * calculates the even/odd row labels in a hierarchical tree by inspecting the
 * row spanning of the first level node. For better performance the calculation
 * results are cached. As the cache needs to be cleared on structural changes,
 * this class also implements the {@link ILayerListener} to clear the cache
 * automatically on {@link IStructuralChangeEvent}s if registered on the given
 * layer via {@link ILayer#addLayerListener(ILayerListener)}.
 *
 * @since 1.6
 */
public class HierarchicalTreeAlternatingRowConfigLabelAccumulator extends AlternatingRowConfigLabelAccumulator implements ILayerListener {

    private Map<Integer, String> rowLabelCache = new HashMap<Integer, String>();

    /**
     *
     * @param layer
     *            The {@link ILayer} that is used to determine the row spanning
     *            in the first column. Should be the HierarchicalTreeLayer.
     */
    public HierarchicalTreeAlternatingRowConfigLabelAccumulator(ILayer layer) {
        super(layer);
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        String label = this.rowLabelCache.get(rowPosition);
        if (label == null) {
            label = calculateLabel(rowPosition);
            this.rowLabelCache.put(rowPosition, label);
        }
        if (label != null) {
            configLabels.addLabel(label);
        }
    }

    /**
     * Calculate the even/odd label based on the above row label, taking the
     * spanning into account.
     *
     * @param rowPosition
     *            The row position for which the label should be determined.
     * @return The even/odd row label to be applied for the given row.
     */
    protected String calculateLabel(int rowPosition) {
        // get cell in first column to determine the origin row position of the
        // spanned cell
        ILayerCell firstColumn = this.layer.getCellByPosition(0, rowPosition);
        // check if there is a label for the origin row already
        String label = this.rowLabelCache.get(firstColumn.getOriginRowPosition());
        if (label == null) {
            if (firstColumn.getOriginRowPosition() == 0) {
                label = EVEN_ROW_CONFIG_TYPE;
            } else {
                // check for the label one row above
                // remember: spanned cells are rendered at the end
                String labelAbove = this.rowLabelCache.get(firstColumn.getOriginRowPosition() - 1);
                if (labelAbove == null) {
                    labelAbove = calculateLabel(firstColumn.getOriginRowPosition() - 1);
                }
                label = (labelAbove == EVEN_ROW_CONFIG_TYPE) ? ODD_ROW_CONFIG_TYPE : EVEN_ROW_CONFIG_TYPE;
            }
        }
        return label;
    }

    /**
     * Clears the local cache of calculated row position to label mappings.
     */
    public void clearCache() {
        this.rowLabelCache.clear();
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            clearCache();
        }
    }
}
