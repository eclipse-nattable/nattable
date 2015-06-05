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

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * {@link IDisplayConverter} that needs to be registered for
 * {@link DisplayMode#NORMAL} in order to support localized rendering of decimal
 * result values of formulas.
 *
 * <pre>
 * configRegistry.registerConfigAttribute(
 *         CellConfigAttributes.DISPLAY_CONVERTER,
 *         new FormulaResultDisplayConverter(formulaDataProvider),
 *         DisplayMode.NORMAL);
 * </pre>
 *
 * @since 1.4
 */
public class FormulaResultDisplayConverter extends DisplayConverter {

    protected FormulaDataProvider dataProvider;
    protected boolean formatNumberValues = false;

    /**
     *
     * @param dataProvider
     *            The {@link FormulaDataProvider} for retrieving the native
     *            formula value.
     */
    public FormulaResultDisplayConverter(FormulaDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        if (canonicalValue instanceof BigDecimal
                || (canonicalValue != null && this.formatNumberValues && this.dataProvider.getFormulaParser().isNumber(canonicalValue.toString()))) {
            FormulaParser parser = this.dataProvider.getFormulaParser();
            return parser.decimalFormat.format(parser.convertToBigDecimal(canonicalValue.toString()));
        }
        return canonicalValue;
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        return displayValue;
    }

    /**
     * Configure whether all number values should be formatted using the
     * {@link DecimalFormat} of the {@link FormulaParser}.
     *
     * @param format
     *            <code>true</code> to perform formatting, <code>false</code> if
     *            not. Default is <code>false</code>.
     */
    public void setFormatNumberValues(boolean format) {
        this.formatNumberValues = format;
    }
}
