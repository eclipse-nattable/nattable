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
