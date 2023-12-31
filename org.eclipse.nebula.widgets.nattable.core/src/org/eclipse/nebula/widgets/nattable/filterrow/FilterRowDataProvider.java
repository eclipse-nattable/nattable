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
package org.eclipse.nebula.widgets.nattable.filterrow;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.util.PersistenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data provider for the filter row
 * <ul>
 * <li>Stores filter strings</li>
 * <li>Applies them to the ca.odell.glazedlists.matchers.MatcherEditor on the
 * ca.odell.glazedlists.FilterList</li>
 * </ul>
 */
public class FilterRowDataProvider<T> implements IDataProvider, IPersistable {

    private static final Logger LOG = LoggerFactory.getLogger(FilterRowDataProvider.class);

    /**
     * Replacement for the pipe character | that is used for persistence. If
     * regular expressions are used for filtering, the pipe character can be
     * used in the regular expression to specify alternations. As the
     * persistence mechanism in NatTable uses the pipe character for separation
     * of values, the persistence breaks for such cases. By replacing the pipe
     * in the regular expression with some silly uncommon value specified here,
     * we ensure to be able to also persist pipes in the regular expressions, as
     * well as being backwards compatible with already saved filter row states.
     */
    public static final String PIPE_REPLACEMENT = "°~°"; //$NON-NLS-1$

    /**
     * Replacement for the comma character , that is used for persisting
     * collection values in case of combobox filters.
     *
     * @since 2.1
     */
    public static final String COMMA_REPLACEMENT = "°#°"; //$NON-NLS-1$

    /**
     * Replacement for the null value that is used for persisting collection
     * values in case of combobox filters. Needed for the inverted persistence
     * in case there are null values in the collection that need to be
     * persisted.
     *
     * @since 2.1
     */
    public static final String NULL_REPLACEMENT = "°null°"; //$NON-NLS-1$

    /**
     * Replacement for an empty String value that is used for persisting
     * collection values in case of combobox filters. Needed for the inverted
     * persistence in case there are empty String values in the collection that
     * need to be persisted.
     *
     * @since 2.1
     */
    public static final String EMPTY_REPLACEMENT = "°empty°"; //$NON-NLS-1$

    /**
     * The prefix String that will be used to mark that the following filter
     * value in the persisted state is a collection.
     */
    public static final String FILTER_COLLECTION_PREFIX = "°coll("; //$NON-NLS-1$

    /**
     * The {@link IFilterStrategy} to which the set filter value should be
     * applied.
     */
    private final IFilterStrategy<T> filterStrategy;
    /**
     * The column header layer where this {@link IDataProvider} is used for
     * filtering. Needed for retrieval of column indexes and firing according
     * filter events.
     */
    private final ILayer columnHeaderLayer;
    /**
     * The {@link IDataProvider} of the column header. This is necessary to
     * retrieve the real column count of the column header and not a transformed
     * one. (e.g. hiding a column would change the column count in the column
     * header but not in the column header {@link IDataProvider}).
     */
    private final IDataProvider columnHeaderDataProvider;

    /**
     * The {@link IConfigRegistry} needed to retrieve the
     * {@link IDisplayConverter} for converting the values on state save/load
     * operations.
     */
    private final IConfigRegistry configRegistry;

    /**
     * Contains the filter objects mapped to the column index. Basically the
     * data storage for the set filters in the filter row so they are visible to
     * the user who entered them.
     */
    private Map<Integer, Object> filterIndexToObjectMap = new HashMap<>();

    /**
     * The {@link FilterRowComboBoxDataProvider} that is needed to support
     * inverted persistence of filter collections. By default the values in the
     * collection are persisted as is. In case of Excel like filters, it can be
     * more feasible to store which values are NOT selected, to be able to load
     * the filter even for different values in the filter list.
     *
     * @see FilterRowDataProvider#invertCollectionPersistence
     *
     * @since 2.1
     */
    private FilterRowComboBoxDataProvider<T> filterRowComboBoxDataProvider;

    /**
     *
     * @param filterStrategy
     *            The {@link IFilterStrategy} to which the set filter value
     *            should be applied.
     * @param columnHeaderLayer
     *            The column header layer where this {@link IDataProvider} is
     *            used for filtering needed for retrieval of column indexes and
     *            firing according filter events..
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the
     *            {@link IDisplayConverter} for converting the values on state
     *            save/load operations.
     */
    public FilterRowDataProvider(
            IFilterStrategy<T> filterStrategy,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry) {
        this.filterStrategy = filterStrategy;
        this.columnHeaderLayer = columnHeaderLayer;
        this.columnHeaderDataProvider = columnHeaderDataProvider;
        this.configRegistry = configRegistry;
    }

    /**
     * Returns the map that contains the filter objects mapped to the column
     * index. It is the data storage for the inserted filters into the filter
     * row by the user.
     * <p>
     * Note: Usually it is not intended to modify this Map directly. You should
     * rather call <code>setDataValue(int, int, Object)</code> or
     * <code>clearAllFilters()</code> to modify this Map to ensure consistency
     * in other framework code. It is made visible because there might be code
     * that needs to modify the Map without index transformations or firing
     * events.
     *
     * @return Map that contains the filter objects mapped to the column index.
     */
    public Map<Integer, Object> getFilterIndexToObjectMap() {
        return this.filterIndexToObjectMap;
    }

    /**
     * Set the map that contains the filter objects mapped to the column index
     * to be the data storage for the inserted filters into the filter row by
     * the user.
     * <p>
     * Note: Usually it is not intended to set this Map from the outside as it
     * is created in the constructor. But there might be use cases where you
     * e.g. need to connect filter rows to each other. In this case it might be
     * useful to override the local Map with the one form another
     * FilterRowDataProvider. This is not a typical use case, therefore you
     * should use this method carefully!
     *
     * @param filterIndexToObjectMap
     *            Map that contains the filter objects mapped to the column
     *            index.
     */
    public void setFilterIndexToObjectMap(Map<Integer, Object> filterIndexToObjectMap) {
        this.filterIndexToObjectMap = filterIndexToObjectMap;
    }

    @Override
    public int getColumnCount() {
        return this.columnHeaderDataProvider.getColumnCount();
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.filterIndexToObjectMap.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        boolean cleared = false;
        Object oldValue = this.filterIndexToObjectMap.get(columnIndex);
        if (newValue != null && newValue.toString().length() > 0) {
            if (!newValue.equals(oldValue)) {
                this.filterIndexToObjectMap.put(columnIndex, newValue);
            }
        } else {
            cleared = true;
            if (oldValue != null) {
                this.filterIndexToObjectMap.remove(columnIndex);
            }
        }

        this.filterStrategy.applyFilter(this.filterIndexToObjectMap);

        this.columnHeaderLayer.fireLayerEvent(
                new FilterAppliedEvent(this.columnHeaderLayer, columnIndex, oldValue, newValue, cleared));
    }

    // Load/save state

    @Override
    public void saveState(String prefix, Properties properties) {
        HashMap<Integer, String> filterTextByIndex = new HashMap<>();
        for (Integer columnIndex : this.filterIndexToObjectMap.keySet()) {
            final IDisplayConverter converter = this.configRegistry.getConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    DisplayMode.NORMAL,
                    FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);

            String filterText = getFilterStringRepresentation(columnIndex, converter);
            filterText = filterText.replace("|", PIPE_REPLACEMENT); //$NON-NLS-1$
            filterTextByIndex.put(columnIndex, filterText);
        }

        String string = PersistenceUtils.mapAsString(filterTextByIndex);

        if (!ObjectUtils.isEmpty(string)) {
            properties.put(prefix + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS, string);
        } else {
            // remove a possible existing filter state from the properties if
            // no filter state is set now
            properties.remove(prefix + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.filterIndexToObjectMap.clear();

        try {
            Object property = properties.get(prefix + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);
            Map<Integer, String> filterTextByIndex = PersistenceUtils.parseString(property);
            for (Integer columnIndex : filterTextByIndex.keySet()) {
                final IDisplayConverter converter = this.configRegistry.getConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + columnIndex);

                String filterText = filterTextByIndex.get(columnIndex);
                filterText = filterText.replace(PIPE_REPLACEMENT, "|"); //$NON-NLS-1$
                this.filterIndexToObjectMap.put(columnIndex, getFilterFromString(columnIndex, filterText, converter));
            }
        } catch (Exception e) {
            LOG.error("Error while restoring filter row text!", e); //$NON-NLS-1$
        }

        this.filterStrategy.applyFilter(this.filterIndexToObjectMap);

        this.columnHeaderLayer.fireLayerEvent(new FilterAppliedEvent(this.columnHeaderLayer));
    }

    /**
     * This method is used to support saving of a filter collection, e.g. in the
     * context of the Excel like filter row. In such cases the filter value is
     * not a simple String but a Collection of filter values that need to be
     * converted to a String representation. As the state persistence is
     * encapsulated to be handled here, we need to take care of such states here
     * also.
     *
     * @param columnIndex
     *            The column index of the filter value object that is used for
     *            filtering.
     * @param converter
     *            The converter that is used to convert the filter value, which
     *            is necessary to support filtering of custom types.
     * @return The String representation of the filter value.
     */
    private String getFilterStringRepresentation(int columnIndex, IDisplayConverter converter) {
        // in case the filter value is a collection of values, we need to create
        // a special string representation
        Object filterValue = this.filterIndexToObjectMap.get(columnIndex);
        if (filterValue instanceof Collection) {
            String collectionSpec = FILTER_COLLECTION_PREFIX + filterValue.getClass().getName() + ")"; //$NON-NLS-1$
            StringBuilder builder = new StringBuilder(collectionSpec);
            builder.append("["); //$NON-NLS-1$
            Collection<?> filterCollection = (Collection<?>) filterValue;

            if (this.filterRowComboBoxDataProvider == null) {
                for (Iterator<?> iterator = filterCollection.iterator(); iterator.hasNext();) {
                    Object filterObject = iterator.next();
                    String displayValue = (String) converter.canonicalToDisplayValue(
                            new LayerCell(null, columnIndex, 0),
                            this.configRegistry,
                            filterObject);
                    displayValue = displayValue.replace(IPersistable.VALUE_SEPARATOR, COMMA_REPLACEMENT);
                    builder.append(displayValue);
                    if (iterator.hasNext()) {
                        builder.append(IPersistable.VALUE_SEPARATOR);
                    }
                }
            } else {
                List<?> allValues = new ArrayList<>(this.filterRowComboBoxDataProvider.getAllValues(columnIndex));
                allValues.removeAll(filterCollection);

                for (Iterator<?> iterator = allValues.iterator(); iterator.hasNext();) {
                    Object filterObject = iterator.next();
                    if (filterObject == null) {
                        builder.append(NULL_REPLACEMENT);
                    } else {
                        String displayValue = (String) converter.canonicalToDisplayValue(
                                new LayerCell(null, columnIndex, 0),
                                this.configRegistry,
                                filterObject);
                        displayValue = displayValue.replace(IPersistable.VALUE_SEPARATOR, COMMA_REPLACEMENT);
                        if (displayValue.isEmpty()) {
                            builder.append(EMPTY_REPLACEMENT);
                        } else {
                            builder.append(displayValue);
                        }
                    }
                    if (iterator.hasNext()) {
                        builder.append(IPersistable.VALUE_SEPARATOR);
                    }
                }
            }

            builder.append("]"); //$NON-NLS-1$
            return builder.toString();
        }
        return (String) converter.canonicalToDisplayValue(
                new LayerCell(null, columnIndex, 0),
                this.configRegistry,
                filterValue);
    }

    /**
     * This method is used to support loading of a filter collection, e.g. in
     * the context of the Excel like filter row. In such cases the saved filter
     * value is not a simple String but represents a Collection of filter values
     * that need to be converted to the corresponding values. As the state
     * persistence is encapsulated to be handled here, we need to take care of
     * such states here also.
     *
     * @param columnIndex
     *            The column index for which the the applied filter was saved.
     * @param filterText
     *            The String representation of the applied saved filter.
     * @param converter
     *            The converter that is used to convert the filter value, which
     *            is necessary to support filtering of custom types.
     * @return The filter value that will be used to apply a filter to the
     *         IFilterStrategy
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object getFilterFromString(int columnIndex, String filterText, IDisplayConverter converter)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

        if (filterText.startsWith(FILTER_COLLECTION_PREFIX)) {
            // the filter text represents a collection
            int indexEndCollSpec = filterText.indexOf(")"); //$NON-NLS-1$
            String collectionSpec = filterText.substring(filterText.indexOf("(") + 1, indexEndCollSpec); //$NON-NLS-1$
            Collection filterCollection = (Collection) Class.forName(collectionSpec).getDeclaredConstructor().newInstance();

            // also get rid of the collection marks
            filterText = filterText.substring(indexEndCollSpec + 2, filterText.length() - 1);
            if (!filterText.isEmpty()) {
                String[] filterSplit = filterText.split(IPersistable.VALUE_SEPARATOR);
                for (String filterString : filterSplit) {
                    filterString = filterString.replace(COMMA_REPLACEMENT, IPersistable.VALUE_SEPARATOR);
                    if (NULL_REPLACEMENT.equals(filterString)) {
                        filterCollection.add(null);
                    } else if (EMPTY_REPLACEMENT.equals(filterString)) {
                        filterCollection.add(""); //$NON-NLS-1$
                    } else {
                        filterCollection.add(converter.displayToCanonicalValue(
                                new LayerCell(null, columnIndex, 0),
                                this.configRegistry,
                                filterString));
                    }
                }
            }

            if (this.filterRowComboBoxDataProvider != null) {
                if (filterCollection.isEmpty()) {
                    return EditConstants.SELECT_ALL_ITEMS_VALUE;
                }

                List<?> allValues = new ArrayList<>(this.filterRowComboBoxDataProvider.getAllValues(columnIndex));
                allValues.removeAll(filterCollection);
                return allValues;
            }

            return filterCollection;
        }
        return converter.displayToCanonicalValue(
                new LayerCell(null, columnIndex, 0),
                this.configRegistry,
                filterText);
    }

    /**
     * Clear all filters that are currently applied.
     */
    public void clearAllFilters() {
        this.filterIndexToObjectMap.clear();
        this.filterStrategy.applyFilter(this.filterIndexToObjectMap);

        this.columnHeaderLayer.fireLayerEvent(new FilterAppliedEvent(this.columnHeaderLayer, true));
    }

    /**
     *
     * @return The {@link IFilterStrategy} to which the set filter value should
     *         be applied.
     *
     * @since 2.1
     */
    public IFilterStrategy<T> getFilterStrategy() {
        return this.filterStrategy;
    }

    /**
     *
     * @param comboBoxDataProvider
     *            The {@link FilterRowComboBoxDataProvider} that should be used
     *            to support inverted persistence of filter collections. By
     *            default the values in the collection are persisted as is. In
     *            case of Excel like filters, it can be more feasible to store
     *            which values are NOT selected, to be able to load the filter
     *            even for different values in the filter list. Passing
     *            <code>null</code> will result in the default persistence.
     *
     * @since 2.1
     */
    public void setFilterRowComboBoxDataProvider(FilterRowComboBoxDataProvider<T> comboBoxDataProvider) {
        this.filterRowComboBoxDataProvider = comboBoxDataProvider;
    }

}
