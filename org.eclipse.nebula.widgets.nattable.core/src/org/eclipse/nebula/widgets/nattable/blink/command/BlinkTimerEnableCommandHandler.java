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

import org.eclipse.nebula.widgets.nattable.blink.BlinkLayer;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;

public class BlinkTimerEnableCommandHandler extends
        AbstractLayerCommandHandler<BlinkTimerEnableCommand> {

    private final BlinkLayer<?> blinkLayer;

    public BlinkTimerEnableCommandHandler(BlinkLayer<?> blinkLayer) {
        this.blinkLayer = blinkLayer;
    }

    @Override
    public Class<BlinkTimerEnableCommand> getCommandClass() {
        return BlinkTimerEnableCommand.class;
    }

    @Override
    protected boolean doCommand(BlinkTimerEnableCommand command) {
        this.blinkLayer.setBlinkingEnabled(command.isEnableBlinkTimer());
        return true;
    }

}
