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
package org.eclipse.nebula.widgets.nattable.data;

/**
 * Maps between the column property name in the backing data bean and its
 * corresponding column index.
 */
public interface IColumnPropertyResolver {

    /**
     * @param columnIndex
     *            i.e the order of the column in the backing bean
     */
    public String getColumnProperty(int columnIndex);

    /**
     * @param propertyName
     *            i.e the name of the column in the backing bean
     */
    public int getColumnIndex(String propertyName);

}
