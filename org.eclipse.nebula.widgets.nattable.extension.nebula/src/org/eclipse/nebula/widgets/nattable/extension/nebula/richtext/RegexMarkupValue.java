/*****************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import java.io.StringReader;
import java.util.regex.Pattern;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.nebula.widgets.richtext.RichTextPainter;

/**
 * {@link MarkupProcessor} implementation that is able to process regular
 * expression values to identify content that should be surrounded by a markup.
 * The regular expression needs to contain one group so the replacement can be
 * done by using a placeholder.
 *
 * @since 1.1
 */
public class RegexMarkupValue implements MarkupProcessor {

    private static final String GROUP_INDEX_PLACEHOLDER = "$1";

    private String originalRegexValue;
    private String markupPrefix;
    private String markupSuffix;

    private String markupValue;
    private String markupRegexValue;

    private XMLInputFactory factory = XMLInputFactory.newInstance();

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
        this.originalRegexValue = value;
        this.markupPrefix = markupPrefix;
        this.markupSuffix = markupSuffix;

        this.markupValue = markupPrefix + GROUP_INDEX_PLACEHOLDER + markupSuffix;
        this.markupRegexValue = markupPrefix + value + markupSuffix;
    }

    @Override
    public String applyMarkup(String input) {
        String result = "";
        if (getOriginalRegexValue() != null && !getOriginalRegexValue().isEmpty()) {
            XMLEventReader parser = null;
            try {
                parser = this.factory.createXMLEventReader(
                        new StringReader(RichTextPainter.FAKE_ROOT_TAG_START + input + RichTextPainter.FAKE_ROOT_TAG_END));

                while (parser.hasNext()) {
                    XMLEvent event = parser.nextEvent();

                    switch (event.getEventType()) {
                        case XMLStreamConstants.START_DOCUMENT:
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            parser.close();
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            Characters characters = event.asCharacters();
                            String text = characters.getData();
                            result += Pattern.compile(getOriginalRegexValue(), Pattern.CASE_INSENSITIVE).matcher(text).replaceAll(this.markupValue);
                            break;
                        default:
                            result += event.toString();
                    }
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } finally {
                if (parser != null) {
                    try {
                        parser.close();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
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
            return input.replaceAll(getMarkupRegexValue(), GROUP_INDEX_PLACEHOLDER);
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
        this.originalRegexValue = value;
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
}
