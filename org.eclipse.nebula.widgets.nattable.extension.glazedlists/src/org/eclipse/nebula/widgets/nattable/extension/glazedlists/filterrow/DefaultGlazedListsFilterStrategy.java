/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 508891
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult;
import org.eclipse.nebula.widgets.nattable.filterrow.ParseResult.MatchType;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

public class DefaultGlazedListsFilterStrategy<T> implements IFilterStrategy<T> {

    private static final Log LOG = LogFactory.getLog(DefaultGlazedListsFilterStrategy.class);

    protected final IColumnAccessor<T> columnAccessor;
    protected final IConfigRegistry configRegistry;
    private final CompositeMatcherEditor<T> matcherEditor;

    protected FilterList<T> filterList;
    protected ReadWriteLock filterLock;

    /**
     * Create a new DefaultGlazedListsFilterStrategy on top of the given
     * FilterList.
     * <p>
     * Note: Using this constructor you don't need to create and set the
     * CompositeMatcherEditor as MatcherEditor on the FilterList yourself! The
     * necessary steps to get it working is done within this constructor.
     * </p>
     *
     * @param filterList
     *            The FilterList that is used within the GlazedLists based
     *            NatTable for filtering.
     * @param columnAccessor
     *            The IColumnAccessor necessary to access the column data of the
     *            row objects in the FilterList.
     * @param configRegistry
     *            The IConfigRegistry necessary to retrieve filter specific
     *            configurations.
     */
    public DefaultGlazedListsFilterStrategy(
            FilterList<T> filterList,
            IColumnAccessor<T> columnAccessor,
            IConfigRegistry configRegistry) {

        this(filterList, new CompositeMatcherEditor<T>(), columnAccessor, configRegistry);
        this.matcherEditor.setMode(CompositeMatcherEditor.AND);
    }

    /**
     * Create a new DefaultGlazedListsFilterStrategy on top of the given
     * FilterList using the given CompositeMatcherEditor. This is necessary to
     * support connection of multiple filter rows.
     * <p>
     * Note: Using this constructor you need to create the
     * CompositeMatcherEditor yourself. It will be added automatically to the
     * given FilterList, so you can skip that step.
     * </p>
     *
     * @param filterList
     *            The FilterList that is used within the GlazedLists based
     *            NatTable for filtering.
     * @param matcherEditor
     *            The CompositeMatcherEditor that should be used by this
     *            DefaultGlazedListsFilterStrategy.
     * @param columnAccessor
     *            The IColumnAccessor necessary to access the column data of the
     *            row objects in the FilterList.
     * @param configRegistry
     *            The IConfigRegistry necessary to retrieve filter specific
     *            configurations.
     */
    public DefaultGlazedListsFilterStrategy(
            FilterList<T> filterList,
            CompositeMatcherEditor<T> matcherEditor,
            IColumnAccessor<T> columnAccessor,
            IConfigRegistry configRegistry) {

        this.columnAccessor = columnAccessor;
        this.configRegistry = configRegistry;

        this.matcherEditor = matcherEditor;

        this.filterList = filterList;
        this.filterList.setMatcherEditor(this.matcherEditor);

        this.filterLock = filterList.getReadWriteLock();
    }

    /**
     * Create GlazedLists matcher editors and apply them to facilitate
     * filtering.
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void applyFilter(Map<Integer, Object> filterIndexToObjectMap) {

        if (filterIndexToObjectMap.isEmpty()) {
            // wait until all listeners had the chance to handle the clear event
            try {
                this.filterLock.writeLock().lock();
                this.matcherEditor.getMatcherEditors().clear();
            } finally {
                this.filterLock.writeLock().unlock();
            }

            return;
        }

        try {
            EventList<MatcherEditor<T>> matcherEditors = new BasicEventList<MatcherEditor<T>>();

            for (Entry<Integer, Object> mapEntry : filterIndexToObjectMap.entrySet()) {
                Integer columnIndex = mapEntry.getKey();
                String filterText = getStringFromColumnObject(columnIndex, mapEntry.getValue());

                String textDelimiter = this.configRegistry.getConfigAttribute(
                        FilterRowConfigAttributes.TEXT_DELIMITER,
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
                TextMatchingMode textMatchingMode = this.configRegistry.getConfigAttribute(
                        FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
                IDisplayConverter displayConverter = this.configRegistry.getConfigAttribute(
                        FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
                Comparator comparator = this.configRegistry.getConfigAttribute(
                        FilterRowConfigAttributes.FILTER_COMPARATOR,
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
                final Function<T, Object> columnValueProvider = getColumnValueProvider(columnIndex);

                List<ParseResult> parseResults = FilterRowUtils.parse(filterText, textDelimiter, textMatchingMode);

                EventList<MatcherEditor<T>> stringMatcherEditors = new BasicEventList<MatcherEditor<T>>();
                for (ParseResult parseResult : parseResults) {
                    try {
                        MatchType matchOperation = parseResult.getMatchOperation();
                        if (matchOperation == MatchType.NONE) {
                            stringMatcherEditors.add(getTextMatcherEditor(
                                    columnIndex,
                                    textMatchingMode,
                                    displayConverter,
                                    parseResult.getValueToMatch()));
                        } else {
                            Object threshold =
                                    displayConverter.displayToCanonicalValue(parseResult.getValueToMatch());
                            matcherEditors.add(getThresholdMatcherEditor(
                                    columnIndex,
                                    threshold,
                                    comparator,
                                    columnValueProvider,
                                    matchOperation));
                        }
                    } catch (PatternSyntaxException e) {
                        LOG.warn("Error on applying a filter: " + e.getLocalizedMessage()); //$NON-NLS-1$
                    }
                }

                if (stringMatcherEditors.size() > 0) {
                    final CompositeMatcherEditor<T> stringCompositeMatcherEditor =
                            new CompositeMatcherEditor<T>(stringMatcherEditors);
                    stringCompositeMatcherEditor.setMode(CompositeMatcherEditor.OR);
                    matcherEditors.add(stringCompositeMatcherEditor);
                }
            }

            // wait until all listeners had the chance to handle the clear event
            try {
                this.filterLock.writeLock().lock();

                // Remove the existing matchers that are removed from
                // 'filterIndexToObjectMap'
                final Iterator<MatcherEditor<T>> existingMatcherEditors =
                        this.matcherEditor.getMatcherEditors().iterator();
                while (existingMatcherEditors.hasNext()) {
                    final MatcherEditor<T> existingMatcherEditor = existingMatcherEditors.next();
                    if (!containsMatcherEditor(matcherEditors, existingMatcherEditor)) {
                        existingMatcherEditors.remove();
                    }
                }

                // Add the new matchers that are added from
                // 'filterIndexToObjectMap'
                for (final MatcherEditor<T> matcherEditor : matcherEditors) {
                    if (!containsMatcherEditor(this.matcherEditor.getMatcherEditors(), matcherEditor)) {
                        this.matcherEditor.getMatcherEditors().add(matcherEditor);
                    }
                }

            } finally {
                this.filterLock.writeLock().unlock();
            }

        } catch (Exception e) {
            LOG.error("Error on applying a filter", e); //$NON-NLS-1$
        }
    }

    /**
     * Converts the object inserted to the filter cell at the given column
     * position to the corresponding String.
     *
     * @param columnIndex
     *            The column index of the filter cell that should be processed.
     * @param object
     *            The value set to the filter cell that needs to be converted
     * @return The String value for the given filter value.
     */
    protected String getStringFromColumnObject(final int columnIndex, final Object object) {
        final IDisplayConverter displayConverter = this.configRegistry.getConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
        return displayConverter.canonicalToDisplayValue(object).toString();
    }

    /**
     * Set up a threshold matcher for tokens like '&gt;20', '&lt;=10' etc.
     *
     * @param columnIndex
     *            the column index of the column for which the matcher editor is
     *            being set up
     * @param threshold
     *            the threshold value used for comparison
     * @param comparator
     *            {@link Comparator} that is used to determine how objects
     *            compare with the threshold value
     * @param columnValueProvider
     *            {@link Function} that exposes the content of the given column
     *            index from a row object
     * @param matchOperation
     *            The NatTable {@link MatchType} used to determine the
     *            GlazedLists ThresholdMatcherEditor#MatchOperation
     * @return A {@link ThresholdMatcherEditor} that filters elements based on
     *         whether they are greater than or less than a threshold.
     */
    protected ThresholdMatcherEditor<T, Object> getThresholdMatcherEditor(
            Integer columnIndex,
            Object threshold,
            Comparator<Object> comparator,
            Function<T, Object> columnValueProvider,
            MatchType matchOperation) {

        ThresholdMatcherEditor<T, Object> thresholdMatcherEditor =
                new ThresholdMatcherEditor<T, Object>(threshold, null, comparator, columnValueProvider);

        FilterRowUtils.setMatchOperation(thresholdMatcherEditor, matchOperation);
        return thresholdMatcherEditor;
    }

    /**
     *
     * @param columnIndex
     *            The column index of the column whose contents should be
     *            exposed.
     * @return {@link Function} which exposes the content of the given column
     *         index from a row object
     */
    protected FunctionList.Function<T, Object> getColumnValueProvider(final int columnIndex) {
        return new FunctionList.Function<T, Object>() {
            @Override
            public Object evaluate(T rowObject) {
                return DefaultGlazedListsFilterStrategy.this.columnAccessor.getDataValue(rowObject, columnIndex);
            }
        };
    }

    /**
     * Sets up a text matcher editor for String tokens
     *
     * @param columnIndex
     *            the column index of the column for which the matcher editor is
     *            being set up
     * @param textMatchingMode
     *            The NatTable {@link TextMatchingMode} that should be used
     * @param converter
     *            The {@link IDisplayConverter} used for converting the cell
     *            value to a String
     * @param filterText
     *            text entered by the user in the filter row
     * @return A {@link TextMatcherEditor} based on the given information.
     */
    protected TextMatcherEditor<T> getTextMatcherEditor(
            Integer columnIndex,
            TextMatchingMode textMatchingMode,
            IDisplayConverter converter,
            String filterText) {
        final TextMatcherEditor<T> textMatcherEditor = new TextMatcherEditor<T>(getTextFilterator(columnIndex, converter));
        textMatcherEditor.setFilterText(new String[] { filterText });
        textMatcherEditor.setMode(getGlazedListsTextMatcherEditorMode(textMatchingMode));
        return textMatcherEditor;
    }

    /**
     *
     * @param columnIndex
     *            The column index of the column whose contents should be
     *            collected as Strings
     * @param converter
     *            The {@link IDisplayConverter} used for converting the cell
     *            value to a String
     * @return {@link TextFilterator} which exposes the contents of the column
     *         as a {@link String}
     */
    protected TextFilterator<T> getTextFilterator(final Integer columnIndex, final IDisplayConverter converter) {
        return new TextFilterator<T>() {
            @Override
            public void getFilterStrings(List<String> objectAsListOfStrings, T rowObject) {
                Object cellData = DefaultGlazedListsFilterStrategy.this.columnAccessor.getDataValue(rowObject, columnIndex);
                Object displayValue = converter.canonicalToDisplayValue(cellData);
                displayValue = (displayValue != null) ? displayValue : ""; //$NON-NLS-1$
                objectAsListOfStrings.add(displayValue.toString());
            }
        };
    }

    /**
     *
     * @param textMatchingMode
     *            The NatTable TextMatchingMode for which the GlazedLists
     *            {@link TextMatcherEditor} mode is requested
     * @return The GlazedLists {@link TextMatcherEditor} mode for the given
     *         NatTable {@link TextMatchingMode}
     */
    public int getGlazedListsTextMatcherEditorMode(TextMatchingMode textMatchingMode) {
        switch (textMatchingMode) {
            case EXACT:
                return TextMatcherEditor.EXACT;
            case STARTS_WITH:
                return TextMatcherEditor.STARTS_WITH;
            case REGULAR_EXPRESSION:
                return TextMatcherEditor.REGULAR_EXPRESSION;
            default:
                return TextMatcherEditor.CONTAINS;
        }
    }

    /**
     * This allows to determinate if the matcher editor in parameter is already
     * existing in the list of matcher editors as first parameter. This function
     * takes care of {@link CompositeMatcherEditor}.
     *
     * @param existingMatcherEditors
     *            The list of existing matcher editors.
     * @param matcherEditor
     *            The matcher editor to search.
     * @return <code>true</code> if the matcher editor is already existing in
     *         the list of matcher editors, <code>false</code> otherwise.
     *
     * @since 1.5
     */
    protected boolean containsMatcherEditor(
            final List<MatcherEditor<T>> existingMatcherEditors, final MatcherEditor<T> matcherEditor) {

        boolean result = false;

        final Iterator<MatcherEditor<T>> existingMatcherEditorsIterator = existingMatcherEditors.iterator();
        while (existingMatcherEditorsIterator.hasNext() && !result) {
            result = matcherEditorEqual(existingMatcherEditorsIterator.next(), matcherEditor);
        }

        return result;
    }

    /**
     * This allows to determinate if two matcher editors are equals.
     *
     * @param first
     *            The first matcher editor to compare.
     * @param second
     *            The second matcher editor to compare.
     * @return <code>true</code> if the matcher editors are equals,
     *         <code>false</code> otherwise.
     *
     * @since 1.5
     */
    protected boolean matcherEditorEqual(final MatcherEditor<T> first, final MatcherEditor<T> second) {

        boolean result = false;

        // Compare the matcher classes, and must be equals
        if (first.getClass().equals(second.getClass())) {
            if (first instanceof CompositeMatcherEditor) {

                // Check that the composite matcher editors have the same number
                // of sub matcher editors
                CompositeMatcherEditor<T> firstComp = (CompositeMatcherEditor<T>) first;
                CompositeMatcherEditor<T> secondComp = (CompositeMatcherEditor<T>) second;

                result = firstComp.getMatcherEditors().size() == secondComp.getMatcherEditors().size();

                // Check that all sub matcher editors of first composite matcher
                // editors are available in the sub matchers of second composite
                // matcher editor
                final Iterator<MatcherEditor<T>> matcherEditors = firstComp.getMatcherEditors().iterator();
                while (matcherEditors.hasNext() && result) {
                    MatcherEditor<T> e = matcherEditors.next();
                    final Iterator<MatcherEditor<T>> iterator = secondComp.getMatcherEditors().iterator();
                    boolean found = false;
                    while (iterator.hasNext() && !found) {
                        found = matcherEditorEqual(iterator.next(), e);
                    }
                    result = found;
                }

            } else if (first instanceof TextMatcherEditor) {
                TextMatcherEditor<T> firstText = (TextMatcherEditor<T>) first;
                TextMatcherEditor<T> secondText = (TextMatcherEditor<T>) second;

                result = first.getMatcher().equals(second.getMatcher())
                        && firstText.getMode() == secondText.getMode()
                        && firstText.getStrategy().equals(secondText.getStrategy());

            } else if (first instanceof ThresholdMatcherEditor) {
                ThresholdMatcherEditor<?, ?> firstThreshold = (ThresholdMatcherEditor<?, ?>) first;
                ThresholdMatcherEditor<?, ?> secondThreshold = (ThresholdMatcherEditor<?, ?>) second;

                result = firstThreshold.getThreshold().equals(secondThreshold.getThreshold())
                        && firstThreshold.getComparator().equals(secondThreshold.getComparator())
                        // MatchOperation is not visible and must be a
                        // references instance, so the 'equals' is not needed
                        && firstThreshold.getMatchOperation() == secondThreshold.getMatchOperation();
            }
        }

        return result;
    }

    /**
     * Returns the {@link CompositeMatcherEditor} that is created and used by
     * this {@link IFilterStrategy}. In prior versions it was necessary to
     * create the {@link CompositeMatcherEditor} outside this class and use it
     * as constructor parameter. We changed this to hide that implementation
     * from users and to ensure that filter operations and possible listeners
     * are executed thread safe. Otherwise there might be concurrency issues
     * while filtering.
     * <p>
     * If you want to use additional filtering you should now use this method to
     * work on the created {@link CompositeMatcherEditor} instead of creating
     * one outside. For static filtering additional to the filter row you might
     * want to consider using the
     * {@link DefaultGlazedListsStaticFilterStrategy}.
     * </p>
     *
     * @return The {@link CompositeMatcherEditor} that is created and used by
     *         this {@link IFilterStrategy}.
     */
    public CompositeMatcherEditor<T> getMatcherEditor() {
        return this.matcherEditor;
    }
}
