/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.coordinate;

import java.util.Comparator;

/**
 * Comparator used to sort collections of {@link PositionCoordinate} in an
 * ascending way.
 *
 * @since 1.6
 */
public class PositionCoordinateComparator implements Comparator<PositionCoordinate> {

    @Override
    public int compare(PositionCoordinate o1, PositionCoordinate o2) {
        int result = o1.getColumnPosition() - o2.getColumnPosition();
        if (result == 0) {
            result = o1.getRowPosition() - o2.getRowPosition();
        }
        return result;
    }
}