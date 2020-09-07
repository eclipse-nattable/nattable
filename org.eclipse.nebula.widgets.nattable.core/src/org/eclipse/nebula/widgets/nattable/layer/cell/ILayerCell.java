/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.graphics.Rectangle;

public interface ILayerCell {

    public int getOriginColumnPosition();

    public int getOriginRowPosition();

    public ILayer getLayer();

    public int getColumnPosition();

    public int getRowPosition();

    public int getColumnIndex();

    public int getRowIndex();

    public int getColumnSpan();

    public int getRowSpan();

    public boolean isSpannedCell();

    public String getDisplayMode();

    public LabelStack getConfigLabels();

    public Object getDataValue();

    public Rectangle getBounds();

}
