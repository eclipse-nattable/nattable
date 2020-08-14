/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.IndexCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionException;
import org.eclipse.nebula.widgets.nattable.formula.function.FunctionValue;
import org.eclipse.nebula.widgets.nattable.formula.function.ProductFunction;
import org.eclipse.nebula.widgets.nattable.formula.function.SumFunction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FormulaParserTest {

    private static Locale defaultLocale;

    @BeforeClass
    public static void setup() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterClass
    public static void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new Object[10][10]);
    FormulaParser parser = new FormulaParser(this.dataProvider);

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingOperator() {
        this.parser.parseFunction("5 3");
    }

    @Test
    public void shouldParseNativeSumFunctionString() {
        FunctionValue result = this.parser.parseFunction("5 + 3");
        assertEquals(new BigDecimal(5 + 3), result.getValue());

        result = this.parser.parseFunction("5+3");
        assertEquals(new BigDecimal(5 + 3), result.getValue());

        result = this.parser.parseFunction("5+3+15");
        assertEquals(new BigDecimal(5 + 3 + 15), result.getValue());

        result = this.parser.parseFunction("5+3+5+3");
        assertEquals(new BigDecimal(5 + 3 + 5 + 3), result.getValue());
    }

    @Test
    public void shouldParseNativeSumWithDecimals() {
        FunctionValue result = this.parser.parseFunction("5.4 + 3.2");
        assertEquals(new BigDecimal("8.6"), result.getValue());
    }

    @Test
    public void shouldParseNativeSubtractFunctionString() {
        FunctionValue result = this.parser.parseFunction("5 - 3");
        assertEquals(new BigDecimal(5 - 3), result.getValue());

        result = this.parser.parseFunction("15 - 3 -2");
        assertEquals(new BigDecimal(15 - 3 - 2), result.getValue());

        result = this.parser.parseFunction("25 - 3-15-10");
        assertEquals(new BigDecimal(25 - 3 - 15 - 10), result.getValue());

        result = this.parser.parseFunction("-42");
        assertEquals(new BigDecimal(-42), result.getValue());

        result = this.parser.parseFunction("-10-5");
        assertEquals(new BigDecimal(-10 - 5), result.getValue());
    }

    @Test
    public void shouldParseNativeSumAndSubtractionFunctionString() {
        FunctionValue result = this.parser.parseFunction("- 5 + 3");
        assertEquals(new BigDecimal(-5 + 3), result.getValue());

        result = this.parser.parseFunction("15 - 3 +2");
        assertEquals(new BigDecimal(15 - 3 + 2), result.getValue());

        result = this.parser.parseFunction("-25 + 5-15");
        assertEquals(new BigDecimal(-25 + 5 - 15), result.getValue());

        result = this.parser.parseFunction("40+10-25-5");
        assertEquals(new BigDecimal(40 + 10 - 25 - 5), result.getValue());
    }

    @Test
    public void shouldParseNativeProductFunctionString() {
        FunctionValue result = this.parser.parseFunction("5 * 3");
        assertEquals(new BigDecimal(5 * 3), result.getValue());

        result = this.parser.parseFunction("5 * 3 *2");
        assertEquals(new BigDecimal(5 * 3 * 2), result.getValue());

        result = this.parser.parseFunction("5 * 3*20 * 7");
        assertEquals(new BigDecimal(5 * 3 * 20 * 7), result.getValue());

        result = this.parser.parseFunction("5 * -3");
        assertEquals(new BigDecimal(5 * -3), result.getValue());

        result = this.parser.parseFunction("-5 * 3");
        assertEquals(new BigDecimal(-5 * 3), result.getValue());

        result = this.parser.parseFunction("5 * -3 * 7");
        assertEquals(new BigDecimal(5 * -3 * 7), result.getValue());
    }

    @Test
    public void shouldParseNativeDifferenceFunctionString() {
        FunctionValue result = this.parser.parseFunction("15 / 3");
        assertEquals(new BigDecimal(15 / 3), result.getValue());

        result = this.parser.parseFunction("20 / 2 /5");
        assertEquals(new BigDecimal(20 / 2 / 5), result.getValue());

        result = this.parser.parseFunction("5 / 2");
        assertEquals(new BigDecimal(5d / 2), result.getValue());

        result = this.parser.parseFunction("100 / 4 / 5 / 2");
        assertEquals(new BigDecimal(100d / 4 / 5 / 2), result.getValue());

        result = this.parser.parseFunction("-15 / 3");
        assertEquals(new BigDecimal(-15 / 3), result.getValue());

        result = this.parser.parseFunction("-5 / 3");
        assertEquals(new BigDecimal(-5).divide(new BigDecimal(3), 9, RoundingMode.HALF_UP), result.getValue());

        result = this.parser.parseFunction("5 / -3");
        assertEquals(new BigDecimal(5).divide(new BigDecimal(-3), 9, RoundingMode.HALF_UP), result.getValue());
    }

    @Test
    public void shouldParseMixedNativeFunction() {
        FunctionValue result = this.parser.parseFunction("5 + 3 * 2");
        assertEquals(new BigDecimal(5 + 3 * 2), result.getValue());
        assertEquals(new BigDecimal(11), result.getValue());

        result = this.parser.parseFunction("5 * 3 + 2");
        assertEquals(new BigDecimal(5 * 3 + 2), result.getValue());
        assertEquals(new BigDecimal(5 * 3 + 2), result.getValue());

        result = this.parser.parseFunction("15/3*6-4+3");
        assertEquals(new BigDecimal(15 / 3 * 6 - 4 + 3), result.getValue());
        assertEquals(new BigDecimal(29), result.getValue());

        result = this.parser.parseFunction("2*5+10/4-3");
        assertEquals(new BigDecimal(2 * 5 + 10d / 4 - 3), result.getValue());
        assertEquals(new BigDecimal(9.5), result.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnclosedParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processParenthesis("(5+3", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnopenedParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processParenthesis("5+3)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test
    public void shouldReplaceOneParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        String processed = this.parser.processParenthesis("(5+3)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("10 + (5+3)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("10 + {0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("(5+3) + 42", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0} + 42", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnclosedTwoParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processParenthesis("(5+3) + (7 - 2", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnopenedTwoParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processParenthesis("(5+3) + 7 - 3)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test
    public void shouldReplaceTwoParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        String processed = this.parser.processParenthesis("(5+3) - (5+5)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0} - {1}", processed);
        assertEquals(2, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertTrue(replacements.get(1) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());
        assertEquals(new BigDecimal(5 + 5), replacements.get(1).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("(5+3) + 10 + (5+5)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0} + 10 + {1}", processed);
        assertEquals(2, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertTrue(replacements.get(1) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());
        assertEquals(new BigDecimal(5 + 5), replacements.get(1).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("10 + (5+3) + (5+5) + 42", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("10 + {0} + {1} + 42", processed);
        assertEquals(2, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertTrue(replacements.get(1) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());
        assertEquals(new BigDecimal(5 + 5), replacements.get(1).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("10 + (5+3) + 42 + (5+5)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("10 + {0} + 42 + {1}", processed);
        assertEquals(2, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertTrue(replacements.get(1) instanceof SumFunction);
        assertEquals(new BigDecimal(5 + 3), replacements.get(0).getValue());
        assertEquals(new BigDecimal(5 + 5), replacements.get(1).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnclosedNestedParanthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processParenthesis("((5+3 - 3)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnopenedNestedParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processParenthesis("(5+3) + 7 - 3)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test
    public void shouldReplaceNestedParenthesis() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        String processed = this.parser.processParenthesis("((5+3)*2)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof ProductFunction);
        assertEquals(new BigDecimal(((5 + 3) * 2)), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("(2*(5+3))", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof ProductFunction);
        assertEquals(new BigDecimal((2 * (5 + 3))), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processParenthesis("5+(2*(5+3))", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("5+{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof ProductFunction);
        assertEquals(new BigDecimal((2 * (5 + 3))), replacements.get(0).getValue());
    }

    @Test
    public void shouldMatchPlaceholderRegex() {
        assertTrue("{0}".matches(FormulaParser.PLACEHOLDER_REGEX));
        assertTrue("{1}".matches(FormulaParser.PLACEHOLDER_REGEX));
        assertTrue("{10}".matches(FormulaParser.PLACEHOLDER_REGEX));
        assertTrue("{42}".matches(FormulaParser.PLACEHOLDER_REGEX));
        assertTrue("{100}".matches(FormulaParser.PLACEHOLDER_REGEX));
    }

    @Test
    public void shouldParseComplexNativeFunctionString() {
        FunctionValue result = this.parser.parseFunction("5+(2*(5+3))");
        assertEquals(new BigDecimal(5 + (2 * (5 + 3))), result.getValue());

        result = this.parser.parseFunction("(5 + 3) * 2");
        assertEquals(new BigDecimal((5 + 3) * 2), result.getValue());
        assertEquals(new BigDecimal(16), result.getValue());

        result = this.parser.parseFunction("(5 + 3) * 2");
        assertEquals(new BigDecimal((5 + 3) * 2), result.getValue());
        assertEquals(new BigDecimal(16), result.getValue());

        result = this.parser.parseFunction("30/(3+2)-2*3");
        assertEquals(new BigDecimal(30 / (3 + 2) - 2 * 3), result.getValue());
        assertEquals(new BigDecimal(0), result.getValue());
    }

    @Test
    public void shouldMatchOperatorRegex() {
        assertTrue("+".matches(FormulaParser.OPERATOR_REGEX));
        assertTrue("-".matches(FormulaParser.OPERATOR_REGEX));
        assertTrue("*".matches(FormulaParser.OPERATOR_REGEX));
        assertTrue("/".matches(FormulaParser.OPERATOR_REGEX));
        assertTrue("^".matches(FormulaParser.OPERATOR_REGEX));
    }

    @Test
    public void shouldMatchDigiRegex() {
        assertTrue("5".matches(FormulaParser.DIGIT_REGEX));
        assertTrue("15".matches(FormulaParser.DIGIT_REGEX));
        assertTrue("426".matches(FormulaParser.DIGIT_REGEX));
    }

    @Test
    public void shouldMatchLocalizedDigitRegex() {
        assertTrue("5".matches(this.parser.localizedDigitRegex));
        assertTrue("15".matches(this.parser.localizedDigitRegex));
        assertTrue("426".matches(this.parser.localizedDigitRegex));
        assertTrue("1.5".matches(this.parser.localizedDigitRegex));
        assertTrue("42.75".matches(this.parser.localizedDigitRegex));

        this.parser.setDecimalFormat((DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN));
        assertTrue("1,5".matches(this.parser.localizedDigitRegex));
        assertTrue("42,75".matches(this.parser.localizedDigitRegex));

        this.parser.setDecimalFormat((DecimalFormat) DecimalFormat.getInstance());
        assertTrue("1.5".matches(this.parser.localizedDigitRegex));
        assertTrue("42.75".matches(this.parser.localizedDigitRegex));
    }

    @Test
    public void shouldParseNativePowerFunction() {
        FunctionValue result = this.parser.parseFunction("5 ^ 2");
        assertEquals(new BigDecimal(5).pow(2), result.getValue());

        result = this.parser.parseFunction("5+3^2");
        assertEquals(new BigDecimal(14), result.getValue());
    }

    @Test
    public void shouldProcessPowerFirst() {
        FunctionValue result = this.parser.parseFunction("(4 * 8) ^ 2");
        assertEquals(new BigDecimal(1024), result.getValue());

        result = this.parser.parseFunction("4 * 8 ^ 2");
        assertEquals(new BigDecimal(256), result.getValue());
    }

    @Test
    public void shouldMatchFunctionRegex() {
        assertTrue(this.parser.functionPattern.matcher("AVERAGE(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("AVERAGE()").find());
        assertTrue(this.parser.functionPattern.matcher("AVERAGE(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("AVERAGE(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("AVERAGE(5; 3; -1)").find());

        assertTrue(this.parser.functionPattern.matcher("NEGATE(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("NEGATE()").find());
        assertTrue(this.parser.functionPattern.matcher("NEGATE(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("NEGATE(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("NEGATE(5; 3; -1)").find());

        assertTrue(this.parser.functionPattern.matcher("POWER(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("POWER()").find());
        assertTrue(this.parser.functionPattern.matcher("POWER(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("POWER(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("POWER(5; 3; -1)").find());

        assertTrue(this.parser.functionPattern.matcher("PRODUCT(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("PRODUCT()").find());
        assertTrue(this.parser.functionPattern.matcher("PRODUCT(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("PRODUCT(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("PRODUCT(5; 3; -1)").find());

        assertTrue(this.parser.functionPattern.matcher("QUOTIENT(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("QUOTIENT()").find());
        assertTrue(this.parser.functionPattern.matcher("QUOTIENT(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("QUOTIENT(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("QUOTIENT(5; 3; -1)").find());

        assertTrue(this.parser.functionPattern.matcher("SQRT(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("SQRT()").find());
        assertTrue(this.parser.functionPattern.matcher("SQRT(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("SQRT(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("SQRT(5; 3; -1)").find());

        assertTrue(this.parser.functionPattern.matcher("SUM(5+3)").find());
        assertTrue(this.parser.functionPattern.matcher("SUM()").find());
        assertTrue(this.parser.functionPattern.matcher("SUM(5; 3)").find());
        assertTrue(this.parser.functionPattern.matcher("SUM(5;3)").find());
        assertTrue(this.parser.functionPattern.matcher("SUM(5; 3; -1)").find());
    }

    @Test
    public void shouldParseFunction() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        String processed = this.parser.processFunctions("SUM(5; 3; 2)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal((5 + 3 + 2)), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processFunctions("42 + SUM(5; 3; 2)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("42 + {0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal((5 + 3 + 2)), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processFunctions("SUM(5; 3; 2) - 42", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0} - 42", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal((5 + 3 + 2)), replacements.get(0).getValue());
    }

    @Test
    public void shouldParseMultipleFunction() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        String processed = this.parser.processFunctions("SUM(5; 3; 2) - PRODUCT(2; 5)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0} - {1}", processed);
        assertEquals(2, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertTrue(replacements.get(1) instanceof ProductFunction);
        assertEquals(new BigDecimal((5 + 3 + 2)), replacements.get(0).getValue());
        assertEquals(new BigDecimal((2 * 5)), replacements.get(1).getValue());
    }

    @Test
    public void shouldParseNestedFunctions() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        String processed = this.parser.processFunctions("SUM(PRODUCT(2; 5); QUOTIENT(10; 5))", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal((2 * 5) + (10 / 5)), replacements.get(0).getValue());

        replacements = new HashMap<>();
        processed = this.parser.processFunctions("SUM(PRODUCT(2; SUM(2; 2)); 5)", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
        assertEquals("{0}", processed);
        assertEquals(1, replacements.size());
        assertTrue(replacements.get(0) instanceof SumFunction);
        assertEquals(new BigDecimal((2 * (2 + 2)) + 5), replacements.get(0).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNotClosingFunction() {
        Map<Integer, FunctionValue> replacements = new HashMap<>();
        this.parser.processFunctions("SUM(5; 3; 2", replacements, new HashMap<IndexCoordinate, Set<IndexCoordinate>>(), null);
    }

    @Test
    public void shouldProcessFunction() {
        FunctionValue result = this.parser.parseFunction("SUM(5; 3; 2)");
        assertEquals(new BigDecimal(10), result.getValue());

        result = this.parser.parseFunction("PRODUCT(7; 6)");
        assertEquals(new BigDecimal(42), result.getValue());

        result = this.parser.parseFunction("AVERAGE(7; 6; 13; 14)");
        assertEquals(new BigDecimal(10), result.getValue());

        result = this.parser.parseFunction("QUOTIENT(15; 3)");
        assertEquals(new BigDecimal(5), result.getValue());

        result = this.parser.parseFunction("POWER(5; 3)");
        assertEquals(new BigDecimal(125), result.getValue());

        result = this.parser.parseFunction("NEGATE(42)");
        assertEquals(new BigDecimal(-42), result.getValue());

        result = this.parser.parseFunction("SQRT(144)");
        assertEquals(BigDecimal.valueOf(12d), result.getValue());
    }

    @Test
    public void shouldProcessMultipleFunctions() {
        FunctionValue result = this.parser.parseFunction("SUM(5; 3; 2) - PRODUCT(2; 5)");
        assertEquals(new BigDecimal(0), result.getValue());
    }

    @Test
    public void shouldProcessNestedFunctions() {
        FunctionValue result = this.parser.parseFunction("SUM(PRODUCT(2; 5); QUOTIENT(10; 5))");
        assertEquals(new BigDecimal(12), result.getValue());

        result = this.parser.parseFunction("SUM(PRODUCT(2; SUM(2; 2)); 5)");
        assertEquals(new BigDecimal(13), result.getValue());
    }

    @Test
    public void shouldMatchReferenceRegex() {
        assertTrue("A7".matches(FormulaParser.REFERENCE_REGEX));
        assertTrue("Z9".matches(FormulaParser.REFERENCE_REGEX));
        assertTrue("AZ1".matches(FormulaParser.REFERENCE_REGEX));
        assertTrue("C27".matches(FormulaParser.REFERENCE_REGEX));
        assertTrue("AC420".matches(FormulaParser.REFERENCE_REGEX));
    }

    @Test
    public void shouldNotMatchReferenceRegex() {
        assertFalse("A".matches(FormulaParser.REFERENCE_REGEX));
        assertFalse("9".matches(FormulaParser.REFERENCE_REGEX));
    }

    @Test
    public void shouldMatchReferenceRangeRegex() {
        assertTrue("A7:C7".matches(FormulaParser.REFERENCE_RANGE_REGEX));
        assertTrue("Z9:Z10".matches(FormulaParser.REFERENCE_RANGE_REGEX));
        assertTrue("AZ1:AZ12".matches(FormulaParser.REFERENCE_RANGE_REGEX));
    }

    @Test
    public void shouldNotMatchReferenceRangeRegex() {
        assertFalse("A:C7".matches(FormulaParser.REFERENCE_RANGE_REGEX));
        assertFalse("Z9:Z".matches(FormulaParser.REFERENCE_RANGE_REGEX));
        assertFalse("1:AZ12".matches(FormulaParser.REFERENCE_RANGE_REGEX));
        assertFalse("AZ1:12".matches(FormulaParser.REFERENCE_RANGE_REGEX));
        assertFalse("A1:A2:A3".matches(FormulaParser.REFERENCE_RANGE_REGEX));
    }

    @Test
    public void shouldMatchRowRangeRegex() {
        assertTrue("7:7".matches(FormulaParser.ROW_RANGE_REGEX));
        assertTrue("9:10".matches(FormulaParser.ROW_RANGE_REGEX));
        assertTrue("1:112".matches(FormulaParser.ROW_RANGE_REGEX));
    }

    @Test
    public void shouldNotMatchRowRangeRegex() {
        assertFalse("A1:C7".matches(FormulaParser.ROW_RANGE_REGEX));
        assertFalse("9:Z".matches(FormulaParser.ROW_RANGE_REGEX));
        assertFalse("1:AZ12".matches(FormulaParser.ROW_RANGE_REGEX));
        assertFalse("AZ1:12".matches(FormulaParser.ROW_RANGE_REGEX));
    }

    @Test
    public void shouldMatchColumnRangeRegex() {
        assertTrue("A:C".matches(FormulaParser.COLUMN_RANGE_REGEX));
        assertTrue("Z:Z".matches(FormulaParser.COLUMN_RANGE_REGEX));
        assertTrue("A:AZ".matches(FormulaParser.COLUMN_RANGE_REGEX));
    }

    @Test
    public void shouldNotMatchColumnRangeRegex() {
        assertFalse("A7:C7".matches(FormulaParser.COLUMN_RANGE_REGEX));
        assertFalse("9:Z".matches(FormulaParser.COLUMN_RANGE_REGEX));
        assertFalse("1:AZ12".matches(FormulaParser.COLUMN_RANGE_REGEX));
        assertFalse("AZ1:12".matches(FormulaParser.COLUMN_RANGE_REGEX));
    }

    @Test
    public void shouldMatchRangeRegex() {
        assertTrue("A:C".matches(FormulaParser.RANGE_REGEX));
        assertTrue("1:1".matches(FormulaParser.RANGE_REGEX));
        assertTrue("A1:C4".matches(FormulaParser.RANGE_REGEX));
    }

    @Test
    public void shouldNotMatchRangeRegex() {
        assertFalse("A:1".matches(FormulaParser.RANGE_REGEX));
        assertFalse("1:A".matches(FormulaParser.RANGE_REGEX));
        assertFalse("A:C4".matches(FormulaParser.RANGE_REGEX));
        assertFalse("A1:4".matches(FormulaParser.RANGE_REGEX));
        assertFalse("A1:A4:B3".matches(FormulaParser.RANGE_REGEX));
    }

    @Test
    public void shouldEvaluateReference() {
        int[] coords = this.parser.evaluateReference("A1");
        assertEquals(0, coords[0]);
        assertEquals(0, coords[1]);

        coords = this.parser.evaluateReference("Z42");
        assertEquals(25, coords[0]);
        assertEquals(41, coords[1]);

        coords = this.parser.evaluateReference("AZ213");
        assertEquals(51, coords[0]);
        assertEquals(212, coords[1]);

        coords = this.parser.evaluateReference("BA1");
        assertEquals(52, coords[0]);
        assertEquals(0, coords[1]);

        coords = this.parser.evaluateReference("ZZ1");
        assertEquals(701, coords[0]);
        assertEquals(0, coords[1]);

        coords = this.parser.evaluateReference("AAA1");
        assertEquals(702, coords[0]);
        assertEquals(0, coords[1]);
    }

    @Test
    public void shouldConvertIndexToColumnString() {
        assertEquals("A", this.parser.convertIndexToColumnString(0));
        assertEquals("Z", this.parser.convertIndexToColumnString(25));
        assertEquals("AZ", this.parser.convertIndexToColumnString(51));
        assertEquals("BA", this.parser.convertIndexToColumnString(52));
        assertEquals("BJ", this.parser.convertIndexToColumnString(61));
        assertEquals("AAA", this.parser.convertIndexToColumnString(702));
    }

    @Test
    public void shouldEvaluateReferences() {
        this.dataProvider.setDataValue(0, 0, 5);
        this.dataProvider.setDataValue(0, 1, 5);
        this.dataProvider.setDataValue(0, 2, "=SUM(A1;A2)");
        this.dataProvider.setDataValue(1, 0, 3);
        this.dataProvider.setDataValue(1, 1, 3);
        this.dataProvider.setDataValue(1, 2, "=SUM(B1;B2)");

        FunctionValue result = this.parser.parseFunction("SUM(A1;A2)");
        assertEquals(new BigDecimal(10), result.getValue());

        result = this.parser.parseFunction("SUM(B1;B2)");
        assertEquals(new BigDecimal(6), result.getValue());

        result = this.parser.parseFunction("AVERAGE(A3;B3)");
        assertEquals(new BigDecimal(8), result.getValue());
    }

    @Test
    public void shouldEvaluateRowRange() {
        for (int row = 0; row < this.dataProvider.getRowCount(); row++) {
            for (int column = 0; column < this.dataProvider.getColumnCount(); column++) {
                this.dataProvider.setDataValue(column, row, row + 1);
            }
        }

        FunctionValue result = this.parser.parseFunction("SUM(5:5)");
        assertEquals(new BigDecimal(50), result.getValue());

        result = this.parser.parseFunction("SUM(5:6)");
        assertEquals(new BigDecimal(110), result.getValue());

        result = this.parser.parseFunction("SUM(5:8)");
        assertEquals(new BigDecimal(260), result.getValue());

        result = this.parser.parseFunction("SUM(8:5)");
        assertEquals(new BigDecimal(260), result.getValue());
    }

    @Test
    public void shouldEvaluateColumnRange() {
        for (int column = 0; column < this.dataProvider.getColumnCount(); column++) {
            for (int row = 0; row < this.dataProvider.getRowCount(); row++) {
                this.dataProvider.setDataValue(column, row, column + 1);
            }
        }

        FunctionValue result = this.parser.parseFunction("SUM(E:E)");
        assertEquals(new BigDecimal(50), result.getValue());

        result = this.parser.parseFunction("SUM(E:F)");
        assertEquals(new BigDecimal(110), result.getValue());

        result = this.parser.parseFunction("SUM(E:H)");
        assertEquals(new BigDecimal(260), result.getValue());

        result = this.parser.parseFunction("SUM(H:E)");
        assertEquals(new BigDecimal(260), result.getValue());
    }

    @Test
    public void shouldEvaluateReferenceRange() {
        for (int row = 0; row < this.dataProvider.getRowCount(); row++) {
            for (int column = 0; column < this.dataProvider.getColumnCount(); column++) {
                this.dataProvider.setDataValue(column, row, row + 1);
            }
        }

        FunctionValue result = this.parser.parseFunction("=SUM(C3:F5)");
        assertEquals(new BigDecimal(48), result.getValue());

        result = this.parser.parseFunction("=SUM(F5:F8)");
        assertEquals(new BigDecimal(26), result.getValue());

        result = this.parser.parseFunction("=SUM(C5:F5)");
        assertEquals(new BigDecimal(20), result.getValue());

        result = this.parser.parseFunction("=SUM(F5:C3)");
        assertEquals(new BigDecimal(48), result.getValue());
    }

    @Test
    public void shouldEvaluateDecimalCalculation() {
        this.dataProvider.setDataValue(0, 0, "3.4");
        this.dataProvider.setDataValue(1, 0, "4");

        FunctionValue result = this.parser.parseFunction("=PRODUCT(A1;B1)");
        assertEquals(new BigDecimal("13.6"), result.getValue());

        this.parser.setDecimalFormat((DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN));

        this.dataProvider.setDataValue(0, 0, "3,4");
        this.dataProvider.setDataValue(1, 0, "4");

        result = this.parser.parseFunction("=PRODUCT(A1;B1)");
        assertEquals(new BigDecimal("13.6"), result.getValue());
    }

    @Test
    public void shouldEvaluateInteger() {
        assertTrue(this.parser.isIntegerValue(new BigDecimal("0")));
        assertTrue(this.parser.isIntegerValue(new BigDecimal("5")));
        assertTrue(this.parser.isIntegerValue(new BigDecimal("10")));
        assertTrue(this.parser.isIntegerValue(new BigDecimal("240")));
        assertTrue(this.parser.isIntegerValue(new BigDecimal("3.0")));

        assertFalse(this.parser.isIntegerValue(new BigDecimal("2.4")));
        assertFalse(this.parser.isIntegerValue(new BigDecimal("12.34")));
        assertFalse(this.parser.isIntegerValue(new BigDecimal("12.01")));
    }

    @Test(expected = FunctionException.class)
    public void shouldThrowExceptionOnUnsupportedTypes() {
        this.dataProvider.setDataValue(0, 0, "3");
        this.dataProvider.setDataValue(1, 0, "a");

        this.parser.parseFunction("=PRODUCT(A1;B1)");
    }

    @Test(expected = FunctionException.class)
    public void shouldThrowExceptionOnUnsupportedTypesInRange() {
        this.dataProvider.setDataValue(0, 0, "3");
        this.dataProvider.setDataValue(1, 0, "a");

        this.parser.parseFunction("=PRODUCT(A1:B1)");
    }

    @Test(expected = FunctionException.class)
    public void shouldThrowExceptionOnNegativeSqrt() {
        this.parser.parseFunction("=SQRT(-9)");
    }

    @Test
    public void shouldEvaluateSqrt() {
        this.dataProvider.setDataValue(0, 1, "34.81");

        FunctionValue result = this.parser.parseFunction("=SQRT(36)");
        assertEquals(new BigDecimal("6.0"), result.getValue());

        result = this.parser.parseFunction("=SQRT(A2)");
        assertTrue(result.getValue().toString().startsWith("5.9"));

        result = this.parser.parseFunction("=SQRT(POWER(2.5;2))");
        assertEquals(new BigDecimal("2.5"), result.getValue());
    }

    @Test(expected = FunctionException.class)
    public void shouldThrowExceptionOnWrongArgumentsMod() {
        this.parser.parseFunction("=MOD(9)").getValue();
    }

    @Test
    public void shouldEvaluateMod() {
        this.dataProvider.setDataValue(0, 2, "6");
        this.dataProvider.setDataValue(1, 2, "2.5");

        FunctionValue result = this.parser.parseFunction("=MOD(6;4)");
        assertEquals(new BigDecimal("2"), result.getValue());

        result = this.parser.parseFunction("=MOD(6;3)");
        assertEquals(new BigDecimal("0"), result.getValue());

        result = this.parser.parseFunction("=MOD(A3;B3)");
        assertEquals(new BigDecimal("1.0"), result.getValue());
    }

    @Test
    public void shouldUpdateReferences() {
        assertEquals("=PRODUCT(C1:D1)", this.parser.updateReferences("=PRODUCT(A1:B1)", 2, 0, 4, 0));
        assertEquals("=PRODUCT(SUM(E4;E5):D1)", this.parser.updateReferences("=PRODUCT(SUM(C4;C5):B1)", 2, 0, 4, 0));
        assertEquals("=SUM(G7;SUM(E6;E7):D3)", this.parser.updateReferences("=SUM(E5;SUM(C4;C5):B1)", 2, 0, 4, 2));
        assertEquals("=PRODUCT(C3:C3)", this.parser.updateReferences("=PRODUCT(A1:A1)", 2, 0, 4, 2));

        assertEquals("=PRODUCT(A1:B1)", this.parser.updateReferences("=PRODUCT(C1:D1)", 4, 0, 2, 0));
        assertEquals("=PRODUCT(A1:B1)", this.parser.updateReferences("=PRODUCT(C3:D3)", 4, 2, 2, 0));
    }

    @Test
    public void shouldEvaluateEmptyReferences() {
        this.dataProvider.setDataValue(1, 1, "=4");

        // both references are not set, result should be 0
        assertEquals(new BigDecimal("0"), this.parser.parseFunction("=A1*B1").getValue());
        assertEquals(new BigDecimal("0"), this.parser.parseFunction("=C1+D1").getValue());
        // only one reference is set, result should be the value of that cell
        assertEquals(new BigDecimal("4"), this.parser.parseFunction("=A2*B2").getValue());
    }

    @Test(expected = FunctionException.class)
    public void shouldThrowExceptionOnSelfReference() {
        this.dataProvider.setDataValue(0, 0, "=A1");
        this.parser.parseFunction("=A1");
    }

    @Test(expected = FunctionException.class)
    public void shouldNoticeSimpleCycleReference() {
        this.dataProvider.setDataValue(0, 0, "=B1");
        this.dataProvider.setDataValue(1, 0, "=A1");

        this.parser.parseFunction("=A1");
    }

    @Test(expected = FunctionException.class)
    public void shouldNoticeCycleReference() {
        this.dataProvider.setDataValue(0, 0, "=B1");
        this.dataProvider.setDataValue(1, 0, "=C1");
        this.dataProvider.setDataValue(2, 0, "=A1");

        this.parser.parseFunction("=A1");
    }

    @Test
    public void shouldEvaluateMultiReferences() {
        this.dataProvider.setDataValue(0, 0, "5");
        this.dataProvider.setDataValue(1, 0, "3");
        this.dataProvider.setDataValue(3, 0, "=A1+B1");

        assertEquals(new BigDecimal("0.625"), this.parser.parseFunction("=A1/D1").getValue());
    }

    @Test(expected = FunctionException.class)
    public void shouldNoticeCycleReferenceWithRange() {
        this.dataProvider.setDataValue(0, 0, "5");
        this.dataProvider.setDataValue(0, 1, "5");
        this.dataProvider.setDataValue(0, 2, "=SUM(A1:A2)");
        this.dataProvider.setDataValue(1, 0, "3");
        this.dataProvider.setDataValue(1, 1, "3");
        this.dataProvider.setDataValue(1, 2, "=SUM(B1:B2)");

        // this should fail for circular dependencies
        this.dataProvider.setDataValue(2, 2, "=SUM(A3:C3)");

        this.parser.parseFunction("=C3");
    }

}
