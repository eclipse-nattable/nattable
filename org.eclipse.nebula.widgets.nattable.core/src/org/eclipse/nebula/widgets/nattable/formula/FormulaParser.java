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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.coordinate.IndexCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.function.AbstractFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.AbstractMathSingleValueFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.AbstractSingleValueFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.AverageFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.BigDecimalFunctionValue;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionValue;
import org.eclipse.nebula.widgets.nattable.formula.function.ModFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.MultipleValueFunctionValue;
import org.eclipse.nebula.widgets.nattable.formula.function.NegateFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.OperatorFunctionValue;
import org.eclipse.nebula.widgets.nattable.formula.function.PowerFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.ProductFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.QuotientFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.SquareRootFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.StringFunctionValue;
import org.eclipse.nebula.widgets.nattable.formula.function.SumFunction;

/**
 * Parser that is able to parse a formula string and calculate the result.
 *
 * @since 1.4
 */
public class FormulaParser {

    public static final String operatorRegex = "[-+/*\\^]"; //$NON-NLS-1$
    public static final String digitRegex = "[\\d]+"; //$NON-NLS-1$
    public static final String placeholderRegex = "\\{" + digitRegex + "\\}"; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String operatorSplitRegex = "((?<=[-+/*\\^\\s])|(?=[-+/*\\^\\s]))"; //$NON-NLS-1$

    public static final String referenceRegex = "[A-Z]+[0-9]+"; //$NON-NLS-1$
    public static final String referenceRangeRegex = referenceRegex + ":" + referenceRegex; //$NON-NLS-1$
    public static final String columnRangeRegex = "[A-Z]+:[A-Z]+"; //$NON-NLS-1$
    public static final String rowRangeRegex = digitRegex + ":" + digitRegex; //$NON-NLS-1$
    public static final String rangeRegex = "(" + referenceRangeRegex + "|" + columnRangeRegex + "|" + rowRangeRegex + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    protected DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
    protected String localizedDigitRegex;

    protected String functionRegex;
    protected Pattern functionPattern;

    protected Pattern referencePattern = Pattern.compile(referenceRegex);

    protected Map<String, Class<? extends AbstractFunction>> functionMapping = new HashMap<String, Class<? extends AbstractFunction>>();

    protected IDataProvider dataProvider;

    /**
     * Creates and initializes a new {@link FormulaParser}.
     *
     * @param dataProvider
     *            The {@link IDataProvider} that provides the data to perform
     *            calculations.
     */
    public FormulaParser(IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        initFunctions();
        updateLocalizedDigitRegex();
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
        this.functionMapping.put(functionName, value);
        updateFunctionRegex();
    }

    /**
     *
     * @return The names of the registered functions that can be evaluated by
     *         this {@link FormulaParser}.
     */
    public Collection<String> getRegisteredFunctions() {
        return this.functionMapping.keySet();
    }

    /**
     * Initialize the functions that are supported by this {@link FormulaParser}
     * .
     */
    protected void initFunctions() {
        this.functionMapping.put("AVERAGE", AverageFunction.class); //$NON-NLS-1$
        this.functionMapping.put("MOD", ModFunction.class); //$NON-NLS-1$
        this.functionMapping.put("NEGATE", NegateFunction.class); //$NON-NLS-1$
        this.functionMapping.put("POWER", PowerFunction.class); //$NON-NLS-1$
        this.functionMapping.put("PRODUCT", ProductFunction.class); //$NON-NLS-1$
        this.functionMapping.put("QUOTIENT", QuotientFunction.class); //$NON-NLS-1$
        this.functionMapping.put("SQRT", SquareRootFunction.class); //$NON-NLS-1$
        this.functionMapping.put("SUM", SumFunction.class); //$NON-NLS-1$

        updateFunctionRegex();
    }

    /**
     * Update the regular expression that is used to identify a function in a
     * function string.
     */
    protected void updateFunctionRegex() {
        StringBuilder builder = new StringBuilder("("); //$NON-NLS-1$
        for (Iterator<String> it = this.functionMapping.keySet().iterator(); it.hasNext();) {
            String functionName = it.next();
            builder.append(functionName);
            if (it.hasNext()) {
                builder.append("|"); //$NON-NLS-1$
            }
        }
        builder.append(")\\("); //$NON-NLS-1$

        this.functionRegex = builder.toString();
        this.functionPattern = Pattern.compile(this.functionRegex);
    }

    /**
     * Parses the given function string to a {@link FunctionValue} to perform
     * calculation.
     *
     * @param function
     *            The function string to parse.
     * @return The {@link FunctionValue} that represents the calculation result
     *         of the parsed function string.
     */
    public FunctionValue parseFunction(String function) {
        return parseFunction(function, new HashMap<Integer, FunctionValue>(), new LinkedHashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    /**
     * Parses the given function string to a {@link FunctionValue} to perform
     * calculation. Creates a new replacement map but keeps the parsed
     * references for cycle detection.
     *
     * @param function
     *            The function string to parse.
     * @param parsedReferences
     *            The references that where parsed already together with their
     *            references if any. Needed for cycle detection.
     * @param referer
     *            The coordinate of the cell that refers to the value to add.
     *            Needed for cycle detection.
     * @return The {@link FunctionValue} that represents the calculation result
     *         of the parsed function string.
     */
    protected FunctionValue parseFunction(String function,
            Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences, IndexCoordinate referer) {
        return parseFunction(function, new HashMap<Integer, FunctionValue>(), parsedReferences, referer);
    }

    /**
     * Parses the given function string to a {@link FunctionValue} to perform
     * calculation.
     *
     * @param function
     *            The function string to parse.
     * @param replacements
     *            The map of replacements to support iterative parsing of
     *            sub-functions.
     * @param parsedReferences
     *            The references that where parsed already together with their
     *            references if any. Needed for cycle detection.
     * @param referer
     *            The coordinate of the cell that refers to the value to add.
     *            Needed for cycle detection.
     * @return The {@link FunctionValue} that represents the calculation result
     *         of the parsed function string.
     */
    protected FunctionValue parseFunction(String function, Map<Integer, FunctionValue> replacements,
            Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences, IndexCoordinate referer) {

        function = getFunctionOnly(function);

        // process functions
        String processedFunction = processFunctions(function, replacements, parsedReferences, referer);

        // process parenthesis
        processedFunction = processParenthesis(processedFunction, replacements, parsedReferences, referer);

        String[] operandsAndOperators = processedFunction.split(operatorSplitRegex);

        List<FunctionValue> values = new ArrayList<FunctionValue>(operandsAndOperators.length);
        for (int i = 0; i < operandsAndOperators.length; i++) {
            String part = operandsAndOperators[i].trim();
            if (part.matches(operatorRegex)) {
                if ("-".equals(part)) { //$NON-NLS-1$
                    values.add(new NegateFunction());
                } else if ("+".equals(part)) { //$NON-NLS-1$
                    values.add(new SumFunction());
                } else if ("*".equals(part)) { //$NON-NLS-1$
                    values.add(new ProductFunction());
                } else if ("/".equals(part)) { //$NON-NLS-1$
                    values.add(new QuotientFunction());
                } else if ("^".equals(part)) { //$NON-NLS-1$
                    values.add(new PowerFunction());
                }
            } else if (part.matches(rangeRegex)) {
                MultipleValueFunctionValue multi = new MultipleValueFunctionValue();
                String[] parts = part.split(":"); //$NON-NLS-1$
                if (part.matches(referenceRangeRegex)) {
                    int[] from = evaluateReference(parts[0]);
                    int[] to = evaluateReference(parts[1]);

                    int fromColumn = Math.min(from[0], to[0]);
                    int toColumn = Math.max(from[0], to[0]);
                    int fromRow = Math.min(from[1], to[1]);
                    int toRow = Math.max(from[1], to[1]);

                    for (int row = fromRow; row <= toRow; row++) {
                        for (int column = fromColumn; column <= toColumn; column++) {
                            addDataProviderValue(column, row, multi.getValue(), parsedReferences, referer);
                        }
                    }
                } else if (part.matches(rowRangeRegex)) {
                    int from = Integer.valueOf(parts[0]) - 1;
                    int to = Integer.valueOf(parts[1]) - 1;

                    if (from > to) {
                        int tmp = to;
                        to = from;
                        from = tmp;
                    }

                    for (int row = from; row <= to; row++) {
                        for (int column = 0; column < getUnderlyingColumnCount(); column++) {
                            addDataProviderValue(column, row, multi.getValue(), parsedReferences, referer);
                        }
                    }
                } else if (part.matches(columnRangeRegex)) {
                    int from = getColumnIndex(parts[0]);
                    int to = getColumnIndex(parts[1]);

                    if (from > to) {
                        int tmp = to;
                        to = from;
                        from = tmp;
                    }

                    for (int column = from; column <= to; column++) {
                        for (int row = 0; row < getUnderlyingRowCount(); row++) {
                            addDataProviderValue(column, row, multi.getValue(), parsedReferences, referer);
                        }
                    }
                }
                values.add(multi);
            } else if (part.matches(referenceRegex)) {
                int[] coords = evaluateReference(part);
                addDataProviderValue(coords[0], coords[1], values, parsedReferences, referer);
            } else if (part.matches(placeholderRegex)) {
                String number = part.substring(1, part.length() - 1);
                try {
                    values.add(replacements.get(Integer.valueOf(number)));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(Messages.getString("FormulaParser.error.replacement"), e); //$NON-NLS-1$
                }
            } else if (part.matches(this.localizedDigitRegex)) {
                // check if last is big decimal and throw exception in that case
                if (values.size() > 0 && values.get(values.size() - 1) instanceof BigDecimalFunctionValue) {
                    throw new IllegalArgumentException(Messages.getString("FormulaParser.error.missingOperator")); //$NON-NLS-1$
                }

                values.add(new BigDecimalFunctionValue(convertToBigDecimal(part.trim())));
            } else {
                String s = part.trim();
                if (s.length() > 0) {
                    values.add(new StringFunctionValue(s));
                }
            }
        }

        int parts = 0;
        do {
            parts = values.size();
            values = processMultiplicationAndDivision(values);
        } while (parts != values.size());

        return combineFunctions(values);
    }

    /**
     * Process parts of a function that represent a function by name. Replaces
     * the function result with placeholders whose representations are put in
     * the given replacements map.
     *
     * @param function
     *            The function string
     * @param replacements
     *            The map of replacements to store the result of the function
     *            parsing.
     * @param parsedReferences
     *            The references that where parsed already together with their
     *            references if any. Needed for cycle detection.
     * @param referer
     *            The coordinate of the cell that refers to the value to add.
     *            Needed for cycle detection.
     * @return The modified string that contains placeholders for functions.
     */
    protected String processFunctions(String function, Map<Integer, FunctionValue> replacements,
            Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences, IndexCoordinate referer) {

        StringBuilder result = new StringBuilder();

        int startIndex = 0;
        Matcher functionMatcher = this.functionPattern.matcher(function);
        while (functionMatcher.find(startIndex)) {

            String functionName = null;
            String parameterString = null;

            // find closing parenthesis and update startSearch
            int openParanthesisCount = 0;
            for (int i = functionMatcher.start(); i < function.length(); i++) {
                char c = function.charAt(i);
                if (c == '(') {
                    openParanthesisCount++;
                    if (i > 0 && openParanthesisCount == 1) {
                        // remember the left side of the function string
                        result.append(function.substring(startIndex, functionMatcher.start()));

                        functionName = function.substring(functionMatcher.start(), i);
                        startIndex = i;
                    }
                } else if (c == ')') {
                    openParanthesisCount--;
                    if (openParanthesisCount < 0) {
                        throw new IllegalArgumentException(Messages.getString("FormulaParser.error.functionParameterNotOpened")); //$NON-NLS-1$
                    }

                    if (openParanthesisCount == 0) {
                        parameterString = function.substring(startIndex + 1, i);
                        startIndex = i + 1;
                        break;
                    }
                }
            }

            if (openParanthesisCount != 0) {
                throw new IllegalArgumentException(Messages.getString("FormulaParser.error.functionParameterNotClosed")); //$NON-NLS-1$
            }

            // determine function and get FunctionValue
            Class<? extends AbstractFunction> functionClass = this.functionMapping.get(functionName);

            if (functionClass == null) {
                throw new IllegalArgumentException("No function '" + functionName + "' registered"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            AbstractFunction fv = null;
            try {
                fv = functionClass.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(Messages.getString("FormulaParser.error.instantiation", e.getLocalizedMessage()), e); //$NON-NLS-1$
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(Messages.getString("FormulaParser.error.instantiation", e.getLocalizedMessage()), e); //$NON-NLS-1$
            }

            // process parameter
            Map<Integer, FunctionValue> nestedReplacements = new HashMap<Integer, FunctionValue>();
            parameterString = processFunctions(parameterString, nestedReplacements, parsedReferences, referer);
            String[] parameter = parameterString.split(";"); //$NON-NLS-1$

            for (String param : parameter) {
                fv.addFunctionValue(parseFunction(param, nestedReplacements, parsedReferences, referer));
            }

            // replace function with placeholder
            Integer pos = replacements.size();
            replacements.put(pos, fv);
            result.append("{").append(pos).append("}"); //$NON-NLS-1$//$NON-NLS-2$
        }

        // remember the right side of the function string
        if (startIndex < function.length()) {
            result.append(function.substring(startIndex, function.length()));
        }

        return result.toString();
    }

    /**
     * Process parts of a function that are combined in parenthesis. Replaces
     * the parenthesis with placeholders whose representations are put in the
     * given replacements map.
     *
     * @param function
     *            The function string.
     * @param replacements
     *            The map of replacements to store the result of the parenthesis
     *            parsing.
     * @param parsedReferences
     *            The references that where parsed already together with their
     *            references if any. Needed for cycle detection.
     * @param referer
     *            The coordinate of the cell that refers to the value to add.
     *            Needed for cycle detection.
     * @return The modified string that contains placeholders for parenthesis.
     */
    protected String processParenthesis(String function, Map<Integer, FunctionValue> replacements,
            Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences, IndexCoordinate referer) {

        StringBuilder result = new StringBuilder();

        int openParanthesisCount = 0;
        int startIndex = 0;
        for (int i = 0; i < function.length(); i++) {
            char c = function.charAt(i);
            if (c == '(') {
                openParanthesisCount++;
                if (i > 0 && openParanthesisCount == 1) {
                    result.append(function.substring(startIndex, i));
                    startIndex = i;
                }
            } else if (c == ')') {
                openParanthesisCount--;
                if (openParanthesisCount < 0) {
                    throw new IllegalArgumentException(Messages.getString("FormulaParser.error.parenthesisNotOpened")); //$NON-NLS-1$
                }

                if (openParanthesisCount == 0) {
                    String paranthesisFunctionString = function.substring(startIndex + 1, i);
                    FunctionValue paranthesisFunction = parseFunction(paranthesisFunctionString, parsedReferences, referer);
                    Integer pos = replacements.size();
                    replacements.put(pos, paranthesisFunction);
                    result.append("{").append(pos).append("}"); //$NON-NLS-1$//$NON-NLS-2$
                    startIndex = i + 1;
                }
            }
        }

        if (startIndex < function.length()) {
            result.append(function.substring(startIndex, function.length()));
        }

        if (openParanthesisCount != 0) {
            throw new IllegalArgumentException(Messages.getString("FormulaParser.error.parenthesisNotClosed")); //$NON-NLS-1$
        }

        return result.toString();
    }

    /**
     * Process multiplication and division {@link FunctionValue}s first.
     *
     * @param values
     *            The list of parsed {@link FunctionValue}s.
     * @return The list of {@link FunctionValue}s where multiplication and
     *         division is already combined.
     */
    protected List<FunctionValue> processMultiplicationAndDivision(List<FunctionValue> values) {
        List<FunctionValue> result = new ArrayList<FunctionValue>();

        // we only process one multiplication/division operation at once
        boolean operatorFound = false;

        for (Iterator<FunctionValue> it = values.iterator(); it.hasNext();) {
            FunctionValue v = it.next();
            if (!operatorFound
                    && it.hasNext()
                    && result.size() > 0
                    && (v instanceof ProductFunction || v instanceof QuotientFunction || v instanceof PowerFunction)
                    && ((AbstractFunction) v).isEmpty()) {

                // remove the last value that was added
                FunctionValue previous = result.remove(result.size() - 1);
                ((OperatorFunctionValue) v).addFunctionValue(previous);

                FunctionValue next = it.next();
                if (next instanceof NegateFunction) {
                    ((NegateFunction) next).addFunctionValue(it.next());
                }
                ((OperatorFunctionValue) v).addFunctionValue(next);

                operatorFound = true;
                result.add(v);
            } else {
                result.add(v);
            }
        }

        return result;
    }

    /**
     * Combines {@link FunctionValue}s for processing the parsed values.
     *
     * @param values
     *            The list of {@link FunctionValue}s to combine.
     * @return The single {@link FunctionValue} as a result of the value
     *         combination.
     */
    protected FunctionValue combineFunctions(List<FunctionValue> values) {
        FunctionValue result = null;

        if (values.size() == 1) {
            result = values.get(0);
        } else {
            for (int i = 0; i < values.size(); i++) {
                FunctionValue v = values.get(i);
                if (v instanceof AbstractSingleValueFunction
                        || v instanceof AbstractMathSingleValueFunction) {
                    i++;
                    ((AbstractFunction) v).addFunctionValue(values.get(i));
                    if (result != null && v instanceof NegateFunction) {
                        SumFunction sum = new SumFunction();
                        sum.addFunctionValue(result);
                        sum.addFunctionValue(v);
                        result = sum;
                    } else {
                        result = v;
                    }
                } else if (v instanceof OperatorFunctionValue) {
                    if (i > 0 && result == null) {
                        ((OperatorFunctionValue) v).addFunctionValue(values.get(i - 1));
                    } else if (result != null) {
                        ((OperatorFunctionValue) v).addFunctionValue(result);
                    }

                    if (i > 0) {
                        i++;
                        if (i < values.size()) {
                            ((OperatorFunctionValue) v).addFunctionValue(values.get(i));
                        }
                    }
                    result = v;
                } else {
                    result = v;
                }
            }
        }

        return result;
    }

    /**
     * Evaluates a reference string to cell coordinates.
     *
     * @param reference
     *            The reference string to evaluate.
     * @return The cell coordinates for the given reference string.
     */
    protected int[] evaluateReference(String reference) {
        String columnString = ""; //$NON-NLS-1$
        String rowString = ""; //$NON-NLS-1$
        for (int i = 0; i < reference.length(); i++) {
            char c = reference.charAt(i);
            if (Character.isLetter(c)) {
                columnString += c;
            } else if (Character.isDigit(c)) {
                rowString += c;
            }
        }

        int column = getColumnIndex(columnString);
        int row = Integer.valueOf(rowString) - 1;

        return new int[] { column, row };
    }

    /**
     * Calculate the column index out of a character based string, e.g. A = 0,
     * AA = 26
     *
     * @param columnLiteral
     *            The string to calculate the column index from.
     * @return The column index for the given string.
     */
    protected int getColumnIndex(String columnLiteral) {
        int column = 0;
        int pos = 0;
        for (int i = columnLiteral.length() - 1; i >= 0; i--) {
            char c = columnLiteral.charAt(i);
            column += (((c) - (pos == 0 ? 65 : 64)) * (Math.pow(26, pos)));
            pos++;
        }
        return column;
    }

    /**
     * Converts the given column index to a character based representation for
     * reference handling.
     *
     * @param index
     *            The column index to convert.
     * @return The parsed character representation for a column index.
     */
    protected String convertIndexToColumnString(int index) {
        int characterAddition = 65;
        int quotient = index;
        int remainder = 0;
        String result = ""; //$NON-NLS-1$
        do {
            remainder = quotient % 26;
            quotient = quotient / 26;

            result = Character.toString((char) (remainder + characterAddition)) + result;
            characterAddition = 64;
        } while (quotient != 0);
        return result;
    }

    /**
     * Retrieves a value from the {@link IDataProvider} for the given
     * coordinates and adds it to the given list of {@link FunctionValue}s for
     * further processing.
     *
     * @param column
     *            The column index of the value.
     * @param row
     *            The row index of the value.
     * @param parsedReferences
     *            The references that where parsed already together with their
     *            references if any. Needed for cycle detection.
     * @param referer
     *            The coordinate of the cell that refers to the value to add.
     *            Needed for cycle detection.
     * @param values
     *            The list of {@link FunctionValue} to add the data provider
     *            value to.
     */
    protected void addDataProviderValue(int column, int row, List<FunctionValue> values,
            Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences, IndexCoordinate referer) {

        Object value = getUnderlyingDataValue(column, row);
        if (value != null) {
            String toParse = value.toString();
            if (value instanceof Number) {
                toParse = this.decimalFormat.format(value);
            }

            // avoid circular references
            IndexCoordinate ref = new IndexCoordinate(column, row);
            if (!parsedReferences.containsKey(ref)) {
                parsedReferences.put(ref, new HashSet<IndexCoordinate>());
            }
            if (referer != null) {
                parsedReferences.get(referer).add(ref);
            }

            if (detectCycle(parsedReferences)) {
                throw new FunctionException("#REF!", Messages.getString("FormulaParser.error.circular")); //$NON-NLS-1$//$NON-NLS-2$
            }

            FunctionValue parseResult = parseFunction(toParse, parsedReferences, ref);
            if (parseResult != null) {
                values.add(parseResult);
            }
        }
    }

    /**
     * Updates the localized regular expression for decimal values. Uses the
     * current set {@link DecimalFormat} to determine the decimal separator.
     *
     * @see FormulaParser#setDecimalFormat(DecimalFormat)
     */
    protected void updateLocalizedDigitRegex() {
        this.localizedDigitRegex = digitRegex + "(\\" + this.decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() + digitRegex + ")?"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Set the {@link DecimalFormat} that should be used to determine the
     * decimal separator. By default the {@link DecimalFormat#getInstance()} is
     * set which uses the current default {@link Locale}.
     *
     * @param format
     *            The {@link DecimalFormat} to use for determine the decimal
     *            separator.
     */
    public void setDecimalFormat(DecimalFormat format) {
        this.decimalFormat = format;
        updateLocalizedDigitRegex();
    }

    /**
     * Checks if a given String is a function or not.
     *
     * @param function
     *            The function String to check
     * @return <code>true</code> if the given String represents a function,
     *         <code>false</code> if not
     */
    public boolean isFunction(String function) {
        return function.startsWith("="); //$NON-NLS-1$
    }

    /**
     * Checks if the given string contains a function marker (default:
     * leading'=') and returns a string without that marker to have the function
     * only.
     *
     * @param function
     *            The function string to modify.
     * @return The function only string
     */
    public String getFunctionOnly(String function) {
        if (function.startsWith("=")) { //$NON-NLS-1$
            function = function.substring(1);
        }
        return function;
    }

    /**
     * Checks if the given value is a number value.
     *
     * @param value
     *            The value to check.
     * @return <code>true</code> if the given value is an integral or decimal
     *         value, <code>false</code> if not
     */
    public boolean isNumber(String value) {
        return value.matches(this.localizedDigitRegex);
    }

    /**
     * Checks if the given {@link BigDecimal} is an integer or a decimal value.
     *
     * @param value
     *            The value to check.
     * @return <code>true</code> if the given value is an integer,
     *         <code>false</code> if it is a decimal.
     */
    public boolean isIntegerValue(BigDecimal value) {
        return value.signum() == 0 || value.scale() <= 0 || value.stripTrailingZeros().scale() <= 0;
    }

    /**
     * Converts a given String into a {@link BigDecimal}. Is able to convert
     * decimal values with localized decimal separators.
     *
     * @param value
     *            The value to convert.
     * @return The {@link BigDecimal} for the given value.
     */
    public BigDecimal convertToBigDecimal(String value) {
        value = value.replaceAll("\\" + this.decimalFormat.getDecimalFormatSymbols().getDecimalSeparator(), "."); //$NON-NLS-1$ //$NON-NLS-2$
        return new BigDecimal(value);
    }

    /**
     * @return The column count of the underlying data model. The base
     *         implementation uses the underlying {@link IDataProvider}.
     */
    protected int getUnderlyingColumnCount() {
        return this.dataProvider.getColumnCount();
    }

    /**
     * @return The row count of the underlying data model. The base
     *         implementation uses the underlying {@link IDataProvider}.
     */
    protected int getUnderlyingRowCount() {
        return this.dataProvider.getRowCount();
    }

    /**
     *
     * @param column
     *            The column index of the cell whose value is requested.
     * @param row
     *            The row index of the cell whose value is requested.
     * @return The data value for the given column and row index out of the
     *         underlying data model. The base implementation uses the
     *         underlying {@link IDataProvider}.
     */
    protected Object getUnderlyingDataValue(int column, int row) {
        return this.dataProvider.getDataValue(column, row);
    }

    /**
     * Updates the references in a function string. Needed for copy operations.
     *
     * @param function
     *            The function string to update the references.
     * @param fromColumn
     *            The column index from where a formula is transfered.
     * @param fromRow
     *            The row index from where a formula is transfered.
     * @param toColumn
     *            The column index to where a formula is transfered.
     * @param toRow
     *            The row index to where a formula is transfered.
     * @return The function string with updated references.
     */
    public String updateReferences(String function, int fromColumn, int fromRow, int toColumn, int toRow) {
        int columnDiff = toColumn - fromColumn;
        int rowDiff = toRow - fromRow;

        Matcher referenceMatcher = this.referencePattern.matcher(function);
        StringBuilder result = new StringBuilder();
        int start = 0;
        while (referenceMatcher.find()) {
            result.append(function.substring(start, referenceMatcher.start()));
            String reference = function.substring(referenceMatcher.start(), referenceMatcher.end());
            int[] coords = evaluateReference(reference);
            coords[0] += columnDiff;
            coords[1] += rowDiff;

            if (coords[0] < 0 || (coords[1] + 1) < 0) {
                throw new FunctionException("#REF!", Messages.getString("FormulaParser.error.referenceNotExist")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            String newReference = convertIndexToColumnString(coords[0]) + (coords[1] + 1);
            result.append(newReference);
            start = referenceMatcher.end();
        }

        if (start < function.length()) {
            result.append(function.substring(start));
        }

        return result.toString();
    }

    // cycle detection code

    protected boolean detectCycle(Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences) {
        Set<IndexCoordinate> initPath = new HashSet<IndexCoordinate>();
        for (Map.Entry<IndexCoordinate, Set<IndexCoordinate>> entry : parsedReferences.entrySet()) {
            if (isCyclic(new Node(entry.getKey(), entry.getValue()), initPath, parsedReferences)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCyclic(Node currNode, Set<IndexCoordinate> path, Map<IndexCoordinate, Set<IndexCoordinate>> parsedReferences) {
        if (currNode != null) {
            if (path.contains(currNode.referer)) {
                return true;
            } else {
                if (!currNode.references.isEmpty()) {
                    path.add(currNode.referer);
                    for (IndexCoordinate node : currNode.references) {
                        if (isCyclic(new Node(node, parsedReferences.get(node)), path, parsedReferences)) {
                            return true;
                        } else {
                            path.remove(node);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Node class to implement depth-first-search algorithm to search for
     * cycles.
     */
    class Node {
        IndexCoordinate referer;
        Set<IndexCoordinate> references;

        public Node(IndexCoordinate referer, Set<IndexCoordinate> references) {
            this.referer = referer;
            this.references = references;
        }
    }

}
