/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to insert rows.
 *
 * @since 1.6
 */
public class RowInsertCommand<T> extends AbstractContextFreeCommand {

    private final int rowIndex;
    private final List<T> objects;

    /**
     * Create a command to insert object(s) at the specified row position. The
     * row index will be calculated from the given layer and the corresponding
     * row position.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param rowPosition
     *            The row position to insert the object(s).
     * @param objects
     *            The object(s) to add.
     */
    @SafeVarargs
    public RowInsertCommand(ILayer layer, int rowPosition, T... objects) {
        this.rowIndex = layer.getRowIndexByPosition(rowPosition);
        this.objects = Arrays.asList(objects);
    }

    /**
     * Create a command to insert object(s) at the specified row position. row
     * index will be calculated from the given layer and the corresponding row
     * position.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param rowPosition
     *            The row position to insert the object(s).
     * @param objects
     *            The object(s) to add.
     */
    public RowInsertCommand(ILayer layer, int rowPosition, List<T> objects) {
        this.rowIndex = layer.getRowIndexByPosition(rowPosition);
        this.objects = objects;
    }

    /**
     * Create a command to add an object.
     *
     * @param rowIndex
     *            The index at which the row should be inserted.
     * @param object
     *            The object to add.
     */
    public RowInsertCommand(int rowIndex, T object) {
        this.rowIndex = rowIndex;
        this.objects = new ArrayList<>(1);
        this.objects.add(object);
    }

    /**
     * Create a command to add object(s).
     *
     * @param rowIndex
     *            The index at which the rows should be inserted.
     * @param objects
     *            The object(s) to add.
     */
    public RowInsertCommand(int rowIndex, List<T> objects) {
        this.rowIndex = rowIndex;
        this.objects = objects;
    }

    /**
     * Create a command to add an object.
     *
     * @param object
     *            The object to add.
     */
    public RowInsertCommand(T object) {
        this.rowIndex = -1;
        this.objects = new ArrayList<>(1);
        this.objects.add(object);
    }

    /**
     * Create a command to add object(s).
     *
     * @param objects
     *            The object(s) to add.
     */
    public RowInsertCommand(List<T> objects) {
        this.rowIndex = -1;
        this.objects = objects;
    }

    /**
     *
     * @return The index at which the row should be inserted.
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     *
     * @return The objects that should be inserted.
     */
    public List<T> getObjects() {
        return this.objects;
    }

}