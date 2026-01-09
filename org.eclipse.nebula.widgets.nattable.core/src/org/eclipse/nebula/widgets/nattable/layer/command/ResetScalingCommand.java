/*******************************************************************************
 * Copyright (c) 2026 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * This command is used to reset the scaling. It is handled in NatTable directly
 * as there is the only place to access the <code>nativeZoom</code> value.
 *
 * @since 2.7
 */
public class ResetScalingCommand extends AbstractContextFreeCommand {
}
