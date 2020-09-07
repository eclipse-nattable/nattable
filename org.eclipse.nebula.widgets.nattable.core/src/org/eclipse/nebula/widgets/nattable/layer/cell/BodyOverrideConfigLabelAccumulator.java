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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * Applies the given labels to all the cells in the grid. Used to apply styles
 * to the entire grid.
 */
public class BodyOverrideConfigLabelAccumulator implements IConfigLabelProvider {

    private ArrayList<String> configLabels = new ArrayList<>();

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        configLabels.addAll(this.configLabels);
    }

    public void registerOverrides(String... configLabels) {
        this.configLabels.addAll(Arrays.asList(configLabels));
    }

    public void addOverride(String configLabel) {
        this.configLabels.add(configLabel);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        return this.configLabels;
    }

}
