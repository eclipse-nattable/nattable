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
