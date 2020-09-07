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
package org.eclipse.nebula.widgets.nattable.data;

/**
 * Maps the properties from the row object to the corresponding columns.
 *
 * @param <T>
 *            type of the bean used as a row object
 */
public interface IColumnAccessor<T> {

    public Object getDataValue(T rowObject, int columnIndex);

    public void setDataValue(T rowObject, int columnIndex, Object newValue);

    public int getColumnCount();

}
