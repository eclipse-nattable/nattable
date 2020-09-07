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
