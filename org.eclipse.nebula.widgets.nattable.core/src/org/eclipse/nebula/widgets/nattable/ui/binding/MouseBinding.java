/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    public MouseBinding(IMouseEventMatcher mouseEventMatcher,
            IMouseAction action) {
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
