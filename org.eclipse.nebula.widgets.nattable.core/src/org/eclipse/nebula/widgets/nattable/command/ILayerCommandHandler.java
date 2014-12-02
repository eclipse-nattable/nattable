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
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public interface ILayerCommandHandler<T extends ILayerCommand> {

    public Class<T> getCommandClass();

    /**
     * @param targetLayer
     *            the target layer
     * @param command
     *            the command
     * @return true if the command has been handled, false otherwise
     */
    public boolean doCommand(ILayer targetLayer, T command);

}
