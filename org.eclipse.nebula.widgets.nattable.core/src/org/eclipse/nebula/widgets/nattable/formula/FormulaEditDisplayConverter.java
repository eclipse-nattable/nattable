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

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.ContextualDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * {@link IDisplayConverter} that needs to be registered for
 * {@link DisplayMode#EDIT} in order to support editing of formulas without
 * formula execution.
 *
 * <p>
 * Technically it does not perform a conversion, but returns the value of the
 * {@link IDataProvider} that is wrapped by a {@link FormulaDataProvider} via
 * {@link FormulaDataProvider#getNativeDataValue(int, int)}.
 * </p>
 *
 * <pre>
 * configRegistry.registerConfigAttribute(
 *         CellConfigAttributes.DISPLAY_CONVERTER,
 *         new FormulaEditDisplayConverter(formulaDataProvider),
 *         DisplayMode.EDIT);
 * </pre>
 *
 * @since 1.4
 */
public class FormulaEditDisplayConverter extends ContextualDisplayConverter {

    protected FormulaDataProvider dataProvider;

    /**
     *
     * @param dataProvider
     *            The {@link FormulaDataProvider} for retrieving the native
     *            formula value.
     */
    public FormulaEditDisplayConverter(FormulaDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
        return this.dataProvider.getNativeDataValue(cell.getColumnIndex(), cell.getRowIndex());
    }

    @Override
    public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
        return displayValue;
    }

}
