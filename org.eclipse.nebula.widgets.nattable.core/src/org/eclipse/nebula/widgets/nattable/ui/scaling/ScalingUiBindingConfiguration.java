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
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

/**
 * Default configuration to add bindings to change the scaling / zoom level of a
 * NatTable.
 *
 * @since 2.0
 */
public class ScalingUiBindingConfiguration extends AbstractUiBindingConfiguration {

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
     *
     * @param natTable
     *            The NatTable instance to which the scaling bindings should be
     *            added. Needed to attach the mouse scroll listener.
     */
    public ScalingUiBindingConfiguration(NatTable natTable) {
        this(natTable, false, null);
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
    public ScalingUiBindingConfiguration(NatTable natTable, Consumer<IConfigRegistry> updater) {
        this(natTable, false, updater);
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
     * @param natTable
     *            The NatTable instance to which the scaling bindings should be
     *            added. Needed to attach the mouse scroll listener.
     * @param percentageScalingChange
     *            <code>true</code> to configure that the scaling changes should
     *            be done by 10% each step. <code>false</code> will change
     *            scaling according to OS scaling options.
     * @since 2.6
     */
    public ScalingUiBindingConfiguration(NatTable natTable, boolean percentageScalingChange) {
        this(natTable, percentageScalingChange, null);
    }

    /**
     * Creates a new {@link ZoomOutScalingAction} with the given updater.
     *
     * @param percentageScalingChange
     *            <code>true</code> to configure that the scaling changes should
     *            be done by 10% each step. <code>false</code> will change
     *            scaling according to OS scaling options.
     * @param updater
     *            The updater that should be called on zoom operations. Needed
     *            to reflect the updated scaling. E.g. re-register ImagePainters
     *            like the CheckBoxPainter, otherwise the images will not be
     *            updated according to the scaling.
     * @since 2.6
     */
    public ScalingUiBindingConfiguration(NatTable natTable, boolean percentageScalingChange, Consumer<IConfigRegistry> updater) {
        if (natTable != null) {
            natTable.addMouseWheelListener(new ScalingMouseWheelListener(percentageScalingChange, updater));
        }
        this.percentageScalingChange = percentageScalingChange;
        this.updater = updater;
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // keyboard

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, '0'),
                new ResetScalingAction(this.updater));

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, '+'),
                new ZoomInScalingAction(this.percentageScalingChange, this.updater));

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, '='),
                new ZoomInScalingAction(this.percentageScalingChange, this.updater));

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, '-'),
                new ZoomOutScalingAction(this.percentageScalingChange, this.updater));

        // keypad

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, SWT.KEYPAD_0),
                new ResetScalingAction(this.updater));

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, SWT.KEYPAD_ADD),
                new ZoomInScalingAction(this.percentageScalingChange, this.updater));

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, SWT.KEYPAD_SUBTRACT),
                new ZoomOutScalingAction(this.percentageScalingChange, this.updater));

    }

}
