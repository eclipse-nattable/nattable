/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import java.util.Map;

public interface IFilterStrategy<T> {

    /**
     * Apply the filter(s) specified by the entered filter text.
     *
     * @param filterIndexToObjectMap
     *            A Map of column indexes to filter text Strings.
     */
    void applyFilter(Map<Integer, Object> filterIndexToObjectMap);

}
