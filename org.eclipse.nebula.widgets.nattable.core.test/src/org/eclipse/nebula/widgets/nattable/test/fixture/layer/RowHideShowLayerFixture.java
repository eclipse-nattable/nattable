/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * A RowHideShowLayer for use in unit tests with a pre-canned set of hidden
 * rows. Row indexes by positions: 4 1 2 5 6
 */
public class RowHideShowLayerFixture extends RowHideShowLayer {

    private ILayerCommand lastCommand;

    public RowHideShowLayerFixture() {
        // Row reorder fixture index positions: 4 1 0 2 3 5 6
        super(new RowReorderLayerFixture());

        hideRowPositions(2, 4);
    }

    public RowHideShowLayerFixture(IUniqueIndexLayer underlyingLayerFixture) {
        super(underlyingLayerFixture);
    }

    public RowHideShowLayerFixture(int... rowPositionsToHide) {
        super(new DataLayerFixture(10, 10, 20, 5));

        hideRowPositions(rowPositionsToHide);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        this.lastCommand = command;
        return super.doCommand(command);
    }

    public ILayerCommand getLastCommand() {
        return this.lastCommand;
    }

}
