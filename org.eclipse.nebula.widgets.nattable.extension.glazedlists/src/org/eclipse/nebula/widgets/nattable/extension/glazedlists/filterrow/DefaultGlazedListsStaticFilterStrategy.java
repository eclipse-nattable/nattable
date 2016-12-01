/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * Default implementation of an {@link IFilterStrategy} for the filter row which
 * can also take static filters and combine them with the filter logic from the
 * filter row.
 *
 * @param <T>
 *            the type of the objects shown within the NatTable
 */
public class DefaultGlazedListsStaticFilterStrategy<T> extends DefaultGlazedListsFilterStrategy<T> {

    protected Map<Matcher<T>, MatcherEditor<T>> staticMatcherEditor = new HashMap<Matcher<T>, MatcherEditor<T>>();

    /**
     * Create a new DefaultGlazedListsStaticFilterStrategy on top of the given
     * FilterList.
     * <p>
     * Note: Using this constructor you don't need to create and set the
     * CompositeMatcherEditor as MatcherEditor on the FilterList yourself! The
     * necessary steps to get it working is done within this constructor.
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
    public DefaultGlazedListsStaticFilterStrategy(FilterList<T> filterList,
            IColumnAccessor<T> columnAccessor, IConfigRegistry configRegistry) {
        super(filterList, columnAccessor, configRegistry);
    }

    /**
     * Create a new DefaultGlazedListsStaticFilterStrategy on top of the given
     * FilterList using the given CompositeMatcherEditor. This is necessary to
     * support connection of multiple filter rows.
     * <p>
     * Note: Using this constructor you need to create the
     * CompositeMatcherEditor yourself. It will be added automatically to the
     * given FilterList, so you can skip that step.
     *
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
    public DefaultGlazedListsStaticFilterStrategy(FilterList<T> filterList,
            CompositeMatcherEditor<T> matcherEditor,
            IColumnAccessor<T> columnAccessor, IConfigRegistry configRegistry) {
        super(filterList, matcherEditor, columnAccessor, configRegistry);
    }

    /**
     * {@inheritDoc} Always adds the static matchers.
     */
    @Override
    public void applyFilter(Map<Integer, Object> filterIndexToObjectMap) {
        super.applyFilter(filterIndexToObjectMap);
        this.getMatcherEditor().getMatcherEditors().addAll(this.staticMatcherEditor.values());
    }

    /**
     * Add a static filter to this filter strategy which will always be applied
     * additionally to any other filter.
     *
     * @param matcher
     *            the static filter to add
     */
    public void addStaticFilter(final Matcher<T> matcher) {
        // create a new MatcherEditor
        MatcherEditor<T> matcherEditor = new AbstractMatcherEditor<T>() {
            {
                fireChanged(matcher);
            }
        };

        addStaticFilter(matcherEditor);
    }

    /**
     * Add a static filter to this filter strategy which will always be applied
     * additionally to any other filter.
     *
     * @param matcherEditor
     *            the static filter to add
     */
    public void addStaticFilter(final MatcherEditor<T> matcherEditor) {
        // add the new MatcherEditor to the CompositeMatcherEditor
        this.getMatcherEditor().getMatcherEditors().add(matcherEditor);

        // remember the MatcherEditor so it can be restored after new
        // MatcherEditors are added by the FilterRow
        this.staticMatcherEditor.put(matcherEditor.getMatcher(), matcherEditor);
    }

    /**
     * Remove the static filter from this filter strategy.
     *
     * @param matcher
     *            the filter to remove
     */
    public void removeStaticFilter(final Matcher<T> matcher) {
        MatcherEditor<T> removed = this.staticMatcherEditor.remove(matcher);
        if (removed != null) {
            this.getMatcherEditor().getMatcherEditors().remove(removed);
        }
    }

    /**
     * Remove the static filter from this filter strategy.
     *
     * @param matcherEditor
     *            the filter to remove
     */
    public void removeStaticFilter(final MatcherEditor<T> matcherEditor) {
        removeStaticFilter(matcherEditor.getMatcher());
    }

    /**
     * Removes all applied static filters from this filter strategy.
     *
     * @since 1.5
     */
    public void clearStaticFilter() {
        Collection<MatcherEditor<T>> staticMatcher = this.staticMatcherEditor.values();
        if (!staticMatcher.isEmpty()) {
            this.getMatcherEditor().getMatcherEditors().removeAll(staticMatcher);
            this.staticMatcherEditor.clear();
        }
    }
}
