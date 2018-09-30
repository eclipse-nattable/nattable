/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
