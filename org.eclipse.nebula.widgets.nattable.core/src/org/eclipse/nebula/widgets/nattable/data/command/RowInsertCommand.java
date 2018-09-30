/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to insert rows.
 *
 * @since 1.6
 */
public class RowInsertCommand<T> extends AbstractRowCommand {

    private final List<T> objects;

    /**
     * Create a command to insert object(s) at the specified row position.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param rowPosition
     *            The row position to insert the object(s).
     * @param objects
     *            The object(s) to add.
     */
    public RowInsertCommand(ILayer layer, int rowPosition, T... objects) {
        super(layer, rowPosition);
        this.objects = Arrays.asList(objects);
    }

    /**
     * Create a command to insert object(s) at the specified row position.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param rowPosition
     *            The row position to insert the object(s).
     * @param objects
     *            The object(s) to add.
     */
    public RowInsertCommand(ILayer layer, int rowPosition, List<T> objects) {
        super(layer, rowPosition);
        this.objects = objects;
    }

    /**
     * Create a command to add an object.
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param object
     *            The object to add.
     */
    public RowInsertCommand(ILayer layer, T object) {
        super(layer, -1);
        this.objects = new ArrayList<T>(1);
        this.objects.add(object);
    }

    /**
     * Create a command to add object(s).
     *
     * @param layer
     *            The layer to which the row position matches.
     * @param objects
     *            The object(s) to add.
     */
    public RowInsertCommand(ILayer layer, List<T> objects) {
        super(layer, -1);
        this.objects = objects;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowInsertCommand(RowInsertCommand<T> command) {
        super(command);
        this.objects = new ArrayList<T>(command.objects);
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        // If the row position is -1 the transported objects will be added at
        // the end of the backing data list. A position conversion is not
        // necessary in that case and we should avoid that the command is
        // blocked because of a failing conversion.
        if (getRowPosition() > -1) {
            return super.convertToTargetLayer(targetLayer);
        }
        return true;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new RowInsertCommand<T>(this);
    }

    /**
     *
     * @return The objects that should be inserted.
     */
    public List<T> getObjects() {
        return this.objects;
    }

}