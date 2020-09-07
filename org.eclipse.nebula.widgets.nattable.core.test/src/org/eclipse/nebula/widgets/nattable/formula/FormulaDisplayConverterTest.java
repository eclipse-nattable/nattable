/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Test;

public class FormulaDisplayConverterTest {

    IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new String[10][10]);
    FormulaDataProvider formulaDataProvider = new FormulaDataProvider(this.dataProvider);

    FormulaResultDisplayConverter resultConverter = new FormulaResultDisplayConverter(this.formulaDataProvider);
    FormulaEditDisplayConverter editConverter = new FormulaEditDisplayConverter(this.formulaDataProvider);

    @Test
    public void shouldConvertFormulaResult() {
        this.formulaDataProvider.getFormulaParser().setDecimalFormat((DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH));

        this.dataProvider.setDataValue(0, 0, "5.3");
        this.dataProvider.setDataValue(1, 0, "3.2");
        this.dataProvider.setDataValue(2, 0, "=A1+B1");

        assertEquals("8.5", this.resultConverter.canonicalToDisplayValue(this.formulaDataProvider.getDataValue(2, 0)));

        this.dataProvider.setDataValue(0, 0, "5.3");
        this.dataProvider.setDataValue(1, 0, "2");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        assertEquals("10.6", this.resultConverter.canonicalToDisplayValue(this.formulaDataProvider.getDataValue(2, 0)));
    }

    @Test
    public void shouldConvertFormulaResultLocalized() {
        this.formulaDataProvider.getFormulaParser().setDecimalFormat((DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN));

        this.dataProvider.setDataValue(0, 0, "5,3");
        this.dataProvider.setDataValue(1, 0, "3,2");
        this.dataProvider.setDataValue(2, 0, "=A1+B1");

        assertEquals("8,5", this.resultConverter.canonicalToDisplayValue(this.formulaDataProvider.getDataValue(2, 0)));

        this.dataProvider.setDataValue(0, 0, "5,3");
        this.dataProvider.setDataValue(1, 0, "2");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        assertEquals("10,6", this.resultConverter.canonicalToDisplayValue(this.formulaDataProvider.getDataValue(2, 0)));
    }

    @Test
    public void shouldConvertToNativeFormulaResult() {
        this.formulaDataProvider.getFormulaParser().setDecimalFormat((DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH));

        this.dataProvider.setDataValue(0, 0, "5.3");
        this.dataProvider.setDataValue(1, 0, "3.2");
        this.dataProvider.setDataValue(2, 0, "=A1+B1");

        DataLayer dataLayer = new DataLayer(this.formulaDataProvider);
        ConfigRegistry configRegistry = new ConfigRegistry();

        assertEquals("=A1+B1", this.editConverter.canonicalToDisplayValue(
                dataLayer.getCellByPosition(2, 0), configRegistry, this.formulaDataProvider.getDataValue(2, 0)));

        this.dataProvider.setDataValue(0, 0, "5.3");
        this.dataProvider.setDataValue(1, 0, "2");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        assertEquals("=A1*B1", this.editConverter.canonicalToDisplayValue(
                dataLayer.getCellByPosition(2, 0), configRegistry, this.formulaDataProvider.getDataValue(2, 0)));
    }
}