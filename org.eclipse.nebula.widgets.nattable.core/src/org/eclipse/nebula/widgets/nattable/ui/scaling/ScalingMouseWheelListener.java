/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth and others.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

/**
 * A {@link MouseWheelListener} that can be added to a NatTable instance to
 * support zoom operations while having the CTRL key pressed and the mousewheel
 * scrolled. Will support 12 dpi min and 288 dpi max.
 * <p>
 * To support zooming of images (e.g. checkboxes) the registration of the
 * ImagePainter need to be overriden in updatePainter().
 * </p>
 *
 * @since 2.0
 */
public class ScalingMouseWheelListener implements MouseWheelListener {

    private Consumer<IConfigRegistry> updater;

    /**
     * Creates a new {@link ScalingMouseWheelListener} without an updater.
     * <p>
     * <b>Note:</b><br>
     * Without an updater manually registered painters will not be updated and
     * therefore won't reflect the updated scaling. This only works in
     * combination with theme styling, as the painter update is implemented in
     * the themes internally.
     * </p>
     */
    public ScalingMouseWheelListener() {
        this(null);
    }

    /**
     * Creates a new {@link ScalingMouseWheelListener} with the given updater.
     *
     * @param updater
     *            The updater that should be called on zoom operations. Needed
     *            to reflect the updated scaling. E.g. re-register ImagePainters
     *            like the CheckBoxPainter, otherwise the images will not be
     *            updated according to the scaling.
     */
    public ScalingMouseWheelListener(Consumer<IConfigRegistry> updater) {
        this.updater = updater;
    }

    @Override
    public void mouseScrolled(MouseEvent e) {
        if (e.widget instanceof NatTable && e.stateMask == SWT.MOD1) {
            NatTable natTable = (NatTable) e.widget;

            if (e.count < 0) {
                ScalingUtil.zoomOut(natTable, this.updater);
            } else {
                ScalingUtil.zoomIn(natTable, this.updater);
            }
        }
    }
}