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
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action to zoom in a NatTable instance which means to increase the scaling
 * settings.
 *
 * @since 2.0
 */
public class ZoomInScalingAction implements IKeyAction {

    private Consumer<IConfigRegistry> updater;

    /**
     * Creates a new {@link ZoomInScalingAction} without an updater.
     * <p>
     * <b>Note:</b><br>
     * Without an updater manually registered painters will not be updated and
     * therefore won't reflect the udpated scaling. This only works in
     * combination with theme styling, as the painter update is implemented in
     * the themes internally.
     * </p>
     */
    public ZoomInScalingAction() {
        this(null);
    }

    /**
     * Creates a new {@link ZoomInScalingAction} with the given updater.
     *
     * @param updater
     *            The updater that should be called on zoom operations. Needed
     *            to reflect the updated scaling. E.g. re-register ImagePainters
     *            like the CheckBoxPainter, otherwise the images will not be
     *            updated according to the scaling.
     */
    public ZoomInScalingAction(Consumer<IConfigRegistry> updater) {
        this.updater = updater;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        ScalingUtil.zoomIn(natTable, this.updater);
    }

}
