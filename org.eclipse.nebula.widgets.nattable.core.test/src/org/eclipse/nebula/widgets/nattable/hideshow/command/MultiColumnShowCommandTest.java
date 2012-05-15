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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.Assert.*;

import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommand;
import org.junit.Test;

public class MultiColumnShowCommandTest {
	
	@Test
	public void arrayCopy() throws Exception {
		MultiColumnShowCommand command = new MultiColumnShowCommand(new int[]{3,6,9,12});
		MultiColumnShowCommand copiedCommand = new MultiColumnShowCommand(command);
		
		int[] copiedColumnIndexes = copiedCommand.getColumnIndexes();
		assertEquals(3, copiedColumnIndexes[0]);
		assertEquals(6, copiedColumnIndexes[1]);
		assertEquals(9, copiedColumnIndexes[2]);
		assertEquals(12, copiedColumnIndexes[3]);
	}
}
