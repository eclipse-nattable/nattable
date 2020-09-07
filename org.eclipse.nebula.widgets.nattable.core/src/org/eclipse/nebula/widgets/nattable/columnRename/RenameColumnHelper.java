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
package org.eclipse.nebula.widgets.nattable.columnRename;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.util.PersistenceUtils;

/**
 * Helper class for handling column renaming by user interactions on the column
 * header.
 */
public class RenameColumnHelper implements IPersistable {

    private static final Log log = LogFactory.getLog(RenameColumnHelper.class);

    public static final String PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS = ".renamedColumnHeaders"; //$NON-NLS-1$

    private final ColumnHeaderLayer columnHeaderLayer;

    /**
     * Tracks the renamed labels provided by the user.
     */
    protected Map<Integer, String> renamedColumnsLabelsByIndex = new TreeMap<Integer, String>();

    /**
     *
     * @param columnHeaderLayer
     *            The {@link ColumnHeaderLayer} this helper is connected to.
     */
    public RenameColumnHelper(ColumnHeaderLayer columnHeaderLayer) {
        this.columnHeaderLayer = columnHeaderLayer;
    }

    /**
     * Rename the column at the given position. Note: This does not change the
     * underlying column name.
     *
     * @param columnPosition
     *            The column position of the column that should be renamed.
     * @param customColumnName
     *            The name that should used as the user defined column name.
     *
     * @return <code>true</code> if the column at the given position was
     *         successfully changed.
     */
    public boolean renameColumnPosition(int columnPosition, String customColumnName) {
        int index = this.columnHeaderLayer.getColumnIndexByPosition(columnPosition);
        return renameColumnIndex(index, customColumnName);
    }

    /**
     * Rename the column at the given index. Note: This does not change the
     * underlying column name.
     *
     * @param index
     *            The column index of the column that should be renamed.
     * @param customColumnName
     *            The name that should used as the user defined column name.
     *
     * @return <code>true</code> if the column at the given index was
     *         successfully changed.
     */
    public boolean renameColumnIndex(int index, String customColumnName) {
        if (index >= 0) {
            if (customColumnName == null) {
                this.renamedColumnsLabelsByIndex.remove(index);
            } else {
                this.renamedColumnsLabelsByIndex.put(index, customColumnName);
            }
            return true;
        }
        return false;
    }

    /**
     * @return the custom label for this column as specified by the user Null if
     *         the columns is not renamed
     */
    public String getRenamedColumnLabel(int columnIndex) {
        return this.renamedColumnsLabelsByIndex.get(columnIndex);
    }

    /**
     * @return <code>true</code> if the column at the specified index was
     *         renamed by a user.
     */
    public boolean isColumnRenamed(int columnIndex) {
        return this.renamedColumnsLabelsByIndex.get(columnIndex) != null;
    }

    /**
     * @return <code>true</code> if a user renamed any column.
     */
    public boolean isAnyColumnRenamed() {
        return this.renamedColumnsLabelsByIndex.size() > 0;
    }

    /**
     * Handle the given collection of {@link StructuralDiff} objects to update
     * the indexes of the renamed column labels.
     *
     * @param columnDiffs
     *            The {@link StructuralDiff}s to handle
     * @since 1.4
     */
    public void handleStructuralChanges(Collection<StructuralDiff> columnDiffs) {
        // the number of all deleted columns that don't have a corresponding
        // index anymore (last column cases)
        List<Integer> toRemove = new ArrayList<Integer>();
        for (StructuralDiff columnDiff : columnDiffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
                Range beforePositionRange = columnDiff.getBeforePositionRange();
                for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
                    int index = i;
                    if (index >= 0)
                        toRemove.add(index);
                }
            }
        }
        // remove the column indexes that are deleted
        for (Integer r : toRemove) {
            this.renamedColumnsLabelsByIndex.remove(r);
        }

        // modify column indexes regarding the deleted columns
        List<Integer> indices = new ArrayList<Integer>(this.renamedColumnsLabelsByIndex.keySet());
        Collections.sort(indices);

        Map<Integer, String> modified = new TreeMap<Integer, String>();
        for (Integer column : indices) {
            // check number of removed indexes that are lower than the current
            // one
            int deletedBefore = 0;
            for (Integer removed : toRemove) {
                if (removed < column) {
                    deletedBefore++;
                }
            }
            int modColumn = column - deletedBefore;
            if (modColumn >= 0)
                modified.put(modColumn, this.renamedColumnsLabelsByIndex.get(column));
        }
        this.renamedColumnsLabelsByIndex.clear();
        this.renamedColumnsLabelsByIndex.putAll(modified);

        for (StructuralDiff columnDiff : columnDiffs) {
            if (columnDiff.getDiffType() != null
                    && columnDiff.getDiffType().equals(DiffTypeEnum.ADD)) {

                indices = new ArrayList<Integer>(this.renamedColumnsLabelsByIndex.keySet());
                Collections.sort(indices);

                Range beforePositionRange = columnDiff.getBeforePositionRange();
                Range afterPositionRange = columnDiff.getAfterPositionRange();
                Map<Integer, String> modifiedColumns = new TreeMap<Integer, String>();
                int beforeIndex = this.columnHeaderLayer.getColumnIndexByPosition(beforePositionRange.start);
                for (Integer column : indices) {
                    if (column >= beforeIndex) {
                        modifiedColumns.put(column + (afterPositionRange.end - afterPositionRange.start), this.renamedColumnsLabelsByIndex.get(column));
                    } else {
                        modifiedColumns.put(column, this.renamedColumnsLabelsByIndex.get(column));
                    }
                }

                this.renamedColumnsLabelsByIndex.clear();
                this.renamedColumnsLabelsByIndex.putAll(modifiedColumns);
            }
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        Object property = properties.get(prefix + PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS);

        try {
            this.renamedColumnsLabelsByIndex = PersistenceUtils.parseString(property);
        } catch (Exception e) {
            log.error("Error while restoring renamed column headers: " + e.getMessage()); //$NON-NLS-1$
            log.error("Skipping restore."); //$NON-NLS-1$
            this.renamedColumnsLabelsByIndex.clear();
        }
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        String string = PersistenceUtils.mapAsString(this.renamedColumnsLabelsByIndex);
        if (string != null && string.length() > 0) {
            properties.put(prefix + PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS, string);
        }
    }
}
