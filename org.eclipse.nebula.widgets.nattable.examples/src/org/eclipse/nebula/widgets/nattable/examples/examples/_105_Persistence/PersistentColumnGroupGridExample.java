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
import org.eclipse.nebula.widgets.nattable.examples.examples._150_Column_and_row_grouping._000_Column_groups;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;

public class PersistentColumnGroupGridExample extends PersistentNatExampleWrapper {
	
	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new PersistentColumnGroupGridExample());
	}
	
	public PersistentColumnGroupGridExample() {
		super(new _000_Column_groups());
	}
	
}
