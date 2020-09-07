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
