/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;

public class FilterRowUtilsTest {

    private static final String NULL_DELIMITER = null;
    private static final String COMMA_DELIMITER = ",";
    private static final String AND_OR_DELIMITER = "[&\\|]";

    @Test
    public void parseWithoutThresholdSymbols() throws Exception {
        ParseResult result = FilterRowUtils.parse(
                "100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.NONE, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    @Disabled
    // this one causes issues when trying to add regular expressions in the
    // filter itself
    // e.g. spaces in the custom regular expression will lead to a wrong parsed
    // valueToMatch
    // -> see the parseMultipleStringsWithSpaces() test below
    public void parseWithInvalidSymbols() {
        ParseResult result = FilterRowUtils.parse(
                "# 100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.NONE, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseGreaterThanSymbol() {
        ParseResult result = FilterRowUtils.parse(
                " > 100 ",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.GREATER_THAN, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseGreaterThanSymbolWithoutSpace() {
        ParseResult result = FilterRowUtils.parse(
                ">100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.GREATER_THAN, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseLessThanSymbol() {
        ParseResult result = FilterRowUtils.parse(
                "< 100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.LESS_THAN, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseLessThanSymbolWithoutSpace() {
        ParseResult result = FilterRowUtils.parse(
                "<100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.LESS_THAN, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseEqualSymbol() {
        ParseResult result = FilterRowUtils.parse(
                "=100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseEqualSymbolWithSpace() {
        ParseResult result = FilterRowUtils.parse(
                "= 100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseNotEqualSymbol() {
        ParseResult result = FilterRowUtils.parse(
                "<>100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.NOT_EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseNotEqualSymbolWithSpace() {
        ParseResult result = FilterRowUtils.parse(
                " <> 100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.NOT_EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseGreaterThanOrEqualSymbol() {
        ParseResult result = FilterRowUtils.parse(
                ">= 100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.GREATER_THAN_OR_EQUAL,
                result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseGreaterThanOrEqualSymbolWithSpace() {
        ParseResult result = FilterRowUtils.parse(
                " >=  100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.GREATER_THAN_OR_EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseLessThanOrEqualSymbol() {
        ParseResult result = FilterRowUtils.parse(
                "<=100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.LESS_THAN_OR_EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    public void parseLessThanOrEqualSymbolWithSpace() {
        ParseResult result = FilterRowUtils.parse(
                "<= 100",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION)
                .get(0);

        assertEquals(MatchType.LESS_THAN_OR_EQUAL, result.getMatchOperation());
        assertEquals("100", result.getValueToMatch());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void shouldMapBetweenNatTableAndGlazedLists() {
        ThresholdMatcherEditor fixture = new ThresholdMatcherEditor();

        FilterRowUtils.setMatchOperation(fixture, MatchType.EQUAL);
        assertEquals(ThresholdMatcherEditor.EQUAL, fixture.getMatchOperation());

        FilterRowUtils.setMatchOperation(fixture, MatchType.NOT_EQUAL);
        assertEquals(ThresholdMatcherEditor.NOT_EQUAL, fixture.getMatchOperation());

        FilterRowUtils.setMatchOperation(fixture, MatchType.GREATER_THAN);
        assertEquals(ThresholdMatcherEditor.GREATER_THAN, fixture.getMatchOperation());

        FilterRowUtils.setMatchOperation(fixture, MatchType.GREATER_THAN_OR_EQUAL);
        assertEquals(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL, fixture.getMatchOperation());

        FilterRowUtils.setMatchOperation(fixture, MatchType.LESS_THAN);
        assertEquals(ThresholdMatcherEditor.LESS_THAN, fixture.getMatchOperation());

        FilterRowUtils.setMatchOperation(fixture, MatchType.LESS_THAN_OR_EQUAL);
        assertEquals(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL, fixture.getMatchOperation());
    }

    @Test
    public void parseMultiple() {
        List<ParseResult> results = FilterRowUtils.parse(
                "100, <=200",
                COMMA_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(2, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("100", results.get(0).getValueToMatch());

        assertEquals(MatchType.LESS_THAN_OR_EQUAL, results.get(1).getMatchOperation());
        assertEquals("200", results.get(1).getValueToMatch());
    }

    @Test
    public void parseMultipleStrings() {
        List<ParseResult> results = FilterRowUtils.parse(
                "(Bart|Lisa)",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(1, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("(Bart|Lisa)", results.get(0).getValueToMatch());
    }

    @Test
    public void parseMultipleStringsWithDelimiter() {
        List<ParseResult> results = FilterRowUtils.parse(
                "Bart|Lisa",
                "\\|",
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(2, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("Bart", results.get(0).getValueToMatch());
        assertEquals(MatchType.NONE, results.get(1).getMatchOperation());
        assertEquals("Lisa", results.get(1).getValueToMatch());
    }

    @Test
    public void parseMultipleStringsWithDelimiterRegex() {
        List<ParseResult> results = FilterRowUtils.parse(
                "Bart|Lisa&Maggie",
                "[\\|&]",
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(3, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("Bart", results.get(0).getValueToMatch());
        assertEquals(MatchType.NONE, results.get(1).getMatchOperation());
        assertEquals("Lisa", results.get(1).getValueToMatch());
        assertEquals(MatchType.NONE, results.get(2).getMatchOperation());
        assertEquals("Maggie", results.get(2).getValueToMatch());
    }

    @Test
    public void parseMultipleExpressionsWithDelimiterRegexAnd() {
        List<ParseResult> results = FilterRowUtils.parse(
                ">= 100 & <= 200",
                AND_OR_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(2, results.size());

        assertEquals(MatchType.GREATER_THAN_OR_EQUAL, results.get(0).getMatchOperation());
        assertEquals("100", results.get(0).getValueToMatch());
        assertEquals(MatchType.LESS_THAN_OR_EQUAL, results.get(1).getMatchOperation());
        assertEquals("200", results.get(1).getValueToMatch());
    }

    @Test
    public void parseMultipleExpressionsWithDelimiterRegexOr() {
        List<ParseResult> results = FilterRowUtils.parse(
                "< 100 | > 200",
                AND_OR_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(2, results.size());

        assertEquals(MatchType.LESS_THAN, results.get(0).getMatchOperation());
        assertEquals("100", results.get(0).getValueToMatch());
        assertEquals(MatchType.GREATER_THAN, results.get(1).getMatchOperation());
        assertEquals("200", results.get(1).getValueToMatch());
    }

    @Test
    public void parseMultipleStringsWithSpaces() {
        List<ParseResult> results = FilterRowUtils.parse(
                "(Bart Simpson|Lisa Simpson)",
                NULL_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(1, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("(Bart Simpson|Lisa Simpson)", results.get(0).getValueToMatch());
    }

    @Test
    public void parseMultipleStringsWithSpacesWithDelimiter() {
        List<ParseResult> results = FilterRowUtils.parse(
                "Bart Simpson|Lisa Simpson",
                AND_OR_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(2, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("Bart Simpson", results.get(0).getValueToMatch());
        assertEquals(MatchType.NONE, results.get(1).getMatchOperation());
        assertEquals("Lisa Simpson", results.get(1).getValueToMatch());
    }

    @Test
    public void parseMultipleStringsWithSpacesWithDelimiterNoSplit() {
        List<ParseResult> results = FilterRowUtils.parse(
                "(Bart Simpson|Lisa Simpson)",
                AND_OR_DELIMITER,
                TextMatchingMode.REGULAR_EXPRESSION);

        assertEquals(1, results.size());

        assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
        assertEquals("(Bart Simpson|Lisa Simpson)", results.get(0).getValueToMatch());
    }

    @Test
    public void shouldMatch() {
        assertNull(FilterRowUtils.getSeparatorCharacters(";"), "Single character matches");
        assertNull(FilterRowUtils.getSeparatorCharacters("[&;"), "Missing end bracket matches");
        assertNull(FilterRowUtils.getSeparatorCharacters("&;]"), "Missing start bracket matches");
        assertNull(FilterRowUtils.getSeparatorCharacters("&;"), "Missing brackets matches");
        assertNull(FilterRowUtils.getSeparatorCharacters("&;\\|"), "More than 2 characters matches");
        assertNull(FilterRowUtils.getSeparatorCharacters(null), "Null matches");
        assertNull(FilterRowUtils.getSeparatorCharacters(""), "Empty matches");
        assertNull(FilterRowUtils.getSeparatorCharacters("NatTable"), "Text matches");

        String[] delimiterChars = FilterRowUtils.getSeparatorCharacters("[&;]");
        assertNotNull(delimiterChars, "Two characters doesn't match");
        assertEquals("&", delimiterChars[0]);
        assertEquals(";", delimiterChars[1]);

        delimiterChars = FilterRowUtils.getSeparatorCharacters(AND_OR_DELIMITER);
        assertNotNull(delimiterChars, "Second character masked doesn't match");
        assertEquals("&", delimiterChars[0]);
        assertEquals("|", delimiterChars[1]);

        delimiterChars = FilterRowUtils.getSeparatorCharacters("[\\|&]");
        assertNotNull(delimiterChars, "First character masked doesn't match");
        assertEquals("|", delimiterChars[0]);
        assertEquals("&", delimiterChars[1]);

        delimiterChars = FilterRowUtils.getSeparatorCharacters("[\\|\\\\]");
        assertNotNull(delimiterChars, "Both characters masked don't match");
        assertEquals("|", delimiterChars[0]);
        assertEquals("\\", delimiterChars[1]);

    }
}
