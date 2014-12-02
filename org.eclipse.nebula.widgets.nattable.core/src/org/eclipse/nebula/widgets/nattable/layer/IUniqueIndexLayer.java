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
package org.eclipse.nebula.widgets.nattable.layer;

/**
 * A layer that has a set of column and row indexes that contain no duplicates,
 * such that there is only one corresponding column or row position for a row or
 * column index in the layer.
 */
public interface IUniqueIndexLayer extends ILayer {

    public int getColumnPositionByIndex(int columnIndex);

    public int getRowPositionByIndex(int rowIndex);

}
