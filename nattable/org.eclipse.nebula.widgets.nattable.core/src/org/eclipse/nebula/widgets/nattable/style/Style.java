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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Style implements IStyle {

	private final Map<ConfigAttribute<?>, Object> styleAttributeValueMap = new HashMap<ConfigAttribute<?>, Object>();

	@SuppressWarnings("unchecked")
	public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
		return (T) styleAttributeValueMap.get(styleAttribute);
	}

	public <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value) {
		styleAttributeValueMap.put(styleAttribute, value);
	}

	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(this.getClass().getSimpleName() + ": "); //$NON-NLS-1$

		Set<Entry<ConfigAttribute<?>, Object>> entrySet = styleAttributeValueMap.entrySet();

		for (Entry<ConfigAttribute<?>, Object> entry : entrySet) {
			resultBuilder.append(entry.getKey() + ": " + entry.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return resultBuilder.toString();
	}

}
