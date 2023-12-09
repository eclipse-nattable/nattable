/*****************************************************************************
 * Copyright (c) 2016, 2023 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.nebula.widgets.richtext.RichTextPainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MarkupProcessor} implementation that is able to process regular
 * expression values to identify content that should be surrounded by a markup.
 * The regular expression needs to contain one group so the replacement can be
 * done by using a placeholder.
 *
 * @since 1.1
 */
public class RegexMarkupValue implements MarkupProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RegexMarkupValue.class);

    private static final String GROUP_INDEX_PLACEHOLDER = "$1";

    private String originalRegexValue;
    private String markupPrefix;
    private String markupSuffix;

    private String markupValue;
    private String markupRegexValue;

    private XMLInputFactory factory = XMLInputFactory.newInstance();

    private boolean caseInsensitive = true;
    private boolean unicodeCase = false;

    /**
     *
     * @param value
     *            The regular expression that specifies the value that should be
     *            surrounded by a markup.
     * @param markupPrefix
     *            The String that should be added as prefix.
     * @param markupSuffix
     *            The String that should be added as suffix.
     */
    public RegexMarkupValue(String value, String markupPrefix, String markupSuffix) {
        // this.originalRegexValue = value;
        this.originalRegexValue = StringEscapeUtils.escapeHtml4(String.valueOf(value));

        this.markupPrefix = markupPrefix;
        this.markupSuffix = markupSuffix;

        this.markupValue = markupPrefix + GROUP_INDEX_PLACEHOLDER + markupSuffix;
        this.markupRegexValue = markupPrefix + value + markupSuffix;
    }

    @Override
    public String applyMarkup(String input) {
        String result = "";
        if (getOriginalRegexValue() != null && !getOriginalRegexValue().isEmpty()) {
            Pattern pattern = null;
            if (this.caseInsensitive) {
                int flags = this.unicodeCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : Pattern.CASE_INSENSITIVE;
                pattern = Pattern.compile(getOriginalRegexValue(), flags);
            } else {
                pattern = Pattern.compile(getOriginalRegexValue());
            }

            XMLEventReader parser = null;
            try (StringReader reader = new StringReader(RichTextPainter.FAKE_ROOT_TAG_START + input + RichTextPainter.FAKE_ROOT_TAG_END)) {
                parser = this.factory.createXMLEventReader(reader);

                String textToParse = "";
                while (parser.hasNext()) {
                    XMLEvent event = parser.nextEvent();

                    switch (event.getEventType()) {
                        case XMLStreamConstants.START_DOCUMENT:
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            parser.close();
                            break;
                        case XMLStreamConstants.START_ELEMENT:
                        case XMLStreamConstants.END_ELEMENT:
                            // if we have collected text to parse, parse the
                            // characters
                            if (textToParse.length() > 0) {
                                textToParse = StringEscapeUtils.escapeHtml4(String.valueOf(textToParse));
                                Matcher matcher = pattern.matcher(textToParse);
                                if (matcher.groupCount() > 0) {
                                    result += matcher.replaceAll(this.markupValue);
                                } else {
                                    result += matcher.replaceAll(this.markupRegexValue);
                                }
                                // clear the text to parse for a possible second
                                // character sequence
                                textToParse = "";
                            }
                            // add the end tag also
                            result += event.toString();
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            Characters characters = event.asCharacters();
                            textToParse += characters.getData();
                            break;
                        default:
                            result += event.toString();
                    }
                }
            } catch (XMLStreamException e) {
                LOG.error("Error on XML processing", e);
            } finally {
                if (parser != null) {
                    try {
                        parser.close();
                    } catch (XMLStreamException e) {
                        LOG.error("Error on closing the XMLEventReader", e);
                    }
                }
            }

            result = result.replace(RichTextPainter.FAKE_ROOT_TAG_START, "").replace(RichTextPainter.FAKE_ROOT_TAG_END, "");
        } else {
            result = input;
        }
        return result;
    }

    @Override
    public String removeMarkup(String input) {
        if (getOriginalRegexValue() != null && !getOriginalRegexValue().isEmpty()) {
            return input.replaceAll(this.markupPrefix, "").replaceAll(this.markupSuffix, "");
        }
        return input;
    }

    /**
     * Set the regular expression that specifies the value that should be
     * surrounded by a markup.
     *
     * @param value
     *            The regular expression that specifies the value that should be
     *            surrounded by a markup.
     */
    public void setRegexValue(String value) {
        this.originalRegexValue = StringEscapeUtils.escapeHtml4(String.valueOf(value));
        this.markupRegexValue = this.markupPrefix + value + this.markupSuffix;
    }

    /**
     * Returns the regular expression that specifies the value that should be
     * surrounded by a markup. Subclasses can override this method to provide a
     * dynamic markup value, e.g. for highlighting values inserted into a text
     * field.
     *
     * @return The regular expression that specifies the value that should be
     *         surrounded by a markup.
     */
    protected String getOriginalRegexValue() {
        return this.originalRegexValue;
    }

    /**
     * Returns the regular expression that specifies the value that should be
     * surrounded by a markup, with the applied markup.
     *
     * @return The original regex value with applied markup.
     */
    protected String getMarkupRegexValue() {
        return this.markupRegexValue;
    }

    /**
     * Return whether the {@link Pattern#CASE_INSENSITIVE} flag is applied to
     * enable case insensitive matching.
     * <p>
     * <b>Note:</b> Case-insensitive matching can also be enabled via the
     * embedded flag expression (?i) if the flag in this
     * {@link RegexMarkupValue} is disabled.
     * </p>
     *
     * @return <code>true</code> if case insensitive matching is enabled,
     *         <code>false</code> if matching is case sensitive.
     *
     * @since 1.2
     */
    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    /**
     * Configure whether the {@link Pattern#CASE_INSENSITIVE} flag should be
     * applied to enable case insensitive matching.
     * <p>
     * <b>Note:</b> Case-insensitive matching can also be enabled via the
     * embedded flag expression (?i) if the flag in this
     * {@link RegexMarkupValue} is disabled.
     * </p>
     *
     * @param caseInsensitive
     *            <code>true</code> if case insensitive matching should be
     *            enabled, <code>false</code> if matching should be case
     *            sensitive.
     *
     * @since 1.2
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Return whether the {@link Pattern#UNICODE_CASE} flag is applied to enable
     * Unicode aware case folding. Only works if case insensitive matching is
     * enabled.
     * <p>
     * <b>Note:</b> Unicode-aware case folding can also be enabled via the
     * embedded flag expression (?u) if the flag in this
     * {@link RegexMarkupValue} is disabled.
     * </p>
     *
     * @return <code>true</code> if Unicode aware case folding is enabled,
     *         <code>false</code> if case-insensitive matching assumes that only
     *         characters in the US-ASCII charset are being matched.
     *
     * @since 1.2
     */
    public boolean isUnicodeCase() {
        return this.unicodeCase;
    }

    /**
     * Configure whether the {@link Pattern#UNICODE_CASE} flag should be applied
     * to enable Unicode aware case folding. Only works if case insensitive
     * matching is enabled.
     * <p>
     * <b>Note:</b> Unicode-aware case folding can also be enabled via the
     * embedded flag expression (?u) if the flag in this
     * {@link RegexMarkupValue} is disabled.
     * </p>
     *
     * @param unicodeCase
     *            <code>true</code> if Unicode aware case folding should be
     *            enabled, <code>false</code> if case-insensitive matching
     *            should only match characters in the US-ASCII charset.
     *
     * @since 1.2
     */
    public void setUnicodeCase(boolean unicodeCase) {
        this.unicodeCase = unicodeCase;
    }

}
