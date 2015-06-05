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

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.function.AbstractFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;

/**
 * {@link IDataProvider} that is able to evaluate formulas. It wraps around a
 * {@link IDataProvider} and checks if the requested value is a formula (starts
 * with '='). Otherwise the value of the wrapped {@link IDataProvider} is
 * returned.
 *
 * @see FormulaParser
 *
 * @since 1.4
 */
public class FormulaDataProvider implements IDataProvider {

    protected IDataProvider underlyingDataProvider;
    protected FormulaParser formulaParser;

    protected FormulaErrorReporter errorReporter;

    protected boolean formulaEvaluationEnabled = true;

    /**
     *
     * @param underlyingDataProvider
     *            The {@link IDataProvider} that should be wrapped.
     */
    public FormulaDataProvider(IDataProvider underlyingDataProvider) {
        this.underlyingDataProvider = underlyingDataProvider;
        this.formulaParser = new FormulaParser(underlyingDataProvider);
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        Object underlying = this.underlyingDataProvider.getDataValue(columnIndex, rowIndex);
        if (this.formulaEvaluationEnabled && underlying != null && this.formulaParser.isFunction(underlying.toString())) {
            try {
                if (this.errorReporter != null) {
                    this.errorReporter.clearFormulaError(columnIndex, rowIndex);
                }
                return this.formulaParser.parseFunction(underlying.toString()).getValue();
            } catch (FunctionException e) {
                if (this.errorReporter != null) {
                    this.errorReporter.addFormulaError(columnIndex, rowIndex, e.getLocalizedMessage());
                }
                return e.getErrorMarkup();
            } catch (Exception e) {
                if (this.errorReporter != null) {
                    this.errorReporter.addFormulaError(columnIndex, rowIndex, e.getLocalizedMessage());
                }
                return "#ERROR!"; //$NON-NLS-1$
            }
        }
        return underlying;
    }

    /**
     * Returns the data value out of the underlying {@link IDataProvider}
     * without checking and performing formula evaluation. Needed in order to
     * edit formulas in a NatTable or outside the NatTable.
     *
     * @param columnIndex
     *            The column index of the requested value.
     * @param rowIndex
     *            The row index of the requested value.
     * @return The value of the underlying {@link IDataProvider} without formula
     *         evaluation.
     */
    public Object getNativeDataValue(int columnIndex, int rowIndex) {
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

    /**
     * @return The underlying {@link IDataProvider}.
     */
    protected IDataProvider getUnderlyingDataProvider() {
        return this.underlyingDataProvider;
    }

    /**
     * Enable/Disable formula evaluation.
     *
     * @param enabled
     *            <code>true</code> to enable formula evalution,
     *            <code>false</code> to disable it.
     */
    public void setFormulaEvaluationEnabled(boolean enabled) {
        this.formulaEvaluationEnabled = enabled;
    }

    /**
     * Register a new function that can be evaluated.
     *
     * @param functionName
     *            The name of the function that is used in a formula
     * @param value
     *            The type of {@link AbstractFunction} that should be used when
     *            evaluation a formula that contains the given function.
     */
    public void registerFunction(String functionName, Class<? extends AbstractFunction> value) {
        this.formulaParser.registerFunction(functionName, value);
    }

    /**
     *
     * @return The names of the registered functions that are evaluated by this
     *         {@link FormulaDataProvider}.
     */
    public Collection<String> getRegisteredFunctions() {
        return this.formulaParser.getRegisteredFunctions();
    }

    /**
     *
     * @return The {@link FormulaParser} that is used by this
     *         {@link FormulaDataProvider} to parse function strings.
     */
    public FormulaParser getFormulaParser() {
        return this.formulaParser;
    }

    /**
     * @return The {@link FormulaErrorReporter} that is used to report formula
     *         errors to the user.
     */
    public FormulaErrorReporter getErrorReporter() {
        return this.errorReporter;
    }

    /**
     * @param errorReporter
     *            The {@link FormulaErrorReporter} that should be used to report
     *            formula errors to the user.
     */
    public void setErrorReporter(FormulaErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }
}
