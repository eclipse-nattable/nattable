/*******************************************************************************
 * Copyright (c) 2013, 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Dirk Fauth <dirk.fauth@googlemail.com> - Bug 454503
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX;
import static org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsLockHelper;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterUtils;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowCategoryValueMapper;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

/**
 * Specialisation of the DefaultGlazedListsStaticFilterStrategy that is intended
 * to be used in combination with FilterRowComboBoxCellEditors that allows
 * filtering via multiselect comboboxes like in Excel. As it extends
 * DefaultGlazedListsStaticFilterStrategy it also supports static filters which
 * allows to integrate it with the GlazedListsRowHideShowLayer.
 * <p>
 * The special case in here is that if nothing is selected in the filter
 * combobox, then everything should be filtered.
 */
public class ComboBoxGlazedListsFilterStrategy<T> extends DefaultGlazedListsStaticFilterStrategy<T> {

    /**
     * The FilterRowComboBoxDataProvider needed to determine whether filters
     * should applied or not. If there are no values specified for filtering of
     * a column then everything should be filtered, if all possible values are
     * given as filter then no filter needs to be applied.
     */
    private FilterRowComboBoxDataProvider<T> comboBoxDataProvider;

    /**
     * A MatcherEditor that will never match anything.
     */
    private MatcherEditor<T> matchNone = GlazedLists.fixedMatcherEditor(Matchers.falseMatcher());

    /**
     *
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider needed to determine whether
     *            filters should applied or not. If there are no values
     *            specified for filtering of a column then everything should be
     *            filtered, if all possible values are given as filter then no
     *            filter needs to be applied.
     * @param filterList
     *            The CompositeMatcherEditor that is used for GlazedLists
     *            filtering
     * @param columnAccessor
     *            The IColumnAccessor needed to access the row data to perform
     *            filtering
     * @param configRegistry
     *            The IConfigRegistry to retrieve several configurations from
     */
    public ComboBoxGlazedListsFilterStrategy(
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            FilterList<T> filterList,
            IColumnAccessor<T> columnAccessor,
            IConfigRegistry configRegistry) {
        super(filterList, columnAccessor, configRegistry);
        this.comboBoxDataProvider = comboBoxDataProvider;
    }

    /**
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider needed to determine whether
     *            filters should applied or not. If there are no values
     *            specified for filtering of a column then everything should be
     *            filtered, if all possible values are given as filter then no
     *            filter needs to be applied.
     * @param filterList
     *            The FilterList that is used within the GlazedLists based
     *            NatTable for filtering.
     * @param matcherEditor
     *            The CompositeMatcherEditor that should be used by this
     *            DefaultGlazedListsStaticFilterStrategy.
     * @param columnAccessor
     *            The IColumnAccessor necessary to access the column data of the
     *            row objects in the FilterList.
     * @param configRegistry
     *            The IConfigRegistry necessary to retrieve filter specific
     *            configurations.
     */
    public ComboBoxGlazedListsFilterStrategy(
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            FilterList<T> filterList,
            CompositeMatcherEditor<T> matcherEditor,
            IColumnAccessor<T> columnAccessor,
            IConfigRegistry configRegistry) {
        super(filterList, matcherEditor, columnAccessor, configRegistry);
        this.comboBoxDataProvider = comboBoxDataProvider;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void applyFilter(Map<Integer, Object> filterIndexToObjectMap) {
        if (filterIndexToObjectMap.isEmpty() && hasComboBoxFilterEditorRegistered()) {
            GlazedListsLockHelper.performWriteOperation(
                    this.filterLock,
                    () -> this.getMatcherEditor().getMatcherEditors().add(this.matchNone));
            return;
        }

        // we need to create a new Map for applying a filter using the parent
        // class otherwise we would remove the previous added pre-selected
        // values
        Map<Integer, Object> newIndexToObjectMap = new HashMap<>();
        newIndexToObjectMap.putAll(filterIndexToObjectMap);

        // remove all complete selected
        for (Iterator<Map.Entry<Integer, Object>> it = newIndexToObjectMap.entrySet().iterator(); it.hasNext();) {
            Entry<Integer, Object> entry = it.next();
            Object filterObject = entry.getValue();

            // Check if the filter editor is the combobox and only handle the
            // collection case with that type of editor. Note that ignoring the
            // SELECT_ALL_ITEMS_VALUE is needed in any case.

            if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(filterObject)) {
                it.remove();
            } else if (ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, entry.getKey())) {
                List<?> dataProviderList = this.comboBoxDataProvider.getAllValues(entry.getKey());

                // selecting all is transported as String to support lazy
                // loading of combo box values
                Collection filterCollection = (filterObject instanceof Collection) ? (Collection) filterObject : null;
                if (filterCollection == null || filterCollection.isEmpty()) {
                    // for one column there are no items selected in the combo,
                    // therefore nothing matches
                    GlazedListsLockHelper.performWriteOperation(
                            this.filterLock,
                            () -> this.getMatcherEditor().getMatcherEditors().add(this.matchNone));
                    return;
                } else if (filterCollectionsEqual(filterCollection, dataProviderList)) {
                    it.remove();
                }
            }
        }

        super.applyFilter(newIndexToObjectMap);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is able to handle Collections and will generate a
     * regular expression containing all values in the Collection.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected String getStringFromColumnObject(final int columnIndex, final Object object) {
        final IDisplayConverter displayConverter = this.configRegistry.getConfigAttribute(
                FILTER_DISPLAY_CONVERTER,
                NORMAL,
                FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);

        if (object instanceof Collection) {
            Collection valueCollection = (Collection) object;
            StringJoiner joiner = new StringJoiner("|", "(", ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

            FilterRowCategoryValueMapper categoryValueMapper = this.comboBoxDataProvider.getCategoryValueMapper(columnIndex);
            if (categoryValueMapper != null) {
                // map categories back to values for filtering
            	valueCollection = categoryValueMapper.resolveCategories(valueCollection);
            }

            for (Object value : valueCollection) {
                String convertedValue = displayConverter.canonicalToDisplayValue(
                        new LayerCell(null, columnIndex, 0),
                        this.configRegistry,
                        value).toString();
                if (convertedValue.isEmpty()) {
                    // for an empty String add the regular expression for empty
                    // String
                    joiner.add("^$"); //$NON-NLS-1$
                } else if (this.comboBoxDataProvider.isFlattenCollectionValues(columnIndex)) {
                    // for a flattened collection we need to match the value in
                    // the string representation of the collection
                    joiner.add(getFlattenedCollectionPatternPrefix() + Pattern.quote(convertedValue) + getFlattenedCollectionPatternSuffix());
                } else {
                    // normal case, just add the quoted value to search for the
                    // exact match
                    joiner.add(Pattern.quote(convertedValue));
                }
            }
            return joiner.toString();
        }

        if (displayConverter != null) {
            Object result = displayConverter.canonicalToDisplayValue(
                    new LayerCell(null, columnIndex, 0),
                    this.configRegistry,
                    object);
            if (result != null) {
                return result.toString();
            }
        }
        return ""; //$NON-NLS-1$
    }

    @SuppressWarnings("rawtypes")
    protected boolean filterCollectionsEqual(Collection filter1, Collection filter2) {
        return ObjectUtils.collectionsEqual(filter1, filter2);
    }

    /**
     * Checks if at least one combobox cell editor is registered. Needed to the
     * <i>matchNone</i> processing if the filter collection is empty.
     *
     * @return <code>true</code> if at least one combobox cell editor is
     *         registered, <code>false</code> if not.
     *
     * @since 2.1
     */
    protected boolean hasComboBoxFilterEditorRegistered() {
        for (int i = 0; i < this.columnAccessor.getColumnCount(); i++) {
            if (ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the pattern prefix for the regular expression when filtering for
     * flattened collection values. By default this is ".*?(?&lt;=^|,|\[|\s)" to
     * match values in a comma-separated list that may have spaces around them
     * or start with a bracket.
     *
     * @return The pattern prefix for the regular expression when filtering for
     *         flattened collection values.
     *
     * @since 2.7
     */
    protected String getFlattenedCollectionPatternPrefix() {
        return ".*?(?<=^|,|\\[|\\s)"; //$NON-NLS-1$
    }

    /**
     * Returns the pattern suffix for the regular expression when filtering for
     * flattened collection values. By default this is "(?=,|\]|\s|$).*?" to
     * match values in a comma-separated list that may have spaces around them
     * or end with a bracket.
     *
     * @return The pattern suffix for the regular expression when filtering for
     *         flattened collection values.
     *
     * @since 2.7
     */
    protected String getFlattenedCollectionPatternSuffix() {
        return "(?=,|\\]|\\s|$).*?"; //$NON-NLS-1$
    }
}
