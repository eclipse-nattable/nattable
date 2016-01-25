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

import org.eclipse.nebula.widgets.nattable.command.DisposeCalculatedValueCacheCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaCachingCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaCachingCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.function.AbstractFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.util.CalculatedValueCache;
import org.eclipse.nebula.widgets.nattable.util.ICalculator;

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

    private CalculatedValueCache valueCache;
    private ILayer cacheLayer;
    private boolean cacheEnabled = false;

    /**
     *
     * @param underlyingDataProvider
     *            The {@link IDataProvider} that should be wrapped.
     */
    public FormulaDataProvider(IDataProvider underlyingDataProvider) {
        this(underlyingDataProvider, new FormulaParser(underlyingDataProvider));
    }

    /**
     * This constructor supports the specification of a {@link FormulaParser} to
     * customize parsing.
     * 
     * @param underlyingDataProvider
     *            The {@link IDataProvider} that should be wrapped.
     * @param parser
     *            The {@link FormulaParser} that should be used for formula
     *            parsing.
     */
    public FormulaDataProvider(IDataProvider underlyingDataProvider, FormulaParser parser) {
        this.underlyingDataProvider = underlyingDataProvider;
        this.formulaParser = parser;
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        final Object underlying = this.underlyingDataProvider.getDataValue(columnIndex, rowIndex);
        if (this.formulaEvaluationEnabled && underlying != null && this.formulaParser.isFunction(underlying.toString())) {
            if (this.cacheEnabled && this.valueCache != null) {
                return this.valueCache.getCalculatedValue(columnIndex, rowIndex, true, new ICalculator() {

                    @Override
                    public Object executeCalculation() {
                        return processFormula(underlying.toString(), columnIndex, rowIndex);
                    }
                });
            } else {
                return processFormula(underlying.toString(), columnIndex, rowIndex);
            }
        }
        return underlying;
    }

    /**
     * Process the given formula String by using the internal
     * {@link FormulaParser}.
     *
     * @param formula
     *            The formula to process.
     * @param columnIndex
     *            The column index of the cell that contains the formula. Needed
     *            for error handling.
     * @param rowIndex
     *            The row index of the cell that contains the formula. Needed
     *            for error handling.
     * @return The result of the processed formula or an error markup in case an
     *         error occurred on processing.
     */
    protected Object processFormula(String formula, int columnIndex, int rowIndex) {
        try {
            if (this.errorReporter != null) {
                this.errorReporter.clearFormulaError(columnIndex, rowIndex);
            }
            return this.formulaParser.parseFunction(formula).getValue();
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

    /**
     * Configure the caching behavior of this {@link FormulaDataProvider}.
     *
     * @param layer
     *            The {@link ILayer} to which the internal
     *            {@link CalculatedValueCache} is connected to. Typically the
     *            {@link DataLayer} to which this {@link FormulaDataProvider} is
     *            set. If this value is <code>null</code> formula result caching
     *            can not be enabled because the {@link CalculatedValueCache}
     *            needs to operate on an {@link ILayer}.
     */
    public void configureCaching(ILayer layer) {
        if (this.cacheLayer != null) {
            this.cacheLayer.unregisterCommandHandler(DisposeResourcesCommand.class);
            this.cacheLayer.unregisterCommandHandler(DisableFormulaCachingCommand.class);
            this.cacheLayer.unregisterCommandHandler(EnableFormulaCachingCommand.class);
        }

        this.cacheLayer = layer;

        if (layer != null) {
            this.valueCache = new CalculatedValueCache(this.cacheLayer, true, true);
            this.cacheEnabled = true;

            // register command handlers
            this.cacheLayer.registerCommandHandler(new DisposeCalculatedValueCacheCommandHandler(this.valueCache));
            this.cacheLayer.registerCommandHandler(new DisableFormulaCachingCommandHandler(this));
            this.cacheLayer.registerCommandHandler(new EnableFormulaCachingCommandHandler(this));
        } else {
            this.valueCache = null;
            this.cacheEnabled = false;
        }
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
        if (this.valueCache != null) {
            // if a value is set we clear the cache
            // since we do not know which cells might reference the specified
            // cell, we simply clear the whole cache
            this.valueCache.clearCache();
        }
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
     *            <code>true</code> to enable formula evaluation,
     *            <code>false</code> to disable it.
     */
    public void setFormulaEvaluationEnabled(boolean enabled) {
        this.formulaEvaluationEnabled = enabled;
    }

    /**
     * Enable/Disable formula result caching. Enabling the formula result
     * caching means that the parsing and calculation of formulas is performed
     * in a background thread. The result is cached to reduce processing time,
     * so the rendering is performed faster. Disabling the formula result
     * caching means that parsing and calculation of formulas is performed
     * always in the current thread which might lead to slower rendering.
     *
     * @param enabled
     *            <code>true</code> to enable formula result caching and
     *            background processing of parsing and calculation,
     *            <code>false</code> to disable it.
     *
     * @see FormulaDataProvider#configureCaching(ILayer)
     */
    public void setFormulaCachingEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
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
