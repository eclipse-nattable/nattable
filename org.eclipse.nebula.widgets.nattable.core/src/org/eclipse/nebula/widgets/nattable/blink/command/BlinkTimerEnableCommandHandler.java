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
package org.eclipse.nebula.widgets.nattable.blink.command;

import org.eclipse.nebula.widgets.nattable.blink.BlinkLayer;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;

public class BlinkTimerEnableCommandHandler extends AbstractLayerCommandHandler<BlinkTimerEnableCommand> {

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
