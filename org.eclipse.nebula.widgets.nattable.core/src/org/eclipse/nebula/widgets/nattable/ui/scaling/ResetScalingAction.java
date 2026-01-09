/*******************************************************************************
 * Copyright (c) 2020, 2026 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.scaling;

import java.util.function.Consumer;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.command.ResetScalingCommand;
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
        natTable.doCommand(new ResetScalingCommand());

        if (this.updater != null) {
            this.updater.accept(natTable.getConfigRegistry());
        }

        natTable.refresh(false);
    }

}
