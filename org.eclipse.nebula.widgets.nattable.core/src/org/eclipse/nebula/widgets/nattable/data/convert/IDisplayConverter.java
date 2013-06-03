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
package org.eclipse.nebula.widgets.nattable.data.convert;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * Converts between two different data representations.
 *
 * The normal data representation is known as the <i>canonical representation</i>
 * The representation displayed on the UI is called the <i>display representation</i>.
 *
 * For example, the canonical representation might be a Date object,
 * whereas the target representation could be a formatted String.
 */
public interface IDisplayConverter {

	/**
	 * Convert backing data value to value to be displayed
	 * Typically converted to a String for display.
	 */
	public Object canonicalToDisplayValue(Object canonicalValue);

	/**
	 * Convert from display value to value in the backing data structure
	 * NOTE: The type the display value is converted to <i>must</i> match the type
	 * in the setter of the backing bean/row object
	 */
	public Object displayToCanonicalValue(Object displayValue);

	/**
	 * Convert backing data value to value to be displayed
	 * Typically converted to a String for display.
	 * Use this method for contextual conversion.
	 */
	public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue);
	
	/**
	 * Convert from display value to value in the backing data structure
	 * NOTE: The type the display value is converted to <i>must</i> match the type
	 * in the setter of the backing bean/row object
	 * Use this method for contextual conversion.
	 */
	public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue);
}
