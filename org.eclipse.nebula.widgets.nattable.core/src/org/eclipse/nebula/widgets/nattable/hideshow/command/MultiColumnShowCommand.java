/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

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
        return Arrays.stream(this.columnIndexes).boxed().collect(Collectors.toList());
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
