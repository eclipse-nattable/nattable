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
package org.eclipse.nebula.widgets.nattable.persistence;

import java.util.Properties;

/**
 * Instances implementing this interface can save and load their
 * state from a properties file.
 */
public interface IPersistable {

	/**
	 * Separator used for properties. Example: .BODY.columnWidth.resizableByDefault
	 */
	public static final String DOT = "."; //$NON-NLS-1$

	/**
	 * Separator used for values. Example: 0,1,2,3,4
	 */
	public static final String VALUE_SEPARATOR = ","; //$NON-NLS-1$

	/**
	 * Save state. The prefix must to be prepended to the property key. 
	 */
	public void saveState(String prefix, Properties properties);

	/**
	 * Restore state. The prefix must to be prepended to the property key.
	 * 
	 */
	public void loadState(String prefix, Properties properties);
	
}
