/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.MoveRowSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * Configure the behavior when the selection is moved. Example: by using arrow
 * keys. This default configuration moves by cell.
 *
 * {@link MoveSelectionCommand} are fired by the
 * {@link DefaultSelectionBindings}. A suitable handler can be plugged in to
 * handle the move commands as required.
 *
 * @see MoveRowSelectionCommandHandler
 */
public class DefaultMoveSelectionConfiguration extends AbstractLayerConfiguration<SelectionLayer> {

    @Override
    public void configureTypedLayer(SelectionLayer layer) {
        layer.registerCommandHandler(new MoveCellSelectionCommandHandler(layer));
    }

}
