/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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
	public void parseWithInvalidSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse("# 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.NONE, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse(" > 100 ", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanSymbolsWithoutSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse(">100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseLessThanSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse("<100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.LESSER, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseEqualsSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse("=100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.EQUALS, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanEqualsSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse(">= 100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER_THAN_EQUALS, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseGreaterThanEqualsSymbolsWithSpace() throws Exception {
		ParseResult result = FilterRowUtils.parse(" >=  100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.GREATER_THAN_EQUALS, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	public void parseLessThanEqualsSymbols() throws Exception {
		ParseResult result = FilterRowUtils.parse("<=100", NULL_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION).get(0);

		assertEquals(MatchType.LESS_THAN_EQUALS, result.getMatchOperation());
		assertEquals("100", result.getValueToMatch());
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void shouldMapBetweenNatTableAndGlazedLists() throws Exception {
		ThresholdMatcherEditor fixture = new ThresholdMatcherEditor();

		FilterRowUtils.setMatchOperation(fixture, MatchType.EQUALS);
		assertEquals(ThresholdMatcherEditor.EQUAL, fixture.getMatchOperation());

		FilterRowUtils.setMatchOperation(fixture, MatchType.GREATER);
		assertEquals(ThresholdMatcherEditor.GREATER_THAN, fixture.getMatchOperation());

		FilterRowUtils.setMatchOperation(fixture, MatchType.GREATER_THAN_EQUALS);
		assertEquals(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL, fixture.getMatchOperation());

		FilterRowUtils.setMatchOperation(fixture, MatchType.LESSER);
		assertEquals(ThresholdMatcherEditor.LESS_THAN, fixture.getMatchOperation());

		FilterRowUtils.setMatchOperation(fixture, MatchType.LESS_THAN_EQUALS);
		assertEquals(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL, fixture.getMatchOperation());
	}
	
	@Test
	public void parseMultiple() {
		List<ParseResult> results = FilterRowUtils.parse("100, <=200", COMMA_DELIMITER, TextMatchingMode.REGULAR_EXPRESSION);

		assertEquals(2, results.size());
		
		assertEquals(MatchType.NONE, results.get(0).getMatchOperation());
		assertEquals("100", results.get(0).getValueToMatch());
		
		assertEquals(MatchType.LESS_THAN_EQUALS, results.get(1).getMatchOperation());
		assertEquals("200", results.get(1).getValueToMatch());
	}
}
