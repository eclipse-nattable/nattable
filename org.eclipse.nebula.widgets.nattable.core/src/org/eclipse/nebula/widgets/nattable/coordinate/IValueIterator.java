/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.coordinate;

import java.util.Iterator;
import java.util.NoSuchElementException;


public interface IValueIterator extends Iterator<Integer> {
	
	
	/**
	 * Returns the next value in the iteration.
	 * <p>
	 * Like {@link #next()}, but returns directly the primitiv value.
	 * 
	 * @return the next value in the iteration
	 * @throws NoSuchElementException if the iteration has no more elements
	 */
	int nextValue();
	
}
