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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;

public class TickUpdateHandlerFixture implements ITickUpdateHandler {

	public boolean isApplicableFor(Object value) {
		return value instanceof String;
	}
	
	public Object getDecrementedValue(Object currentValue) {
		return ((String) currentValue).concat("down");
	}

	public Object getIncrementedValue(Object currentValue) {
		return ((String) currentValue).concat("up");
	}

    public Object getIncrementedValue(Object currentValue, double incrementSize)
    {
        return ((String)getDecrementedValue(currentValue)).concat(String.valueOf(incrementSize));
    }

    public Object getDecrementedValue(Object currentValue, double decrementSize)
    {
        return ((String)getDecrementedValue(currentValue)).concat(String.valueOf(decrementSize));
    }

}
