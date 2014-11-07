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

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * This interface is used to configure the traversal behavior when moving the
 * selection in a NatTable via key actions, e.g. tab or the arrow keys. You can
 * specify the scope, if the traversal should cycle and specify logic to
 * determine the step count if necessary. It is also possible to add logic that
 * determines whether a target is a valid selection movement target. This allows
 * traversal of editable cells while non-editable cells would be skipped.
 */
public interface ITraversalStrategy {

    /**
     * The scope of the traversal which is necessary to determine what should
     * happen on cycling.
     */
    enum TraversalScope {
        /**
         * The scope of the traversal is on the axis, row or column. Using this
         * TraversalScope means that if cycling is enabled the selection stays
         * on the same row/column.
         */
        AXIS,

        /**
         * The scope of the traversal is on the table itself. Using this
         * TraversalScope means that if cycling is enabled the selection will
         * move to the next row/column too.
         */
        TABLE
    }

    /**
     *
     * @return The {@link TraversalScope} this traversal strategy specifies.
     */
    TraversalScope getTraversalScope();

    /**
     *
     * @return <code>true</code> if on traversal the selection should cycle,
     *         <code>false</code> if the selection should stay at the last/first
     *         position without cycling.
     */
    boolean isCycle();

    /**
     *
     * @return The number of steps to jump on traversal.
     */
    int getStepCount();

    /**
     * Checks whether the {@link ILayerCell} that would be selected after the
     * selection movement is valid or not. In case the target is invalid, the
     * responsible command handler is able to react accordingly, e.g. move the
     * next valid cell.
     * <p>
     * This is for example useful for editing traversal. If the selection
     * movement is triggered on committing a value in an editor, the next
     * editable cell should be selected and the editor opened immediately.
     * </p>
     *
     * @param from
     *            The {@link ILayerCell} from which the selection movement is
     *            started.
     * @param to
     *            The {@link ILayerCell} to which the selection movement should
     *            be performed.
     * @return <code>true</code> if the target is a valid target for selection
     *         movement, <code>false</code> if not
     */
    boolean isValidTarget(ILayerCell from, ILayerCell to);

    /**
     * {@link ITraversalStrategy} that specifies the following:<br>
     * <ul>
     * <li>traversal scope = axis</li>
     * <li>cycle = false</li>
     * <li>step count = 1</li>
     * <li>valid = true</li>
     * </ul>
     * This means for example, on moving a selection to the right, the selection
     * will move one cell at a time and stop at the right border.
     * <p>
     * This is the default traversal strategy.
     * </p>
     */
    ITraversalStrategy AXIS_TRAVERSAL_STRATEGY = new ITraversalStrategy() {

        @Override
        public TraversalScope getTraversalScope() {
            return TraversalScope.AXIS;
        }

        @Override
        public boolean isCycle() {
            return false;
        }

        @Override
        public int getStepCount() {
            return 1;
        }

        @Override
        public boolean isValidTarget(ILayerCell from, ILayerCell to) {
            return true;
        };
    };

    /**
     * {@link ITraversalStrategy} that specifies the following:<br>
     * <ul>
     * <li>traversal scope = axis</li>
     * <li>cycle = true</li>
     * <li>step count = 1</li>
     * <li>valid = true</li>
     * </ul>
     * This means for example, on moving a selection to the right, the selection
     * will move one cell at a time and jump to the first column when moving
     * over the right border.
     */
    ITraversalStrategy AXIS_CYCLE_TRAVERSAL_STRATEGY = new ITraversalStrategy() {

        @Override
        public TraversalScope getTraversalScope() {
            return TraversalScope.AXIS;
        }

        @Override
        public boolean isCycle() {
            return true;
        }

        @Override
        public int getStepCount() {
            return 1;
        }

        @Override
        public boolean isValidTarget(ILayerCell from, ILayerCell to) {
            return true;
        };
    };

    /**
     * {@link ITraversalStrategy} that specifies the following:<br>
     * <ul>
     * <li>traversal scope = table</li>
     * <li>cycle = false</li>
     * <li>step count = 1</li>
     * <li>valid = true</li>
     * </ul>
     * This means for example, on moving a selection to the right, the selection
     * will move one cell at a time and jump to the first column and move one
     * row down when moving over the right border. At the end of the table the
     * selection will stop and doesn't move to the beginning.
     */
    ITraversalStrategy TABLE_TRAVERSAL_STRATEGY = new ITraversalStrategy() {

        @Override
        public TraversalScope getTraversalScope() {
            return TraversalScope.TABLE;
        }

        @Override
        public boolean isCycle() {
            return false;
        }

        @Override
        public int getStepCount() {
            return 1;
        }

        @Override
        public boolean isValidTarget(ILayerCell from, ILayerCell to) {
            return true;
        };
    };

    /**
     * {@link ITraversalStrategy} that specifies the following:<br>
     * <ul>
     * <li>traversal scope = table</li>
     * <li>cycle = true</li>
     * <li>step count = 1</li>
     * <li>valid = true</li>
     * </ul>
     * This means for example, on moving a selection to the right, the selection
     * will move one cell at a time and jump to the first column and move one
     * row down when moving over the right border. At the end of the table the
     * selection will cycle to the beginning of the table.
     */
    ITraversalStrategy TABLE_CYCLE_TRAVERSAL_STRATEGY = new ITraversalStrategy() {

        @Override
        public TraversalScope getTraversalScope() {
            return TraversalScope.TABLE;
        }

        @Override
        public boolean isCycle() {
            return true;
        }

        @Override
        public int getStepCount() {
            return 1;
        }

        @Override
        public boolean isValidTarget(ILayerCell from, ILayerCell to) {
            return true;
        };
    };
}
