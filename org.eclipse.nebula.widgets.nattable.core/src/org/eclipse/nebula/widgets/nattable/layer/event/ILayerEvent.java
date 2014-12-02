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
package org.eclipse.nebula.widgets.nattable.layer.event;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Event fired by the {@link ILayerCommandHandler} classes (usually to signal to
 * handling of a {@link ILayerCommand}). Every layer in the grid is given a
 * chance to respond to an event via
 * {@link ILayer#handleLayerEvent(ILayerEvent)}.
 *
 * @see ILayerEventHandler
 */
public interface ILayerEvent {

    /**
     * Convert the column/row positions carried by the event to the layer about
     * to handle the event.
     *
     * @param localLayer
     *            layer about to receive the event
     * @return TRUE if successfully converted, FALSE otherwise
     */
    public boolean convertToLocal(ILayer localLayer);

    /**
     * @return A cloned copy of the event. This cloned copy is provided to each
     *         listener.
     */
    public ILayerEvent cloneEvent();

}
