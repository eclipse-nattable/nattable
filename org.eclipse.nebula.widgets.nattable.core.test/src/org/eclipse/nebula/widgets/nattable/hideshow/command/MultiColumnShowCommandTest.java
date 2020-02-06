/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class MultiColumnShowCommandTest {

    @Test
    public void testClone() throws Exception {
        MultiColumnShowCommand command = new MultiColumnShowCommand(3, 6, 9, 12);
        MultiColumnShowCommand copiedCommand = command.cloneCommand();

        Collection<Integer> commandIndexes = command.getColumnIndexes();
        Collection<Integer> cloneIndexes = copiedCommand.getColumnIndexes();

        int[] commandIndexesArray = command.getColumnIndexesArray();
        int[] cloneIndexesArray = copiedCommand.getColumnIndexesArray();

        assertTrue("The commands reference the same instance",
                command != copiedCommand);
        assertTrue("The command collections reference the same instance",
                commandIndexes != cloneIndexes);
        assertTrue("The cloned command does not contain index 3",
                cloneIndexes.contains(3));
        assertTrue("The cloned command does not contain index 6",
                cloneIndexes.contains(6));
        assertTrue("The cloned command does not contain index 9",
                cloneIndexes.contains(9));
        assertTrue("The cloned command does not contain index 12",
                cloneIndexes.contains(12));

        assertTrue("The command arrays reference the same instance",
                commandIndexesArray != cloneIndexesArray);
        assertTrue("The cloned command does not contain index 3",
                cloneIndexesArray[0] == 3);
        assertTrue("The cloned command does not contain index 6",
                cloneIndexesArray[1] == 6);
        assertTrue("The cloned command does not contain index 9",
                cloneIndexesArray[2] == 9);
        assertTrue("The cloned command does not contain index 12",
                cloneIndexesArray[3] == 12);
    }
}
