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
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * {@link IKeyAction} to perform selection movements on key press. By default it
 * will move the selection by 1 into the specified direction. It is possible to
 * specify an {@link ITraversalStrategy} that should be used on moving into the
 * given direction. This allows different traversal behavior on different key
 * strokes, e.g. axis cycle on up/down, table cycle on left/right.
 */
public class MoveSelectionAction extends AbstractKeySelectAction {

    /**
     * The {@link ITraversalStrategy} to use. If <code>null</code> a step count
     * of 1 and the {@link ITraversalStrategy} registered with the
     * {@link MoveSelectionCommandHandler} subclass will be used.
     */
    private ITraversalStrategy traversalStrategy;

    /**
     * Create a MoveSelectionAction that executes a {@link MoveSelectionCommand}
     * to move the selection into the given direction by 1. Using this
     * constructor the {@link ITraversalStrategy} registered with the
     * {@link MoveSelectionCommandHandler} will be used to handle traversal.
     *
     * @param direction
     *            The direction the selection should move to.
     */
    public MoveSelectionAction(MoveDirectionEnum direction) {
        super(direction);
    }

    /**
     * Create a MoveSelectionAction that executes a {@link MoveSelectionCommand}
     * to move the selection into the given direction by 1. Using this
     * constructor the {@link ITraversalStrategy} registered with the
     * {@link MoveSelectionCommandHandler} will be used to handle traversal.
     * <p>
     * This constructor allows to specify if key modifiers should are activated.
     * </p>
     *
     * @param direction
     *            The direction the selection should move to.
     * @param shiftMask
     *            Whether the shift modifier is activated.
     * @param ctrlMask
     *            Whether the control modifier is activated.
     */
    public MoveSelectionAction(MoveDirectionEnum direction, boolean shiftMask, boolean ctrlMask) {
        super(direction, shiftMask, ctrlMask);
    }

    /**
     * Create a MoveSelectionAction that executes a {@link MoveSelectionCommand}
     * to move the selection into the given direction by using the given
     * {@link ITraversalStrategy}.
     *
     * @param direction
     *            The direction the selection should move to.
     * @param traversalStrategy
     *            The {@link ITraversalStrategy} that should be used by moving
     *            the selection.
     */
    public MoveSelectionAction(MoveDirectionEnum direction, ITraversalStrategy traversalStrategy) {
        super(direction);
        this.traversalStrategy = traversalStrategy;
    }

    /**
     * Create a MoveSelectionAction that executes a {@link MoveSelectionCommand}
     * to move the selection into the given direction by using the given
     * {@link ITraversalStrategy}.
     * <p>
     * This constructor allows to specify if key modifiers should are activated.
     * </p>
     *
     * @param direction
     *            The direction the selection should move to.
     * @param traversalStrategy
     *            The {@link ITraversalStrategy} that should be used by moving
     *            the selection.
     * @param shiftMask
     *            Whether the shift modifier is activated.
     * @param ctrlMask
     *            Whether the control modifier is activated.
     */
    public MoveSelectionAction(MoveDirectionEnum direction, ITraversalStrategy traversalStrategy,
            boolean shiftMask, boolean ctrlMask) {
        super(direction, shiftMask, ctrlMask);
        this.traversalStrategy = traversalStrategy;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        super.run(natTable, event);

        MoveSelectionCommand command = null;
        if (this.traversalStrategy == null) {
            command = new MoveSelectionCommand(
                    getDirection(), 1, isShiftMask(), isControlMask());
        }
        else {
            command = new MoveSelectionCommand(
                    getDirection(), this.traversalStrategy, isShiftMask(), isControlMask());
        }
        natTable.doCommand(command);
    }

}
