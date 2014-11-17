/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

public class TreeImagePainter extends ImagePainter {

    private Image collapsedImage;
    private Image expandedImage;
    private Image leafImage;

    /**
     * Create a TreeImagePainter that uses the default icons to show the tree
     * state.
     *
     * @param treeRowModel
     *            The ITreeRowModel to determine the tree state. <b>Is not used
     *            anymore!</b>
     *
     * @deprecated Use constructor without ITreeRowModel parameter
     */
    @Deprecated
    public TreeImagePainter(ITreeRowModel<?> treeRowModel) {
        this(
                GUIHelper.getImage("plus"), GUIHelper.getImage("minus"), GUIHelper.getImage("leaf")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Create a TreeImagePainter that uses the given icons to show the tree
     * state.
     *
     * @param treeRowModel
     *            The ITreeRowModel to determine the tree state. <b>Is not used
     *            anymore!</b>
     * @param plusImage
     *            The image that should be shown for collapsed tree nodes.
     * @param minusImage
     *            The image that should be shown for expanded tree nodes.
     * @param leafImage
     *            The image that should be shown for leafs without children.
     *
     * @deprecated Use constructor without ITreeRowModel parameter
     */
    @Deprecated
    public TreeImagePainter(ITreeRowModel<?> treeRowModel, Image plusImage,
            Image minusImage, Image leafImage) {
        this(plusImage, minusImage, leafImage);
    }

    /**
     * Create a TreeImagePainter that uses the default icons to show the tree
     * state and renders the background.
     */
    public TreeImagePainter() {
        this(true);
    }

    /**
     * Create a TreeImagePainter that uses the default icons to show the tree
     * state.
     *
     * @param paintBg
     *            <code>true</code> if it should render the background itself,
     *            <code>false</code> if the background rendering should be
     *            skipped in here.
     */
    public TreeImagePainter(boolean paintBg) {
        this(
                paintBg,
                GUIHelper.getImage("plus"), GUIHelper.getImage("minus"), GUIHelper.getImage("leaf")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Create a TreeImagePainter that uses the given icons to show the tree
     * state and renders the background.
     *
     * @param plusImage
     *            The image that should be shown for collapsed tree nodes.
     * @param minusImage
     *            The image that should be shown for expanded tree nodes.
     * @param leafImage
     *            The image that should be shown for leafs without children.
     */
    public TreeImagePainter(Image plusImage, Image minusImage, Image leafImage) {
        this(true, plusImage, minusImage, leafImage);
    }

    /**
     * Create a TreeImagePainter that uses the given icons to show the tree
     * state.
     *
     * @param paintBg
     *            <code>true</code> if it should render the background itself,
     *            <code>false</code> if the background rendering should be
     *            skipped in here.
     * @param plusImage
     *            The image that should be shown for collapsed tree nodes.
     * @param minusImage
     *            The image that should be shown for expanded tree nodes.
     * @param leafImage
     *            The image that should be shown for leafs without children.
     */
    public TreeImagePainter(boolean paintBg, Image plusImage, Image minusImage,
            Image leafImage) {
        super(null, paintBg);

        this.collapsedImage = plusImage;
        this.expandedImage = minusImage;
        this.leafImage = leafImage;
    }

    public Image getPlusImage() {
        return this.collapsedImage;
    }

    public Image getMinusImage() {
        return this.expandedImage;
    }

    public Image getLeafImage() {
        return this.leafImage;
    }

    @Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
        Image icon = null;

        if (isLeaf(cell)) {
            icon = this.leafImage;
        } else if (isCollapsed(cell)) {
            icon = this.collapsedImage;
        } else if (isExpanded(cell)) {
            icon = this.expandedImage;
        }

        return icon;
    }

    private boolean isLeaf(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(
                DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE);
    }

    private boolean isCollapsed(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(
                DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE);
    }

    private boolean isExpanded(ILayerCell cell) {
        return cell.getConfigLabels().hasLabel(
                DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE);
    }

    /**
     * Set the images that should be used to indicate the current tree state.
     *
     * @param collapsedImage
     *            The image that should be shown for collapsed tree nodes.
     * @param expandedImage
     *            The image that should be shown for expanded tree nodes.
     * @param leafImage
     *            The image that should be shown for leafs without children.
     */
    public void setExpandCollapseImages(Image collapsedImage,
            Image expandedImage, Image leafImage) {
        this.collapsedImage = collapsedImage;
        this.expandedImage = expandedImage;
        this.leafImage = leafImage;
    }

}
