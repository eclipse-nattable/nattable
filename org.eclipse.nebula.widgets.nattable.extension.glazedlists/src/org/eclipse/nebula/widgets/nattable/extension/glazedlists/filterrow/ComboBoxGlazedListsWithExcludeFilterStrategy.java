/*******************************************************************************
 * Copyright (c) 2023 Dirk Fauth and others.
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * Specialized {@link ComboBoxGlazedListsFilterStrategy} that can be used to
 * exclude items from filtering. This means you can register a {@link Matcher}
 * that avoids that matching items get filtered by the filter row.
 *
 * @param <T>
 *
 * @since 2.1
 */
public class ComboBoxGlazedListsWithExcludeFilterStrategy<T> extends ComboBoxGlazedListsFilterStrategy<T> {

    private final CompositeMatcherEditor<T> compositeMatcherEditor;

    protected Map<Matcher<T>, MatcherEditor<T>> excludeMatcherEditor = new HashMap<>();

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
    public ComboBoxGlazedListsWithExcludeFilterStrategy(
            FilterRowComboBoxDataProvider<T> comboBoxDataProvider,
            FilterList<T> filterList,
            IColumnAccessor<T> columnAccessor,
            IConfigRegistry configRegistry) {
        super(comboBoxDataProvider, filterList, columnAccessor, configRegistry);

        // The default MatcherEditor is created and stored as member in the
        // DefaultGlazedListsFilterStrategy. That MatcherEditor is used for
        // the default filter operations. To exclude entries from filtering,
        // we create another CompositeMatcherEditor with an OR mode and set
        // that one on the FilterList.
        // Note that we need both, the default MatcherEditor from the
        // DefaultGlazedListsFilterStrategy for the user filters that are
        // combined with AND mode, and the MatcherEditor with OR mode that
        // combines the user filters with the exclude MatcherEditors registered
        // via this strategy.
        this.compositeMatcherEditor = new CompositeMatcherEditor<>();
        this.compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);

        this.compositeMatcherEditor.getMatcherEditors().add(getMatcherEditor());

        this.filterList.setMatcherEditor(this.compositeMatcherEditor);
    }

    /**
     * Add a exclude filter to this filter strategy which will always be applied
     * additionally to any other filter to exclude from filtering.
     *
     * @param matcher
     *            the exclude filter to add
     */
    public void addExcludeFilter(final Matcher<T> matcher) {
        // create a new MatcherEditor
        MatcherEditor<T> matcherEditor = GlazedLists.fixedMatcherEditor(matcher);
        addExcludeFilter(matcherEditor);
    }

    /**
     * Add a exclude filter to this filter strategy which will always be applied
     * additionally to any other filter to exclude items from filtering.
     *
     * @param matcherEditor
     *            the exclude filter to add
     */
    public void addExcludeFilter(final MatcherEditor<T> matcherEditor) {
        // add the new MatcherEditor to the CompositeMatcherEditor
        if (isActive()) {
            this.filterLock.writeLock().lock();
            try {
                this.compositeMatcherEditor.getMatcherEditors().add(matcherEditor);
            } finally {
                this.filterLock.writeLock().unlock();
            }
        }

        this.excludeMatcherEditor.put(matcherEditor.getMatcher(), matcherEditor);
    }

    /**
     * Remove the exclude filter from this filter strategy.
     *
     * @param matcher
     *            the filter to remove
     */
    public void removeExcludeFilter(final Matcher<T> matcher) {
        MatcherEditor<T> removed = this.excludeMatcherEditor.remove(matcher);
        if (removed != null && isActive()) {
            this.filterLock.writeLock().lock();
            try {
                this.compositeMatcherEditor.getMatcherEditors().remove(removed);
            } finally {
                this.filterLock.writeLock().unlock();
            }
        }
    }

    /**
     * Remove the exclude filter from this filter strategy.
     *
     * @param matcherEditor
     *            the filter to remove
     */
    public void removeExcludeFilter(final MatcherEditor<T> matcherEditor) {
        removeExcludeFilter(matcherEditor.getMatcher());
    }

    /**
     * Removes all applied exclude filters from this filter strategy.
     */
    public void clearExcludeFilter() {
        Collection<MatcherEditor<T>> excludeMatcher = this.excludeMatcherEditor.values();
        if (!excludeMatcher.isEmpty() && isActive()) {
            this.filterLock.writeLock().lock();
            try {
                this.compositeMatcherEditor.getMatcherEditors().removeAll(excludeMatcher);
            } finally {
                this.filterLock.writeLock().unlock();
            }
        }
        this.excludeMatcherEditor.clear();
    }

    @Override
    public void activateFilterStrategy() {
        if (!isActive()) {
            Collection<MatcherEditor<T>> excludeMatcher = this.excludeMatcherEditor.values();
            if (!excludeMatcher.isEmpty()) {
                this.filterLock.writeLock().lock();
                try {
                    this.compositeMatcherEditor.getMatcherEditors().addAll(excludeMatcher);
                } finally {
                    this.filterLock.writeLock().unlock();
                }
            }
        }
        super.activateFilterStrategy();
    }

    @Override
    public void deactivateFilterStrategy() {
        if (isActive()) {
            Collection<MatcherEditor<T>> excludeMatcher = this.excludeMatcherEditor.values();
            if (!excludeMatcher.isEmpty()) {
                this.filterLock.writeLock().lock();
                try {
                    this.compositeMatcherEditor.getMatcherEditors().removeAll(excludeMatcher);
                } finally {
                    this.filterLock.writeLock().unlock();
                }
            }
        }
        super.deactivateFilterStrategy();
    }
}