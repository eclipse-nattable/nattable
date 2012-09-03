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
	
	public boolean isSpannedCell() {
		return getColumnSpan() > 1 || getRowSpan() > 1;
	}

	public String getDisplayMode() {
		if (!isDisplayModeCached) {
			isDisplayModeCached = true;

			displayMode = getLayer().getDisplayModeByPosition(getColumnPosition(), getRowPosition());
		}

		return displayMode;
	}

	public LabelStack getConfigLabels() {
		if (!isConfigLabelsCached) {
			isConfigLabelsCached = true;

			configLabels = getLayer().getConfigLabelsByPosition(getColumnPosition(), getRowPosition());
		}

		return configLabels;
	}

	public Object getDataValue() {
		if (!isDataValueCached) {
			isDataValueCached = true;

			dataValue = getLayer().getDataValueByPosition(getColumnPosition(), getRowPosition());
		}

		return dataValue;
	}

	public Rectangle getBounds() {
		if (!isBoundsCached) {
			isBoundsCached = true;

			bounds = getLayer().getBoundsByPosition(getColumnPosition(), getRowPosition());
		}

		return bounds;
	}

}
