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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

public class SimpleConfigLabelAccumulator implements IConfigLabelProvider {

    private final String configLabel;

    public SimpleConfigLabelAccumulator(String configLabel) {
        this.configLabel = configLabel;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        configLabels.addLabel(this.configLabel);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        return Arrays.asList(this.configLabel);
    }

}
