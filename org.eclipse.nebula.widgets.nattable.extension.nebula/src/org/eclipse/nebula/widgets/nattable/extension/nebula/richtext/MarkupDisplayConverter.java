/*****************************************************************************
 * Copyright (c) 2015, 2023 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.ContextualDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * {@link IDisplayConverter} that can be used to add HTML markups via String
 * replacements. Intended to be used in combination with the
 * {@link RichTextCellPainter} to support dynamic text highlighting.
 */
public class MarkupDisplayConverter extends ContextualDisplayConverter {

    protected IDisplayConverter wrappedConverter;
    /**
     * Collection of general markups.
     */
    protected Map<String, MarkupProcessor> markups = new LinkedHashMap<>();
    /**
     * Collection of markups that are registered for specific labels.
     *
     * @since 2.3
     */
    protected List<MarkupValueForLabels> markupsForLabels = new ArrayList<>();

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

            result = StringEscapeUtils.escapeHtml4(String.valueOf(result));

            // add markups
            for (MarkupProcessor markup : getMarkupProcessors(cell)) {
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
            List<MarkupProcessor> mc = new ArrayList<>(getMarkupProcessors(cell));
            Collections.reverse(mc);
            for (MarkupProcessor markup : mc) {
                result = markup.removeMarkup(result);
            }
            result = StringEscapeUtils.unescapeHtml4(String.valueOf(result));
            return this.wrappedConverter.displayToCanonicalValue(cell, configRegistry, result);
        }
        return this.wrappedConverter.displayToCanonicalValue(cell, configRegistry, displayValue);
    }

    /**
     * Get the list of {@link MarkupProcessor} that should be applied on the
     * given {@link ILayerCell}.
     *
     * @param cell
     *            The {@link ILayerCell} to get the {@link MarkupProcessor}s
     *            for.
     * @return The list of {@link MarkupProcessor} that should be applied on the
     *         given {@link ILayerCell}.
     * @since 2.3
     */
    protected List<MarkupProcessor> getMarkupProcessors(ILayerCell cell) {
        // first add all generally registered markups
        ArrayList<MarkupProcessor> mc = new ArrayList<>(this.markups.values());

        // then add all markups for lables if the labels are on the cell
        mc.addAll(
                this.markupsForLabels.stream()
                        .filter(m -> cell.getConfigLabels().hasAllLabels(m.labels))
                        .map(m -> m.processor)
                        .collect(Collectors.toList()));

        return mc;
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
        MarkupValue markupValue = new MarkupValue();
        markupValue.original = value;
        markupValue.markup = markupPrefix + value + markupSuffix;
        registerMarkup(value, markupValue);
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
     * @param labels
     *            The cell labels for which a markup should be registered.
     * @since 2.3
     */
    public void registerMarkupForLabel(String value, String markupPrefix, String markupSuffix, String... labels) {
        MarkupValue markupValue = new MarkupValue();
        markupValue.original = value;
        markupValue.markup = markupPrefix + value + markupSuffix;
        registerMarkupForLabel(markupValue, labels);
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
     * @param labels
     *            The cell labels for which a markup should be registered.
     * @since 2.3
     */
    public void registerMarkupForLabel(String value, String markupPrefix, String markupSuffix, List<String> labels) {
        MarkupValue markupValue = new MarkupValue();
        markupValue.original = value;
        markupValue.markup = markupPrefix + value + markupSuffix;
        registerMarkupForLabel(markupValue, labels);
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
     * @param labels
     *            The cell labels for which a markup should be registered.
     *
     * @since 2.3
     */
    public void registerRegexMarkupForLabel(String value, String markupPrefix, String markupSuffix, String... labels) {
        RegexMarkupValue markup = new RegexMarkupValue(value, markupPrefix, markupSuffix);
        registerMarkupForLabel(markup, labels);
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
     * @param labels
     *            The cell labels for which a markup should be registered.
     *
     * @since 2.3
     */
    public void registerRegexMarkupForLabel(String value, String markupPrefix, String markupSuffix, List<String> labels) {
        RegexMarkupValue markup = new RegexMarkupValue(value, markupPrefix, markupSuffix);
        registerMarkupForLabel(markup, labels);
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
     * Registers a custom {@link MarkupProcessor} for a given id.
     *
     * @param processor
     *            The custom {@link MarkupProcessor} that should be registered.
     * @param labels
     *            The cell labels for which a markup should be registered.
     *
     * @since 2.3
     */
    public void registerMarkupForLabel(MarkupProcessor processor, String... labels) {
        registerMarkupForLabel(processor, Arrays.asList(labels));
    }

    /**
     * Registers a custom {@link MarkupProcessor} for a given id.
     *
     * @param processor
     *            The custom {@link MarkupProcessor} that should be registered.
     * @param labels
     *            The cell labels for which a markup should be registered.
     *
     * @since 2.3
     */
    public void registerMarkupForLabel(MarkupProcessor processor, List<String> labels) {
        MarkupValueForLabels markupValue = new MarkupValueForLabels();
        markupValue.labels = labels;
        markupValue.processor = processor;
        this.markupsForLabels.add(markupValue);
    }

    /**
     * Unregister the markup that was registered for the given labels.
     *
     * @param labels
     *            The cell labels for which a markup was registered.
     * @since 2.3
     */
    public void unregisterMarkupForLabel(String... labels) {
        for (Iterator<MarkupValueForLabels> it = this.markupsForLabels.iterator(); it.hasNext();) {
            MarkupValueForLabels markupValue = it.next();
            if (ObjectUtils.collectionsEqual(markupValue.labels, Arrays.asList(labels))) {
                it.remove();
            }
        }
    }

    /**
     * Remove all registered markups.
     */
    public void clearMarkups() {
        this.markups.clear();
        this.markupsForLabels.clear();
    }

    /**
     * Simple value class to store the original value and the markup
     * replacement.
     */
    protected class MarkupValue implements MarkupProcessor {
        String original;
        String markup;

        /**
         * @since 1.1
         */
        @Override
        public String applyMarkup(String input) {
            return input.replaceAll(this.original, this.markup);
        }

        /**
         * @since 1.1
         */
        @Override
        public String removeMarkup(String input) {
            return input.replaceAll(this.markup, this.original);
        }
    }

    /**
     * Simple value class to store the original value and the markup
     * replacement. Additionally stores the cell labels for which the markup
     * should be applied.
     *
     * @since 2.3
     */
    protected class MarkupValueForLabels {
        List<String> labels;
        MarkupProcessor processor;
    }
}
