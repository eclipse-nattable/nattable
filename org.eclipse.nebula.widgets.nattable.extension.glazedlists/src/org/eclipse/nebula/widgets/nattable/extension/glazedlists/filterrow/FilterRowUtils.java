/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;

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

    private static final String TWO_CHARACTER_REGEX = "\\[(((.){2})|((.)\\\\(.))|(\\\\(.){2})|(\\\\(.)\\\\(.)))\\]"; //$NON-NLS-1$
    private static final String MASKED_BACKSLASH = "\\\\"; //$NON-NLS-1$
    private static final String BACKSLASH_REPLACEMENT = "backslash"; //$NON-NLS-1$

    /**
     * This method tries to extract the AND and the OR character that should be
     * used as delimiter, so that a user is able to specify the operation for
     * combined filter criteria. If it does not start with [ and ends with ] and
     * does not match one of the following regular expressions, this method
     * returns <code>null</code> which causes the default behavior, e.g. OR for
     * String matchers, AND for threshold matchers.
     * <ul>
     * <li>(.){2}</li>
     * <li>(.)\\\\(.)</li>
     * <li>\\\\(.){2}</li>
     * <li>\\\\(.)\\\\(.)</li>
     * </ul>
     *
     * @param delimiter
     *            The delimiter that is configured via
     *            {@link FilterRowConfigAttributes#TEXT_DELIMITER}. Can be
     *            <code>null</code>.
     * @return String array with the configured AND and the configured OR
     *         character, or <code>null</code> if the delimiter is not a two
     *         character regular expression. The first element in the array is
     *         the AND character, the second element is the OR character.
     * @since 2.1
     */
    public static String[] getSeparatorCharacters(String delimiter) {
        // start with [ and end with ]
        // (.){2} => e.g. ab
        // (.)\\\\(.) => a\b
        // \\\\(.){2} => \ab
        // \\\\(.)\\\\(.) => \a\b

        if (delimiter != null && delimiter.matches(TWO_CHARACTER_REGEX)) {
            String inspect = delimiter.substring(1, delimiter.length() - 1);

            // special handling if the backslash is used as delimiter for AND or
            // OR
            inspect = inspect.replace(MASKED_BACKSLASH, BACKSLASH_REPLACEMENT);

            // now replace all backslashed
            inspect = inspect.replaceAll(MASKED_BACKSLASH, ""); //$NON-NLS-1$

            // convert back the "backslash" to "\"
            inspect = inspect.replace(BACKSLASH_REPLACEMENT, "\\"); //$NON-NLS-1$
            if (inspect.length() == 2) {
                String[] result = new String[] { inspect.substring(0, 1), inspect.substring(1, 2) };
                return result;
            }
        }

        return null;
    }
}