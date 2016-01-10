/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.cell;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;

/**
 * Applies 'odd'/'even' labels to all the rows. These labels are the used to
 * apply color to alternate rows.
 *
 * @see DefaultRowStyleConfiguration
 */
public class AlternatingRowConfigLabelAccumulator implements IConfigLabelProvider {

    public static final String ODD_ROW_CONFIG_TYPE = "ODD_" + GridRegion.BODY; //$NON-NLS-1$

    public static final String EVEN_ROW_CONFIG_TYPE = "EVEN_" + GridRegion.BODY; //$NON-NLS-1$

    private ILayer layer;

    /**
     * Creates an AlternatingRowConfigLabelAccumulator that operates on row
     * positions. In several layer compositions, this will lead to jumping
     * alternating colors, as e.g. the ViewportLayer updates row positions on
     * scrolling.
     */
    public AlternatingRowConfigLabelAccumulator() {}

    /**
     * Creates an AlternatingRowConfigLabelAccumulator that operates on row
     * indices. To achieve that it uses the given layer to calculate the row
     * index by given row position.
     *
     * @param layer
     *            The layer that should be used for row index transformation,
     *            typically the ViewportLayer or body layer stack.
     */
    public AlternatingRowConfigLabelAccumulator(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        int row = rowPosition;
        if (this.layer != null) {
            row = this.layer.getRowIndexByPosition(rowPosition);
        }

        configLabels.addLabel((row % 2 == 0 ? EVEN_ROW_CONFIG_TYPE : ODD_ROW_CONFIG_TYPE));
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        return Arrays.asList(ODD_ROW_CONFIG_TYPE, EVEN_ROW_CONFIG_TYPE);
    }
}
