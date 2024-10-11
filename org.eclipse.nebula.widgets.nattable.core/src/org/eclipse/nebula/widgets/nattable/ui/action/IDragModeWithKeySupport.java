/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.ui.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.events.KeyEvent;

/**
 * Extension of the {@link IDragMode} interface that adds support to react on
 * key interactions while the drag mode is active.
 *
 * @since 2.5
 */
public interface IDragModeWithKeySupport extends IDragMode {

    void keyPressed(NatTable natTable, KeyEvent event);

    void keyReleased(NatTable natTable, KeyEvent event);

}
