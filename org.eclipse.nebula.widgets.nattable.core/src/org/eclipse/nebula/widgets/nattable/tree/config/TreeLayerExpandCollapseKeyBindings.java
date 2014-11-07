/*******************************************************************************
 * Copyright (c) 2014 Roman Flueckiger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roman Flueckiger <roman.flueckiger@mac.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.config;

import static org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE;
import static org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE;
import static org.eclipse.nebula.widgets.nattable.ui.matcher.SelectionAnchorCellLabelKeyEventMatcher.anchorLabel;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

/**
 * A default configuration that can be used as an addition for a nattable with
 * both a {@link TreeLayer} and a {@link SelectionLayer}. It adds the key
 * bindings that allows the space bar to be pressed to expand/collapse tree
 * nodes.
 */
public class TreeLayerExpandCollapseKeyBindings extends AbstractUiBindingConfiguration {

    protected final TreeLayer treeLayer;
    protected final SelectionLayer selectionLayer;

    /**
     * @param treeLayer
     *            provides the necessary labels to detect tree nodes that can be
     *            expanded/collapsed.
     * @param selectionLayer
     *            the {@link SelectionLayer} - provides the selection anchor
     *            (the context for the action).
     */
    public TreeLayerExpandCollapseKeyBindings(TreeLayer treeLayer, SelectionLayer selectionLayer) {
        if (treeLayer == null) {
            throw new IllegalArgumentException("treeLayer must not be null."); //$NON-NLS-1$
        }
        if (selectionLayer == null) {
            throw new IllegalArgumentException("selectionLayer must not be null."); //$NON-NLS-1$
        }

        this.treeLayer = treeLayer;
        this.selectionLayer = selectionLayer;
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        TreeExpandCollapseKeyAction action = new TreeExpandCollapseKeyAction(this.selectionLayer);
        uiBindingRegistry.registerFirstKeyBinding(anchorLabel(this.selectionLayer, this.treeLayer, TREE_EXPANDED_CONFIG_TYPE, new KeyEventMatcher(SWT.NONE, 32)), action);
        uiBindingRegistry.registerFirstKeyBinding(anchorLabel(this.selectionLayer, this.treeLayer, TREE_COLLAPSED_CONFIG_TYPE, new KeyEventMatcher(SWT.NONE, 32)), action);
    }

}
