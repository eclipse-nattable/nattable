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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;

/**
 * Adds the Java class name of the cell's data value as a label.
 */
public class ClassNameConfigLabelAccumulator implements IConfigLabelProvider {

    private IRowDataProvider<?> dataProvider;

    public ClassNameConfigLabelAccumulator(IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabel, int columnPosition, int rowPosition) {
        Object value = this.dataProvider.getDataValue(columnPosition, rowPosition);
        if (value != null) {
            configLabel.addLabel(value.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = new HashSet<String>();
        for (int i = 0; i < this.dataProvider.getColumnCount(); i++) {
            Object value = this.dataProvider.getDataValue(i, 0);
            if (value != null) {
                result.add(value.getClass().getName());
            }
        }
        return result;
    }

}
