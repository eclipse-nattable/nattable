/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth - added ITraversalStrategy handling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * Abstraction of the selection behavior during navigation in the grid.
 * Implementations of this class specify what to select when the selection moves
 * by responding to the {@link MoveSelectionCommand}.
 *
 * @param <T>
 *            The type of the {@link ILayerCommand} this
 *            {@link ILayerCommandHandler} handles. Needs to be a
 *            {@link MoveSelectionCommand} or subtype.
 *
 * @see MoveCellSelectionCommandHandler
 * @see MoveRowSelectionCommandHandler
 */
public abstract class MoveSelectionCommandHandler<T extends MoveSelectionCommand> implements ILayerCommandHandler<T> {

    /**
     * The SelectionLayer instance which is needed to perform selection
     * operations.
     */
    protected final SelectionLayer selectionLayer;

    /**
     * The strategy to use on horizontal traversal. Specifies the behavior when
     * the movement reaches a border.
     */
    protected final ITraversalStrategy horizontalTraversalStrategy;

    /**
     * The strategy to use on vertical traversal. Specifies the behavior when
     * the movement reaches a border.
     */
    protected final ITraversalStrategy verticalTraversalStrategy;

    /**
     * Create a MoveSelectionCommandHandler for the given {@link SelectionLayer}
     * . Uses the {@link ITraversalStrategy#AXIS_TRAVERSAL_STRATEGY} as default
     * strategy for selection movement.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     */
    public MoveSelectionCommandHandler(SelectionLayer selectionLayer) {
        this(selectionLayer, ITraversalStrategy.AXIS_TRAVERSAL_STRATEGY);
    }

    /**
     * Create a MoveSelectionCommandHandler for the given {@link SelectionLayer}
     * .
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     * @param traversalStrategy
     *            The strategy that should be used for selection movements. Can
     *            not be <code>null</code>.
     */
    public MoveSelectionCommandHandler(SelectionLayer selectionLayer, ITraversalStrategy traversalStrategy) {
        this(selectionLayer, traversalStrategy, traversalStrategy);
    }

    /**
     * Create a MoveSelectionCommandHandler for the given {@link SelectionLayer}
     * .
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     * @param horizontalTraversalStrategy
     *            The strategy that should be used for horizontal selection
     *            movements. Can not be <code>null</code>.
     * @param verticalTraversalStrategy
     *            The strategy that should be used for vertical selection
     *            movements. Can not be <code>null</code>.
     */
    public MoveSelectionCommandHandler(SelectionLayer selectionLayer,
            ITraversalStrategy horizontalTraversalStrategy, ITraversalStrategy verticalTraversalStrategy) {
        if (horizontalTraversalStrategy == null || verticalTraversalStrategy == null) {
            throw new IllegalArgumentException("You need to specify an ITraversalStrategy!"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.horizontalTraversalStrategy = horizontalTraversalStrategy;
        this.verticalTraversalStrategy = verticalTraversalStrategy;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, T command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            moveSelection(command.getDirection(), getTraversalStrategy(command),
                    command.isShiftMask(), command.isControlMask());
            return true;
        }
        return false;
    }

    /**
     * Determines the {@link ITraversalStrategy} that should be used to move the
     * selection on handling the given command. The strategy is determined in
     * the following way:
     * <ol>
     * <li>Return the {@link ITraversalStrategy} carried by the command</li>
     * <li>If it doesn't contain a {@link ITraversalStrategy} but a carries a
     * dedicated step count, create a temporary {@link ITraversalStrategy} that
     * is configured with the locally configured {@link ITraversalStrategy} but
     * returns the step count carried by the command.</li>
     * <li>If the command doesn't carry a {@link ITraversalStrategy} and no
     * dedicated step count, the {@link ITraversalStrategy} registered with this
     * command handler is returned.</li>
     * </ol>
     *
     * @param command
     *            The current handled command.
     * @return The {@link ITraversalStrategy} that should be used to move the
     *         selection. <code>null</code> for {@link MoveDirectionEnum#NONE}.
     */
    protected ITraversalStrategy getTraversalStrategy(final T command) {
        if (MoveDirectionEnum.DOWN.equals(command.getDirection())
                || MoveDirectionEnum.UP.equals(command.getDirection())) {
            return getTraversalStrategy(command, this.verticalTraversalStrategy);
        }
        else if (MoveDirectionEnum.LEFT.equals(command.getDirection())
                || MoveDirectionEnum.RIGHT.equals(command.getDirection())) {
            return getTraversalStrategy(command, this.horizontalTraversalStrategy);
        }
        // the MoveDirectionEnum that is not handled yet is NONE
        // so since no movement is involved, we return null
        return null;
    }

    /**
     * Determines the {@link ITraversalStrategy} that should be used to move the
     * selection on handling the given command. The strategy is determined in
     * the following way:
     * <ol>
     * <li>Return the {@link ITraversalStrategy} carried by the command</li>
     * <li>If it doesn't contain a {@link ITraversalStrategy} but a carries a
     * dedicated step count, create a temporary {@link ITraversalStrategy} that
     * is configured with the locally configured {@link ITraversalStrategy} but
     * returns the step count carried by the command.</li>
     * <li>If the command doesn't carry a {@link ITraversalStrategy} and no
     * dedicated step count, the {@link ITraversalStrategy} registered with this
     * command handler is returned.</li>
     * </ol>
     *
     * @param command
     *            The current handled command.
     * @param baseTraversalStrategy
     *            The {@link ITraversalStrategy} that should be used in case the
     *            given command does not carry one.
     * @return The {@link ITraversalStrategy} that should be used to move the
     *         selection.
     */
    private ITraversalStrategy getTraversalStrategy(final T command, final ITraversalStrategy baseTraversalStrategy) {
        // if the command comes with a strategy we use it
        ITraversalStrategy result = command.getTraversalStrategy();

        if (result == null) {
            if (command.getStepSize() != null) {
                // command carries a step size, so we use the provided strategy
                // with the transported step size this is mainly for backwards
                // compatibility
                result = new ITraversalStrategy() {

                    @Override
                    public TraversalScope getTraversalScope() {
                        return baseTraversalStrategy.getTraversalScope();
                    }

                    @Override
                    public boolean isCycle() {
                        return baseTraversalStrategy.isCycle();
                    }

                    @Override
                    public int getStepCount() {
                        return command.getStepSize();
                    }

                    @Override
                    public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                        return baseTraversalStrategy.isValidTarget(from, to);
                    }
                };
            }
            else {
                result = baseTraversalStrategy;
            }
        }

        return result;
    }

    /**
     * Moves the selection from the current position into the given move
     * direction.
     *
     * @param moveDirection
     *            The direction to move to.
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected void moveSelection(MoveDirectionEnum moveDirection, ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask) {

        switch (moveDirection) {
            case UP:
                moveLastSelectedUp(traversalStrategy, withShiftMask, withControlMask);
                break;
            case DOWN:
                moveLastSelectedDown(traversalStrategy, withShiftMask, withControlMask);
                break;
            case LEFT:
                moveLastSelectedLeft(traversalStrategy, withShiftMask, withControlMask);
                break;
            case RIGHT:
                moveLastSelectedRight(traversalStrategy, withShiftMask, withControlMask);
                break;
            default:
                break;
        }
    }

    /**
     * Moves the selection from the current position to the right.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedRight(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

    /**
     * Moves the selection from the current position to the left.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedLeft(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

    /**
     * Moves the selection from the current position up.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedUp(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);

    /**
     * Moves the selection from the current position down.
     *
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param withShiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param withControlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    protected abstract void moveLastSelectedDown(ITraversalStrategy traversalStrategy,
            boolean withShiftMask, boolean withControlMask);
}
