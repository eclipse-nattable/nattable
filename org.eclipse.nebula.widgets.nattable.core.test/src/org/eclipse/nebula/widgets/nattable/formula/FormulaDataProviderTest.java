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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.junit.Test;

public class FormulaDataProviderTest {

    IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new Object[10][10]);
    FormulaDataProvider formulaDataProvider = new FormulaDataProvider(this.dataProvider);

    @Test
    public void shouldReturnEvaluatedFormula() {
        this.dataProvider.setDataValue(0, 0, "5");
        this.dataProvider.setDataValue(1, 0, "3");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        assertEquals(new BigDecimal("15"), this.formulaDataProvider.getDataValue(2, 0));
    }

    @Test
    public void shouldReturnNativeFormula() {
        this.dataProvider.setDataValue(0, 0, "5");
        this.dataProvider.setDataValue(1, 0, "3");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        assertEquals("=A1*B1", this.formulaDataProvider.getNativeDataValue(2, 0));
    }

    @Test
    public void shouldReturnNativeFormulaIfDisabled() {
        this.dataProvider.setDataValue(0, 0, "5");
        this.dataProvider.setDataValue(1, 0, "3");
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        this.formulaDataProvider.setFormulaEvaluationEnabled(false);

        assertEquals("=A1*B1", this.formulaDataProvider.getDataValue(2, 0));
    }

    @Test
    public void shouldHandleNumberValueTypes() {
        this.dataProvider.setDataValue(0, 0, Integer.valueOf("5"));
        this.dataProvider.setDataValue(1, 0, Integer.valueOf("3"));
        this.dataProvider.setDataValue(2, 0, "=A1*B1");

        assertEquals(new BigDecimal("15"), this.formulaDataProvider.getDataValue(2, 0));

        this.dataProvider.setDataValue(0, 0, Double.valueOf("5"));
        this.dataProvider.setDataValue(1, 0, Double.valueOf("3"));
        assertEquals(new BigDecimal("15"), this.formulaDataProvider.getDataValue(2, 0));

        this.dataProvider.setDataValue(0, 0, Double.valueOf("5.2"));
        this.dataProvider.setDataValue(1, 0, Double.valueOf("3.2"));
        assertEquals(new BigDecimal("16.64"), this.formulaDataProvider.getDataValue(2, 0));

        this.dataProvider.setDataValue(0, 0, Double.valueOf("5.2"));
        this.dataProvider.setDataValue(1, 0, Integer.valueOf("3"));
        assertEquals(new BigDecimal("15.6"), this.formulaDataProvider.getDataValue(2, 0));

        this.dataProvider.setDataValue(0, 0, Double.valueOf("5.2"));
        this.dataProvider.setDataValue(1, 0, "3");
        assertEquals(new BigDecimal("15.6"), this.formulaDataProvider.getDataValue(2, 0));
    }
}
