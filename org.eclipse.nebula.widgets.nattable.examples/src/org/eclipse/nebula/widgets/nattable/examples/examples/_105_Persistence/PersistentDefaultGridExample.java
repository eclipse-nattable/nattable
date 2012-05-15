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
package org.eclipse.nebula.widgets.nattable.examples.examples._105_Persistence;

import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.examples._000_Default_NatTable;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;

public class PersistentDefaultGridExample extends PersistentNatExampleWrapper {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new PersistentDefaultGridExample());
	}
	
	public PersistentDefaultGridExample() {
		super(new _000_Default_NatTable());
	}
	
}
