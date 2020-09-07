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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to show a column that is currently hidden. As a hidden column has no
 * position itself the position of an adjacent column is transported down the
 * layer stack.
 *
 * @since 1.6
 */
public class ColumnShowCommand extends AbstractColumnCommand {

    private final boolean showLeftPosition;
    private final boolean showAll;

    /**
     * Creates a {@link ColumnShowCommand} to show a column that is hidden next
     * to the transported column position.
     *
     * @param layer
     *            The layer to which the column position correlates.
     * @param columnPosition
     *            The visible adjacent column position of the hidden columns
     *            that should be shown again.
     * @param showLeftPosition
     *            Whether the column positions to the left or the right of the
     *            transported column position should be shown again.
     * @param showAll
     *            Whether all hidden adjacent columns should be shown again or
     *            only the single direct adjacent column.
     */
    public ColumnShowCommand(ILayer layer, int columnPosition, boolean showLeftPosition, boolean showAll) {
        super(layer, columnPosition);
        this.showLeftPosition = showLeftPosition;
        this.showAll = showAll;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ColumnShowCommand(ColumnShowCommand command) {
        super(command);
        this.showLeftPosition = command.showLeftPosition;
        this.showAll = command.showAll;
    }

    @Override
    public ColumnShowCommand cloneCommand() {
        return new ColumnShowCommand(this);
    }

    /**
     *
     * @return <code>true</code> if the column positions to the left of the
     *         transported column position should be shown again,
     *         <code>false</code> if the columns to the right should be shown
     *         again.
     */
    public boolean isShowLeftPosition() {
        return this.showLeftPosition;
    }

    /**
     *
     * @return <code>true</code> if all hidden adjacent columns should be shown
     *         again, <code>false</code> if only the direct neighbor should be
     *         shown again but further hidden columns should stay hidden.
     */
    public boolean isShowAll() {
        return this.showAll;
    }
}
