/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - enhancement for extensions
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.sort.painter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

/**
 * Paints the triangular sort icon images. It is used to decorate column header
 * cells to show the current sort state.
 */
public class SortIconPainter extends ImagePainter {

    private List<Image> upImages = new ArrayList<>();
    private List<Image> downImages = new ArrayList<>();

    /**
     * Create a SortIconPainter that uses the default icons (black triangles)
     * and renders the background.
     */
    public SortIconPainter() {
        this(true);
    }

    /**
     * Create a SortIconPainter that uses the default icons (black triangles).
     *
     * @param paintBg
     *            <code>true</code> if it should render the background itself,
     *            <code>false</code> if the background rendering should be
     *            skipped in here.
     */
    public SortIconPainter(boolean paintBg) {
        this(paintBg, false);
    }

    /**
     * Create a SortIconPainter.
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
    public SortIconPainter(boolean paintBg, boolean invertIcons) {
        super(null, paintBg);

        String postFix = ""; //$NON-NLS-1$
        if (invertIcons) {
            postFix = "_inv"; //$NON-NLS-1$
        }

        this.upImages.add(GUIHelper.getImage("up_0" + postFix)); //$NON-NLS-1$
        this.upImages.add(GUIHelper.getImage("up_1" + postFix)); //$NON-NLS-1$
        this.upImages.add(GUIHelper.getImage("up_2" + postFix)); //$NON-NLS-1$

        this.downImages.add(GUIHelper.getImage("down_0" + postFix)); //$NON-NLS-1$
        this.downImages.add(GUIHelper.getImage("down_1" + postFix)); //$NON-NLS-1$
        this.downImages.add(GUIHelper.getImage("down_2" + postFix)); //$NON-NLS-1$
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Image icon = null;

        if (isSortedAscending(cell)) {
            icon = selectUpImage(getSortSequence(cell));
        } else if (isSortedDescending(cell)) {
            icon = selectDownImage(getSortSequence(cell));
        }

        return icon;
    }

    private boolean isSortedAscending(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE);
    }

    private boolean isSortedDescending(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE);
    }

    private int getSortSequence(ILayerCell cell) {
        int sortSeq = 0;

        for (String configLabel : cell.getConfigLabels()) {
            if (configLabel.startsWith(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE)) {
                String[] tokens = configLabel.split("_"); //$NON-NLS-1$
                sortSeq = Integer.parseInt(tokens[tokens.length - 1]);
            }
        }
        return sortSeq;
    }

    /**
     * Returns the sort icon that should be used to indicate ascending sorting
     * state.
     *
     * @param sortSequence
     *            The sort sequence number for which the sort icon is requested.
     * @return The sort icon that should be rendered to indicate the current
     *         sort state.
     */
    protected Image selectUpImage(int sortSequence) {
        return (sortSequence < this.upImages.size())
                ? this.upImages.get(sortSequence)
                : this.upImages.get(this.upImages.size() - 1);
    }

    /**
     * Returns the sort icon that should be used to indicate descending sorting
     * state.
     *
     * @param sortSequence
     *            The sort sequence number for which the sort icon is requested.
     * @return The sort icon that should be rendered to indicate the current
     *         sort state.
     */
    protected Image selectDownImage(int sortSequence) {
        return (sortSequence < this.downImages.size())
                ? this.downImages.get(sortSequence)
                : this.downImages.get(this.downImages.size() - 1);
    }

    /**
     * Set the images that should be used to indicate the current sort state.
     *
     * @param upImage0
     *            Image to be used to indicate first level ascending sorting.
     * @param upImage1
     *            Image to be used to indicate second level ascending sorting.
     * @param upImage2
     *            Image to be used to indicate third level ascending sorting.
     * @param downImage0
     *            Image to be used to indicate first level descending sorting.
     * @param downImage1
     *            Image to be used to indicate second level descending sorting.
     * @param downImage2
     *            Image to be used to indicate third level descending sorting.
     */
    public void setSortImages(
            Image upImage0, Image upImage1, Image upImage2,
            Image downImage0, Image downImage1, Image downImage2) {

        this.upImages.clear();
        this.upImages.add(upImage0);
        this.upImages.add(upImage1);
        this.upImages.add(upImage2);

        this.downImages.clear();
        this.downImages.add(downImage0);
        this.downImages.add(downImage1);
        this.downImages.add(downImage2);
    }

    /**
     * Set the images that should be used to indicate the current sort state.
     *
     * @param upImages
     *            Images that should be used to indicate ascending sorting.
     * @param downImages
     *            Images that should be used to indicate descending sorting.
     *
     * @since 2.3
     */
    public void setSortImages(List<Image> upImages, List<Image> downImages) {

        this.upImages = upImages;
        this.downImages = downImages;
    }
}