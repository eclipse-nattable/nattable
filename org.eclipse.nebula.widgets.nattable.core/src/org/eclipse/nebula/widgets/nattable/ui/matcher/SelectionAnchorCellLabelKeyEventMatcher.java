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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.events.KeyEvent;

/**
 * This class allows checking the current selection anchor's cell for a matching
 * label. Only labels from the given {@link IUniqueIndexLayer} downwards are
 * considered. Optionally the matcher can be chained with an additional
 * {@link IKeyEventMatcher} (results are and'ed).
 */
public class SelectionAnchorCellLabelKeyEventMatcher implements IKeyEventMatcher {

    protected final SelectionLayer selectionLayer;
    protected final IUniqueIndexLayer layer;
    protected final String labelToMatch;
    protected final IKeyEventMatcher aggregate;

    /**
     * Create a {@link SelectionAnchorCellLabelKeyEventMatcher} that checks only
     * if the given label is assigned to the selection anchor's cell, while the
     * label stack is retrieved from the given layer.
     *
     * @param selectionLayer
     *            the {@link SelectionLayer} used to get the current selection
     *            anchor's position.
     * @param layer
     *            the layer used to retrieve the label stack at the selection
     *            anchor's position.
     * @param labelToMatch
     *            the label that should be part of the label stack at the
     *            selection anchor's position.
     */
    public SelectionAnchorCellLabelKeyEventMatcher(SelectionLayer selectionLayer, IUniqueIndexLayer layer, String labelToMatch) {
        this(selectionLayer, layer, labelToMatch, null);
    }

    /**
     * Create a {@link SelectionAnchorCellLabelKeyEventMatcher} that checks if
     * the aggregate matches as well as if the given label is assigned to the
     * selection anchor's cell, while the label stack is retrieved from the
     * given layer.
     *
     * @param selectionLayer
     *            the {@link SelectionLayer} used to get the current selection
     *            anchor's position.
     * @param layer
     *            the layer used to retrieve the label stack at the selection
     *            anchor's position.
     * @param labelToMatch
     *            the label that should be part of the label stack at the
     *            selection anchor's position.
     * @param aggregate
     *            (optional) an additional {@link IKeyEventMatcher} to be
     *            chained with the result of this matcher (results are and'ed).
     */
    public SelectionAnchorCellLabelKeyEventMatcher(SelectionLayer selectionLayer, IUniqueIndexLayer layer, String labelToMatch, IKeyEventMatcher aggregate) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("selectionLayer must not be null."); //$NON-NLS-1$
        }
        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null."); //$NON-NLS-1$
        }
        if (labelToMatch == null || labelToMatch.length() == 0) {
            throw new IllegalArgumentException("labelToMatch must not be blank."); //$NON-NLS-1$
        }

        this.selectionLayer = selectionLayer;
        this.layer = layer;
        this.labelToMatch = labelToMatch;
        this.aggregate = aggregate;
    }

    @Override
    public boolean matches(KeyEvent event) {
        PositionCoordinate anchorPosition = this.selectionLayer.getSelectionAnchor();

        if (anchorPosition.rowPosition != SelectionLayer.NO_SELECTION && anchorPosition.columnPosition != SelectionLayer.NO_SELECTION) {
            int layerColumnPosition = LayerUtil.convertColumnPosition(this.selectionLayer, anchorPosition.columnPosition, this.layer);
            int layerRowPosition = LayerUtil.convertRowPosition(this.selectionLayer, anchorPosition.rowPosition, this.layer);

            LabelStack labels = this.layer.getConfigLabelsByPosition(layerColumnPosition, layerRowPosition);
            boolean labelMatches = labels.hasLabel(this.labelToMatch);
            if (this.aggregate != null) {
                return labelMatches && this.aggregate.matches(event);
            }
            return labelMatches;
        }
        return false;
    }

    /**
     * Create a {@link SelectionAnchorCellLabelKeyEventMatcher} that checks only
     * if the given label is assigned to the selection anchor's cell, while the
     * label stack is retrieved from the given layer.
     *
     * @param selectionLayer
     *            the {@link SelectionLayer} used to get the current selection
     *            anchor's position.
     * @param layer
     *            the layer used to retrieve the label stack at the selection
     *            anchor's position.
     * @param labelToMatch
     *            the label that should be part of the label stack at the
     *            selection anchor's position.
     */
    public static SelectionAnchorCellLabelKeyEventMatcher anchorLabel(SelectionLayer selectionLayer, IUniqueIndexLayer layer, String labelToMatch) {
        return anchorLabel(selectionLayer, layer, labelToMatch, null);
    }

    /**
     * Create a {@link SelectionAnchorCellLabelKeyEventMatcher} that checks if
     * the aggregate matches as well as if the given label is assigned to the
     * selection anchor's cell, while the label stack is retrieved from the
     * given layer.
     *
     * @param selectionLayer
     *            the {@link SelectionLayer} used to get the current selection
     *            anchor's position.
     * @param layer
     *            the layer used to retrieve the label stack at the selection
     *            anchor's position.
     * @param labelToMatch
     *            the label that should be part of the label stack at the
     *            selection anchor's position.
     * @param aggregate
     *            (optional) an additional {@link IKeyEventMatcher} to be
     *            chained with the result of this matcher (results are and'ed).
     */
    public static SelectionAnchorCellLabelKeyEventMatcher anchorLabel(SelectionLayer selectionLayer, IUniqueIndexLayer layer, String labelToMatch, IKeyEventMatcher aggregate) {
        return new SelectionAnchorCellLabelKeyEventMatcher(selectionLayer, layer, labelToMatch, aggregate);
    }

}
