/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleProxy;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * <p>
 * A cell painter that can blend multiple background colours together when more
 * than one has been registered for a given cell.
 * </p>
 * <p>
 * For example, if using the alternating row colour style, when multiple rows
 * are selected, with a normal painter, a single selection colour is applied to
 * all the selected cell's backgrounds.
 * </p>
 * <p>
 * With this painter, the selection background colour blends with the
 * alternating colour rather than just replacing it leaving two alternating
 * selection colours.
 * </p>
 *
 * @author Stefan Bolton
 *
 */
public class BlendedBackgroundPainter extends TextPainter {

    // We wont blend colours with the background colour of the grid. Otherwise,
    // if the
    // grid's background colour was white, for exmaple, this would result in all
    // background colours becoming paler than intended.
    private final RGB gridBackgroundColour;

    public BlendedBackgroundPainter(final RGB gridBackgroundColour) {
        this.gridBackgroundColour = gridBackgroundColour;
    }

    @Override
    protected Color getBackgroundColour(final ILayerCell cell,
            final IConfigRegistry configRegistry) {
        return blendBackgroundColour(cell, configRegistry,
                this.gridBackgroundColour);
    }

    /**
     * Returns a background colour for the specified cell. If multiple colours
     * have been registered, they are all blended together.
     *
     * @param cell
     *            the
     *            {@link org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell}
     *            to get a background colour for.
     * @param configRegistry
     *            an
     *            {@link org.eclipse.nebula.widgets.nattable.config.IConfigRegistry}
     *            .
     * @param baseColor
     *            Colours are not blended with this colour.
     * @return A blended background colour.
     */
    public static Color blendBackgroundColour(final ILayerCell cell,
            final IConfigRegistry configRegistry, final RGB baseColor) {

        // Get all of the background colours registered for the cell in normal
        // mode.
        final List<Color> colours = CellStyleUtil.getAllBackgroundColors(cell,
                configRegistry, DisplayMode.NORMAL);

        // If the cell is selected, get it's selected background colour and add
        // to the blending mix.
        if (cell.getDisplayMode().equals(DisplayMode.SELECT)) {
            final IStyle cellStyle = new CellStyleProxy(configRegistry,
                    DisplayMode.SELECT, cell.getConfigLabels().getLabels());
            colours.add(cellStyle
                    .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        }

        if (colours.size() == 0) {
            return null;

        } else if (colours.size() == 1) {
            return colours.get(0);

        } else {
            RGB rgb = colours.get(0).getRGB();

            for (int i = 1; i < colours.size(); i++) {
                // Don't blend with the grid background colour.
                if (rgb.equals(baseColor)) {
                    rgb = colours.get(i).getRGB();

                } else if (!colours.get(i).getRGB().equals(baseColor)) {
                    rgb = GUIHelper.blend(rgb, colours.get(i).getRGB());
                }
            }

            return GUIHelper.getColor(rgb);
        }
    }
}
