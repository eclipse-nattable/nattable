/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.formula;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;

/**
 * {@link FormulaDataProvider} that wraps around an {@link IRowDataProvider}.
 * Needed to have a {@link FormulaDataProvider} that can be used as
 * {@link IRowDataProvider} for extended use cases.
 *
 * @since 1.4
 */
public class FormulaRowDataProvider<T> extends FormulaDataProvider implements IRowDataProvider<T> {

    /**
     *
     * @param underlyingDataProvider
     *            The underlying {@link IRowDataProvider}
     */
    public FormulaRowDataProvider(IRowDataProvider<T> underlyingDataProvider) {
        super(underlyingDataProvider);
    }

    @Override
    public T getRowObject(int rowIndex) {
        return getUnderlyingDataProvider().getRowObject(rowIndex);
    }

    @Override
    public int indexOfRowObject(T rowObject) {
        return getUnderlyingDataProvider().indexOfRowObject(rowObject);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IRowDataProvider<T> getUnderlyingDataProvider() {
        return (IRowDataProvider<T>) super.getUnderlyingDataProvider();
    }
}
