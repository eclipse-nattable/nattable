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

/**
 * Command to delete row objects.
 *
 * @param <T>
 *            The type contained in the backing data list.
 *
 * @since 1.6
 */
public class RowObjectDeleteCommand<T> extends AbstractContextFreeCommand {

    private List<T> objectsToDelete = new ArrayList<T>();

    /**
     * Creates a {@link RowObjectDeleteCommand}.
     *
     * @param rowObjects
     *            The row objects to delete.
     */
    @SafeVarargs
    public RowObjectDeleteCommand(T... rowObjects) {
        this(Arrays.asList(rowObjects));
    }

    /**
     * Creates a {@link RowObjectDeleteCommand}.
     *
     * @param rowObjects
     *            The row objects to delete.
     */
    public RowObjectDeleteCommand(List<T> rowObjects) {
        this.objectsToDelete.addAll(rowObjects);
    }

    /**
     *
     * @return The row objects that should be deleted.
     */
    public List<T> getObjectsToDelete() {
        return this.objectsToDelete;
    }

}
