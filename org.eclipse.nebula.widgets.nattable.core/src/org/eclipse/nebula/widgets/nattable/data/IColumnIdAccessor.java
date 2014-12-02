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
 * Maps between column indexes and id(s). A column id is a unique identifier for
 * a column.
 */
public interface IColumnIdAccessor {

    public String getColumnId(int columnIndex);

    public int getColumnIndex(String columnId);

}
