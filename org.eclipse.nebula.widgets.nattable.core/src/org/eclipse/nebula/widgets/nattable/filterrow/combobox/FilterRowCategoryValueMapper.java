/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.Collection;
import java.util.List;

/**
 * Interface to map the values in a filter row combo box to categories.
 *
 * @since 2.7
 */
public interface FilterRowCategoryValueMapper<T> {

    /**
     * Maps the values in the input list to their corresponding categories.
     *
     * @param values
     *            The list of values to be mapped to categories.
     * @return A list of categories corresponding to the input values. Needs to
     *         be a mutable list to support further modifications.
     */
    public List<T> valuesToCategories(List<T> values);

    /**
     * Resolves the categories in the given collection back to their original
     * values. If a value does not belong to a category it should be returned as
     * is.
     *
     * @param valuesWithCategories
     *            The values selected for filtering, including selected
     *            categories.
     * @return The values selected for filtering, with categories resolved back
     *         to their original values.
     */
    public Collection<T> resolveCategories(Collection<T> valuesWithCategories);
}
