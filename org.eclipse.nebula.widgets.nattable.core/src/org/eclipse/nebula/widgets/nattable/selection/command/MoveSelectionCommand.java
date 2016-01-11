/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth - added ITraversalStrategy handling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Command that is used to move a selection into a given direction.
 * <p>
 * Since 1.2.0 it can be created in several ways, to be able to customize the
 * determination of the steps to move. The following lists the different ways
 * and their effects:
 * </p>
 * <ol>
 * <li><b>no steps, no strategy</b> <br>
 * the {@link ITraversalStrategy} configured with the
 * {@link MoveSelectionCommandHandler} is used and will be asked for the step
 * count</li>
 * <li><b>specify steps to move</b> <br>
 * the {@link ITraversalStrategy} configured with the
 * {@link MoveSelectionCommandHandler} is used but modified to return the given
 * step count</li>
 * <li><b>specify traversal strategy</b> <br>
 * the given {@link ITraversalStrategy} is used and will be asked for the step
 * count. The {@link ITraversalStrategy} configured with the
 * {@link MoveSelectionCommandHandler} will be ignored.</li>
 * </ol>
 */
public class MoveSelectionCommand extends AbstractSelectionCommand {

    private final MoveDirectionEnum direction;
    private final Integer stepSize;
    private final ITraversalStrategy traversalStrategy;

    /**
     * Creates a {@link MoveSelectionCommand} that doesn't specify a step count
     * nor an {@link ITraversalStrategy}. Using this constructor will lead to a
     * selection movement that uses the {@link ITraversalStrategy} that is
     * registered with the {@link MoveSelectionCommandHandler}.
     *
     * @param direction
     *            The direction to move to.
     * @param shiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param controlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    public MoveSelectionCommand(MoveDirectionEnum direction, boolean shiftMask, boolean controlMask) {
        this(direction, (ITraversalStrategy) null, shiftMask, controlMask);
    }

    /**
     * Creates a {@link MoveSelectionCommand} that specifies the number of steps
     * to move directly. Using this constructor will cause the usage of the
     * registered {@link ITraversalStrategy} in the
     * {@link MoveSelectionCommandHandler} while using the specified step size.
     *
     * @param direction
     *            The direction to move to.
     * @param stepSize
     *            The number of steps to move in the given direction.
     * @param shiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param controlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    public MoveSelectionCommand(MoveDirectionEnum direction, Integer stepSize,
            boolean shiftMask, boolean controlMask) {
        super(shiftMask, controlMask);
        this.direction = direction;
        this.stepSize = stepSize;
        this.traversalStrategy = null;
    }

    /**
     *
     * @param direction
     *            The direction to move to.
     * @param traversalStrategy
     *            the traversal strategy to determine the number of steps to
     *            move and the behavior on moving over the border
     * @param shiftMask
     *            boolean flag to indicate whether the shift key modifier is
     *            enabled or not
     * @param controlMask
     *            boolean flag to indicate whether the control key modifier is
     *            enabled or not
     */
    public MoveSelectionCommand(MoveDirectionEnum direction, ITraversalStrategy traversalStrategy,
            boolean shiftMask, boolean controlMask) {
        super(shiftMask, controlMask);
        this.direction = direction;
        this.stepSize = null;
        this.traversalStrategy = traversalStrategy;
    }

    /**
     *
     * @return The direction to move to.
     */
    public MoveDirectionEnum getDirection() {
        return this.direction;
    }

    /**
     *
     * @return The number of steps to move. Can be <code>null</code>.
     */
    public Integer getStepSize() {
        return this.stepSize;
    }

    /**
     *
     * @return The traversal strategy to use for moving.
     */
    public ITraversalStrategy getTraversalStrategy() {
        return this.traversalStrategy;
    }
}
