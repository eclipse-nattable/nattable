/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.junit.Ignore;
import org.junit.Test;

import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;

public class FilterRowUtilsTest {

	private static final String NULL_DELIMITER = null;
	private static final String COMMA_DELIMITER = ",";

	@Test
	public void parseWithoutThresholdSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse("100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.NONE, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	@Ignore 
	//this one causes issues when trying to add regular expressions in the filter itself
	//e.g. spaces in the custom regular expression will lead to a wrong parsed valueToMatch
	//-> see the parseMultipleStringsWithSpaces() test below 
	public void parseWithInvalidSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse("# 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.NONE, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanSymbol() throws Exception {
		ParseResult result = FilterRowUtils.parse(" > 100 ", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER_THAN, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanSymbolWithoutSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse(">100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER_THAN, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseLessThanSymbol() throws Exception {
		ParseResult result = FilterRowUtils.parse("< 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.LESS_THAN, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseLessThanSymbolWithoutSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse("<100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.LESS_THAN, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseEqualSymbol() throws Exception {
		ParseResult result = FilterRowUtils.parse("=100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseEqualSymbolWithSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse("= 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseNotEqualSymbol() throws Exception {
		ParseResult result = FilterRowUtils.parse("<>100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.NOT_EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseNotEqualSymbolWithSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse(" <> 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.NOT_EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanOrEqualSymbol() throws Exception {
		ParseResult result = FilterRowUtils.parse(">= 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER_THAN_OR_EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanOrEqualSymbolWithSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse(" >=  100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER_THAN_OR_EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseLessThanOrEqualSymbol() throws Exception {
		ParseResult result = FilterRowUtils.parse("<=100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.LESS_THAN_OR_EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseLessThanOrEqualSymbolWithSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse("<= 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.LESS_THAN_OR_EQUAL, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldMapBetweenNatTableAndGlazedLists() throws Exception {
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
		List<ParseResult> results = FilterRowUtils.parse("100, <=200", COMMA_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION);

		assertEquals(2, results.size());
		
		assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
		assertEquals("100", results.get(0).getValueToMatch());
		
		assertEquals(MatchType.LESS_THAN_OR_EQUAL, results.get(1).getMatchOperation());
		assertEquals("200", results.get(1).getValueToMatch());
	}
	
	@Test
	public void parseMultipleStrings() {
		List<ParseResult> results = FilterRowUtils.parse("(Bart|Lisa)", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION);
		
		assertEquals(1, results.size());
		
		assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
		assertEquals("(Bart|Lisa)", results.get(0).getValueToMatch());
	}
	
	@Test
	public void parseMultipleStringsWithSpaces() {
		List<ParseResult> results = FilterRowUtils.parse("(Bart Simpson|Lisa Simpson)", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION);
		
		assertEquals(1, results.size());
		
		assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
		assertEquals("(Bart Simpson|Lisa Simpson)", results.get(0).getValueToMatch());
	}
	
}
