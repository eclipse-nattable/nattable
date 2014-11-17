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

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.SizeConfig;

/**
 * This command is used to configure scaling behavior. It transports
 * {@link IDpiConverter} for horizontal and vertical dpi based scaling down the
 * layer stack. This way every layer that directly performs pixel based size
 * calculations is able to consume the converter.
 * <p>
 * <b>IMPORTANT</b><br>
 * This command is not intended to be consumed at any layer. It is necessary
 * that it is transported down the whole layer stack of all layer regions so
 * every layer has the chance to consume the {@link IDpiConverter}.
 * </p>
 * <p>
 * The registering of {@link IDpiConverter} for automatic scaling is done via
 * this command to avoid dependencies between {@link SizeConfig},
 * {@link DataLayer} and the {@link IDpiConverter}. This is necessary because
 * every UI toolkit has its own mechanism to provide the current DPI values.
 * </p>
 * <p>
 * DPI factors could be different for x and y but the Windows API always
 * delivers the same values for x and y. To avoid later refactorings because
 * this behavior changes, this command transports an {@link IDpiConverter} for
 * horizontal and vertical DPI conversion.
 * </p>
 */
public class ConfigureScalingCommand extends AbstractContextFreeCommand {

    private final IDpiConverter horizontalDpiConverter;
    private final IDpiConverter verticalDpiConverter;

    /**
     * @param horizontalDpiConverter
     *            The {@link IDpiConverter} that should be registered for
     *            horizontal DPI conversion.
     * @param verticalDpiConverter
     *            The {@link IDpiConverter} that should be registered for
     *            vertical DPI conversion.
     */
    public ConfigureScalingCommand(IDpiConverter horizontalDpiConverter, IDpiConverter verticalDpiConverter) {
        this.horizontalDpiConverter = horizontalDpiConverter;
        this.verticalDpiConverter = verticalDpiConverter;
    }

    /**
     *
     * @return The {@link IDpiConverter} that should be registered for
     *         horizontal DPI conversion.
     */
    public IDpiConverter getHorizontalDpiConverter() {
        return this.horizontalDpiConverter;
    }

    /**
     *
     * @return The {@link IDpiConverter} that should be registered for vertical
     *         DPI conversion.
     */
    public IDpiConverter getVerticalDpiConverter() {
        return this.verticalDpiConverter;
    }
}
