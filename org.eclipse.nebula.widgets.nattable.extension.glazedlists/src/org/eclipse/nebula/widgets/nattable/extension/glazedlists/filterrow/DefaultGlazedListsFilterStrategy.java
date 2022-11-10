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
 *     Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 508891
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.ThresholdMatcherEditor;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

public class DefaultGlazedListsFilterStrategy<T> implements IFilterStrategy<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGlazedListsFilterStrategy.class);

    protected final IColumnAccessor<T> columnAccessor;
    protected final IConfigRegistry configRegistry;
    private final CompositeMatcherEditor<T> matcherEditor;

    protected FilterList<T> filterList;
    protected ReadWriteLock filterLock;

    /**
     * Special {@link MatcherEditor} that is used to force a re-evaluation of
     * the local {@link CompositeMatcherEditor} in case the
     * {@link CompositeMatcherEditor} was not changed but maybe the collection
     * content might have been changed.
     *
     * @since 1.6
     */
    private MatcherEditor<T> matchAll = GlazedLists.fixedMatcherEditor(Matchers.trueMatcher());

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

        this(filterList, new CompositeMatcherEditor<>(), columnAccessor, configRegistry);
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
            EventList<MatcherEditor<T>> matcherEditors = new BasicEventList<>();

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
                IDisplayConverter displayConverter = getFilterContentDisplayConverter(columnIndex);
                Comparator comparator = this.configRegistry.getConfigAttribute(
                        FilterRowConfigAttributes.FILTER_COMPARATOR,
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
                final Function<T, Object> columnValueProvider = getColumnValueProvider(columnIndex);

                List<ParseResult> parseResults = FilterRowUtils.parse(filterText, textDelimiter, textMatchingMode);

                EventList<MatcherEditor<T>> stringMatcherEditors = new BasicEventList<>();
                EventList<MatcherEditor<T>> thresholdMatcherEditors = new BasicEventList<>();
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
                            thresholdMatcherEditors.add(getThresholdMatcherEditor(
                                    columnIndex,
                                    threshold,
                                    comparator,
                                    columnValueProvider,
                                    matchOperation));
                        }
                    } catch (PatternSyntaxException e) {
                        LOG.warn("Error on applying a filter: {}", e.getLocalizedMessage()); //$NON-NLS-1$
                    }
                }

                String[] separator = getSeparatorCharacters(textDelimiter);

                if (!stringMatcherEditors.isEmpty()) {
                    CompositeMatcherEditor<T> stringCompositeMatcherEditor = new CompositeMatcherEditor<>(stringMatcherEditors);
                    if (separator == null || filterText.contains(separator[1])) {
                        stringCompositeMatcherEditor.setMode(CompositeMatcherEditor.OR);
                    } else {
                        stringCompositeMatcherEditor.setMode(CompositeMatcherEditor.AND);
                    }
                    matcherEditors.add(stringCompositeMatcherEditor);
                }

                if (!thresholdMatcherEditors.isEmpty()) {
                    CompositeMatcherEditor<T> thresholdCompositeMatcherEditor = new CompositeMatcherEditor<>(thresholdMatcherEditors);
                    if (separator == null || filterText.contains(separator[0])) {
                        thresholdCompositeMatcherEditor.setMode(CompositeMatcherEditor.AND);
                    } else {
                        thresholdCompositeMatcherEditor.setMode(CompositeMatcherEditor.OR);
                    }
                    matcherEditors.add(thresholdCompositeMatcherEditor);
                }
            }

            // wait until all listeners had the chance to handle the clear event
            try {
                this.filterLock.writeLock().lock();

                boolean changed = false;

                // Remove the existing matchers that are removed from
                // 'filterIndexToObjectMap'
                final Iterator<MatcherEditor<T>> existingMatcherEditors =
                        this.matcherEditor.getMatcherEditors().iterator();
                while (existingMatcherEditors.hasNext()) {
                    final MatcherEditor<T> existingMatcherEditor = existingMatcherEditors.next();
                    if (!containsMatcherEditor(matcherEditors, existingMatcherEditor)) {
                        existingMatcherEditors.remove();
                        changed = true;
                    }
                }

                // Add the new matchers that are added from
                // 'filterIndexToObjectMap'
                for (final MatcherEditor<T> me : matcherEditors) {
                    if (!containsMatcherEditor(this.matcherEditor.getMatcherEditors(), me)) {
                        this.matcherEditor.getMatcherEditors().add(me);
                        changed = true;
                    }
                }

                // If there was no change to the MatcherEditors but
                // applyFilter() was called, probably the re-evaluation of the
                // filter was requested. To trigger the re-evaluation we need to
                // add a MatcherEditor that matches all.
                if (!changed) {
                    this.matcherEditor.getMatcherEditors().add(this.matchAll);
                    this.matcherEditor.getMatcherEditors().remove(this.matchAll);
                }

            } finally {
                this.filterLock.writeLock().unlock();
            }

        } catch (Exception e) {
            LOG.error("Error on applying a filter", e); //$NON-NLS-1$
        }
    }

    // TODO 2.1 move constants and getSeparatorCharacters() to FilterRowUtils

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
     */
    private String[] getSeparatorCharacters(String delimiter) {
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

    /**
     * Retrieves the {@link IDisplayConverter} that should be used for
     * converting the body content to string for text match filter operations.
     * <p>
     * First checks if there is a converter registered for
     * {@link FilterRowConfigAttributes#FILTER_CONTENT_DISPLAY_CONVERTER} which
     * should be the same converter that is also registered in the body region.
     * For backwards compatibility if no value is registered for that
     * configuration attribute it will check for
     * {@link FilterRowConfigAttributes#FILTER_DISPLAY_CONVERTER}.
     * </p>
     *
     * @param columnIndex
     *            The column index of the column for which a filter should be
     *            applied.
     * @return The {@link IDisplayConverter} to be used for converting the body
     *         content to string for text match filter operations.
     *
     * @since 2.0
     */
    protected IDisplayConverter getFilterContentDisplayConverter(int columnIndex) {
        IDisplayConverter displayConverter = this.configRegistry.getConfigAttribute(
                FilterRowConfigAttributes.FILTER_CONTENT_DISPLAY_CONVERTER,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);

        if (displayConverter == null) {
            displayConverter = this.configRegistry.getConfigAttribute(
                    FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);
        }

        return displayConverter;
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
                new ThresholdMatcherEditor<>(threshold, null, comparator, columnValueProvider);

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
        return rowObject -> DefaultGlazedListsFilterStrategy.this.columnAccessor.getDataValue(rowObject, columnIndex);
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
        TextMatcherEditor<T> textMatcherEditor = new TextMatcherEditor<>(getTextFilterator(columnIndex, converter));
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
        return new ColumnTextFilterator(converter, columnIndex);
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
     * This allows to determinate if two matcher editors are equal.
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

                // check if both CompositeMatcherEditor have the same size and
                // the same mode
                result = (firstComp.getMatcherEditors().size() == secondComp.getMatcherEditors().size())
                        && (firstComp.getMode() == secondComp.getMode());

                if (result) {
                    // Check that all sub matcher editors of first composite
                    // matcher editors are available in the sub matchers of
                    // second composite matcher editor
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
                }

            } else if (first instanceof TextMatcherEditor) {
                TextMatcherEditor<T> firstText = (TextMatcherEditor<T>) first;
                TextMatcherEditor<T> secondText = (TextMatcherEditor<T>) second;

                result = first.getMatcher().equals(second.getMatcher())
                        && firstText.getFilterator().equals(secondText.getFilterator())
                        && firstText.getMode() == secondText.getMode()
                        && firstText.getStrategy().equals(secondText.getStrategy());

            } else if (first instanceof ThresholdMatcherEditor) {
                ThresholdMatcherEditor<?, ?> firstThreshold = (ThresholdMatcherEditor<?, ?>) first;
                ThresholdMatcherEditor<?, ?> secondThreshold = (ThresholdMatcherEditor<?, ?>) second;

                result = (firstThreshold.getThreshold() != null && secondThreshold.getThreshold() != null)
                        && firstThreshold.getThreshold().equals(secondThreshold.getThreshold())
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

    /**
     * {@link TextFilterator} implementation that extracts the cell value for a
     * column as String by using an {@link IDisplayConverter}.
     *
     * @since 1.6
     */
    public class ColumnTextFilterator implements TextFilterator<T> {
        private final IDisplayConverter converter;
        private final Integer columnIndex;

        public ColumnTextFilterator(IDisplayConverter converter, Integer columnIndex) {
            this.converter = converter;
            this.columnIndex = columnIndex;
        }

        @Override
        public void getFilterStrings(List<String> objectAsListOfStrings, T rowObject) {
            Object cellData = DefaultGlazedListsFilterStrategy.this.columnAccessor.getDataValue(rowObject, this.columnIndex);
            Object displayValue = this.converter.canonicalToDisplayValue(cellData);
            displayValue = (displayValue != null) ? displayValue : ""; //$NON-NLS-1$
            objectAsListOfStrings.add(displayValue.toString());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((this.columnIndex == null) ? 0 : this.columnIndex.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            @SuppressWarnings("unchecked")
            ColumnTextFilterator other = (ColumnTextFilterator) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (this.columnIndex == null) {
                if (other.columnIndex != null)
                    return false;
            } else if (!this.columnIndex.equals(other.columnIndex))
                return false;
            return true;
        }

        private DefaultGlazedListsFilterStrategy<T> getOuterType() {
            return DefaultGlazedListsFilterStrategy.this;
        }
    }

}
