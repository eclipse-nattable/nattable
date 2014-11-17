/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

/**
 * Paints the triangular expand/collapse row header images. It is used to
 * decorate row header cells to show the current row group expand/collapse
 * state.
 */
public class RowGroupExpandCollapseImagePainter extends ImagePainter {

    private Image collapsedImage;
    private Image expandedImage;

    /**
     * Create a RowGroupExpandCollapseImagePainter that uses the default icons
     * (black triangles) and renders the background.
     */
    public RowGroupExpandCollapseImagePainter() {
        this(true);
    }

    /**
     * Create a RowGroupExpandCollapseImagePainter that uses the default icons
     * (black triangles).
     *
     * @param paintBg
     *            <code>true</code> if it should render the background itself,
     *            <code>false</code> if the background rendering should be
     *            skipped in here.
     */
    public RowGroupExpandCollapseImagePainter(boolean paintBg) {
        this(paintBg, false);
    }

    /**
     * Create a RowGroupExpandCollapseImagePainter.
     *
     * @param paintBg
     *            <code>true</code> if it should render the background itself,
     *            <code>false</code> if the background rendering should be
     *            skipped in here.
     * @param invertIcons
     *            Specify whether the default icons should be used (black
     *            triangles) or if inverted icons should be used (white
     *            triangles).
     */
    public RowGroupExpandCollapseImagePainter(boolean paintBg, boolean invertIcons) {
        super(null, paintBg);

        String postFix = ""; //$NON-NLS-1$
        if (invertIcons)
            postFix = "_inv"; //$NON-NLS-1$

        this.collapsedImage = GUIHelper.getImage("down_0" + postFix); //$NON-NLS-1$
        this.expandedImage = GUIHelper.getImage("up_0" + postFix); //$NON-NLS-1$
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Image icon = null;

        if (isCollapsed(cell)) {
            icon = this.collapsedImage;
        } else if (isExpanded(cell)) {
            icon = this.expandedImage;
        }

        return icon;
    }

    private boolean isCollapsed(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(DefaultRowGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE);
    }

    private boolean isExpanded(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(DefaultRowGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE);
    }

    /**
     * Set the images that should be used to indicate the current row group
     * expand/collapse state.
     *
     * @param expandedImage
     *            Image to be used to indicate that a row group is expanded.
     * @param collapsedImage
     *            Image to be used to indicate that a row group is collapsed.
     */
    public void setExpandCollapseImages(Image expandedImage, Image collapsedImage) {
        this.collapsedImage = collapsedImage;
        this.expandedImage = expandedImage;
    }

}