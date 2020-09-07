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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Command for showing hidden columns again via index.
 */
public class MultiColumnShowCommand extends AbstractContextFreeCommand {

    /**
     * The indexes of the columns that should be showed again.
     */
    private final int[] columnIndexes;

    /**
     *
     * @param columnIndexes
     *            The indexes of the columns that should be showed again.
     */
    public MultiColumnShowCommand(Collection<Integer> columnIndexes) {
        this.columnIndexes = columnIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     *
     * @param columnIndexes
     *            The indexes of the columns that should be showed again.
     * @since 2.0
     */
    public MultiColumnShowCommand(int... columnIndexes) {
        this.columnIndexes = columnIndexes;
    }

    /**
     *
     * @return The indexes of the columns that should be showed again.
     */
    public Collection<Integer> getColumnIndexes() {
        return ArrayUtil.asIntegerList(this.columnIndexes);
    }

    /**
     *
     * @return The indexes of the columns that should be showed again.
     *
     * @since 2.0
     */
    public int[] getColumnIndexesArray() {
        return this.columnIndexes;
    }

    @Override
    public MultiColumnShowCommand cloneCommand() {
        return new MultiColumnShowCommand(Arrays.copyOf(this.columnIndexes, this.columnIndexes.length));
    }
}
