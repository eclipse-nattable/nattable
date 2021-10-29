/*******************************************************************************
 * Copyright (c) 2012, 2021 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles persisting of the sorting state. The sorting state is read from and
 * restored to the {@link ISortModel}.
 *
 * @param <T>
 *            Type of the Beans in the backing data source.
 */
public class SortStatePersistor<T> implements IPersistable {

    private static final Logger LOG = LoggerFactory.getLogger(SortStatePersistor.class);

    public static final String PERSISTENCE_KEY_SORTING_STATE = ".SortHeaderLayer.sortingState"; //$NON-NLS-1$
    private final ISortModel sortModel;

    public SortStatePersistor(ISortModel sortModel) {
        this.sortModel = sortModel;
    }

    /**
     * Save the sorting state in the properties file.
     * <p>
     * Key: {@link #PERSISTENCE_KEY_SORTING_STATE}
     * <p>
     * Format: column index : sort direction : sort order |
     */
    @Override
    public void saveState(String prefix, Properties properties) {
        StringBuilder builder = new StringBuilder();

        for (int columnIndex : this.sortModel.getSortedColumnIndexes()) {
            SortDirectionEnum sortDirection = this.sortModel.getSortDirection(columnIndex);
            int sortOrder = this.sortModel.getSortOrder(columnIndex);

            builder.append(columnIndex);
            builder.append(":"); //$NON-NLS-1$
            builder.append(sortDirection.toString());
            builder.append(":"); //$NON-NLS-1$
            builder.append(sortOrder);
            builder.append("|"); //$NON-NLS-1$
        }

        String result = builder.toString();
        if (result != null && result.length() > 0) {
            properties.put(prefix + PERSISTENCE_KEY_SORTING_STATE, result);
        } else {
            // remove a possible existing sorting state from the properties if
            // no sorting state is set now
            properties.remove(prefix + PERSISTENCE_KEY_SORTING_STATE);
        }
    }

    /**
     * Parses the saved string and restores the state to the {@link ISortModel}.
     */
    @Override
    public void loadState(String prefix, Properties properties) {

        /*
         * restoring the sortState starts with a clean sortModel. This step is
         * necessary because there could be calls to the sortModel before which
         * leads to an undefined state afterwards ...
         */
        this.sortModel.clear();

        Object savedValue = properties.get(prefix + PERSISTENCE_KEY_SORTING_STATE);
        if (savedValue == null) {
            return;
        }

        try {
            String savedState = savedValue.toString();
            String[] sortedColumns = savedState.split("\\|"); //$NON-NLS-1$
            final List<SortState> stateInfo = new ArrayList<>();

            // Parse string
            for (String token : sortedColumns) {
                stateInfo.add(getSortStateFromString(token));
            }

            // Restore to the model
            Collections.sort(stateInfo, Comparator.comparingInt((SortState state) -> state.sortOrder));

            for (SortState state : stateInfo) {
                this.sortModel.sort(state.columnIndex, state.sortDirection, true);
            }
        } catch (Exception ex) {
            this.sortModel.clear();
            LOG.error("Error while restoring sorting state: {}", ex.getLocalizedMessage(), ex); //$NON-NLS-1$
        }
    }

    /**
     * Parse the string representation to extract the column index, sort
     * direction and sort order
     */
    protected SortState getSortStateFromString(String token) {
        String[] split = token.split(":"); //$NON-NLS-1$
        int columnIndex = Integer.parseInt(split[0]);
        SortDirectionEnum sortDirection = SortDirectionEnum.valueOf(split[1]);
        int sortOrder = Integer.parseInt(split[2]);

        return new SortState(columnIndex, sortDirection, sortOrder);
    }

    /**
     * Encapsulation of the sort state of a column
     */
    protected class SortState {

        public int columnIndex;
        public SortDirectionEnum sortDirection;
        public int sortOrder;

        public SortState(int columnIndex, SortDirectionEnum sortDirection, int sortOrder) {
            this.columnIndex = columnIndex;
            this.sortDirection = sortDirection;
            this.sortOrder = sortOrder;
        }
    }
}
