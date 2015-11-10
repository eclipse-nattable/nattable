/*******************************************************************************
 * Copyright (c) 2013, 2014, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;

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
    private MatcherEditor<T> matchNone = new AbstractMatcherEditor<T>() {
        {
            fireMatchNone();
        }
    };

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
        if (filterIndexToObjectMap.isEmpty()) {
            this.getMatcherEditor().getMatcherEditors().add(this.matchNone);
            return;
        }

        // we need to create a new Map for applying a filter using the parent
        // class otherwise we would remove the previous added pre-selected
        // values
        Map<Integer, Object> newIndexToObjectMap = new HashMap<Integer, Object>();
        newIndexToObjectMap.putAll(filterIndexToObjectMap);

        // remove all complete selected
        for (Iterator<Map.Entry<Integer, Object>> it = newIndexToObjectMap.entrySet().iterator(); it.hasNext();) {
            Entry<Integer, Object> entry = it.next();
            Object filterObject = entry.getValue();
            if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(filterObject)) {
                it.remove();
            } else {
                List<?> dataProviderList = this.comboBoxDataProvider.getValues(entry.getKey(), 0);

                // selecting all is transported as String to support lazy
                // loading of combo box values
                Collection filterCollection = (filterObject != null && filterObject instanceof Collection) ? (Collection) filterObject : null;
                if (filterCollection == null || filterCollection.isEmpty()) {
                    // for one column there are no items selected in the combo,
                    // therefore nothing matches
                    this.getMatcherEditor().getMatcherEditors().add(this.matchNone);
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
    @SuppressWarnings("rawtypes")
    @Override
    protected String getStringFromColumnObject(final int columnIndex, final Object object) {
        final IDisplayConverter displayConverter = this.configRegistry.getConfigAttribute(
                FILTER_DISPLAY_CONVERTER,
                NORMAL,
                FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);

        if (object instanceof Collection) {
            String result = ""; //$NON-NLS-1$
            Collection valueCollection = (Collection) object;
            for (Object value : valueCollection) {
                if (result.length() > 0) {
                    result += "|"; //$NON-NLS-1$
                }
                String convertedValue = displayConverter.canonicalToDisplayValue(value).toString();
                if (convertedValue.isEmpty()) {
                    // for an empty String add the regular expression for empty
                    // String
                    result += "^$"; //$NON-NLS-1$
                } else {
                    result += Pattern.quote(convertedValue);
                }
            }
            return "(" + result + ")"; //$NON-NLS-1$//$NON-NLS-2$
        }

        if (displayConverter != null) {
            Object result = displayConverter.canonicalToDisplayValue(object);
            if (result != null) {
                return result.toString();
            }
        }
        return ""; //$NON-NLS-1$
    }

    @SuppressWarnings("rawtypes")
    protected boolean filterCollectionsEqual(Collection filter1, Collection filter2) {
        if ((filter1 != null && filter2 != null)
                && filter1.size() == filter2.size()) {

            if (!filter1.equals(filter2)) {
                // as equality for collections take into account the order and
                // the elements we perform an additional check if the same items
                // regardless the order are contained in both lists
                for (Object f1 : filter1) {
                    if (!filter2.contains(f1)) {
                        return false;
                    }
                }
                // as lists can contain the same element twice, we also perform
                // a counter check
                for (Object f2 : filter2) {
                    if (!filter1.contains(f2)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }
}
