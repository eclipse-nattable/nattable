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
