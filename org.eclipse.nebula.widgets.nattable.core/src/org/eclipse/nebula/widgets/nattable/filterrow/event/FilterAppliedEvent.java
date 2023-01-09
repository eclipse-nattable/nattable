/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow.event;

import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

/**
 * Special {@link RowStructuralRefreshEvent} that is fired in case the filter
 * changes via {@link FilterRowDataProvider}. This includes clearing a filter.
 */
public class FilterAppliedEvent extends RowStructuralRefreshEvent {

    /**
     * The index of the column on which a filter was set or cleared. -1 if a
     * general filter operation like <i>clear all</i> or <i>loadState</i> was
     * executed.
     */
    private int columnIndex = -1;

    /**
     * The old value that was set previously as filter.
     */
    private Object oldValue;
    /**
     * The new value that was set as filter.
     */
    private Object newValue;
    /**
     * <code>true</code> if the filter editor was a filter combobox editor,
     * <code>false</code> if it was any other editor.
     */
    private boolean filterComboEditor = false;
    /**
     * Flag to inform about whether a filter was applied or cleared.
     */
    private boolean cleared = false;

    /**
     *
     * @param layer
     *            The {@link ILayer} from which this event was fired.
     */
    public FilterAppliedEvent(ILayer layer) {
        super(layer);
    }

    /**
     *
     * @param layer
     *            The {@link ILayer} from which this event was fired.
     * @param cleared
     *            <code>true</code> if the filter was cleared,
     *            <code>false</code> if a filter was applied.
     * @since 2.1
     */
    public FilterAppliedEvent(ILayer layer, boolean cleared) {
        super(layer);
        this.cleared = cleared;
    }

    /**
     *
     * @param layer
     *            The {@link ILayer} from which this event was fired.
     * @param columnIndex
     *            The index of the column on which a filter was set or cleared.
     *            -1 if a general filter operation like <i>clear all</i> or
     *            <i>loadState</i> was executed.
     * @param oldValue
     *            The old value that was set previously as filter.
     * @param newValue
     *            The new value that was set as filter.
     * @param cleared
     *            <code>true</code> if the filter was cleared,
     *            <code>false</code> if a filter was applied.
     * @since 2.1
     */
    public FilterAppliedEvent(ILayer layer, int columnIndex, Object oldValue, Object newValue, boolean cleared) {
        super(layer);
        this.columnIndex = columnIndex;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.cleared = cleared;
    }

    /**
     *
     * @return The index of the column on which a filter was set or cleared. -1
     *         if a general filter operation like <i>clear all</i> or
     *         <i>loadState</i> was executed.
     * @since 2.1
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     *
     * @return The old value that was set previously as filter.
     * @since 2.1
     */
    public Object getOldValue() {
        return this.oldValue;
    }

    /**
     *
     * @return The new value that was set as filter.
     * @since 2.1
     */
    public Object getNewValue() {
        return this.newValue;
    }

    /**
     *
     * @return <code>true</code> if the filter editor was a filter combobox
     *         editor, <code>false</code> if it was any other editor.
     * @since 2.1
     */
    public boolean isFilterComboEditor() {
        return this.filterComboEditor;
    }

    /**
     *
     * @param filterComboEditor
     *            <code>true</code> if the filter editor was a filter combobox
     *            editor, <code>false</code> if it was any other editor.
     * @since 2.1
     */
    public void setFilterComboEditor(boolean filterComboEditor) {
        this.filterComboEditor = filterComboEditor;
    }

    /**
     *
     * @return <code>true</code> if the filter was cleared, <code>false</code>
     *         if a filter was applied.
     * @since 2.1
     */
    public boolean isCleared() {
        return this.cleared;
    }

    /**
     *
     * @param cleared
     *            <code>true</code> if the filter was cleared,
     *            <code>false</code> if a filter was applied.
     * @since 2.1
     */
    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }
}
