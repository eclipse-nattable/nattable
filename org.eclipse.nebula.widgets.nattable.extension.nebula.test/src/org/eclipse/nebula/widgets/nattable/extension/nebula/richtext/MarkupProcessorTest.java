/*******************************************************************************
 * Copyright (c) 2023 Original authors and others.
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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MarkupProcessorTest {

	ConfigRegistry configRegistry = new ConfigRegistry();
    MarkupDisplayConverter converter = new MarkupDisplayConverter();
    
    @Test
    public void shouldNotMatch() {
    	converter.registerRegexMarkup("(ZZ)", "<b>", "</b>");
    	
    	runTest(converter, "abcfdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    }

    @Test
    public void shouldMatch() {
    	// matching
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("^(.*)$", "<b>", "</b>");
    	
    	runTest(converter, "abcdef", "<b>abcdef</b>");
    	runTest(converter, "a|b", "<b>a|b</b>");
    	runTest(converter, "a < b", "<b>a < b</b>", "<b>a &lt; b</b>");
    }

    @Test
    public void shouldNotMatchWithTwoMarkup() {
    	// two markup non matching
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("(ZZ)", "<b>", "</b>");
    	converter.registerRegexMarkup("(span)", "<span>", "</<span>");
    	
    	runTest(converter, "abcdef");
    	runTest(converter, "a|b");
    	runTest(converter, "a < b");
    }

    @Test
    public void shouldMatchFirstWithTwoMarkup() {
    	// two markup first matching
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("^(.*)$", "<b>", "</b>");
    	converter.registerRegexMarkup("(span)", "<span>", "</<span>");
    	
    	runTest(converter, "abcdef", "<b>abcdef</b>");
    	runTest(converter, "a|b", "<b>a|b</b>");
    	runTest(converter, "a < b", "<b>a < b</b>", "<b>a &lt; b</b>");
    }

    @Test
    public void shouldMatchSecondWithTwoMarkup() {
    	// two markup second matching
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("(ZZ)", "<b>", "</b>");
    	converter.registerRegexMarkup("^(.*)$", "<span>", "</span>");
    	
    	runTest(converter, "abcdef", "<span>abcdef</span>");
    	runTest(converter, "a|b", "<span>a|b</span>");
    	runTest(converter, "a < b", "<span>a < b</span>", "<span>a &lt; b</span>");
    }

    @Test
    public void shouldMatchBothWithTwoMarkup() {
    	// two markup both matching
    	converter = new MarkupDisplayConverter();
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
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("(" + Pattern.quote("|") + ")", "<b>", "</b>");
    	converter.registerRegexMarkup("(" + Pattern.quote("<") + ")", "<span>", "</span>");
    	
    	runTest(converter, "abcdef", "abcdef");
    	runTest(converter, "a|b", "a<b>|</b>b", "a<b>|</b>b");
    	runTest(converter, "a < b", "a <span><</span> b", "a <span>&lt;</span> b");
    }

    @Test
    public void shouldMatchHtmlFullTextWithTwoMarkup() {
    	// markup html full text matching
    	converter = new MarkupDisplayConverter();
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
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("^(.*)$", "<span>", "</span>");
    	converter.registerRegexMarkup("(-)", "<b>", "</b>");
    	runTest(converter, "x-02", "<span>x<b>-</b>02</span>", "<span>x<b>-</b>02</span>");
    }

    @Test
    @Disabled
    public void shouldFirstMatchesSecond() {
    	// second matches first fail
    	// because the elements between the tags are parsed separately
    	// -> result=<span>x</span><b><span>-</span></b><span>02</span>
    	converter = new MarkupDisplayConverter();
    	converter.registerRegexMarkup("(-)", "<b>", "</b>");
    	converter.registerRegexMarkup("^(.*)$", "<span>", "</span>");
    	runTest(converter, "x-02", "<span>x<b>-</b>02</span>", "<span>x<b>-</b>02</span>");
    }

    private void runTest(MarkupDisplayConverter converter, String toTest) {
        runTest(converter, toTest, StringEscapeUtils.escapeHtml4(toTest));
    }

    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded) {
        runTest(converter, toTest, expectedUnencoded, expectedUnencoded);
    }

    private void runTest(MarkupDisplayConverter converter, String toTest, String expectedUnencoded, String expectedWithEncoding) {
        String result = (String) converter.canonicalToDisplayValue(
                new LayerCell(null, 0, 0),
                this.configRegistry,
                toTest);

        assertEquals(expectedWithEncoding, result);
    }
}
