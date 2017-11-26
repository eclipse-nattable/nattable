/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger row resizing.
 */
public class RowResizeCommand extends AbstractRowCommand {

    private final int newHeight;
    private final boolean downScale;

    /**
     * Create a {@link RowResizeCommand} to resize the row at the given position
     * to the given height. The given row height will be taken as is without
     * scaling.
     *
     * @param layer
     *            The {@link ILayer} to which the row position correlates.
     * @param rowPosition
     *            The position of the row that should be resized.
     * @param newHeight
     *            The new height that should be applied to the given row.
     */
    public RowResizeCommand(ILayer layer, int rowPosition, int newHeight) {
        this(layer, rowPosition, newHeight, false);
    }

    /**
     * Create a {@link RowResizeCommand} to resize the row at the given position
     * to the given height.
     *
     * @param layer
     *            The {@link ILayer} to which the row position correlates.
     * @param rowPosition
     *            The position of the row that should be resized.
     * @param newHeight
     *            The new height that should be applied to the given row.
     * @param downScale
     *            <code>true</code> if the newHeight value should be down scaled
     *            according to the scaling level, <code>false</code> if the
     *            value should be taken as is.
     *
     * @since 1.6
     */
    public RowResizeCommand(ILayer layer, int rowPosition, int newHeight, boolean downScale) {
        super(layer, rowPosition);
        this.newHeight = newHeight;
        this.downScale = downScale;
    }

    /**
     * Constructor used to clone the given command.
     *
     * @param command
     *            The command to clone.
     */
    protected RowResizeCommand(RowResizeCommand command) {
        super(command);
        this.newHeight = command.newHeight;
        this.downScale = command.downScale;
    }

    /**
     *
     * @return The new height value that should be applied.
     */
    public int getNewHeight() {
        return this.newHeight;
    }

    /**
     *
     * @return <code>true</code> if the newHeight value should be down scaled
     *         according to the scaling level, <code>false</code> if the value
     *         should be taken as is.
     *
     * @since 1.6
     */
    public boolean downScaleValue() {
        return this.downScale;
    }

    @Override
    public RowResizeCommand cloneCommand() {
        return new RowResizeCommand(this);
    }

}
