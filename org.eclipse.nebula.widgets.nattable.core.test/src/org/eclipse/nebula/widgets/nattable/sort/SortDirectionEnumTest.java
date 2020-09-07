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
package org.eclipse.nebula.widgets.nattable.sort;

import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.ASC;
import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.DESC;
import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.NONE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SortDirectionEnumTest {

    @Test
    public void shouldCalculateNextSortDirectionCorrectly() throws Exception {
        assertEquals(ASC, NONE.getNextSortDirection());
        assertEquals(DESC, ASC.getNextSortDirection());
        assertEquals(NONE, DESC.getNextSortDirection());
    }
}
