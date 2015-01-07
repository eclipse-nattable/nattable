/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;

/**
 * {@link Comparator} interface specialization for a comparator that can be used
 * to sort the tree structure in a groupBy composition.
 *
 * @param <T>
 *            The type of the base objects carried in the TreeList
 */
public interface IGroupByComparator<T> extends Comparator<Object> {

    /**
     * @return The {@link ISortModel} that is used to retrieve the column value
     *         comparators and the sort direction in case a groupBy value is
     *         sorted which will directly affect the tree structure build. Can
     *         be <code>null</code> which leads to a static tree structure that
     *         only allows sorting for leafs within groups.
     */
    ISortModel getSortModel();

    /**
     * @param sortModel
     *            The {@link ISortModel} that is used to retrieve the column
     *            value comparators and the sort direction in case a groupBy
     *            value is sorted which will directly affect the tree structure
     *            build. Can be <code>null</code> which leads to a static tree
     *            structure that only allows sorting for leafs within groups.
     */
    void setSortModel(ISortModel sortModel);

    /**
     * @param treeLayer
     *            The {@link IUniqueIndexLayer} that should be used to retrieve
     *            config labels for a column. Needed to be able to determine if
     *            the column which is used for sorting is the tree column. Can
     *            be <code>null</code> which avoids specialized handling of tree
     *            / non tree columns.
     */
    void setTreeLayer(IUniqueIndexLayer treeLayer);

    /**
     * @param dataLayer
     *            The {@link GroupByDataLayer} that should be used to retrieve
     *            groupBy summary values for sorting the tree structure. Can be
     *            <code>null</code> to avoid retrieving and inspecting summary
     *            values on sorting.
     */
    void setDataLayer(GroupByDataLayer<T> dataLayer);

    /**
     * For performance reasons the {@link IGroupByComparator} might cache
     * several information e.g. retrieved via {@link GroupByDataLayer}. This
     * method is intended to clear those cached informations to avoid memory
     * leaks or sorting based on old information.
     */
    void clearCache();
}
