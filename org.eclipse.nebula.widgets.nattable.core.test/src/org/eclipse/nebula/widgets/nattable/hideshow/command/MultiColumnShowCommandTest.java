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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommand;
import org.junit.Test;

public class MultiColumnShowCommandTest {
	
	@Test
	public void testClone() throws Exception {
		Collection<Integer> columnIndexes = new ArrayList<Integer>();
		columnIndexes.add(3);
		columnIndexes.add(6);
		columnIndexes.add(9);
		columnIndexes.add(12);
		MultiColumnShowCommand command = new MultiColumnShowCommand(columnIndexes);
		MultiColumnShowCommand copiedCommand = command.cloneCommand();
		
		Collection<Integer> commandIndexes = command.getColumnIndexes();
		Collection<Integer> cloneIndexes = copiedCommand.getColumnIndexes();
		
		assertTrue("The commands reference the same instance", command != copiedCommand);
		assertTrue("The command collections reference the same instance", commandIndexes != cloneIndexes);
		assertTrue("The cloned command does not contain index 3", cloneIndexes.contains(3));
		assertTrue("The cloned command does not contain index 6", cloneIndexes.contains(6));
		assertTrue("The cloned command does not contain index 9", cloneIndexes.contains(9));
		assertTrue("The cloned command does not contain index 12", cloneIndexes.contains(12));
	}
}
