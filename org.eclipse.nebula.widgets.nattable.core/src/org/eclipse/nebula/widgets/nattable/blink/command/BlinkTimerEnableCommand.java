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
package org.eclipse.nebula.widgets.nattable.blink.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

public class BlinkTimerEnableCommand extends AbstractContextFreeCommand {

    private boolean enableBlinkTimer;

    public BlinkTimerEnableCommand(boolean enableBlinkTimer) {
        this.enableBlinkTimer = enableBlinkTimer;
    }

    public boolean isEnableBlinkTimer() {
        return this.enableBlinkTimer;
    }

    public void setEnableBlinkTimer(boolean enableBlinkTimer) {
        this.enableBlinkTimer = enableBlinkTimer;
    }
}
