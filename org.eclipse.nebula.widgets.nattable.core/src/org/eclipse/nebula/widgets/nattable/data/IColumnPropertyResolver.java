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
 * Maps between the column property name in the backing data bean and its
 * corresponding column index.
 */
public interface IColumnPropertyResolver {

    /**
     * @param columnIndex
     *            i.e the order of the column in the backing bean
     * @return the column property name for the provided column index.
     */
    public String getColumnProperty(int columnIndex);

    /**
     * @param propertyName
     *            i.e the name of the column in the backing bean
     * @return the column index for the provided property name.
     */
    public int getColumnIndex(String propertyName);

}
