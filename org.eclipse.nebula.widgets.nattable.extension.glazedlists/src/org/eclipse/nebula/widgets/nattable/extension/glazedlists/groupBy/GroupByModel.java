/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 447185
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

/**
 * The model that is used to internally store the groupby state. It is used to
 * define the tree structure.
 */
public class GroupByModel extends Observable implements IPersistable {

    public static final String PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES = ".groupByColumnIndexes"; //$NON-NLS-1$

    private List<Integer> groupByColumnIndexes = new ArrayList<Integer>();

    /**
     * Add the given column index to the list of column indexes that are
     * currently grouped.
     *
     * @param columnIndex
     *            The column index to add to the grouping.
     * @return <code>true</code> if the list did not already contain the given
     *         column index, <code>false</code> if the list is unchanged.
     */
    public boolean addGroupByColumnIndex(int columnIndex) {
        if (!this.groupByColumnIndexes.contains(columnIndex)) {
            this.groupByColumnIndexes.add(columnIndex);
            update();
            return true;
        } else {
            // unchanged
            return false;
        }
    }

    /**
     * Remove the given column index from the list of column indexes that are
     * currently grouped.
     *
     * @param columnIndex
     *            The column index to remove from the grouping.
     * @return <code>true</code> if the list contained the element and was
     *         therefore changed, <code>false</code> if the list is unchanged.
     */
    public boolean removeGroupByColumnIndex(int columnIndex) {
        if (this.groupByColumnIndexes.contains(columnIndex)) {
            this.groupByColumnIndexes.remove(Integer.valueOf(columnIndex));
            update();
            return true;
        } else {
            // unchanged;
            return false;
        }
    }

    /**
     * Clear the local list of indexes of columns that are currently grouped.
     * This means to perform a complete ungrouping.
     */
    public void clearGroupByColumnIndexes() {
        this.groupByColumnIndexes.clear();
        update();
    }

    /**
     *
     * @return The indexes of the columns that are currently grouped.
     */
    public List<Integer> getGroupByColumnIndexes() {
        return this.groupByColumnIndexes;
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        StringBuilder strBuilder = new StringBuilder();
        for (Integer index : this.groupByColumnIndexes) {
            strBuilder.append(index);
            strBuilder.append(IPersistable.VALUE_SEPARATOR);
        }
        properties.setProperty(
                prefix + PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES,
                strBuilder.toString());
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.groupByColumnIndexes.clear();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.groupByColumnIndexes.add(Integer.valueOf(index));
            }
        }

        update();
    }

    /**
     * Notifies the observers about a change.
     *
     * @see #setChanged()
     * @see #notifyObservers()
     */
    public void update() {
        setChanged();
        notifyObservers();
    }
}
