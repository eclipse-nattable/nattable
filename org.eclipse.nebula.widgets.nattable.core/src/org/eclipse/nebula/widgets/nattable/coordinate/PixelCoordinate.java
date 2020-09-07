/*******************************************************************************
 * Copyright (c) 2013, 2020 Edwin Park and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Edwin Park - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.coordinate;

public class PixelCoordinate {

    private final int x;
    private final int y;

    public PixelCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "[" + getClass().getName() + " x: " + this.x + ", y: " + this.y + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
    }

}
