/*******************************************************************************
 * Copyright (c) 2023, 2024 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.junit.jupiter.api.Test;

public class MarkupProcessorTest {

	ConfigRegistry configRegistry = new ConfigRegistry();
    MarkupDisplayConverter converter = new MarkupDisplayConverter();
    
    @Test
    public void shouldNotMatch() {
    	converter.registerMarkup("blubb", "<b>", "</b>");
    	
    	runTest(converter, "abcfdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    }
    
    @Test
    public void shouldNotMatchRegex() {
    	converter.registerRegexMarkup("(ZZ)", "<b>", "</b>");
    	
    	runTest(converter, "abcfdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    }

    @Test
    public void shouldMatch() {
    	// matching
    	converter.registerMarkup("blubb", "<b>", "</b>");
    	
    	runTest(converter, "abcblubbdef", "abc<b>blubb</b>def");

    	converter.registerMarkup("blubb", "UI", "UI");
    	runTest(converter, "abcblubbdef", "abcUIblubbUIdef");
    	
    	// test without a correct regex group
    	converter.clearMarkups();
    	converter.registerRegexMarkup("(Simpson)", "<em>", "</em>");
    	runTest(converter, "Simpson", "<em>Simpson</em>");

    	// test with a correct regex group
    	converter.clearMarkups();
    	converter.registerRegexMarkup("Simpson", "<em>", "</em>");
    	runTest(converter, "Simpson", "<em>Simpson</em>");

    	converter.clearMarkups();
    	converter.registerRegexMarkup("(imp)", "<em>", "</em>");
    	runTest(converter, "Simpson", "S<em>imp</em>son");
    	
    	converter.clearMarkups();
    	converter.registerRegexMarkup("imp", "<em>", "</em>");
    	runTest(converter, "Simpson", "S<em>imp</em>son");
    }
    
    @Test
    public void shouldMatchRegex() {
    	// matching
    	converter.registerRegexMarkup("^(.*)$", "<b>", "</b>");
    	
    	runTest(converter, "abcdef", "<b>abcdef</b>");
    	runTest(converter, "a|b", "<b>a|b</b>");
    	runTest(converter, "a < b", "<b>a < b</b>", "<b>a &lt; b</b>");
    }
    
    @Test
    public void shouldMatchRegexSpecialCharacter() {
    	// matching
    	converter.registerRegexMarkup("(&!0)", "<b>", "</b>");
    	
    	runTest(converter, "&!08", "<b>&amp;!0</b>8");
    }

    @Test
    public void shouldNotMatchWithTwoMarkup() {
    	// two markup non matching
    	converter.registerRegexMarkup("(ZZ)", "<b>", "</b>");
    	converter.registerRegexMarkup("(span)", "<span>", "</<span>");
    	
    	runTest(converter, "abcdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    }

    @Test
    public void shouldMatchFirstWithTwoMarkup() {
    	// two markup first matching
    	converter.registerRegexMarkup("^(.*)$", "<b>", "</b>");
    	converter.registerRegexMarkup("(span)", "<span>", "</<span>");
    	
    	runTest(converter, "abcdef", "<b>abcdef</b>");
    	runTest(converter, "a|b", "<b>a|b</b>");
    	runTest(converter, "a < b", "<b>a < b</b>", "<b>a &lt; b</b>");
    }

    @Test
    public void shouldMatchSecondWithTwoMarkup() {
    	// two markup second matching
    	converter.registerRegexMarkup("(ZZ)", "<b>", "</b>");
    	converter.registerRegexMarkup("^(.*)$", "<span>", "</span>");
    	
    	runTest(converter, "abcdef", "<span>abcdef</span>");
    	runTest(converter, "a|b", "<span>a|b</span>");
    	runTest(converter, "a < b", "<span>a < b</span>", "<span>a &lt; b</span>");
    }

    @Test
    public void shouldMatchBothWithTwoMarkup() {
    	// two markup both matching
    	// order is important as the content is wrapped with the markup,
    	// so the outer tag needs to be registered first
    	converter.registerMarkup("1", new RegexMarkupValue("^(.*)$", "<span>", "</span>"));
    	converter.registerMarkup("2", new RegexMarkupValue("^(.*)$", "<b>", "</b>"));
    	
    	runTest(converter, "abcdef", "<span><b>abcdef</b></span>");
    	runTest(converter, "a|b", "<span><b>a|b</b></span>");
    	runTest(converter, "a < b", "<span><b>a < b</b></span>", "<span><b>a &lt; b</b></span>");
    }

    @Test
    public void shouldMatchWithTwoMarkup() {
    	// two markup regex matching
    	converter.registerRegexMarkup("(" + Pattern.quote("|") + ")", "<b>", "</b>");
    	converter.registerRegexMarkup("(" + Pattern.quote("<") + ")", "<span>", "</span>");
    	
    	runTest(converter, "abcdef", "abcdef");
    	runTest(converter, "a|b", "a<b>|</b>b", "a<b>|</b>b");
    	runTest(converter, "a < b", "a <span><</span> b", "a <span>&lt;</span> b");
    }

    @Test
    public void shouldMatchHtmlFullTextWithTwoMarkup() {
    	// markup html full text matching
    	converter.registerRegexMarkup("(" + Pattern.quote("<") + ")", "<span>", "</span>");
    	runTest(converter, "x<02", "x<span><</span>02", "x<span>&lt;</span>02");
    	
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("(" + Pattern.quote("x") + ")", "<span>", "</span>");
    	runTest(converter, "x<02", "<span>x</span><02", "<span>x</span>&lt;02");
    	
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("(" + Pattern.quote("x") + ")", "<span>", "</span>");
    	runTest(converter, "x<02", "<span>x</span><02", "<span>x</span>&lt;02");
    }

    @Test
    public void shouldSecondMatchesFirst() {
    	// second matches first success
    	converter.registerRegexMarkup("^(.*)$", "<span>", "</span>");
    	converter.registerRegexMarkup("(-)", "<b>", "</b>");
    	runTest(converter, "x-02", "<span>x<b>-</b>02</span>", "<span>x<b>-</b>02</span>");
    }

    @Test
    public void shouldFirstMatchesSecond() {
    	// second matches first fail
    	// because the elements between the tags are parsed separately
    	converter.registerRegexMarkup("(-)", "<b>", "</b>");
    	converter.registerRegexMarkup("^(.*)$", "<span>", "</span>");
    	runTest(converter, "x-02", "<span>x</span><b><span>-</span></b><span>02</span>", "<span>x</span><b><span>-</span></b><span>02</span>");
    }
    
    @Test
    public void shouldNotMatchForLabel() {
    	converter.registerMarkupForLabel("blubb", "<b>", "</b>", "TEST1");
    	
    	// test with no labels on the cell
    	runTest(converter, "abcblubbdef");
    	
    	// test with wrong labels on the cell
    	runTest(converter, "abcblubbdef", Arrays.asList("TEST2"));
    }
    
    @Test
    public void shouldMatchForLabel() {
    	converter.registerMarkupForLabel("blubb", "<b>", "</b>", "TEST1");
    	
    	// test with wrong labels on the cell
    	runTest(converter, "abcblubbdef", "abc<b>blubb</b>def", Arrays.asList("TEST1"));
    }
    
    @Test
    public void shouldNotMatchRegexForLabel() {
    	converter.registerRegexMarkupForLabel("(ZZ)", "<b>", "</b>", "TEST1");
    	
    	// test with no labels on the cell
    	runTest(converter, "abcfdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    	
    	// test with wrong labels on the cell
    	runTest(converter, "abcfdef", Arrays.asList("TEST2"));
    	runTest(converter, "a|b", Arrays.asList("TEST2"));
    	runTest(converter, "a < b", Arrays.asList("TEST2"));
    }
    
    @Test
    public void shouldNotMatchRegexForMultiLabel() {
    	converter.registerRegexMarkupForLabel("(ZZ)", "<b>", "</b>", "BOLD", "ITALIC");
    	
    	// test with no labels on the cell
    	runTest(converter, "abcfdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    	
    	// test with wrong labels on the cell
    	runTest(converter, "abcfdef", Arrays.asList("BOLD"));
    	runTest(converter, "a|b", Arrays.asList("ITALIC"));
    	runTest(converter, "a < b", Arrays.asList("TEST1", "TEST2"));
    }

    @Test
    public void shouldMatchRegexForMultiLabel() {
    	// matching
    	converter.registerRegexMarkupForLabel("^(.*)$", "<b>", "</b>", "BOLD", "ITALIC");
    	
    	runTest(converter, "abcdef", "<b>abcdef</b>", Arrays.asList("BOLD", "ITALIC"));
    	runTest(converter, "a|b", "<b>a|b</b>", Arrays.asList("BOLD", "ITALIC"));
    	runTest(converter, "a < b", "<b>a < b</b>", "<b>a &lt; b</b>", Arrays.asList("ITALIC", "BOLD"));
    }
    
    @Test
    public void shouldMatchRegexForDifferentMultiLabel() {
    	// matching
    	converter.registerRegexMarkupForLabel("^(.*)$", "<strong>", "</strong>", "BOLD");
    	converter.registerRegexMarkupForLabel("^(.*)$", "<em>", "</em>", "ITALIC");
    	converter.registerRegexMarkupForLabel("^(.*)$", "<u>", "</u>", "BOLD", "ITALIC");
    	
    	runTest(converter, "abcdef", "<strong>abcdef</strong>", Arrays.asList("BOLD"));
    	runTest(converter, "abcdef", "<em>abcdef</em>", Arrays.asList("ITALIC"));
    	// apply in the order registered
    	runTest(converter, "abcdef", "<strong><em><u>abcdef</u></em></strong>", Arrays.asList("BOLD", "ITALIC"));
    	// apply in the order registered, test label order not relevant
    	runTest(converter, "abcdef", "<strong><em><u>abcdef</u></em></strong>", Arrays.asList("ITALIC", "BOLD"));
    }
    
    @Test
    public void shouldMatchRegexGeneralAndLabel() {
    	// matching
    	converter.registerRegexMarkup("^(.*)$", "<strong>", "</strong>");
    	converter.registerRegexMarkupForLabel("^(.*)$", "<u>", "</u>", "IMPORTANT");
    	
    	runTest(converter, "abcdef", "<strong>abcdef</strong>");
    	runTest(converter, "abcdef", "<strong>abcdef</strong>", Arrays.asList("ITALIC"));
    	runTest(converter, "abcdef", "<strong><u>abcdef</u></strong>", Arrays.asList("IMPORTANT"));
    	
    	converter.clearMarkups();

    	runTest(converter, "abcdef");
    	runTest(converter, "abcdef", Arrays.asList("ITALIC"));
    	runTest(converter, "abcdef", Arrays.asList("IMPORTANT"));
    }

    @Test
    public void shouldMatchStringCaseSensitive() {
    	RegexMarkupValue markup = new RegexMarkupValue("u", "<b>", "</b>");
    	markup.setCaseInsensitive(false);
    	converter.registerMarkup("u", markup);
    	
    	runTest(converter, "Unum&", "Un<b>u</b>m&", "Un<b>u</b>m&amp;");
    }
    
    @Test
    public void shouldMatchStringWithHtmlEntities() {
    	converter.registerRegexMarkup("u", "<b>", "</b>");
    	
    	runTest(converter, "Onum&", "On<b>u</b>m&", "On<b>u</b>m&amp;");
    	
    	converter.unregisterMarkup("u");
    	runTest(converter, "Onum&");
    	
    	converter.registerRegexMarkup("m", "<b>", "</b>");
    	runTest(converter, "Onum&", "Onu<b>m</b>&", "Onu<b>m</b>&amp;");
    	runTest(converter, "Onum&Onum", "Onu<b>m</b>&Onu<b>m</b>", "Onu<b>m</b>&amp;Onu<b>m</b>");
    }
    
    @Test
    public void shouldMatchStringWithMultipleHtmlEntities() {
    	converter.registerRegexMarkup("m", "<b>", "</b>");
    	runTest(converter, "Onüm&Onüm", "Onü<b>m</b>&Onü<b>m</b>", "On&uuml;<b>m</b>&amp;On&uuml;<b>m</b>");
    }

    private void runTest(MarkupDisplayConverter converter, String toTest) {
        runTest(converter, toTest, StringEscapeUtils.escapeHtml4(toTest));
    }
    
    private void runTest(MarkupDisplayConverter converter, String toTest, List<String> labels) {
    	runTest(converter, toTest, StringEscapeUtils.escapeHtml4(toTest), labels);
    }

    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded) {
        runTest(converter, toTest, expectedUnencoded, expectedUnencoded);
    }
    
    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded, List<String> labels) {
    	runTest(converter, toTest, expectedUnencoded, expectedUnencoded, labels);
    }

    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded, String expectedWithEncoding) {
    	runTest(converter, toTest, expectedUnencoded, expectedWithEncoding, new String[] {});
    }
    
    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded, String expectedWithEncoding, String... labels) {
    	runTest(converter, toTest, expectedUnencoded, expectedWithEncoding, Arrays.asList(labels));
    }
    
    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded, String expectedWithEncoding, List<String> labels) {
    	String displayValue = (String) converter.canonicalToDisplayValue(
    			new LayerCell(null, 0, 0) {
    				@Override
    				public LabelStack getConfigLabels() {
    					return new LabelStack(labels);
    				}
    			},
    			this.configRegistry,
    			toTest);
    	
    	assertEquals(expectedWithEncoding, displayValue);
    	
    	// test conversion back
    	String canonicalValue = (String) converter.displayToCanonicalValue(
    			new LayerCell(null, 0, 0) {
					@Override
					public LabelStack getConfigLabels() {
						return new LabelStack(labels);
					}
				}, 
    			this.configRegistry, 
    			expectedWithEncoding);
    	
    	assertEquals(toTest, canonicalValue);
    }
}
