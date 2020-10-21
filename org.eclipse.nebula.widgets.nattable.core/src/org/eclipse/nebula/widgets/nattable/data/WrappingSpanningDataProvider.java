/*******************************************************************************
 * Copyright (c) 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.data;

/**
 * Abstract implementation of {@link ISpanningDataProvider} that wraps an
 * existing {@link IDataProvider}.
 *
 * @since 2.0
 */
public abstract class WrappingSpanningDataProvider implements ISpanningDataProvider {

    protected final IDataProvider underlyingDataProvider;

    public WrappingSpanningDataProvider(IDataProvider underlyingDataProvider) {
        this.underlyingDataProvider = underlyingDataProvider;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.underlyingDataProvider.getDataValue(columnIndex, rowIndex);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        this.underlyingDataProvider.setDataValue(columnIndex, rowIndex, newValue);
    }

    @Override
    public int getColumnCount() {
        return this.underlyingDataProvider.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return this.underlyingDataProvider.getRowCount();
    }

}
