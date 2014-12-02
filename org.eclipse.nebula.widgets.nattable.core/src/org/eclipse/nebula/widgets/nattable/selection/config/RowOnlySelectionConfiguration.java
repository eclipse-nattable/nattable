/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.selection.MoveRowSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Configure the move selection behavior so that we always move by a row. Add
 * {@link ILayerEventHandler} to preserve row selection.
 *
 * @see DefaultMoveSelectionConfiguration
 */
public class RowOnlySelectionConfiguration<T> extends
        AbstractLayerConfiguration<SelectionLayer> {

    @Override
    public void configureTypedLayer(SelectionLayer layer) {
        layer.registerCommandHandler(new MoveRowSelectionCommandHandler(layer));
    }
}
