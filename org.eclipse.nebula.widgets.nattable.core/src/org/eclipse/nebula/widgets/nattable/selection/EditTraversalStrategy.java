/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * Wrapper for basic {@link ITraversalStrategy} implementations that add target
 * checks. Requests for traversal scope, cycle and step count will be delegated
 * to the wrapped {@link ITraversalStrategy}.
 * {@link #isValidTarget(ILayerCell, ILayerCell)} will check whether the
 * {@link ILayerCell} from which the movement should be performed is in edit
 * mode or not. If it is in edit mode, validations will be performed to check
 * whether the target cell is also editable.
 */
public class EditTraversalStrategy implements ITraversalStrategy {

    /**
     * The {@link ITraversalStrategy} that is wrapped by this
     * {@link EditTraversalStrategy}.
     */
    protected ITraversalStrategy baseStrategy;

    /**
     * The current NatTable instance this strategy is connected to. Needed to be
     * able to perform checks related to NatTable states and configurations.
     */
    protected NatTable natTable;

    /**
     *
     * @param baseStrategy
     *            The {@link ITraversalStrategy} that shoud be wrapped by this
     *            {@link EditTraversalStrategy}.
     * @param natTable
     *            The NatTable instance this strategy is connected to. Needed to
     *            be able to perform checks related to NatTable states and
     *            configurations.
     */
    public EditTraversalStrategy(ITraversalStrategy baseStrategy, NatTable natTable) {
        this.baseStrategy = baseStrategy;
        this.natTable = natTable;
    }

    @Override
    public TraversalScope getTraversalScope() {
        return this.baseStrategy.getTraversalScope();
    }

    @Override
    public boolean isCycle() {
        return this.baseStrategy.isCycle();
    }

    @Override
    public int getStepCount() {
        return this.baseStrategy.getStepCount();
    }

    @Override
    public boolean isValidTarget(ILayerCell from, ILayerCell to) {
        // check if NatTable is currently in edit mode
        if (this.natTable.getActiveCellEditor() != null) {
            // if there is an open editor, we suppose that the movement should
            // be performed from there, as the focus is typically in the editor
            return EditUtils.isCellEditable(
                    to.getLayer(),
                    this.natTable.getConfigRegistry(),
                    new PositionCoordinate(to.getLayer(), to.getColumnPosition(), to.getRowPosition()));
        }
        return true;
    }

}
