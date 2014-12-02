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

import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;

public class DragBinding {

    private IMouseEventMatcher mouseEventMatcher;

    private IDragMode dragMode;

    public DragBinding(IMouseEventMatcher mouseEventMatcher, IDragMode dragMode) {
        this.mouseEventMatcher = mouseEventMatcher;
        this.dragMode = dragMode;
    }

    public IMouseEventMatcher getMouseEventMatcher() {
        return this.mouseEventMatcher;
    }

    public IDragMode getDragMode() {
        return this.dragMode;
    }

}
