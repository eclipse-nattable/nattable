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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to show a row that is currently hidden. As a hidden row has no
 * position itself the position of an adjacent row is transported down the layer
 * stack.
 *
 * @since 1.6
 */
public class RowShowCommand extends AbstractRowCommand {

    private final boolean showTopPosition;
    private final boolean showAll;

    /**
     * Creates a {@link RowShowCommand} to show a row that is hidden next to the
     * transported row position.
     *
     * @param layer
     *            The layer to which the row position correlates.
     * @param rowPosition
     *            The visible adjacent row position of the hidden rows that
     *            should be shown again.
     * @param showTopPosition
     *            Whether the row positions to the top or the bottom of the
     *            transported row position should be shown again.
     * @param showAll
     *            Whether all hidden adjacent rows should be shown again or only
     *            the single direct adjacent row.
     */
    public RowShowCommand(ILayer layer, int rowPosition, boolean showTopPosition, boolean showAll) {
        super(layer, rowPosition);
        this.showTopPosition = showTopPosition;
        this.showAll = showAll;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowShowCommand(RowShowCommand command) {
        super(command);
        this.showTopPosition = command.showTopPosition;
        this.showAll = command.showAll;
    }

    @Override
    public RowShowCommand cloneCommand() {
        return new RowShowCommand(this);
    }

    /**
     *
     * @return <code>true</code> if the row positions to the top of the
     *         transported row position should be shown again,
     *         <code>false</code> if the rows to the bottom should be shown
     *         again.
     */
    public boolean isShowTopPosition() {
        return this.showTopPosition;
    }

    /**
     *
     * @return <code>true</code> if all hidden adjacent rows should be shown
     *         again, <code>false</code> if only the direct neighbor should be
     *         shown again but further hidden rows should stay hidden.
     */
    public boolean isShowAll() {
        return this.showAll;
    }
}
