/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * Applies the given labels to all the cells in the grid. Used to apply styles
 * to the entire grid.
 */
public class BodyOverrideConfigLabelAccumulator implements IConfigLabelProvider {

    private List<String> configLabels = new ArrayList<String>();

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        configLabels.getLabels().addAll(this.configLabels);
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
