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
package org.eclipse.nebula.widgets.nattable.tickupdate;

public interface ITickUpdateHandler {
	
	public boolean isApplicableFor(Object value);
	
	/**
	 * @param currentValue of the cell
	 * @return new value after INcrementing
	 */
	public Object getIncrementedValue(Object currentValue);
	
	public Object getIncrementedValue(Object currentValue, double incrementSize);

	/**
	 * @param currentValue of the cell
	 * @return new value after DEcrementing
	 */
	public Object getDecrementedValue(Object currentValue);
	
	public Object getDecrementedValue(Object currentValue, double decrementSize);

	// Default implementation
	
	ITickUpdateHandler DEFAULT_TICK_UPDATE_HANDLER = new ITickUpdateHandler() {

		public boolean isApplicableFor(Object value) {
			return value instanceof Number ;//|| value == null;
		}
		
		public Object getDecrementedValue(Object currentValue) {
		    return getIncrementedValue(currentValue, 1);
		}

		public Object getIncrementedValue(Object currentValue, double incrementSize) {
		    Number oldValue = (Number) currentValue;
		    return Double.valueOf(oldValue.doubleValue() + Math.abs(incrementSize));
		}
		
		public Object getIncrementedValue(Object currentValue) {
		    return getDecrementedValue(currentValue, 1);
		}

        public Object getDecrementedValue(Object currentValue, double decrementSize) {
            Number oldValue = (Number) currentValue;
            return Double.valueOf(oldValue.doubleValue() - Math.abs(decrementSize));
        }
		
	};

}
