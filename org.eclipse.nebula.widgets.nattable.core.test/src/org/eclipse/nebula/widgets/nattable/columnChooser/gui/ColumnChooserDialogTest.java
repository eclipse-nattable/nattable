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
package org.eclipse.nebula.widgets.nattable.columnChooser.gui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnGroupEntry;
import org.eclipse.nebula.widgets.nattable.columnChooser.ISelectionTreeListener;
import org.eclipse.nebula.widgets.nattable.columnChooser.gui.ColumnChooserDialog;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("boxing")
public class ColumnChooserDialogTest {

    ColumnEntry entry1 = new ColumnEntry("one", 1, 1);
    ColumnEntry entry2 = new ColumnEntry("Two", 2, 2);
    ColumnEntry entry3 = new ColumnEntry("Three", 3, 3);
    ColumnEntry entry4 = new ColumnEntry("Four", 4, 4);
    ColumnEntry entry5 = new ColumnEntry("Five", 5, 5);
    ColumnEntry entry6 = new ColumnEntry("Six", 6, 6);
    ColumnEntry entry7 = new ColumnEntry("Seven", 7, 7);
    ColumnEntry entry8 = new ColumnEntry("Eight", 8, 8);
    ColumnEntry entry9 = new ColumnEntry("Nine", 9, 9);
    ColumnEntry entry10 = new ColumnEntry("Ten", 10, 10);
    List<ColumnEntry> visibleEntries = Arrays.asList(this.entry1, this.entry2, this.entry3,
            this.entry4, this.entry5, this.entry6, this.entry7, this.entry8, this.entry9, this.entry10);

    /*
     * Leaf index 0 1 2 3 4 5 6 7 8 Col Index 0 1 2 3 4 5 6 7 8 9 10 11 12
     * -------------------------------------------------------------------------
     * |<- G1 ->| |<-- G2 -->| |<--- G3 --->|
     */

    ColumnGroupModel columnGroupModel = new ColumnGroupModelFixture();
    private Shell shell;
    private ColumnChooserDialog testDialog;
    private TestTreeListener testListener;

    @Before
    public void setup() {
        this.shell = new Shell();
        this.testDialog = new ColumnChooserDialog(this.shell, "", "");
        this.testDialog.createDialogArea(this.shell);

        this.testListener = new TestTreeListener();
        this.testDialog.addListener(this.testListener);

        this.testDialog.populateSelectedTree(this.visibleEntries, this.columnGroupModel);
    }

    @After
    public void tearDown() {
        this.testDialog.close();
        this.shell.dispose();
    }

    @Ignore
    public void singleColumnMovedDown() throws Exception {
        this.testDialog.setSelectionIncludingNested(Arrays.asList(2));

        this.testDialog.moveSelectedDown();

        List<List<Integer>> fromPositions = this.testListener.fromPositions;
        Assert.assertEquals(1, fromPositions.size());
        Assert.assertEquals(2, fromPositions.get(0));
    }

    @Test
    public void setSelection() throws Exception {
        this.testDialog.setSelection(this.testDialog.getSelectedTree(),
                Arrays.asList(0, 1, 2));
        List<TreeItem> selectedLeaves = ArrayUtil.asList(this.testDialog
                .getSelectedTree().getSelection());

        Assert.assertEquals(3, selectedLeaves.size());
        Assert.assertEquals("G1", selectedLeaves.get(0).getText());
        Assert.assertEquals(this.entry2.getLabel(), selectedLeaves.get(1).getText());
        Assert.assertEquals("G2", selectedLeaves.get(2).getText());
    }

    @Test
    public void getIndexesOfSelectedLeaves() throws Exception {
        this.testDialog.setSelection(this.testDialog.getSelectedTree(),
                Arrays.asList(0, 1, 2));

        List<Integer> indexesOfSelectedLeaves = this.testDialog
                .getIndexesOfSelectedLeaves(this.testDialog.getSelectedTree());

        Assert.assertEquals(3, indexesOfSelectedLeaves.size());
        Assert.assertEquals(0, indexesOfSelectedLeaves.get(0).intValue());
        Assert.assertEquals(1, indexesOfSelectedLeaves.get(1).intValue());
        Assert.assertEquals(2, indexesOfSelectedLeaves.get(2).intValue());
    }

    @Test
    public void populateTree() throws Exception {
        this.columnGroupModel.getColumnGroupByIndex(0).toggleCollapsed();
        Assert.assertEquals(9, this.testDialog.getSelectedTree().getItemCount());

        TreeItem item = this.testDialog.getSelectedTree().getItem(0);
        Assert.assertTrue(item.getData() instanceof ColumnGroupEntry);

        item = this.testDialog.getSelectedTree().getItem(1);
        Assert.assertTrue(item.getData() instanceof ColumnEntry);

        item = this.testDialog.getSelectedTree().getItem(2);
        Assert.assertTrue(item.getData() instanceof ColumnGroupEntry);
    }

    class TestTreeListener implements ISelectionTreeListener {
        List<ColumnEntry> entriesRemoved;
        List<ColumnEntry> entriesAdded;
        List<List<Integer>> fromPositions;
        List<Integer> toPositions;

        @Override
        public void itemsRemoved(List<ColumnEntry> removedItems) {
            this.entriesRemoved = removedItems;
        }

        @Override
        public void itemsSelected(List<ColumnEntry> addedItems) {
            this.entriesAdded = addedItems;
        }

        @Override
        public void itemsCollapsed(ColumnGroupEntry columnGroupEntry) {}

        @Override
        public void itemsExpanded(ColumnGroupEntry columnGroupEntry) {}

        public void itemsMoved(
                List<ColumnGroupEntry> selectedColumnGroupEntries,
                List<ColumnEntry> movedColumnEntries,
                List<List<Integer>> fromPositions, List<Integer> toPositions) {}

        @Override
        public void itemsMoved(MoveDirectionEnum direction,
                List<ColumnGroupEntry> selectedColumnGroupEntries,
                List<ColumnEntry> movedColumnEntries,
                List<List<Integer>> fromPositions, List<Integer> toPositions) {
            this.fromPositions = fromPositions;
            this.toPositions = toPositions;
        }
    }
}
