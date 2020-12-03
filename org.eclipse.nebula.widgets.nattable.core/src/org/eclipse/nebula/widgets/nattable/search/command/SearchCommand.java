/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.search.command;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.search.SearchDirection;
import org.eclipse.nebula.widgets.nattable.search.strategy.ISearchStrategy;

public class SearchCommand implements ILayerCommand {

    private ILayer context;
    private final ISearchStrategy searchStrategy;
    private final String searchText;
    private final boolean isWrapSearch;
    private final boolean isCaseSensitive;
    private final boolean isWholeWord;
    private final boolean isIncremental;
    private final boolean isRegex;
    private final boolean isIncludeCollapsed;
    private final SearchDirection searchDirection;
    private final Comparator<?> comparator;
    private ILayerListener searchEventListener;

    /**
     *
     * @param layer
     *            The layer to search for the cell with the provided text.
     *            Typically the SelectionLayer.
     * @param searchStrategy
     *            The search strategy to perform
     * @param searchDirection
     *            The search direction.
     * @param isWrapSearch
     *            is search wrap enabled
     * @param isCaseSensitive
     *            is search case sensitive
     * @param isWholeWord
     *            only search whole words
     * @param isIncremental
     *            is search incremental
     * @param isRegex
     *            is search based on regular expressions
     * @param isIncludeCollapsed
     *            is search including collapsed nodes
     * @param comparator
     *            the comparator to use
     * @deprecated Use constructor with {@link SearchDirection} parameter and
     *             text to search for
     */
    @Deprecated
    public SearchCommand(ILayer layer, ISearchStrategy searchStrategy,
            String searchDirection, boolean isWrapSearch,
            boolean isCaseSensitive, boolean isWholeWord,
            boolean isIncremental, boolean isRegex, boolean isIncludeCollapsed,
            Comparator<?> comparator) {
        this(null, layer, searchStrategy, searchDirection, isWrapSearch,
                isCaseSensitive, isWholeWord, isIncremental, isRegex,
                isIncludeCollapsed, comparator);
    }

    /**
     *
     * @param searchText
     *            The text to search.
     * @param layer
     *            The layer to search for the cell with the provided text.
     *            Typically the SelectionLayer.
     * @param searchStrategy
     *            The search strategy to perform
     * @param searchDirection
     *            The search direction.
     * @param isWrapSearch
     *            is search wrap enabled
     * @param isCaseSensitive
     *            is search case sensitive
     * @param isWholeWord
     *            only search whole words
     * @param isIncremental
     *            is search incremental
     * @param isRegex
     *            is search based on regular expressions
     * @param isIncludeCollapsed
     *            is search including collapsed nodes
     * @param comparator
     *            the comparator to use
     * @deprecated Use constructor with {@link SearchDirection} parameter
     */
    @Deprecated
    public SearchCommand(String searchText, ILayer layer,
            ISearchStrategy searchStrategy, String searchDirection,
            boolean isWrapSearch, boolean isCaseSensitive, boolean isWholeWord,
            boolean isIncremental, boolean isRegex, boolean isIncludeCollapsed,
            Comparator<?> comparator) {
        this(searchText, layer,
                searchStrategy, SearchDirection.valueOf(searchDirection),
                isWrapSearch, isCaseSensitive, isWholeWord,
                isIncremental, isRegex, isIncludeCollapsed,
                comparator);
    }

    /**
     *
     * @param searchText
     *            The text to search.
     * @param layer
     *            The layer to search for the cell with the provided text.
     *            Typically the SelectionLayer.
     * @param searchStrategy
     *            The search strategy to perform
     * @param searchDirection
     *            The search direction.
     * @param isWrapSearch
     *            is search wrap enabled
     * @param isCaseSensitive
     *            is search case sensitive
     * @param isWholeWord
     *            only search whole words
     * @param isIncremental
     *            is search incremental
     * @param isRegex
     *            is search based on regular expressions
     * @param isIncludeCollapsed
     *            is search including collapsed nodes
     * @param comparator
     *            the comparator to use
     * @since 2.0
     */
    public SearchCommand(String searchText, ILayer layer,
            ISearchStrategy searchStrategy, SearchDirection searchDirection,
            boolean isWrapSearch, boolean isCaseSensitive, boolean isWholeWord,
            boolean isIncremental, boolean isRegex, boolean isIncludeCollapsed,
            Comparator<?> comparator) {
        this.searchText = searchText;
        this.context = layer;
        this.searchStrategy = searchStrategy;
        this.searchDirection = searchDirection;
        this.isWrapSearch = isWrapSearch;
        this.isCaseSensitive = isCaseSensitive;
        this.isWholeWord = isWholeWord;
        this.isIncremental = isIncremental;
        this.isRegex = isRegex;
        this.isIncludeCollapsed = isIncludeCollapsed;
        this.comparator = comparator;
    }

    protected SearchCommand(SearchCommand command) {
        this(command.searchText, command.context, command.searchStrategy,
                command.searchDirection, command.isWrapSearch,
                command.isCaseSensitive, command.isWholeWord,
                command.isIncremental, command.isRegex,
                command.isIncludeCollapsed, command.comparator);
        this.searchEventListener = command.searchEventListener;
    }

    public ILayer getContext() {
        return this.context;
    }

    public ISearchStrategy getSearchStrategy() {
        return this.searchStrategy;
    }

    public String getSearchText() {
        return this.searchText;
    }

    /**
     *
     * @return the search direction.
     * @since 2.0
     */
    public SearchDirection getSearchDirection() {
        return this.searchDirection;
    }

    public boolean isWrapSearch() {
        return this.isWrapSearch;
    }

    public boolean isCaseSensitive() {
        return this.isCaseSensitive;
    }

    public boolean isWholeWord() {
        return this.isWholeWord;
    }

    public boolean isIncremental() {
        return this.isIncremental;
    }

    public boolean isIncludeCollapsed() {
        return this.isIncludeCollapsed;
    }

    public boolean isRegex() {
        return this.isRegex;
    }

    public ILayerListener getSearchEventListener() {
        return this.searchEventListener;
    }

    public void setSearchEventListener(ILayerListener listener) {
        this.searchEventListener = listener;
    }

    public Comparator<?> getComparator() {
        return this.comparator;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        this.context = targetLayer;
        return true;
    }

    @Override
    public SearchCommand cloneCommand() {
        return new SearchCommand(this);
    }

}
