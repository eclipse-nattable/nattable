/*******************************************************************************
 * Copyright (c) 2020, 2024 Dirk Fauth and others.
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
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action to zoom in a NatTable instance which means to increase the scaling
 * settings.
 *
 * @since 2.0
 */
public class ZoomOutScalingAction implements IKeyAction {

    private Consumer<IConfigRegistry> updater;
    private boolean percentageScalingChange = false;

    /**
     * Creates a new {@link ZoomOutScalingAction} without an updater.
     * <p>
     * <b>Note:</b><br>
     * Without an updater manually registered painters will not be updated and
     * therefore won't reflect the udpated scaling. This only works in
     * combination with theme styling, as the painter update is implemented in
     * the themes internally.
     * </p>
     */
    public ZoomOutScalingAction() {
        this(false, null);
    }

    /**
     * Creates a new {@link ZoomOutScalingAction} with the given updater.
     *
     * @param updater
     *            The updater that should be called on zoom operations. Needed
     *            to reflect the updated scaling. E.g. re-register ImagePainters
     *            like the CheckBoxPainter, otherwise the images will not be
     *            updated according to the scaling.
     */
    public ZoomOutScalingAction(Consumer<IConfigRegistry> updater) {
        this(false, updater);
    }

    /**
     * Creates a new {@link ZoomOutScalingAction} without an updater.
     * <p>
     * <b>Note:</b><br>
     * Without an updater manually registered painters will not be updated and
     * therefore won't reflect the udpated scaling. This only works in
     * combination with theme styling, as the painter update is implemented in
     * the themes internally.
     * </p>
     *
     * @since 2.6
     */
    public ZoomOutScalingAction(boolean percentageScalingChange) {
        this(percentageScalingChange, null);
    }

    /**
     * Creates a new {@link ZoomOutScalingAction} with the given updater.
     *
     * @param updater
     *            The updater that should be called on zoom operations. Needed
     *            to reflect the updated scaling. E.g. re-register ImagePainters
     *            like the CheckBoxPainter, otherwise the images will not be
     *            updated according to the scaling.
     * @since 2.6
     */
    public ZoomOutScalingAction(boolean percentageScalingChange, Consumer<IConfigRegistry> updater) {
        this.percentageScalingChange = percentageScalingChange;
        this.updater = updater;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        ScalingUtil.zoomOut(natTable, this.percentageScalingChange, this.updater);
    }

}
