/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.command;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
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
    private final String searchDirection;
    private final Comparator<?> comparator;
    private ILayerListener searchEventListener;

    public SearchCommand(ILayer layer, ISearchStrategy searchStrategy,
            String searchDirection, boolean isWrapSearch,
            boolean isCaseSensitive, boolean isWholeWord,
            boolean isIncremental, boolean isRegex, boolean isIncludeCollapsed,
            Comparator<?> comparator) {
        this(null, layer, searchStrategy, searchDirection, isWrapSearch,
                isCaseSensitive, isWholeWord, isIncremental, isRegex,
                isIncludeCollapsed, comparator);
    }

    public SearchCommand(String searchText, ILayer layer,
            ISearchStrategy searchStrategy, String searchDirection,
            boolean isWrapSearch, boolean isCaseSensitive, boolean isWholeWord,
            boolean isIncremental, boolean isRegex, boolean isIncludeCollapsed,
            Comparator<?> comparator) {
        this.context = layer;
        this.searchStrategy = searchStrategy;
        this.searchText = searchText;
        this.isWrapSearch = isWrapSearch;
        this.isCaseSensitive = isCaseSensitive;
        this.isWholeWord = isWholeWord;
        this.isIncremental = isIncremental;
        this.isRegex = isRegex;
        this.isIncludeCollapsed = isIncludeCollapsed;
        this.searchDirection = searchDirection;
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

    public String getSearchDirection() {
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
