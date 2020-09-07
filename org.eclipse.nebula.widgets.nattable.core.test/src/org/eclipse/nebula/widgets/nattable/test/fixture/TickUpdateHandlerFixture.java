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

import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;

public class TickUpdateHandlerFixture implements ITickUpdateHandler {

    @Override
    public boolean isApplicableFor(Object value) {
        return value instanceof String;
    }

    @Override
    public Object getDecrementedValue(Object currentValue) {
        return ((String) currentValue).concat("down");
    }

    @Override
    public Object getIncrementedValue(Object currentValue) {
        return ((String) currentValue).concat("up");
    }

    @Override
    public Object getIncrementedValue(Object currentValue, double incrementSize) {
        return ((String) getDecrementedValue(currentValue)).concat(String.valueOf(incrementSize));
    }

    @Override
    public Object getDecrementedValue(Object currentValue, double decrementSize) {
        return ((String) getDecrementedValue(currentValue)).concat(String.valueOf(decrementSize));
    }

}
