/*******************************************************************************
 * Copyright (c) 2013, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterUtils;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboUpdateEvent;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.IFilterRowComboUpdateListener;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeGridCommand;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;

/**
 * CompositeLayer that is build out of the columnheader layer stack and a filter
 * row layer. The column header layer stack needs to be provided at creation
 * time while the filter row layer is created in here.
 * <p>
 * The special about this ComboBoxFilterRowHeaderComposite is that it creates a
 * filter row that behaves and looks like the Excel filter. So it doesn't
 * provide text fields for free filtering but adds comboboxes for all columns
 * that contain all available values for that column that can be de-/selected
 * via checkboxes.
 */
public class ComboBoxFilterRowHeaderComposite<T> extends CompositeLayer implements IFilterRowComboUpdateListener {

    /**
     * The FilterRowDataLayer which serves as the filter row layer.
     */
    protected final FilterRowDataLayer<T> filterRowDataLayer;
    /**
     * Flag that allows to set the filter row visible or invisible.
     */
    protected boolean filterRowVisible = true;
    /**
     * The IComboBoxDataProvider that is used to fill the filter row comboboxes.
     */
    protected final FilterRowComboBoxDataProvider<T> comboBoxDataProvider;
    /**
     * The IFilterStrategy that is used by this
     * ComboBoxFilterRowHeaderComposite.
     */
    protected final ComboBoxGlazedListsFilterStrategy<T> filterStrategy;

    /**
     * The {@link IConfigRegistry} needed to retrieve various configurations,
     * e.g. the filter cell editor per column.
     */
    private IConfigRegistry configRegistry;

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Using this constructor will create the
     * FilterRowComboBoxDataProvider needed for filtering and add the default
     * ComboBoxFilterRowConfiguration.
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed for
     *            creation of the FilterRowComboBoxDataProvider.
     * @param baseCollection
     *            The base collection that is used to fill the body. Needed to
     *            determine the values to show in the filter comboboxes and
     *            initially pre-select them.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> bodyDataColumnAccessor,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry) {

        this(filterList,
                bodyLayer,
                baseCollection,
                bodyDataColumnAccessor,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                true);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Using this constructor will create the
     * FilterRowComboBoxDataProvider needed for filtering .
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed for
     *            creation of the FilterRowComboBoxDataProvider.
     * @param baseCollection
     *            The base collection that is used to fill the body. Needed to
     *            determine the values to show in the filter comboboxes and
     *            initially pre-select them.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> bodyDataColumnAccessor,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(filterList,
                new GlazedListsFilterRowComboBoxDataProvider<>(
                        bodyLayer,
                        baseCollection,
                        bodyDataColumnAccessor),
                bodyDataColumnAccessor,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Using this constructor will create the
     * FilterRowComboBoxDataProvider needed for filtering .
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed for
     *            creation of the FilterRowComboBoxDataProvider.
     * @param baseCollection
     *            The base collection that is used to fill the body. Needed to
     *            determine the values to show in the filter comboboxes and
     *            initially pre-select them.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param lazyLoading
     *            Flag to configure whether the content of the comboboxes should
     *            be loaded lazily or not. Default is <code>true</code>
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> bodyDataColumnAccessor,
            boolean lazyLoading,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(filterList,
                new GlazedListsFilterRowComboBoxDataProvider<>(
                        bodyLayer,
                        baseCollection,
                        bodyDataColumnAccessor,
                        lazyLoading),
                bodyDataColumnAccessor,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Using this constructor will create the
     * FilterRowComboBoxDataProvider needed for filtering .
     * <p>
     * <b>Note:</b>This constructor should only be used in cases where it is
     * absolutely necessary to use a custom CompositeMatcherEditor. This could
     * be for example to create a complex NatTable instance where several filter
     * rows need to be combined.
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param matcherEditor
     *            The CompositeMatcherEditor that is set to the FilterList and
     *            needs to be used by the ComboBoxGlazedListsFilterStrategy to
     *            apply the filters via filter row.
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed for
     *            creation of the FilterRowComboBoxDataProvider.
     * @param baseCollection
     *            The base collection that is used to fill the body. Needed to
     *            determine the values to show in the filter comboboxes and
     *            initially pre-select them.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            CompositeMatcherEditor<T> matcherEditor,
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> bodyDataColumnAccessor,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(filterList,
                matcherEditor,
                new GlazedListsFilterRowComboBoxDataProvider<>(
                        bodyLayer,
                        baseCollection,
                        bodyDataColumnAccessor),
                bodyDataColumnAccessor,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Using this constructor will create the
     * FilterRowComboBoxDataProvider needed for filtering .
     * <p>
     * <b>Note:</b>This constructor should only be used in cases where it is
     * absolutely necessary to use a custom CompositeMatcherEditor. This could
     * be for example to create a complex NatTable instance where several filter
     * rows need to be combined.
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param matcherEditor
     *            The CompositeMatcherEditor that is set to the FilterList and
     *            needs to be used by the ComboBoxGlazedListsFilterStrategy to
     *            apply the filters via filter row.
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed for
     *            creation of the FilterRowComboBoxDataProvider.
     * @param baseCollection
     *            The base collection that is used to fill the body. Needed to
     *            determine the values to show in the filter comboboxes and
     *            initially pre-select them.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param lazyLoading
     *            Flag to configure whether the content of the comboboxes should
     *            be loaded lazily or not. Default is <code>true</code>
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            CompositeMatcherEditor<T> matcherEditor,
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> bodyDataColumnAccessor,
            boolean lazyLoading,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(filterList,
                matcherEditor,
                new GlazedListsFilterRowComboBoxDataProvider<>(
                        bodyLayer,
                        baseCollection,
                        bodyDataColumnAccessor,
                        lazyLoading),
                bodyDataColumnAccessor,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Using this constructor will add the default
     * ComboBoxFilterRowConfiguration.
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider that should be used to fill
     *            the filter comboboxes.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            IColumnAccessor<T> bodyDataColumnAccessor,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry) {

        this(filterList,
                comboBoxDataProvider,
                bodyDataColumnAccessor,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                true);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Will use the given FilterList for creating the
     * ComboBoxGlazedListsFilterStrategy and the given
     * FilterRowComboBoxDataProvider instead of creating a new one.
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider that should be used to fill
     *            the filter comboboxes.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            IColumnAccessor<T> bodyDataColumnAccessor,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(
                new ComboBoxGlazedListsFilterStrategy<>(
                        comboBoxDataProvider,
                        filterList,
                        bodyDataColumnAccessor,
                        configRegistry),
                comboBoxDataProvider,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Will use the given FilterList and MatcherEditor for
     * creating the ComboBoxGlazedListsFilterStrategy and the given
     * FilterRowComboBoxDataProvider instead of creating a new one.
     * <p>
     * <b>Note:</b>This constructor should only be used in cases where it is
     * absolutely necessary to use a custom CompositeMatcherEditor. This could
     * be for example to create a complex NatTable instance where several filter
     * rows need to be combined.
     *
     * @param filterList
     *            The FilterList that will be used for filtering.
     * @param matcherEditor
     *            The CompositeMatcherEditor that is set to the FilterList and
     *            needs to be used by the ComboBoxGlazedListsFilterStrategy to
     *            apply the filters via filter row.
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider that should be used to fill
     *            the filter comboboxes.
     * @param bodyDataColumnAccessor
     *            The IColumnAccessor that is needed by the IFilterStrategy to
     *            perform filtering.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            FilterList<T> filterList,
            CompositeMatcherEditor<T> matcherEditor,
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            IColumnAccessor<T> bodyDataColumnAccessor,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(
                new ComboBoxGlazedListsFilterStrategy<>(
                        comboBoxDataProvider,
                        filterList,
                        matcherEditor,
                        bodyDataColumnAccessor,
                        configRegistry),
                comboBoxDataProvider,
                columnHeaderLayer,
                columnHeaderDataProvider,
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Will use the given ComboBoxGlazedListsFilterStrategy
     * instead of creating a new one.
     *
     * @param filterStrategy
     *            The ComboBoxGlazedListsFilterStrategy that should be used for
     *            filtering.
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider that should be used to fill
     *            the filter comboboxes.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     */
    public ComboBoxFilterRowHeaderComposite(
            ComboBoxGlazedListsFilterStrategy<T> filterStrategy,
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        this(
                filterStrategy,
                comboBoxDataProvider,
                columnHeaderLayer,
                new FilterRowDataLayer<>(
                        filterStrategy,
                        columnHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry),
                configRegistry,
                useDefaultConfiguration);
    }

    /**
     * Creates a new ComboBoxFilterRowHeaderComposite based on the given
     * informations. Will use the given ComboBoxGlazedListsFilterStrategy
     * instead of creating a new one.
     *
     * @param filterStrategy
     *            The ComboBoxGlazedListsFilterStrategy that should be used for
     *            filtering.
     * @param comboBoxDataProvider
     *            The FilterRowComboBoxDataProvider that should be used to fill
     *            the filter comboboxes.
     * @param columnHeaderLayer
     *            The columnheader layer the filter row layer is related to.
     *            Needed for building this CompositeLayer, dimensionally connect
     *            the filter row to and retrieve information and perform actions
     *            related to filtering.
     * @param filterRowDataLayer
     *            The {@link FilterRowDataLayer} which is used for backing the
     *            filter data and is rendered in the second row of this
     *            {@link CompositeLayer}.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve various
     *            configurations.
     * @param useDefaultConfiguration
     *            Tell whether the default configuration should be used or not.
     *            If not you need to ensure to add a configuration that adds at
     *            least the needed configuration specified in
     *            ComboBoxFilterRowConfiguration
     * @since 2.3
     */
    public ComboBoxFilterRowHeaderComposite(
            ComboBoxGlazedListsFilterStrategy<T> filterStrategy,
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            ILayer columnHeaderLayer,
            FilterRowDataLayer<T> filterRowDataLayer,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        super(1, 2);

        this.filterStrategy = filterStrategy;

        this.comboBoxDataProvider = comboBoxDataProvider;
        this.comboBoxDataProvider.addCacheUpdateListener(this);

        this.filterRowDataLayer = filterRowDataLayer;

        this.configRegistry = configRegistry;

        setAllValuesSelected();

        setChildLayer("columnHeader", columnHeaderLayer, 0, 0); //$NON-NLS-1$
        setChildLayer(GridRegion.FILTER_ROW, this.filterRowDataLayer, 0, 1);

        if (useDefaultConfiguration) {
            addConfiguration(new ComboBoxFilterRowConfiguration(this.comboBoxDataProvider));
        }
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        // we need to cleanup SELECT_ALL values for the mixed filter row case,
        // because the SELECT_ALL value would be an invalid value for non-string
        // filters, e.g. a Date filter. The filter editor check can only be
        // performed AFTER the configuration was applied, so an issue could
        // occur for example if the state is saved after configure() was called.
        FilterRowDataProvider<T> dataProvider = this.filterRowDataLayer.getFilterRowDataProvider();
        for (int i = 0; i < dataProvider.getColumnCount(); i++) {
            if (!ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, i)
                    && EditConstants.SELECT_ALL_ITEMS_VALUE.equals(dataProvider.getFilterIndexToObjectMap().get(Integer.valueOf(i)))) {
                this.filterRowDataLayer.getFilterRowDataProvider().getFilterIndexToObjectMap().remove(i);
            }
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        boolean wasEnabled = this.comboBoxDataProvider.isUpdateEventsEnabled();
        this.comboBoxDataProvider.disableUpdateEvents();
        super.loadState(prefix, properties);
        if (wasEnabled) {
            this.comboBoxDataProvider.enableUpdateEvents();
        }
    }

    /**
     * @return The FilterRowDataLayer which serves as the filter row layer.
     */
    public FilterRowDataLayer<T> getFilterRowDataLayer() {
        return this.filterRowDataLayer;
    }

    /**
     * @return The CompositeMatcherEditor that is used for filtering. As this
     *         one is filled with MatcherEditors by the IFilterStrategy, direct
     *         modifications may be overridden on changing cell values in the
     *         filter row.
     */
    public CompositeMatcherEditor<T> getMatcherEditor() {
        return this.filterStrategy.getMatcherEditor();
    }

    /**
     * Returns the IFilterStrategy that is used by this
     * ComboBoxFilterRowHeaderComposite. This is needed to integrate static
     * filters, e.g. using GlazedListsRowHideShowLayer together with this
     * ComboBoxFilterRowHeaderComposite by adding the MatcherEditor as static
     * filter to the ComboBoxGlazedListsFilterStrategy.
     *
     * @return The IFilterStrategy that is used by this
     *         ComboBoxFilterRowHeaderComposite.
     */
    public ComboBoxGlazedListsFilterStrategy<T> getFilterStrategy() {
        return this.filterStrategy;
    }

    /**
     * @return The IComboBoxDataProvider that is used to fill the filter row
     *         comboboxes.
     */
    public FilterRowComboBoxDataProvider<T> getComboBoxDataProvider() {
        return this.comboBoxDataProvider;
    }

    /**
     * @return <code>true</code> if the filter row is visible,
     *         <code>false</code> if not.
     */
    public boolean isFilterRowVisible() {
        return this.filterRowVisible;
    }

    /**
     * Sets the visibility state of the filter row.
     *
     * @param filterRowVisible
     *            <code>true</code> to set the filter row visible,
     *            <code>false</code> to hide it.
     */
    public void setFilterRowVisible(boolean filterRowVisible) {
        this.filterRowVisible = filterRowVisible;
        fireLayerEvent(new RowStructuralRefreshEvent(this.filterRowDataLayer));
    }

    /**
     * Sets all values for all comboboxes as selected. This is needed because
     * selecting all items in the comboboxes mean to have no filter applied.
     * <p>
     * Simply applies the SELECT_ALL value to all columns, irrespective of the
     * configured filter editor.
     * <p>
     * Note: The filter row IDataProvider is filled by modifying the Map of
     * filter objects directly instead of calling setDataValue(). This is
     * because calling setDataValue() will transform indexes which is not
     * necessary on initialization and causes strange behaviour in some
     * compositions.
     */
    public void setAllValuesSelected() {
        setAllValuesSelected(false);
    }

    /**
     * Sets all values for all comboboxes as selected. This is needed because
     * selecting all items in the comboboxes mean to have no filter applied.
     * <p>
     * Note: The filter row IDataProvider is filled by modifying the Map of
     * filter objects directly instead of calling setDataValue(). This is
     * because calling setDataValue() will transform indexes which is not
     * necessary on initialization and causes strange behaviour in some
     * compositions.
     *
     * @param performEditorCheck
     *            Whether it should be checked if the filter editor is a
     *            combobox editor or not. If set to <code>false</code> the
     *            SELECT_ALL value will be applied for all columns, otherwise
     *            the value will be only applied for columns that have a
     *            combobox filter editor configured.
     *
     * @since 2.2
     */
    public void setAllValuesSelected(boolean performEditorCheck) {
        FilterRowDataProvider<T> dataProvider = this.filterRowDataLayer.getFilterRowDataProvider();
        dataProvider.getFilterIndexToObjectMap().clear();
        for (int i = 0; i < dataProvider.getColumnCount(); i++) {
            boolean setAllSelected = performEditorCheck
                    ? ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, i)
                    : true;

            if (setAllSelected) {
                dataProvider.getFilterIndexToObjectMap().put(i, EditConstants.SELECT_ALL_ITEMS_VALUE);
            }
        }
        getFilterStrategy().applyFilter(dataProvider.getFilterIndexToObjectMap());
    }

    @Override
    public Object getDataValueByPosition(int compositeColumnPosition, int compositeRowPosition) {
        // special handling only if the filter row is visible and the value of
        // the filter row is requested
        if (this.filterRowVisible && compositeRowPosition == getRowCount() - 1) {
            Object filterValue = super.getDataValueByPosition(compositeColumnPosition, compositeRowPosition);

            // The SELECT_ALL_ITEMS_VALUE is set to the FilterRowDataProvider by
            // setAllValueSelected(). It is actually ignored in the
            // ComboBoxGlazedListsFilterStrategy as it technically means "no
            // filter" and results in having all entries checked in the
            // FilterRowComboBoxCellEditor. For other filter editors this
            // replacement is not done and causes incorrect visualization as it
            // shows the value and implies an active filter. As the
            // SELECT_ALL_ITEMS_VALUE is propagated in the constructor at a time
            // where the editors are not yet configured, we remove the value
            // reactively on accessing it for a consistent view.

            int columnIndex = getColumnIndexByPosition(compositeColumnPosition);
            if (!ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, columnIndex)
                    && EditConstants.SELECT_ALL_ITEMS_VALUE.equals(filterValue)) {
                this.filterRowDataLayer.getFilterRowDataProvider().getFilterIndexToObjectMap().remove(columnIndex);
                filterValue = null;
            }

            return filterValue;
        }

        return super.getDataValueByPosition(compositeColumnPosition, compositeRowPosition);
    }

    @Override
    public int getHeight() {
        if (this.filterRowVisible) {
            return super.getHeight();
        } else {
            return getHeightOffset(1);
        }
    }

    @Override
    public int getRowCount() {
        if (this.filterRowVisible) {
            return super.getRowCount();
        } else {
            return super.getRowCount() - 1;
        }
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        boolean handled = false;
        if (command instanceof ToggleFilterRowCommand) {
            setFilterRowVisible(!this.filterRowVisible);
            return true;
        }
        // as clearing the filter means to select all items in the combo, we
        // need to handle clear filter commands in here instead of delegating it
        // to the FilterRowDataLayer
        else if (command instanceof ClearFilterCommand
                && command.convertToTargetLayer(this)) {
            int columnPosition = ((ClearFilterCommand) command).getColumnPosition();
            int columnIndex = getColumnIndexByPosition(columnPosition);
            if (ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, columnIndex)) {
                this.filterRowDataLayer.setDataValueByPosition(
                        columnPosition,
                        0,
                        EditConstants.SELECT_ALL_ITEMS_VALUE);
                handled = true;
            }
        } else if (command instanceof ClearAllFiltersCommand) {
            setAllValuesSelected(true);
            ILayer childLayer = getChildLayerByLayoutCoordinate(0, 0);
            childLayer.fireLayerEvent(new FilterAppliedEvent(childLayer, true));
            handled = true;
        } else if (command instanceof InitializeGridCommand) {
            setAllValuesSelected(true);
        } else if (command instanceof DisposeResourcesCommand) {
            this.comboBoxDataProvider.dispose();
        }

        if (handled) {
            fireLayerEvent(new RowStructuralRefreshEvent(this));
            return true;
        } else {
            return super.doCommand(command);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void handleEvent(FilterRowComboUpdateEvent event) {
        Map<Integer, Object> filterIndexToObjectMap =
                this.filterRowDataLayer.getFilterRowDataProvider().getFilterIndexToObjectMap();

        for (int i = 0; i < event.updateContentSize(); i++) {
            Object filterObject = filterIndexToObjectMap.get(event.getColumnIndex(i));
            if (filterObject instanceof Collection) {
                Collection filterCollection = (Collection) filterObject;
                // if a new value was added than ensure it is also added to the
                // filter
                if (event.getAddedItems(i) != null
                        && !event.getAddedItems(i).isEmpty()) {
                    // as the filter collection is a list, we need to ensure
                    // that no double values are added
                    HashSet itemsToAdd = new HashSet(event.getAddedItems(i));
                    itemsToAdd.removeAll(new HashSet(filterCollection));

                    filterCollection.addAll(itemsToAdd);
                }
                // if a value was removed than ensure it is also removed from
                // the filter
                if (event.getRemovedItems(i) != null
                        && !event.getRemovedItems(i).isEmpty()) {
                    filterCollection.removeAll(event.getRemovedItems(i));
                }
            }
        }

        // apply the filter to be sure filter row and applied filter are the
        // same
        getFilterStrategy().applyFilter(
                this.filterRowDataLayer.getFilterRowDataProvider().getFilterIndexToObjectMap());

        // If multiple columns were updated at once, the trigger was a general
        // list change event, or structural change event. In this case we fire a
        // generic FilterAppliedEvent to trigger an update of the filter
        // comboboxes
        if (event.updateContentSize() > 1) {
            fireLayerEvent(new FilterAppliedEvent(this));
        }
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof FilterAppliedEvent) {
            // add combobox related informations to the event
            FilterAppliedEvent filterEvent = (FilterAppliedEvent) event;
            int columnIndex = filterEvent.getColumnIndex();
            if (columnIndex >= 0 && ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(this.configRegistry, columnIndex)) {
                Map<Integer, Object> filterIndexToObjectMap =
                        this.filterRowDataLayer.getFilterRowDataProvider().getFilterIndexToObjectMap();
                Object filterObject = filterIndexToObjectMap.get(columnIndex);
                filterEvent.setFilterComboEditor(true);
                filterEvent.setCleared(ComboBoxFilterUtils.isAllSelected(columnIndex, filterObject, this.comboBoxDataProvider));
            } else if (columnIndex == -1 && filterEvent.isCleared() && this.filterStrategy.hasComboBoxFilterEditorRegistered()) {
                // if all filters are cleared and there is at least one combobox
                // filter
                filterEvent.setFilterComboEditor(true);
            }
        }
        super.handleLayerEvent(event);
    }

}
