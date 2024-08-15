/*****************************************************************************
 * Copyright (c) 2015, 2024 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.event;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleBoundsProvider;
import org.eclipse.nebula.widgets.nattable.fillhandle.FillHandleLayerPainter;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

/**
 * Matcher that returns <code>true</code> in case the mouse moves over the fill
 * handle rendered by the {@link FillHandleLayerPainter}.
 *
 * @since 1.4
 */
public class FillHandleEventMatcher extends MouseEventMatcher {

    @Deprecated
    protected FillHandleLayerPainter fillHandlePainter;
    /**
     * @since 2.5
     */
    protected FillHandleBoundsProvider fillHandleBoundsProvider;

    /**
     * Create a {@link FillHandleEventMatcher} that reacts when the mouse is
     * moved over the fill handle rendered by the given
     * {@link FillHandleLayerPainter}.
     *
     * @param fillHandlePainter
     *            The {@link FillHandleLayerPainter} that should be used to
     *            determine the bounds of the fill handle. Can not be
     *            <code>null</code>.
     */
    @Deprecated
    public FillHandleEventMatcher(FillHandleLayerPainter fillHandlePainter) {
        if (fillHandlePainter == null) {
            throw new IllegalArgumentException("FillHandleLayerPainter can not be null"); //$NON-NLS-1$
        }
        this.fillHandlePainter = fillHandlePainter;
        this.fillHandleBoundsProvider = fillHandlePainter;
    }

    /**
     * Create a {@link FillHandleEventMatcher} that reacts when the mouse is
     * moved over the fill handle bounds provided by the given
     * {@link FillHandleBoundsProvider}.
     *
     * @param fillHandleBoundsProvider
     *            The {@link FillHandleBoundsProvider} that should be used to
     *            determine the bounds of the fill handle. Can not be
     *            <code>null</code>.
     * @since 2.5
     */
    public FillHandleEventMatcher(FillHandleBoundsProvider fillHandleBoundsProvider) {
        if (fillHandleBoundsProvider == null) {
            throw new IllegalArgumentException("FillHandleBoundsProvider can not be null"); //$NON-NLS-1$
        }
        this.fillHandleBoundsProvider = fillHandleBoundsProvider;
    }

    @Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
        if (this.fillHandleBoundsProvider.getSelectionHandleBounds() != null) {
            return this.fillHandleBoundsProvider.getSelectionHandleBounds().contains(event.x, event.y);
        }
        return false;
    }

}
