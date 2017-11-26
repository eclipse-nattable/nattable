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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger column resizing.
 */
public class ColumnResizeCommand extends AbstractColumnCommand {

    private final int newColumnWidth;
    private final boolean downScale;

    /**
     * Create a {@link ColumnResizeCommand} to resize the column at the given
     * position to the given width. The given column width will be taken as is
     * without scaling.
     *
     * @param layer
     *            The {@link ILayer} to which the column position correlates.
     * @param columnPosition
     *            The position of the column that should be resized.
     * @param newWidth
     *            The new width that should be applied to the given column.
     */
    public ColumnResizeCommand(ILayer layer, int columnPosition, int newWidth) {
        this(layer, columnPosition, newWidth, false);
    }

    /**
     * Create a {@link ColumnResizeCommand} to resize the column at the given
     * position to the given width.
     *
     * @param layer
     *            The {@link ILayer} to which the column position correlates.
     * @param columnPosition
     *            The position of the column that should be resized.
     * @param newWidth
     *            The new width that should be applied to the given column.
     * @param downScale
     *            <code>true</code> if the newWidth value should be down scaled
     *            according to the scaling level, <code>false</code> if the
     *            value should be taken as is.
     *
     * @since 1.6
     */
    public ColumnResizeCommand(ILayer layer, int columnPosition, int newWidth, boolean downScale) {
        super(layer, columnPosition);
        this.newColumnWidth = newWidth;
        this.downScale = downScale;
    }

    /**
     * Constructor used to clone the given command.
     * 
     * @param command
     *            The command to clone.
     */
    protected ColumnResizeCommand(ColumnResizeCommand command) {
        super(command);
        this.newColumnWidth = command.newColumnWidth;
        this.downScale = command.downScale;
    }

    /**
     *
     * @return The new width value that should be applied.
     */
    public int getNewColumnWidth() {
        return this.newColumnWidth;
    }

    /**
     *
     * @return <code>true</code> if the newWidth value should be down scaled
     *         according to the scaling level, <code>false</code> if the value
     *         should be taken as is.
     *
     * @since 1.6
     */
    public boolean downScaleValue() {
        return this.downScale;
    }

    @Override
    public ColumnResizeCommand cloneCommand() {
        return new ColumnResizeCommand(this);
    }

}
