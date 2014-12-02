/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.graphics.Rectangle;

public abstract class AbstractLayerCell implements ILayerCell {

    private boolean isDisplayModeCached = false;
    private String displayMode = null;

    private boolean isConfigLabelsCached = false;
    private LabelStack configLabels = null;

    private boolean isDataValueCached = false;
    private Object dataValue = null;

    private boolean isBoundsCached = false;
    private Rectangle bounds = null;

    @Override
    public boolean isSpannedCell() {
        return getColumnSpan() > 1 || getRowSpan() > 1;
    }

    @Override
    public String getDisplayMode() {
        if (!this.isDisplayModeCached) {
            this.isDisplayModeCached = true;

            this.displayMode = getLayer().getDisplayModeByPosition(
                    getColumnPosition(), getRowPosition());
        }

        return this.displayMode;
    }

    @Override
    public LabelStack getConfigLabels() {
        if (!this.isConfigLabelsCached) {
            this.isConfigLabelsCached = true;

            this.configLabels = getLayer().getConfigLabelsByPosition(
                    getColumnPosition(), getRowPosition());
        }

        return this.configLabels;
    }

    @Override
    public Object getDataValue() {
        if (!this.isDataValueCached) {
            this.isDataValueCached = true;

            this.dataValue = getLayer().getDataValueByPosition(getColumnPosition(),
                    getRowPosition());
        }

        return this.dataValue;
    }

    @Override
    public Rectangle getBounds() {
        if (!this.isBoundsCached) {
            this.isBoundsCached = true;

            this.bounds = getLayer().getBoundsByPosition(getColumnPosition(),
                    getRowPosition());
        }

        return this.bounds;
    }

}
