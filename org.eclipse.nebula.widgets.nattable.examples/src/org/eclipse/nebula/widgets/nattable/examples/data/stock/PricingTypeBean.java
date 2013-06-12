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
package org.eclipse.nebula.widgets.nattable.examples.data.stock;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;

/**
 * Bean representing the pricing type. Used as the canonical data source for the
 * combo box - used to test the canonical to display conversion
 */
public class PricingTypeBean implements Comparable<PricingTypeBean>{
	public String type;

	public PricingTypeBean(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	/**
	 * Format: Items displayed in the Combo &lt;-&gt; Canonical value
	 */
	public static IDisplayConverter getDisplayConverter() {
		return new DisplayConverter() {
			public Object canonicalToDisplayValue(Object canonicalValue) {
				if (canonicalValue == null) {
					return null;
				} else {
					return canonicalValue.toString().equals("MN") ? "Manual" : "Automatic";
				}
			}

			public Object displayToCanonicalValue(Object displayValue) {
				return displayValue.toString().equals("Manual") ? new PricingTypeBean("MN") : new PricingTypeBean("AT");
			}
		};
	}

	public int compareTo(PricingTypeBean o) {
		return this.toString().compareTo(o.toString());
	}

}
