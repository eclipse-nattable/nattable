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
package org.eclipse.nebula.widgets.nattable.dataset.valuegenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;


public abstract class AbstractListValueGenerator<T> implements IValueGenerator
{
	private final List<T> listOfValues;

	public AbstractListValueGenerator(List<T> listOfValues) {
		
		this.listOfValues = Collections.unmodifiableList(listOfValues);
	}

	public AbstractListValueGenerator(T... values) {
		
		this(Arrays.asList(values));
	}
	
	public Object newValue(Random random) {
		
		return listOfValues.get(random.nextInt(listOfValues.size()));
	}

	protected static <V> String[] toStringArray(V... values) {
		
		String[] retStrings = new String[values.length];
		for (int i=0; i < values.length; i++) {
			retStrings[i] = String.valueOf(values[i]);
		}
		return retStrings;
	}
}
