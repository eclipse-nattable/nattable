/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class MultiRowShowCommandTest {

    @Test
    public void testClone() {
        MultiRowShowCommand command = new MultiRowShowCommand(3, 6, 9, 12);
        MultiRowShowCommand copiedCommand = command.cloneCommand();

        Collection<Integer> commandIndexes = command.getRowIndexes();
        Collection<Integer> cloneIndexes = copiedCommand.getRowIndexes();

        int[] commandIndexesArray = command.getRowIndexesArray();
        int[] cloneIndexesArray = copiedCommand.getRowIndexesArray();

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
