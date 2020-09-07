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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.swt.events.KeyEvent;

public class KeyEventMatcher implements IKeyEventMatcher {

    private int stateMask;

    private int keyCode;

    public KeyEventMatcher(int keyCode) {
        this(0, keyCode);
    }

    public KeyEventMatcher(int stateMask, int keyCode) {
        this.stateMask = stateMask;
        this.keyCode = keyCode;
    }

    public int getStateMask() {
        return this.stateMask;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    @Override
    public boolean matches(KeyEvent event) {
        boolean stateMaskMatches = this.stateMask == event.stateMask;

        boolean keyCodeMatches = this.keyCode == event.keyCode;

        return stateMaskMatches && keyCodeMatches;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyEventMatcher other = (KeyEventMatcher) obj;
        if (this.keyCode != other.keyCode)
            return false;
        if (this.stateMask != other.stateMask)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.keyCode;
        result = prime * result + this.stateMask;
        return result;
    }

}
