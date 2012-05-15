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

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;


public class PersistableFixture implements IPersistable {
	
	public Properties loadedProperties;
	public Properties savedProperties;
	public boolean stateLoaded = false;
	public boolean stateSaved = false;

	public void loadState(String prefix, Properties properties) {
		this.stateLoaded = true;
		this.loadedProperties = properties;
	}

	public void saveState(String prefix, Properties properties) {
		this.stateSaved = true;
		this.savedProperties = properties;
	}

}
