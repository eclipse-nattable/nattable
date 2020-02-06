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
 * Command for showing hidden rows again via index.
 */
public class MultiRowShowCommand extends AbstractContextFreeCommand {

    /**
     * The indexes of the rows that should be showed again.
     */
    private final int[] rowIndexes;

    /**
     *
     * @param rowIndexes
     *            The indexes of the rows that should be showed again.
     */
    public MultiRowShowCommand(Collection<Integer> rowIndexes) {
        this.rowIndexes = rowIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     *
     * @param rowIndexes
     *            The indexes of the rows that should be showed again.
     * @since 2.0
     */
    public MultiRowShowCommand(int... rowIndexes) {
        this.rowIndexes = rowIndexes;
    }

    /**
     *
     * @return The indexes of the rows that should be showed again.
     */
    public Collection<Integer> getRowIndexes() {
        return Arrays.stream(this.rowIndexes).boxed().collect(Collectors.toList());
    }

    /**
     *
     * @return The indexes of the rows that should be showed again.
     *
     * @since 2.0
     */
    public int[] getRowIndexesArray() {
        return this.rowIndexes;
    }

    @Override
    public MultiRowShowCommand cloneCommand() {
        return new MultiRowShowCommand(Arrays.copyOf(this.rowIndexes, this.rowIndexes.length));
    }
}
