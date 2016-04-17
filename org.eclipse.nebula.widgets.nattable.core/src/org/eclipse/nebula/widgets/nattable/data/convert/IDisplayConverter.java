/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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
 * The normal data representation is known as the <i>canonical
 * representation</i> The representation displayed on the UI is called the
 * <i>display representation</i>.
 *
 * For example, the canonical representation might be a Date object, whereas the
 * target representation could be a formatted String.
 */
public interface IDisplayConverter {

    /**
     * Convert backing data value to value to be displayed. Typically converts
     * to a String for display.
     *
     * @param canonicalValue
     *            The data value from the backing data.
     * @return The converted value to display.
     */
    public Object canonicalToDisplayValue(Object canonicalValue);

    /**
     * Convert from display value to value in the backing data structure.
     * <p>
     * <b>NOTE:</b><br>
     * The type the display value is converted to <i>must</i> match the type in
     * the setter of the backing bean/row object
     * </p>
     *
     * @param displayValue
     *            The display value that should be converted to the data value
     *            that matches the backing data.
     * @return The converted value to put to the backing data.
     */
    public Object displayToCanonicalValue(Object displayValue);

    /**
     * Convert backing data value to value to be displayed. Typically converts
     * to a String for display. Use this method for contextual conversion.
     * <p>
     * Note that on returning a different type than String,
     * <code>toString()</code> will be called on the returned object to render
     * the value for displaying.
     * </p>
     *
     * @param cell
     *            The {@link ILayerCell} whose canonical value should be
     *            converted.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the NatTable to which the
     *            {@link ILayerCell} belongs.
     * @param canonicalValue
     *            The data value from the backing data.
     * @return The converted value to display.
     */
    public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue);

    /**
     * Convert from display value to value in the backing data structure.
     * <p>
     * <b>NOTE:</b><br>
     * The type the display value is converted to <i>must</i> match the type in
     * the setter of the backing bean/row object Use this method for contextual
     * conversion.
     * 
     * @param cell
     *            The {@link ILayerCell} whose canonical value should be
     *            converted.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the NatTable to which the
     *            {@link ILayerCell} belongs.
     * @param displayValue
     *            The display value that should be converted to the data value
     *            that matches the backing data.
     * @return The converted value to put to the backing data.
     */
    public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue);
}
