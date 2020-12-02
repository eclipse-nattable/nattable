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
import org.eclipse.nebula.widgets.nattable.config.NatTableConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

/**
 * Helper class for scaling calculations.
 *
 * @since 2.0
 */
public final class ScalingUtil {

    private ScalingUtil() {
        // private default constructor for helper class
    }

    /**
     * Performs a {@link ConfigureScalingCommand} to increase the scaling to
     * "zoom in" a NatTable.
     *
     * @param natTable
     *            The {@link NatTable} instance on which the zoom in operation
     *            should be performed.
     * @param registryUpdater
     *            Callback to trigger registry updates. Needed to re-register
     *            image cell painters for example.
     */
    public static void zoomIn(NatTable natTable, Consumer<IConfigRegistry> registryUpdater) {

        IConfigRegistry configRegistry = natTable.getConfigRegistry();

        IDpiConverter hDpiConverter = configRegistry.getConfigAttribute(
                NatTableConfigAttributes.HORIZONTAL_DPI_CONVERTER,
                DisplayMode.NORMAL);
        int hdpi = hDpiConverter.getDpi();
        int newHorizontalDpi = getNewZoomInDPI(hdpi);

        IDpiConverter vDpiConverter = configRegistry.getConfigAttribute(
                NatTableConfigAttributes.VERTICAL_DPI_CONVERTER,
                DisplayMode.NORMAL);
        int vdpi = vDpiConverter.getDpi();
        int newVerticalDpi = getNewZoomInDPI(vdpi);

        // only perform an update if dpi values have changed
        if (hdpi != newHorizontalDpi || vdpi != newVerticalDpi) {
            natTable.doCommand(new ConfigureScalingCommand(
                    new FixedScalingDpiConverter(newHorizontalDpi),
                    new FixedScalingDpiConverter(newVerticalDpi)));

            if (registryUpdater != null) {
                registryUpdater.accept(configRegistry);
            }

            natTable.refresh(false);
        }
    }

    /**
     * Performs a {@link ConfigureScalingCommand} to decrease the scaling to
     * "zoom out" a NatTable.
     *
     * @param natTable
     *            The {@link NatTable} instance on which the zoom in operation
     *            should be performed.
     * @param registryUpdater
     *            Callback to trigger registry updates. Needed to re-register
     *            image cell painters for example.
     */
    public static void zoomOut(NatTable natTable, Consumer<IConfigRegistry> registryUpdater) {

        IConfigRegistry configRegistry = natTable.getConfigRegistry();

        IDpiConverter hDpiConverter = configRegistry.getConfigAttribute(
                NatTableConfigAttributes.HORIZONTAL_DPI_CONVERTER,
                DisplayMode.NORMAL);
        int hdpi = hDpiConverter.getDpi();
        int newHorizontalDpi = getNewZoomOutDPI(hdpi);

        IDpiConverter vDpiConverter = configRegistry.getConfigAttribute(
                NatTableConfigAttributes.VERTICAL_DPI_CONVERTER,
                DisplayMode.NORMAL);
        int vdpi = vDpiConverter.getDpi();
        int newVerticalDpi = getNewZoomOutDPI(vdpi);

        // only perform an update if dpi values have changed
        if (hdpi != newHorizontalDpi || vdpi != newVerticalDpi) {
            natTable.doCommand(new ConfigureScalingCommand(
                    new FixedScalingDpiConverter(newHorizontalDpi),
                    new FixedScalingDpiConverter(newVerticalDpi)));

            if (registryUpdater != null) {
                registryUpdater.accept(configRegistry);
            }

            natTable.refresh(false);
        }
    }

    /**
     * Get the new DPI value for zoom out based on the given DPI value. The
     * minimum value is 12.
     *
     * @param currentDPI
     *            The current active DPI value.
     * @return The new DPI value for zooming out.
     */
    public static int getNewZoomOutDPI(int currentDPI) {
        int dpi = currentDPI;
        if (currentDPI == 288) {
            dpi = 192;
        } else if (currentDPI == 192) {
            dpi = 144;
        } else if (currentDPI == 144) {
            dpi = 120;
        } else if (currentDPI == 120) {
            dpi = 96;
        } else if (currentDPI <= 96) {
            dpi = Math.max(12, dpi / 2);
        }
        return dpi;
    }

    /**
     * Get the new DPI value for zoom in based on the given DPI value. The
     * maximum value is 288.
     *
     * @param currentDPI
     *            The current active DPI value.
     * @return The new DPI value for zooming in.
     */
    public static int getNewZoomInDPI(int currentDPI) {
        int dpi = currentDPI;
        if (currentDPI == 192) {
            dpi = 288;
        } else if (currentDPI == 144) {
            dpi = 192;
        } else if (currentDPI == 120) {
            dpi = 144;
        } else if (currentDPI == 96) {
            dpi = 120;
        } else if (currentDPI < 96) {
            dpi = dpi * 2;
        }

        return dpi;
    }

}
