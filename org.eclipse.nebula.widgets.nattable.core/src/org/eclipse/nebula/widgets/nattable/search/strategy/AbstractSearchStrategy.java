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
package org.eclipse.nebula.widgets.nattable.search.strategy;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.SearchDirection;

public abstract class AbstractSearchStrategy implements ISearchStrategy {
    private ILayer contextLayer;
    protected SearchDirection searchDirection;
    protected boolean caseSensitive;
    protected boolean wrapSearch;
    protected boolean wholeWord;
    protected boolean incremental;
    protected boolean regex;
    protected boolean includeCollapsed;
    protected boolean columnFirst;
    protected Comparator<?> comparator;

    public void setContextLayer(ILayer contextLayer) {
        this.contextLayer = contextLayer;
    }

    public ILayer getContextLayer() {
        return this.contextLayer;
    }

    public void setSearchDirection(String searchDirection) {
        setSearchDirection(SearchDirection.valueOf(searchDirection));
    }

    /**
     * @param searchDirection
     *            The {@link SearchDirection} to use.
     * @since 2.0
     */
    public void setSearchDirection(SearchDirection searchDirection) {
        this.searchDirection = searchDirection;
    }

    /**
     *
     * @return The used {@link SearchDirection}
     * @since 2.0
     */
    public SearchDirection getSearchDirection() {
        return this.searchDirection;
    }

    public void setWrapSearch(boolean wrapSearch) {
        this.wrapSearch = wrapSearch;
    }

    public boolean isWrapSearch() {
        return this.wrapSearch;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return this.caseSensitive;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }

    public boolean isWholeWord() {
        return this.wholeWord;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    public boolean isIncremental() {
        return this.incremental;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public boolean isRegex() {
        return this.regex;
    }

    public void setIncludeCollapsed(boolean includeCollapsed) {
        this.includeCollapsed = includeCollapsed;
    }

    public boolean isIncludeCollapsed() {
        return this.includeCollapsed;
    }

    public void setColumnFirst(boolean columnFirst) {
        this.columnFirst = columnFirst;
    }

    public boolean isColumnFirst() {
        return this.columnFirst;
    }

    public Comparator<?> getComparator() {
        return this.comparator;
    }

    public void setComparator(Comparator<?> comparator) {
        this.comparator = comparator;
    }

    /**
     *
     * @return <code>false</code> if the result should be processed by the
     *         caller, <code>true</code> if the strategy deals with the result
     *         itself.
     *
     * @since 1.5
     */
    public boolean processResultInternally() {
        return false;
    }
}
