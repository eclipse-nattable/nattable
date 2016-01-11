/*******************************************************************************
 * Copyright (c) 2012, 2015 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 462143
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * Command that is used to trigger a scrolling operation. Typically fired by
 * other drag operations.
 */
public class ViewportDragCommand implements ILayerCommand {

    private int x;
    private int y;

    /**
     * Flag to indicate if the command was created for MoveDirectionEnum values
     * or with screen coordinates. Needed for backwards compatibility.
     */
    private boolean configuredForMoveDirection = false;

    private MoveDirectionEnum horizontal;
    private MoveDirectionEnum vertical;

    /**
     * Create a ViewportDragCommand that transports the coordinates of the mouse
     * cursor. This command typically triggers a background thread that performs
     * a scroll operation for the current mouse cursor position.
     *
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     *
     * @see ViewportLayer#drag(int, int)
     */
    public ViewportDragCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create a ViewportDragCommand that transports the scroll directions. This
     * command typically triggers an immediate scrolling operation by one cell
     * position in the given direction.
     *
     * @param horizontal
     *            The horizontal movement for the scroll operation
     *            <code>MoveDirectionEnum.LEFT</code>,
     *            <code>MoveDirectionEnum.RIGHT</code>,
     *            <code>MoveDirectionEnum.NONE</code>
     * @param vertical
     *            The vertical movement for the scroll operation
     *            <code>MoveDirectionEnum.UP</code>,
     *            <code>MoveDirectionEnum.DOWN</code>,
     *            <code>MoveDirectionEnum.NONE</code>
     *
     * @see ViewportLayer#drag(MoveDirectionEnum, MoveDirectionEnum)
     *
     * @since 1.3
     */
    public ViewportDragCommand(MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.configuredForMoveDirection = true;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    /**
     * @return The horizontal movement for the scroll operation
     *         <code>MoveDirectionEnum.LEFT</code>,
     *         <code>MoveDirectionEnum.RIGHT</code>,
     *         <code>MoveDirectionEnum.NONE</code>
     * @since 1.3
     */
    public MoveDirectionEnum getHorizontal() {
        return this.horizontal;
    }

    /**
     * @return The vertical movement for the scroll operation
     *         <code>MoveDirectionEnum.UP</code>,
     *         <code>MoveDirectionEnum.DOWN</code>,
     *         <code>MoveDirectionEnum.NONE</code>
     * @since 1.3
     */
    public MoveDirectionEnum getVertical() {
        return this.vertical;
    }

    /**
     * @return whether the command was created for MoveDirectionEnum values or
     *         with screen coordinates
     * @since 1.3
     */
    public boolean isConfiguredForMoveDirection() {
        return this.configuredForMoveDirection;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        return true;
    }

    @Override
    public ILayerCommand cloneCommand() {
        if (!this.configuredForMoveDirection) {
            return new ViewportDragCommand(this.x, this.y);
        }
        return new ViewportDragCommand(this.horizontal, this.vertical);
    }

}
