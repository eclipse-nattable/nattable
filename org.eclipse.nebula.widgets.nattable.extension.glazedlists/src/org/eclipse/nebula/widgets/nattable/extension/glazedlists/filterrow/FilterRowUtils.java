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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;

import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;

public final class FilterRowUtils {

    private FilterRowUtils() {
        // private default constructor for helper class
    }

    public static List<ParseResult> parse(
            String string,
            String textDelimiter,
            TextMatchingMode textMatchingMode) {

        List<ParseResult> parseResults = new ArrayList<>();

        // avoid splitting if string is in brackets
        // needed to be able to use "|" as OR delimiter but still use it in
        // regular expressions, e.g. combobox filters with checkboxes
        if (textDelimiter != null && !(string.startsWith("(") && string.endsWith(")"))) { //$NON-NLS-1$ //$NON-NLS-2$
            String[] splitted = string.split(textDelimiter);
            for (String split : splitted) {
                parse(split, textMatchingMode, parseResults);
            }
        } else {
            parse(string, textMatchingMode, parseResults);
        }

        return parseResults;
    }

    private static void parse(
            String string,
            TextMatchingMode textMatchingMode,
            List<ParseResult> parseResults) {

        ParseResult parseResult;

        if (textMatchingMode == TextMatchingMode.REGULAR_EXPRESSION) {
            parseResult = parseExpression(string);
        } else {
            parseResult = parseLiteral(string);
        }

        parseResults.add(parseResult);
    }

    /**
     * Parses the text entered in the filter row. The text is parsed to figure
     * out the type of match operation (&lt;, &gt; etc.) and the value next to
     * it.
     *
     * @param string
     *            entered by the user in the filter row text box
     * @return the result of the parse operation
     */
    public static ParseResult parseExpression(String string) {
        Scanner scanner = new Scanner(string.trim());
        ParseResult parseResult = new ParseResult();

        Pattern p = Pattern.compile("<>|([>|<]?=?)"); //$NON-NLS-1$
        String opToken = scanner.findWithinHorizon(p, 2);
        if (isNotEmpty(opToken)) {
            parseResult.setMatchType(MatchType.parse(opToken));
            while (scanner.hasNext()) {
                parseResult.setValueToMatch(scanner.next());
            }
        } else {
            parseResult.setValueToMatch(string.trim());
        }
        scanner.close();
        return parseResult;
    }

    public static ParseResult parseLiteral(String string) {
        ParseResult parseResult = new ParseResult();
        parseResult.setMatchType(MatchType.NONE);
        parseResult.setValueToMatch(string);
        return parseResult;
    }

    /**
     * Set the Match operation on the {@link ThresholdMatcherEditor}
     * corresponding to the {@link MatchType}. This must be done this way since
     * ThresholdMatcherEditor.MatcherEditor is private.
     *
     * @param <T>
     *            type of the row object
     * @param thresholdMatcherEditor
     *            The {@link ThresholdMatcherEditor} on which the match
     *            operation should be applied.
     * @param matchType
     *            The match type to apply.
     */
    public static <T> void setMatchOperation(
            ThresholdMatcherEditor<T, Object> thresholdMatcherEditor,
            MatchType matchType) {
        switch (matchType) {
            case GREATER_THAN:
                thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
                break;
            case GREATER_THAN_OR_EQUAL:
                thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
                break;
            case LESS_THAN:
                thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
                break;
            case LESS_THAN_OR_EQUAL:
                thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
                break;
            case NOT_EQUAL:
                thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.NOT_EQUAL);
                break;
            default:
                thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        }
    }
}