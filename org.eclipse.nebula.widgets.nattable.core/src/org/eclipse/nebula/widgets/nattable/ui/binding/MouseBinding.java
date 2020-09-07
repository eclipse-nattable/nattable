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

import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;

public class MouseBinding {

    private IMouseEventMatcher mouseEventMatcher;

    private IMouseAction action;

    public MouseBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        this.mouseEventMatcher = mouseEventMatcher;
        this.action = action;
    }

    public IMouseEventMatcher getMouseEventMatcher() {
        return this.mouseEventMatcher;
    }

    public IMouseAction getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[mouseEventMatcher=" + this.mouseEventMatcher + " action=" + this.action + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
