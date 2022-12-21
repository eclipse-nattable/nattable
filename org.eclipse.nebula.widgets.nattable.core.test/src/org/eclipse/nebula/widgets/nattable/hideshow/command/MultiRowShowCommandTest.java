/*******************************************************************************
 * Copyright (c) 2013, 2022 Dirk Fauth and others.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Test;

public class MultiRowShowCommandTest {

    @Test
    public void testClone() {
        MultiRowShowCommand command = new MultiRowShowCommand(3, 6, 9, 12);
        MultiRowShowCommand copiedCommand = command.cloneCommand();

        Collection<Integer> commandIndexes = command.getRowIndexes();
        Collection<Integer> cloneIndexes = copiedCommand.getRowIndexes();

        int[] commandIndexesArray = command.getRowIndexesArray();
        int[] cloneIndexesArray = copiedCommand.getRowIndexesArray();

        assertTrue(command != copiedCommand, "The commands reference the same instance");
        assertTrue(commandIndexes != cloneIndexes, "The command collections reference the same instance");
        assertTrue(cloneIndexes.contains(3), "The cloned command does not contain index 3");
        assertTrue(cloneIndexes.contains(6), "The cloned command does not contain index 6");
        assertTrue(cloneIndexes.contains(9), "The cloned command does not contain index 9");
        assertTrue(cloneIndexes.contains(12), "The cloned command does not contain index 12");

        assertTrue(commandIndexesArray != cloneIndexesArray, "The command arrays reference the same instance");
        assertTrue(cloneIndexesArray[0] == 3, "The cloned command does not contain index 3");
        assertTrue(cloneIndexesArray[1] == 6, "The cloned command does not contain index 6");
        assertTrue(cloneIndexesArray[2] == 9, "The cloned command does not contain index 9");
        assertTrue(cloneIndexesArray[3] == 12, "The cloned command does not contain index 12");
    }
}
