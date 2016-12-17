/*****************************************************************************
 * Copyright (c) 2015, 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.ContextualDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * {@link IDisplayConverter} that can be used to add HTML markups via String
 * replacements. Intended to be used in combination with the
 * {@link RichTextCellPainter} to support dynamic text highlighting.
 */
public class MarkupDisplayConverter extends ContextualDisplayConverter {

    protected IDisplayConverter wrappedConverter;
    protected Map<String, MarkupProcessor> markups = new LinkedHashMap<>();

    public MarkupDisplayConverter() {
        this(new DefaultDisplayConverter());
    }

    public MarkupDisplayConverter(IDisplayConverter wrappedConverter) {
        this.wrappedConverter = wrappedConverter;
    }

    @Override
    public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
        Object wrappedConverterResult = this.wrappedConverter.canonicalToDisplayValue(cell, configRegistry, canonicalValue);
        String result = null;
        if (wrappedConverterResult != null) {
            result = wrappedConverterResult.toString();

            // add markups
            for (MarkupProcessor markup : this.markups.values()) {
                result = markup.applyMarkup(result);
            }
        }
        return result;
    }

    @Override
    public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
        // remove markups
        if (displayValue != null) {
            String result = displayValue.toString();
            // perform removal in reverse order
            List<MarkupProcessor> mc = new ArrayList<>(this.markups.values());
            Collections.reverse(mc);
            for (MarkupProcessor markup : mc) {
                result = markup.removeMarkup(result);
            }
            return this.wrappedConverter.displayToCanonicalValue(cell, configRegistry, result);
        }
        return this.wrappedConverter.displayToCanonicalValue(cell, configRegistry, displayValue);
    }

    /**
     * Registers a value and the markup that should be placed around the value
     * while rendering.
     *
     * @param value
     *            The value that should be replacement with the markup for
     *            rendering.
     * @param markupPrefix
     *            The String that will be added as prefix to the value.
     * @param markupSuffix
     *            The String that will be added as suffix to the value.
     */
    public void registerMarkup(String value, String markupPrefix, String markupSuffix) {
        MarkupValue markup = new MarkupValue();
        markup.originalValue = value;
        markup.markupValue = markupPrefix + value + markupSuffix;
        registerMarkup(value, markup);
    }

    /**
     * Registers a regular expression and a markup that should be placed around
     * the value that is specified by the regular expression. The regular
     * expression needs to contain at least one group.
     * <p>
     * Example: (IMPORTANT|URGENT) as value will result as either of both words
     * will be surrounded by the markup.
     * </p>
     *
     * @param value
     *            A regular expression that specifies the value that should be
     *            replacement with the markup for rendering. Needs to contain at
     *            least one group that will be replaced.
     * @param markupPrefix
     *            The String that will be added as prefix to the value.
     * @param markupSuffix
     *            The String that will be added as suffix to the value.
     *
     * @since 1.1
     */
    public void registerRegexMarkup(String value, String markupPrefix, String markupSuffix) {
        RegexMarkupValue markup = new RegexMarkupValue(value, markupPrefix, markupSuffix);
        registerMarkup(value, markup);
    }

    /**
     * Registers a custom {@link MarkupProcessor} for a given id.
     *
     * @param id
     *            The id under which the {@link MarkupProcessor} should be
     *            registered.
     * @param processor
     *            The custom {@link MarkupProcessor} that should be registered.
     *
     * @since 1.1
     */
    public void registerMarkup(String id, MarkupProcessor processor) {
        this.markups.put(id, processor);
    }

    /**
     * Unregister the markup that was registered for the given value.
     *
     * @param value
     *            The value for which a markup was registered. In case of a
     *            value or regex markup this is the value for which the markup
     *            was registered. In case of a custom {@link MarkupProcessor} it
     *            needs to be the id for which it was registered.
     */
    public void unregisterMarkup(String value) {
        this.markups.remove(value);
    }

    /**
     * Remove all registered markups.
     */
    public void clearMarkups() {
        this.markups.clear();
    }

    /**
     * Simple value class to store the original value and the markup
     * replacement.
     */
    protected class MarkupValue implements MarkupProcessor {
        String originalValue;
        String markupValue;

        /**
         * @since 1.1
         */
        @Override
        public String applyMarkup(String input) {
            return input.replaceAll(this.originalValue, this.markupValue);
        }

        /**
         * @since 1.1
         */
        @Override
        public String removeMarkup(String input) {
            return input.replaceAll(this.markupValue, this.originalValue);
        }
    }
}
