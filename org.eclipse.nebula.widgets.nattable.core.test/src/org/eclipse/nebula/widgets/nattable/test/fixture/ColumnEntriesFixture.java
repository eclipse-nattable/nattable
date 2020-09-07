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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;

public class ColumnEntriesFixture {

    public static List<ColumnEntry> getEntriesWithOddIndexes() {
        return Arrays.asList(
                new ColumnEntry("Index1", 1, 2),
                new ColumnEntry("Index3", 3, 6),
                new ColumnEntry("Index5", 5, 3),
                new ColumnEntry("Index7", 7, 4),
                new ColumnEntry("Index9", 9, 5));
    }

    public static List<ColumnEntry> getEntriesWithEvenIndexes() {
        return Arrays.asList(
                new ColumnEntry("Index2", 2, 2),
                new ColumnEntry("Index4", 4, 6),
                new ColumnEntry("Index6", 6, 3),
                new ColumnEntry("Index8", 8, 4),
                new ColumnEntry("Index10", 10, 5));
    }
}
