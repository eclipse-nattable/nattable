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
package org.eclipse.nebula.widgets.nattable.ui.scaling;

import java.util.function.Consumer;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.DefaultHorizontalDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.DefaultVerticalDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action to reset any scaling/zoom settings in a NatTable instance to the
 * display scaling settings.
 *
 * @since 2.0
 */
public class ResetScalingAction implements IKeyAction {

    private Consumer<IConfigRegistry> updater;

    /**
     * Creates a new {@link ResetScalingAction} without an updater.
     * <p>
     * <b>Note:</b><br>
     * Without an updater manually registered painters will not be updated and
     * therefore won't reflect the udpated scaling. This only works in
     * combination with theme styling, as the painter update is implemented in
     * the themes internally.
     * </p>
     */
    public ResetScalingAction() {
        this(null);
    }

    /**
     * Creates a new {@link ResetScalingAction} with the given updater.
     *
     * @param updater
     *            The updater that should be called on zoom operations. Needed
     *            to reflect the updated scaling. E.g. re-register ImagePainters
     *            like the CheckBoxPainter, otherwise the images will not be
     *            updated according to the scaling.
     */
    public ResetScalingAction(Consumer<IConfigRegistry> updater) {
        this.updater = updater;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        natTable.doCommand(new ConfigureScalingCommand(
                new DefaultHorizontalDpiConverter(),
                new DefaultVerticalDpiConverter()));

        if (this.updater != null) {
            this.updater.accept(natTable.getConfigRegistry());
        }

        natTable.refresh();
    }

}
