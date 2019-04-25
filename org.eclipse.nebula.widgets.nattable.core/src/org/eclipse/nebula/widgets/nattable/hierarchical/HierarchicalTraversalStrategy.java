/*****************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;

/**
 * Wrapper for basic {@link ITraversalStrategy} implementations that add target
 * checks. Requests for traversal scope, cycle and step count will be delegated
 * to the wrapped {@link ITraversalStrategy}.
 * {@link #isValidTarget(ILayerCell, ILayerCell)} will check whether the
 * {@link ILayerCell} from which the movement should be performed is part of a
 * collapsed parent node and therefore the {@link ILayerCell} to move the
 * selection to would be in a hidden area.
 *
 * @since 1.6
 */
public class HierarchicalTraversalStrategy implements ITraversalStrategy {

    /**
     * The {@link ITraversalStrategy} that is wrapped by this
     * {@link HierarchicalTraversalStrategy}.
     */
    protected ITraversalStrategy baseStrategy;

    /**
     * The {@link HierarchicalTreeLayer} on which this
     * {@link ITraversalStrategy} is applied.
     */
    protected HierarchicalTreeLayer layer;

    /**
     *
     * @param baseStrategy
     *            The {@link ITraversalStrategy} that should be wrapped by this
     *            {@link HierarchicalTraversalStrategy}.
     * @param layer
     *            The {@link HierarchicalTreeLayer} on which this
     *            {@link ITraversalStrategy} is applied.
     */
    public HierarchicalTraversalStrategy(ITraversalStrategy baseStrategy, HierarchicalTreeLayer layer) {
        this.baseStrategy = baseStrategy;
        this.layer = layer;
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
        return !this.layer.isRowIndexHidden(to.getRowIndex());
    }

}
