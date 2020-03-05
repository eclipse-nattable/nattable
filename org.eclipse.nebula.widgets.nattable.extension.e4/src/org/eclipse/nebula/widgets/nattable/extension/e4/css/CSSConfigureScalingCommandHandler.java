/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.css;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;

/**
 * {@link ILayerCommandHandler} for the {@link ConfigureScalingCommand} that
 * actively triggers an update of the CSS styling.
 *
 * @since 1.3
 */
@SuppressWarnings("restriction")
public class CSSConfigureScalingCommandHandler implements ILayerCommandHandler<ConfigureScalingCommand> {

    private final NatTable natTable;

    /**
     *
     * @param natTable
     *            The NatTable instance to which this command handler should be
     *            bound. Needed to trigger the styling update on the NatTable
     *            Widget.
     */
    public CSSConfigureScalingCommandHandler(NatTable natTable) {
        this.natTable = natTable;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, ConfigureScalingCommand command) {
        CSSEngine engine = WidgetElement.getEngine(this.natTable);
        engine.applyStyles(this.natTable, true);

        // do not consume the command so it is processed further down the layer
        // stack
        return false;
    }

    @Override
    public Class<ConfigureScalingCommand> getCommandClass() {
        return ConfigureScalingCommand.class;
    }

}
