/*******************************************************************************
 * Copyright (c) 2012, 2020 Edwin Park and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class RangeTest {

    @Test
    public void testAdjacentRangeOverlap() {
        assertFalse(new Range(0, 1).overlap(new Range(1, 2)));
    }

    @Test
    public void testEmptyRangeOverlap() {
        assertFalse(new Range(0, 1).overlap(new Range(1, 1)));
        assertFalse(new Range(1, 1).overlap(new Range(1, 2)));
        assertFalse(new Range(1, 1).overlap(new Range(0, 2)));
    }

}
