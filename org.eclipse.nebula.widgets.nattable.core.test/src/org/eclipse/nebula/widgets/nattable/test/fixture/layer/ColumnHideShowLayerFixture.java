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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * A ColumnHideShowLayer for use in unit tests with a pre-canned set of hidden
 * columns. Column indexes by positions: 4 1 2
 */
public class ColumnHideShowLayerFixture extends ColumnHideShowLayer {

    private ILayerCommand lastCommand;

    public ColumnHideShowLayerFixture() {
        // Column reorder fixture index positions: 4 1 0 2 3
        super(new ColumnReorderLayerFixture());

        hideColumnPositions(2, 4);
    }

    public ColumnHideShowLayerFixture(IUniqueIndexLayer underlyingLayerFixture) {
        super(underlyingLayerFixture);
    }

    public ColumnHideShowLayerFixture(int... columnPositionsToHide) {
        super(new DataLayerFixture(10, 10, 20, 5));

        hideColumnPositions(columnPositionsToHide);
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
