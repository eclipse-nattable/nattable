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
package org.eclipse.nebula.widgets.nattable.print.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * This command is handled by the viewport. It essentially causes the viewport
 * to turn off by relaying all dimension requests to the underlying scrollable
 * layer.
 *
 * This is useful when operations have to be performed on the entire grid
 * including the areas outside the viewport. Example printing, excel export,
 * auto resize all columns etc.
 */
public class TurnViewportOffCommand extends AbstractContextFreeCommand {
}
