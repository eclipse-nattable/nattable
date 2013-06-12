/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class MultiRowShowCommandTest {
	
	@Test
	public void testClone() throws Exception {
		Collection<Integer> rowIndexes = new ArrayList<Integer>();
		rowIndexes.add(3);
		rowIndexes.add(6);
		rowIndexes.add(9);
		rowIndexes.add(12);
		MultiRowShowCommand command = new MultiRowShowCommand(rowIndexes);
		MultiRowShowCommand copiedCommand = command.cloneCommand();
		
		Collection<Integer> commandIndexes = command.getRowIndexes();
		Collection<Integer> cloneIndexes = copiedCommand.getRowIndexes();
		
		assertTrue("The commands reference the same instance", command != copiedCommand);
		assertTrue("The command collections reference the same instance", commandIndexes != cloneIndexes);
		assertTrue("The cloned command does not contain index 3", cloneIndexes.contains(3));
		assertTrue("The cloned command does not contain index 6", cloneIndexes.contains(6));
		assertTrue("The cloned command does not contain index 9", cloneIndexes.contains(9));
		assertTrue("The cloned command does not contain index 12", cloneIndexes.contains(12));
	}
}
