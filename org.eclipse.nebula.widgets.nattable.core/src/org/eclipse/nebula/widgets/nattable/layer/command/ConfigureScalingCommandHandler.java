/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;

/**
 * {@link ILayerCommandHandler} for handling {@link ConfigureScalingCommand}s.
 * Simply updates the {@link SizeConfig} instances with the transported
 * {@link IDpiConverter}.
 */
public class ConfigureScalingCommandHandler
        implements ILayerCommandHandler<ConfigureScalingCommand> {

    private final SizeConfig columnWidthConfig;
    private final SizeConfig rowHeightConfig;

    /**
     * Create a new {@link ConfigureScalingCommandHandler} that updates the
     * given {@link SizeConfig} instances on handling a
     * {@link ConfigureScalingCommand}.
     * <p>
     * Note: In case no {@link SizeConfig} instance is registered, this command
     * handler will do nothing.
     * </p>
     *
     * @param columnWidthConfig
     *            The {@link SizeConfig} for column width configuration. Can be
     *            <code>null</code>.
     * @param rowHeightConfig
     *            The {@link SizeConfig} for row height configuration. Can be
     *            <code>null</code>.
     */
    public ConfigureScalingCommandHandler(
            SizeConfig columnWidthConfig, SizeConfig rowHeightConfig) {
        this.columnWidthConfig = columnWidthConfig;
        this.rowHeightConfig = rowHeightConfig;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, ConfigureScalingCommand command) {
        if (this.columnWidthConfig != null) {
            this.columnWidthConfig.setDpiConverter(
                    command.getHorizontalDpiConverter());
        }
        if (this.rowHeightConfig != null) {
            this.rowHeightConfig.setDpiConverter(
                    command.getVerticalDpiConverter());
        }
        // always return false to ensure the command is processed further
        // and so all layers that are interested are able to react
        return false;
    }

    @Override
    public Class<ConfigureScalingCommand> getCommandClass() {
        return ConfigureScalingCommand.class;
    }

}
