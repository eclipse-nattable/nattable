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
package org.eclipse.nebula.widgets.nattable.ui.binding;

import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public interface IUiBindingRegistry {

    public IKeyAction getKeyEventAction(KeyEvent event);

    public IDragMode getDragMode(MouseEvent event);

    public IMouseAction getMouseMoveAction(MouseEvent event);

    public IMouseAction getMouseDownAction(MouseEvent event);

    public IMouseAction getSingleClickAction(MouseEvent event);

    public IMouseAction getDoubleClickAction(MouseEvent event);

    public IMouseAction getMouseHoverAction(MouseEvent event);

    public IMouseAction getMouseEnterAction(MouseEvent event);

    public IMouseAction getMouseExitAction(MouseEvent event);

}
