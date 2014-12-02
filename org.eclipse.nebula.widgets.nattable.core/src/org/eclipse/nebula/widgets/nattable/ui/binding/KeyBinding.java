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

import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IKeyEventMatcher;

public class KeyBinding {

    private IKeyEventMatcher keyEventMatcher;

    private IKeyAction action;

    public KeyBinding(IKeyEventMatcher keyEventMatcher, IKeyAction action) {
        this.keyEventMatcher = keyEventMatcher;
        this.action = action;
    }

    public IKeyEventMatcher getKeyEventMatcher() {
        return this.keyEventMatcher;
    }

    public IKeyAction getAction() {
        return this.action;
    }

}
