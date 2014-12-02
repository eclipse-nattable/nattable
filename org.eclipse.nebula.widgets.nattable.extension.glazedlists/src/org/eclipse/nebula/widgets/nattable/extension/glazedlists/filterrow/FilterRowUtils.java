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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;

import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;

public class FilterRowUtils {

    public static List<ParseResult> parse(String string, String textDelimiter,
            TextMatchingMode textMatchingMode) {
        List<ParseResult> parseResults = new ArrayList<ParseResult>();

        if (textDelimiter != null) {
            StringTokenizer tok = new StringTokenizer(string, textDelimiter);
            while (tok.hasMoreTokens()) {
                parse(tok.nextToken(), textMatchingMode, parseResults);
            }
        } else {
            parse(string, textMatchingMode, parseResults);
        }

        return parseResults;
    }

    private static void parse(String string, TextMatchingMode textMatchingMode,
            List<ParseResult> parseResults) {
        ParseResult parseResult;

        switch (textMatchingMode) {
            case REGULAR_EXPRESSION:
                parseResult = parseExpression(string);
                break;
            default:
                parseResult = parseLiteral(string);
        }

        if (parseResult != null) {
            parseResults.add(parseResult);
        }
    }

    /**
     * Parses the text entered in the filter row. The text is parsed to figure
     * out the type of match operation (&lt;, &gt; etc.) and the value next to
     * it.
     *
     * @param string
     *            entered by the user in the filter row text box
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
            parseResult.setValueToMatch(string);
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
     */
    public static <T> void setMatchOperation(
            ThresholdMatcherEditor<T, Object> thresholdMatcherEditor,
            MatchType matchType) {
        switch (matchType) {
            case GREATER_THAN:
                thresholdMatcherEditor
                        .setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
                break;
            case GREATER_THAN_OR_EQUAL:
                thresholdMatcherEditor
                        .setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
                break;
            case LESS_THAN:
                thresholdMatcherEditor
                        .setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
                break;
            case LESS_THAN_OR_EQUAL:
                thresholdMatcherEditor
                        .setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
                break;
            case NOT_EQUAL:
                thresholdMatcherEditor
                        .setMatchOperation(ThresholdMatcherEditor.NOT_EQUAL);
                break;
            default:
                thresholdMatcherEditor
                        .setMatchOperation(ThresholdMatcherEditor.EQUAL);
        }
    }

}
