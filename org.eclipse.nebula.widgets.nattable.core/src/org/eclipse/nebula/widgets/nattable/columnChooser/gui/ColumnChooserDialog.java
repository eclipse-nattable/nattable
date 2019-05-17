/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Roman Flueckiger <roman.flueckiger@mac.com> - Bug 451486
 *     Roman Flueckiger <rflueckiger@inventage.com> - Bug 459582
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460052
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnChooser.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnGroupEntry;
import org.eclipse.nebula.widgets.nattable.columnChooser.ISelectionTreeListener;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Dialog that contains two {@link Tree}s to support hide/show and reordering of
 * columns.
 */
public class ColumnChooserDialog extends AbstractColumnChooserDialog {

    private Tree availableTree;
    private Tree selectedTree;
    private final String selectedLabel;
    private final String availableLabel;
    private boolean preventHidingAllColumns = false;

    private ColumnGroupModel columnGroupModel;
    private ColumnGroupHeaderLayer columnGroupHeaderLayer;

    /**
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     * @param availableLabel
     *            The label to be shown for the available tree.
     * @param selectedLabel
     *            The label to be shown for the selected tree.
     */
    public ColumnChooserDialog(Shell parentShell, String availableLabel, String selectedLabel) {
        super(parentShell);

        this.availableLabel = availableLabel;
        this.selectedLabel = selectedLabel;
    }

    @Override
    public void populateDialogArea(Composite parent) {
        GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
        parent.setLayout(new GridLayout(4, false));

        createLabels(parent, this.availableLabel, this.selectedLabel);

        this.availableTree = new Tree(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.Expand);

        GridData gridData = GridDataFactory.fillDefaults().grab(true, true).create();
        this.availableTree.setLayoutData(gridData);
        this.availableTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                addSelected();
            }
        });

        this.availableTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.character == ' ')
                    addSelected();
            }
        });

        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1, true));

        Button addButton = new Button(buttonComposite, SWT.PUSH);
        addButton.setImage(GUIHelper.getImage("arrow_right")); //$NON-NLS-1$
        gridData = GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create();
        addButton.setLayoutData(gridData);
        addButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                addSelected();
            }
        });

        final Button removeButton = new Button(buttonComposite, SWT.PUSH);
        removeButton.setImage(GUIHelper.getImage("arrow_left")); //$NON-NLS-1$
        gridData = GridDataFactory.copyData(gridData);
        removeButton.setLayoutData(gridData);
        removeButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelected();
            }
        });

        this.selectedTree = new Tree(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.Expand);

        gridData = GridDataFactory.fillDefaults().grab(true, true).create();
        this.selectedTree.setLayoutData(gridData);
        this.selectedTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                if (ColumnChooserDialog.this.preventHidingAllColumns) {
                    if (!isSelectedTreeCompletelySelected()) {
                        removeSelected();
                    }
                } else {
                    removeSelected();
                }
            }
        });

        this.selectedTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean controlMask = (e.stateMask & SWT.MOD1) == SWT.MOD1;
                if (controlMask && e.keyCode == SWT.ARROW_UP) {
                    moveSelectedUp();
                    e.doit = false;
                } else if (controlMask && e.keyCode == SWT.ARROW_DOWN) {
                    moveSelectedDown();
                    e.doit = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.character == ' ')
                    removeSelected();
            }
        });

        this.selectedTree.addTreeListener(new TreeListener() {

            @Override
            public void treeCollapsed(TreeEvent event) {
                selectedTreeCollapsed(event);
            }

            @Override
            public void treeExpanded(TreeEvent event) {
                selectedTreeExpanded(event);
            }
        });

        this.selectedTree.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }

            @Override
            public void widgetSelected(SelectionEvent event) {
                toggleColumnGroupSelection((TreeItem) event.item);

                if (ColumnChooserDialog.this.preventHidingAllColumns) {
                    removeButton.setEnabled(!isSelectedTreeCompletelySelected());
                }
            }
        });

        Composite upDownbuttonComposite = new Composite(parent, SWT.NONE);
        upDownbuttonComposite.setLayout(new GridLayout(1, true));

        Button topButton = new Button(upDownbuttonComposite, SWT.PUSH);
        topButton.setImage(GUIHelper.getImage("arrow_up_top")); //$NON-NLS-1$
        gridData = GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create();
        topButton.setLayoutData(gridData);
        topButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedToTop();
            }
        });

        Button upButton = new Button(upDownbuttonComposite, SWT.PUSH);
        upButton.setImage(GUIHelper.getImage("arrow_up")); //$NON-NLS-1$
        gridData = GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).create();
        upButton.setLayoutData(gridData);
        upButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedUp();
            }
        });

        Button downButton = new Button(upDownbuttonComposite, SWT.PUSH);
        downButton.setImage(GUIHelper.getImage("arrow_down")); //$NON-NLS-1$
        gridData = GridDataFactory.copyData(gridData);
        downButton.setLayoutData(gridData);
        downButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedDown();
            }
        });

        Button bottomButton = new Button(upDownbuttonComposite, SWT.PUSH);
        bottomButton.setImage(GUIHelper.getImage("arrow_down_end")); //$NON-NLS-1$
        gridData = GridDataFactory.copyData(gridData);
        bottomButton.setLayoutData(gridData);
        bottomButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedToBottom();
            }
        });
    }

    protected final void fireItemsSelected(List<ColumnEntry> addedItems) {
        for (Object listener : this.listeners.getListeners()) {
            ((ISelectionTreeListener) listener).itemsSelected(addedItems);
        }
    }

    protected final void fireItemsRemoved(List<ColumnEntry> removedItems) {
        for (Object listener : this.listeners.getListeners()) {
            ((ISelectionTreeListener) listener).itemsRemoved(removedItems);
        }
    }

    protected final void fireItemsMoved(MoveDirectionEnum direction,
            List<ColumnGroupEntry> selectedColumnGroupEntries,
            List<ColumnEntry> selectedColumnEntries,
            List<List<Integer>> fromPositions, List<Integer> toPositions) {

        for (Object listener : this.listeners.getListeners()) {
            ((ISelectionTreeListener) listener).itemsMoved(direction, selectedColumnGroupEntries, selectedColumnEntries, fromPositions, toPositions);
        }
    }

    private void fireGroupExpanded(ColumnGroupEntry columnGroupEntry) {
        for (Object listener : this.listeners.getListeners()) {
            ((ISelectionTreeListener) listener).itemsExpanded(columnGroupEntry);
        }
    }

    private void fireGroupCollapsed(ColumnGroupEntry columnGroupEntry) {
        for (Object listener : this.listeners.getListeners()) {
            ((ISelectionTreeListener) listener).itemsCollapsed(columnGroupEntry);
        }
    }

    /**
     * Populates the available item tree with the given column entries for the
     * given {@link ColumnGroupModel}.
     *
     * @param columnEntries
     *            The column entries to add as available items.
     * @param columnGroupModel
     *            The {@link ColumnGroupModel} needed to inspect the column
     *            groups, can be <code>null</code> if no column grouping is
     *            supported in the underlying table composition.
     */
    public void populateAvailableTree(List<ColumnEntry> columnEntries, ColumnGroupModel columnGroupModel) {
        populateModel(this.availableTree, columnEntries, columnGroupModel);
    }

    /**
     * Populates the selected item tree with the given column entries for the
     * given {@link ColumnGroupModel}.
     *
     * @param columnEntries
     *            The column entries to add as selected items.
     * @param columnGroupModel
     *            The {@link ColumnGroupModel} needed to inspect the column
     *            groups, can be <code>null</code> if no column grouping is
     *            supported in the underlying table composition.
     */
    public void populateSelectedTree(List<ColumnEntry> columnEntries, ColumnGroupModel columnGroupModel) {
        populateModel(this.selectedTree, columnEntries, columnGroupModel);
    }

    /**
     * Populates the given tree with the given column entries for the given
     * {@link ColumnGroupModel}. Looks for column groups if the
     * {@link ColumnGroupModel} is not <code>null</code> and adds an extra node
     * for the group. The column leaves carry a {@link ColumnEntry} object as
     * data. The column group leaves carry a {@link ColumnGroupEntry} object as
     * data.
     *
     * @param tree
     *            The {@link Tree} that should be populated.
     * @param columnEntries
     *            The column entries to add items to the tree.
     * @param columnGroupModel
     *            The {@link ColumnGroupModel} needed to inspect the column
     *            groups, can be <code>null</code> if no column grouping is
     *            supported in the underlying table composition.
     */
    private void populateModel(Tree tree, List<ColumnEntry> columnEntries, ColumnGroupModel columnGroupModel) {
        this.columnGroupModel = columnGroupModel;

        for (ColumnEntry columnEntry : columnEntries) {
            TreeItem treeItem;
            int columnEntryIndex = columnEntry.getIndex().intValue();

            // Create a node for the column group - if needed
            if (columnGroupModel != null
                    && columnGroupModel.isPartOfAGroup(columnEntryIndex)) {
                ColumnGroup columnGroup = columnGroupModel.getColumnGroupByIndex(columnEntryIndex);
                String columnGroupName = columnGroup.getName();
                TreeItem columnGroupTreeItem = getTreeItem(tree, columnGroupName);

                if (columnGroupTreeItem == null) {
                    columnGroupTreeItem = new TreeItem(tree, SWT.NONE);
                    ColumnGroupEntry columnGroupEntry =
                            new ColumnGroupEntry(columnGroupName, columnEntry.getPosition(), columnEntry.getIndex(), columnGroup.isCollapsed());
                    columnGroupTreeItem.setData(columnGroupEntry);
                    columnGroupTreeItem.setText(columnGroupEntry.getLabel());
                }
                treeItem = new TreeItem(columnGroupTreeItem, SWT.NONE);
            } else {
                treeItem = new TreeItem(tree, SWT.NONE);
            }
            treeItem.setText(columnEntry.getLabel());
            treeItem.setData(columnEntry);
        }
    }

    /**
     * Populates the available item tree with the given column entries for the
     * new performance {@link ColumnGroupHeaderLayer}.
     *
     * @param columnEntries
     *            The column entries to add as available items.
     * @param columnGroupHeaderLayer
     *            The new performance {@link ColumnGroupHeaderLayer} needed to
     *            inspect the column groups, can be <code>null</code> if no
     *            column grouping is supported in the underlying table
     *            composition.
     * @since 1.6
     */
    public void populateAvailableTree(List<ColumnEntry> columnEntries, ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        populateModel(this.availableTree, columnEntries, columnGroupHeaderLayer);
    }

    /**
     * Populates the selected item tree with the given column entries for the
     * new performance {@link ColumnGroupHeaderLayer}.
     *
     * @param columnEntries
     *            The column entries to add as selected items.
     * @param columnGroupHeaderLayer
     *            The new performance {@link ColumnGroupHeaderLayer} needed to
     *            inspect the column groups, can be <code>null</code> if no
     *            column grouping is supported in the underlying table
     *            composition.
     * @since 1.6
     */
    public void populateSelectedTree(List<ColumnEntry> columnEntries, ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        populateModel(this.selectedTree, columnEntries, columnGroupHeaderLayer);
    }

    /**
     * Populates the given tree with the given column entries for the new
     * performance {@link ColumnGroupHeaderLayer}. Looks for column groups if
     * the {@link ColumnGroupHeaderLayer} is not <code>null</code> and adds an
     * extra node for the group. The column leaves carry a {@link ColumnEntry}
     * object as data. The column group leaves carry a {@link ColumnGroupEntry}
     * object as data.
     *
     * @param tree
     *            The {@link Tree} that should be populated.
     * @param columnEntries
     *            The column entries to add as items to the tree.
     * @param columnGroupHeaderLayer
     *            The new performance {@link ColumnGroupHeaderLayer} needed to
     *            inspect the column groups, can be <code>null</code> if no
     *            column grouping is supported in the underlying table
     *            composition.
     * @since 1.6
     */
    private void populateModel(Tree tree, List<ColumnEntry> columnEntries, ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;

        for (ColumnEntry columnEntry : columnEntries) {
            TreeItem treeItem = null;
            int columnEntryPosition = columnEntry.getPosition();

            // Create a node for the column group - if needed
            // Operate on the GroupModel so we can handle also positions not
            // visible
            // TODO check for multi level
            GroupModel groupModel = null;
            if (columnGroupHeaderLayer != null) {
                groupModel = columnGroupHeaderLayer.getGroupModel(0);
                columnEntryPosition = columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(columnEntry.getIndex());
            }

            Group columnGroup = null;
            if (groupModel != null && groupModel.isPartOfAGroup(columnEntryPosition)) {
                columnGroup = groupModel.getGroupByPosition(columnEntryPosition);
            } else if (groupModel != null && columnEntryPosition < 0) {
                // try to find the collapsed column group that contains the
                // index of the column entry
                columnGroup = groupModel.findGroupByMemberIndex(columnEntry.getIndex());
            }

            if (columnGroup != null) {
                TreeItem columnGroupTreeItem = getTreeItem(tree, columnGroup);
                if (columnGroupTreeItem == null) {
                    columnGroupTreeItem = new TreeItem(tree, SWT.NONE);
                    ColumnGroupEntry columnGroupEntry = new ColumnGroupEntry(columnGroup);
                    columnGroupTreeItem.setData(columnGroupEntry);
                    columnGroupTreeItem.setText(columnGroupEntry.getLabel());
                }
                treeItem = new TreeItem(columnGroupTreeItem, SWT.NONE);
            }

            if (treeItem == null) {
                treeItem = new TreeItem(tree, SWT.NONE);
            }

            treeItem.setText(columnEntry.getLabel());
            treeItem.setData(columnEntry);
        }
    }

    /**
     * Find the {@link TreeItem} for the given label.
     *
     * @param tree
     *            The {@link Tree} in which the {@link TreeItem} should be
     *            searched.
     * @param label
     *            The label to check for.
     * @return The {@link TreeItem} in the given {@link Tree} with the specified
     *         label, or <code>null</code> if not found.
     */
    private TreeItem getTreeItem(Tree tree, String label) {
        for (TreeItem treeItem : tree.getItems()) {
            if (treeItem.getText().equals(label)) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * Find the {@link TreeItem} for the given Group.
     *
     * @param tree
     *            The {@link Tree} in which the {@link TreeItem} should be
     *            searched.
     * @param group
     *            The {@link Group} to search for.
     * @return The {@link TreeItem} in the given {@link Tree} with the specified
     *         {@link Group} as data, or <code>null</code> if not found.
     */
    private TreeItem getTreeItem(Tree tree, Group group) {
        for (TreeItem treeItem : tree.getItems()) {
            if (isColumnGroupLeaf(treeItem)
                    && group.equals(((ColumnGroupEntry) treeItem.getData()).getGroup())) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * Get the {@link ColumnEntry}s from the selected {@link TreeItem}s.
     * Includes nested column group entries if the column group is selected.
     * Does not include parent of the nested entries since that does not denote
     * an actual column
     *
     * @param selectedTreeItems
     *            The selected {@link TreeItem}s to process.
     * @return The {@link ColumnEntry}s out of the selected {@link TreeItem}s.
     */
    private List<ColumnEntry> getColumnEntriesIncludingNested(TreeItem[] selectedTreeItems) {
        List<ColumnEntry> selectedColumnEntries = new ArrayList<ColumnEntry>();

        for (int i = 0; i < selectedTreeItems.length; i++) {
            // Column Group selected - get all children
            if (isColumnGroupLeaf(selectedTreeItems[i])) {
                TreeItem[] itemsInGroup = selectedTreeItems[i].getItems();
                for (TreeItem itemInGroup : itemsInGroup) {
                    selectedColumnEntries.add((ColumnEntry) itemInGroup.getData());
                }
            } else {
                // Column
                selectedColumnEntries.add(getColumnEntryInLeaf(selectedTreeItems[i]));
            }
        }
        return selectedColumnEntries;
    }

    private List<ColumnGroupEntry> getSelectedColumnGroupEntries(TreeItem[] selectedTreeItems) {
        List<ColumnGroupEntry> selectedColumnGroups = new ArrayList<ColumnGroupEntry>();

        for (int i = 0; i < selectedTreeItems.length; i++) {
            if (isColumnGroupLeaf(selectedTreeItems[i])) {
                selectedColumnGroups.add((ColumnGroupEntry) selectedTreeItems[i].getData());
            }
        }
        return selectedColumnGroups;
    }

    private List<ColumnEntry> getSelectedColumnEntriesIncludingNested(Tree tree) {
        return getColumnEntriesIncludingNested(tree.getSelection());
    }

    private List<ColumnGroupEntry> getSelectedColumnGroupEntries(Tree tree) {
        return getSelectedColumnGroupEntries(tree.getSelection());
    }

    // Event handlers

    /**
     * Add selected items: 'Available tree' --> 'Selected tree' Notify
     * listeners.
     */
    private void addSelected() {
        if (isAnyLeafSelected(this.availableTree)) {
            TreeItem topAvailableItem = this.availableTree.getTopItem();
            int topAvailableIndex = this.availableTree.indexOf(topAvailableItem);
            TreeItem topSelectedItem = this.selectedTree.getTopItem();
            int topSelectedIndex = topSelectedItem != null ? this.selectedTree.indexOf(topSelectedItem) : 0;

            fireItemsSelected(getSelectedColumnEntriesIncludingNested(this.availableTree));

            if (topAvailableIndex > -1 && topAvailableIndex < this.availableTree.getItemCount()) {
                this.availableTree.setTopItem(this.availableTree.getItem(topAvailableIndex));
            }
            if (topSelectedIndex > -1 && topSelectedIndex < this.selectedTree.getItemCount()) {
                this.selectedTree.setTopItem(this.selectedTree.getItem(topSelectedIndex));
            }
        }
    }

    /**
     * Add selected items: 'Available tree' <-- 'Selected tree' Notify
     * listeners.
     */
    private void removeSelected() {
        if (isAnyLeafSelected(this.selectedTree)) {
            TreeItem topAvailableItem = this.availableTree.getTopItem();
            int topIndex = topAvailableItem == null ? -1 : this.availableTree.indexOf(topAvailableItem);

            TreeItem topSelectedItem = this.selectedTree.getTopItem();
            int topSelectedIndex = topSelectedItem == null ? -1 : this.selectedTree.indexOf(topSelectedItem);

            fireItemsRemoved(getSelectedColumnEntriesIncludingNested(this.selectedTree));

            if (topIndex > -1 && topIndex < this.availableTree.getItemCount()) {
                this.availableTree.setTopItem(this.availableTree.getItem(topIndex));
            }
            if (topSelectedIndex > -1 && topSelectedIndex < this.selectedTree.getItemCount()) {
                this.selectedTree.setTopItem(this.selectedTree.getItem(topSelectedIndex));
            }
        }
    }

    private void selectedTreeCollapsed(TreeEvent event) {
        TreeItem item = (TreeItem) event.item;
        ColumnGroupEntry columnGroupEntry = (ColumnGroupEntry) item.getData();
        fireGroupCollapsed(columnGroupEntry);
    }

    private void selectedTreeExpanded(TreeEvent event) {
        TreeItem item = (TreeItem) event.item;
        ColumnGroupEntry columnGroupEntry = (ColumnGroupEntry) item.getData();
        fireGroupExpanded(columnGroupEntry);
    }

    private void toggleColumnGroupSelection(TreeItem treeItem) {
        if (isColumnGroupLeaf(treeItem)) {
            Collection<TreeItem> selectedLeaves = ArrayUtil.asCollection(this.selectedTree.getSelection());
            boolean selected = selectedLeaves.contains(treeItem);
            if (selected) {
                selectAllChildren(this.selectedTree, treeItem);
            } else {
                unSelectAllChildren(this.selectedTree, treeItem);
            }
        }
    }

    private void selectAllChildren(Tree tree, TreeItem treeItem) {
        Collection<TreeItem> selectedLeaves = ArrayUtil.asCollection(tree.getSelection());
        if (isColumnGroupLeaf(treeItem)) {
            selectedLeaves.addAll(ArrayUtil.asCollection(treeItem.getItems()));
        }
        tree.setSelection(selectedLeaves.toArray(new TreeItem[] {}));
        tree.showSelection();
    }

    private void unSelectAllChildren(Tree tree, TreeItem treeItem) {
        Collection<TreeItem> selectedLeaves = ArrayUtil.asCollection(tree.getSelection());
        if (isColumnGroupLeaf(treeItem)) {
            selectedLeaves.removeAll(ArrayUtil.asCollection(treeItem.getItems()));
        }
        tree.setSelection(selectedLeaves.toArray(new TreeItem[] {}));
        tree.showSelection();
    }

    /**
     * Move selected items in the selected tree to the top. In case the selected
     * position is part of an unbreakable group, moves the selected items to the
     * start of the group.
     */
    void moveSelectedToTop() {
        if (isAnyLeafSelected(this.selectedTree)) {
            if (!isFirstLeafSelected(this.selectedTree)) {
                List<ColumnEntry> selectedColumnEntries = getSelectedColumnEntriesIncludingNested(this.selectedTree);
                List<ColumnGroupEntry> selectedColumnGroupEntries = getSelectedColumnGroupEntries(this.selectedTree);

                List<Integer> allSelectedPositions = merge(selectedColumnEntries, selectedColumnGroupEntries);

                // Group continuous positions
                List<List<Integer>> positionsGroupedByContiguous = PositionUtil.getGroupedByContiguous(allSelectedPositions);
                List<Integer> toPositions = new ArrayList<Integer>();

                // TODO all level?
                GroupModel groupModel = null;
                if (this.columnGroupHeaderLayer != null) {
                    groupModel = this.columnGroupHeaderLayer.getGroupModel(0);
                }

                int shift = getUpperMostPosition();
                for (List<Integer> groupedPositions : positionsGroupedByContiguous) {
                    // check for unbreakable group
                    // Position of first element in list
                    int firstPositionInGroup = groupedPositions.get(0);

                    boolean columnGroupMoved = columnGroupMoved(groupedPositions, selectedColumnGroupEntries);

                    // Column entry
                    ColumnEntry columnEntry = getNextColumnEntryForPosition(this.selectedTree, firstPositionInGroup);
                    int columnEntryPosition = -1;
                    if (groupModel != null) {
                        columnEntryPosition = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(columnEntry.getIndex());
                    }

                    if (this.columnGroupModel != null && this.columnGroupModel.isPartOfAnUnbreakableGroup(columnEntry.getIndex())) {
                        List<Integer> groupMembers = this.columnGroupModel.getColumnGroupByIndex(columnEntry.getIndex()).getMembers();
                        int groupUpperMost = groupMembers.get(0);
                        toPositions.add(groupUpperMost);
                    } else if (groupModel != null && groupModel.isPartOfAnUnbreakableGroup(columnEntryPosition) && !columnGroupMoved) {
                        toPositions.add(groupModel.getGroupByPosition(columnEntryPosition).getVisibleStartPosition());
                    } else {
                        toPositions.add(shift);
                        shift += groupedPositions.size();
                    }
                }
                fireItemsMoved(MoveDirectionEnum.UP, selectedColumnGroupEntries, selectedColumnEntries, positionsGroupedByContiguous, toPositions);
            }
        }
    }

    /**
     * Move selected items in the selected tree up (left).
     */
    protected void moveSelectedUp() {
        if (isAnyLeafSelected(this.selectedTree)) {
            if (!isFirstLeafSelected(this.selectedTree)) {
                List<ColumnEntry> selectedColumnEntries = getSelectedColumnEntriesIncludingNested(this.selectedTree);
                List<ColumnGroupEntry> selectedColumnGroupEntries = getSelectedColumnGroupEntries(this.selectedTree);

                List<Integer> allSelectedPositions = merge(selectedColumnEntries, selectedColumnGroupEntries);

                // Group continuous positions. If a column group moves, a bunch
                // of 'from' positions move to a single 'to' position
                List<List<Integer>> postionsGroupedByContiguous = PositionUtil.getGroupedByContiguous(allSelectedPositions);
                List<Integer> toPositions = new ArrayList<Integer>();

                // TODO all level?
                GroupModel groupModel = null;
                if (this.columnGroupHeaderLayer != null) {
                    groupModel = this.columnGroupHeaderLayer.getGroupModel(0);
                }

                // Set destination positions
                for (List<Integer> groupedPositions : postionsGroupedByContiguous) {
                    // Do these contiguous positions contain a column group ?
                    boolean columnGroupMoved = columnGroupMoved(groupedPositions, selectedColumnGroupEntries);

                    // If already at first position do not move
                    int firstPositionInGroup = groupedPositions.get(0);

                    // Column entry
                    ColumnEntry columnEntry = getPreviousColumnEntryForPosition(this.selectedTree, firstPositionInGroup);
                    int columnEntryIndex = columnEntry.getIndex();
                    int columnEntryPosition = -1;
                    if (groupModel != null) {
                        columnEntryPosition = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(columnEntry.getIndex());
                    }

                    // Previous column entry
                    ColumnEntry previousColumnEntry = getPreviousColumnEntryForPosition(this.selectedTree, firstPositionInGroup - 1);
                    int previousColumnEntryPosition = -1;
                    if (groupModel != null && previousColumnEntry != null) {
                        previousColumnEntryPosition = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(previousColumnEntry.getIndex());
                    }

                    // Previous column entry is null if the last leaf in the
                    // tree is selected
                    if (previousColumnEntry == null) {
                        toPositions.add(firstPositionInGroup);
                    } else {
                        int previousColumnEntryIndex = previousColumnEntry.getIndex();

                        if (columnGroupMoved) {
                            // If the previous entry is a column group
                            // move above it.
                            if (this.columnGroupModel != null
                                    && this.columnGroupModel.isPartOfAGroup(previousColumnEntryIndex)) {
                                ColumnGroup previousColumnGroup = this.columnGroupModel.getColumnGroupByIndex(previousColumnEntryIndex);
                                toPositions.add(firstPositionInGroup - previousColumnGroup.getSize());
                            } else if (groupModel != null
                                    && groupModel.isPartOfAGroup(previousColumnEntryPosition)) {
                                Group previousGroup = groupModel.getGroupByPosition(previousColumnEntryPosition);
                                toPositions.add(firstPositionInGroup - previousGroup.getVisibleSpan());
                            } else {
                                toPositions.add(firstPositionInGroup - 1);
                            }
                        } else {
                            // If is first member of the unbreakable group,
                            // can't move up i.e. out of the group
                            if (this.columnGroupModel != null
                                    && this.columnGroupModel.isPartOfAnUnbreakableGroup(columnEntryIndex)
                                    && !ColumnGroupUtils.isInTheSameGroup(columnEntryIndex, previousColumnEntryIndex, this.columnGroupModel)) {
                                return;
                            }

                            if (groupModel != null
                                    && groupModel.isPartOfAnUnbreakableGroup(columnEntryIndex)
                                    // TODO all level?
                                    && !ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, columnEntryPosition, previousColumnEntryPosition)) {
                                return;
                            }

                            // If previous entry is an unbreakable column group
                            // move above it
                            if (this.columnGroupModel != null
                                    && this.columnGroupModel.isPartOfAnUnbreakableGroup(previousColumnEntryIndex)
                                    && !this.columnGroupModel.isPartOfAGroup(columnEntryIndex)
                                    && !ColumnGroupUtils.isInTheSameGroup(columnEntryIndex, previousColumnEntryIndex, this.columnGroupModel)) {

                                ColumnGroup previousColumnGroup = this.columnGroupModel.getColumnGroupByIndex(previousColumnEntryIndex);
                                toPositions.add(firstPositionInGroup - previousColumnGroup.getSize());
                            } else if (groupModel != null
                                    && groupModel.isPartOfAnUnbreakableGroup(previousColumnEntryPosition)
                                    && !groupModel.isPartOfAGroup(columnEntryPosition)
                                    // TODO all level?
                                    && !ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, columnEntryPosition, previousColumnEntryPosition)) {

                                Group previousGroup = groupModel.getGroupByPosition(previousColumnEntryPosition);
                                toPositions.add(firstPositionInGroup - previousGroup.getVisibleSpan());
                            } else if (groupModel != null
                                    && ((groupModel.isPartOfAGroup(columnEntryPosition)
                                            && groupModel.getGroupByPosition(columnEntryPosition).isGroupStart(columnEntryPosition))
                                            || (groupModel.isPartOfAGroup(previousColumnEntryPosition)
                                                    && groupModel.getGroupByPosition(previousColumnEntryPosition).isGroupEnd(previousColumnEntryPosition)
                                                    && !groupModel.getGroupByPosition(previousColumnEntryPosition).isUnbreakable()))) {
                                // we first move out of the group if we are at
                                // the left edge in a current group or the right
                                // edge of the previous group
                                toPositions.add(firstPositionInGroup);
                            } else {
                                toPositions.add(firstPositionInGroup - 1);
                            }
                        }
                    }
                }

                fireItemsMoved(MoveDirectionEnum.UP, selectedColumnGroupEntries, selectedColumnEntries, postionsGroupedByContiguous, toPositions);
            }
        }
    }

    private List<Integer> merge(List<ColumnEntry> selectedColumnEntries, List<ColumnGroupEntry> selectedColumnGroupEntries) {
        // Convert to positions
        List<Integer> columnEntryPositions = ColumnChooserUtils.getColumnEntryPositions(selectedColumnEntries);
        List<Integer> columnGroupEntryPositions = ColumnGroupEntry.getColumnGroupEntryPositions(selectedColumnGroupEntries);

        // Selected columns + column groups
        Set<Integer> allSelectedPositionsSet = new HashSet<Integer>();
        allSelectedPositionsSet.addAll(columnEntryPositions);
        allSelectedPositionsSet.addAll(columnGroupEntryPositions);
        List<Integer> allSelectedPositions = new ArrayList<Integer>(allSelectedPositionsSet);
        Collections.sort(allSelectedPositions);

        return allSelectedPositions;
    }

    /**
     * Move selected items in the selected tree down (right).
     */
    protected void moveSelectedDown() {
        if (isAnyLeafSelected(this.selectedTree)) {
            if (!isLastLeafSelected(this.selectedTree)) {
                List<ColumnEntry> selectedColumnEntries = getSelectedColumnEntriesIncludingNested(this.selectedTree);
                List<ColumnGroupEntry> selectedColumnGroupEntries = getSelectedColumnGroupEntries(this.selectedTree);

                List<Integer> allSelectedPositions = merge(selectedColumnEntries, selectedColumnGroupEntries);

                // Group continuous positions
                List<List<Integer>> postionsGroupedByContiguous = PositionUtil.getGroupedByContiguous(allSelectedPositions);
                List<Integer> toPositions = new ArrayList<Integer>();

                // TODO all level?
                GroupModel groupModel = null;
                if (this.columnGroupHeaderLayer != null) {
                    groupModel = this.columnGroupHeaderLayer.getGroupModel(0);
                }

                // Set destination positions
                for (List<Integer> groupedPositions : postionsGroupedByContiguous) {
                    // Do these contiguous positions contain a column group ?
                    boolean columnGroupMoved = columnGroupMoved(groupedPositions, selectedColumnGroupEntries);

                    // Position of last element in list
                    int lastListIndex = groupedPositions.size() - 1;
                    int lastPositionInGroup = groupedPositions.get(lastListIndex);

                    // Column entry
                    ColumnEntry columnEntry = getNextColumnEntryForPosition(this.selectedTree, lastPositionInGroup);
                    int columnEntryIndex = columnEntry.getIndex();
                    int columnEntryPosition = -1;
                    if (groupModel != null) {
                        columnEntryPosition = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(columnEntry.getIndex());
                    }

                    // Next Column Entry
                    ColumnEntry nextColumnEntry = getNextColumnEntryForPosition(this.selectedTree, lastPositionInGroup + 1);
                    int nextColumnEntryPosition = -1;
                    if (groupModel != null && nextColumnEntry != null) {
                        nextColumnEntryPosition = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(nextColumnEntry.getIndex());
                    }

                    // Next column entry will be null the last leaf in the tree
                    // is selected
                    if (nextColumnEntry == null) {
                        toPositions.add(lastPositionInGroup);
                    } else {
                        int nextColumnEntryIndex = nextColumnEntry.getIndex();

                        if (columnGroupMoved) {
                            // If the next entry is a column group
                            // move past it.
                            if (this.columnGroupModel != null
                                    && this.columnGroupModel.isPartOfAGroup(nextColumnEntryIndex)) {
                                ColumnGroup nextColumnGroup = this.columnGroupModel.getColumnGroupByIndex(nextColumnEntryIndex);
                                toPositions.add(lastPositionInGroup + nextColumnGroup.getSize());
                            } else if (groupModel != null
                                    && groupModel.isPartOfAGroup(nextColumnEntryPosition)) {
                                Group nextGroup = groupModel.getGroupByPosition(nextColumnEntryPosition);
                                toPositions.add(lastPositionInGroup + nextGroup.getVisibleSpan());
                            } else {
                                toPositions.add(lastPositionInGroup + 1);
                            }
                        } else {
                            // If is last member of the unbreakable group, can't
                            // move down i.e. out of the group
                            if (this.columnGroupModel != null
                                    && this.columnGroupModel.isPartOfAnUnbreakableGroup(columnEntryIndex)
                                    && !ColumnGroupUtils.isInTheSameGroup(columnEntryIndex, nextColumnEntryIndex, this.columnGroupModel)) {
                                return;
                            }

                            if (groupModel != null
                                    && groupModel.isPartOfAnUnbreakableGroup(columnEntryPosition)
                                    // TODO all level?
                                    && !ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, columnEntryPosition, nextColumnEntryPosition)) {
                                return;
                            }

                            // If next entry is an unbreakable column group
                            // move past it
                            if (this.columnGroupModel != null
                                    && this.columnGroupModel.isPartOfAnUnbreakableGroup(nextColumnEntryIndex)
                                    && !this.columnGroupModel.isPartOfAGroup(columnEntryIndex)
                                    && !ColumnGroupUtils.isInTheSameGroup(columnEntryIndex, nextColumnEntryIndex, this.columnGroupModel)) {
                                ColumnGroup nextColumnGroup = this.columnGroupModel.getColumnGroupByIndex(nextColumnEntryIndex);
                                toPositions.add(lastPositionInGroup + nextColumnGroup.getSize());
                            } else if (groupModel != null
                                    && groupModel.isPartOfAnUnbreakableGroup(nextColumnEntryPosition)
                                    && !groupModel.isPartOfAGroup(columnEntryPosition)) {
                                Group nextGroup = groupModel.getGroupByPosition(nextColumnEntryPosition);
                                toPositions.add(lastPositionInGroup + nextGroup.getVisibleSpan());
                            } else if (groupModel != null
                                    && ((groupModel.isPartOfAGroup(columnEntryPosition)
                                            && groupModel.getGroupByPosition(columnEntryPosition).isGroupEnd(columnEntryPosition))
                                            || (groupModel.isPartOfAGroup(nextColumnEntryPosition)
                                                    && groupModel.getGroupByPosition(nextColumnEntryPosition).isGroupStart(nextColumnEntryPosition)
                                                    && !groupModel.getGroupByPosition(nextColumnEntryPosition).isUnbreakable()))) {
                                // we first move out of the group if we are at
                                // the left edge in a current group or the right
                                // edge of the previous group
                                toPositions.add(lastPositionInGroup);
                            } else {
                                toPositions.add(lastPositionInGroup + 1);
                            }
                        }
                    }
                }
                fireItemsMoved(MoveDirectionEnum.DOWN, selectedColumnGroupEntries, selectedColumnEntries, postionsGroupedByContiguous, toPositions);
            }
        }
    }

    /**
     * Move selected items in the selected tree to the bottom. In case the
     * selected position is part of an unbreakable group, moves the selected
     * items to the end of the group.
     */
    void moveSelectedToBottom() {
        if (isAnyLeafSelected(this.selectedTree)) {
            if (!isLastLeafSelected(this.selectedTree)) {
                List<ColumnEntry> selectedColumnEntries = getSelectedColumnEntriesIncludingNested(this.selectedTree);
                List<ColumnGroupEntry> selectedColumnGroupEntries = getSelectedColumnGroupEntries(this.selectedTree);

                List<Integer> allSelectedPositions = merge(selectedColumnEntries, selectedColumnGroupEntries);

                // Group continuous positions
                List<List<Integer>> positionsGroupedByContiguous = PositionUtil.getGroupedByContiguous(allSelectedPositions);
                List<Integer> toPositions = new ArrayList<Integer>();

                List<List<Integer>> reversed = new ArrayList<List<Integer>>(positionsGroupedByContiguous);
                Collections.reverse(reversed);

                int lowerMost = getLowerMostPosition();

                // TODO all level?
                GroupModel groupModel = null;
                if (this.columnGroupHeaderLayer != null) {
                    groupModel = this.columnGroupHeaderLayer.getGroupModel(0);
                }

                int shift = 0;
                for (List<Integer> groupedPositions : reversed) {
                    // check for unbreakable group
                    // Position of last element in list
                    int lastListIndex = groupedPositions.size() - 1;
                    int lastPositionInGroup = groupedPositions.get(lastListIndex);

                    boolean columnGroupMoved = columnGroupMoved(groupedPositions, selectedColumnGroupEntries);

                    // Column entry
                    ColumnEntry columnEntry = getNextColumnEntryForPosition(this.selectedTree, lastPositionInGroup);
                    int columnEntryPosition = -1;
                    if (groupModel != null) {
                        columnEntryPosition = this.columnGroupHeaderLayer.getPositionLayer().getColumnPositionByIndex(columnEntry.getIndex());
                    }

                    int columnEntryIndex = columnEntry.getIndex();
                    if (this.columnGroupModel != null && this.columnGroupModel.isPartOfAnUnbreakableGroup(columnEntryIndex)) {
                        List<Integer> groupMembers = this.columnGroupModel.getColumnGroupByIndex(columnEntryIndex).getMembers();
                        int groupLowerMost = groupMembers.get(groupMembers.size() - 1);
                        toPositions.add(groupLowerMost);
                    } else if (groupModel != null && groupModel.isPartOfAnUnbreakableGroup(columnEntryPosition) && !columnGroupMoved) {
                        Group group = groupModel.getGroupByPosition(columnEntryPosition);
                        toPositions.add(group.getVisibleStartPosition() + group.getVisibleSpan() - 1);
                    } else {
                        toPositions.add(Integer.valueOf(lowerMost - shift));
                        shift += groupedPositions.size();
                    }
                }

                fireItemsMoved(MoveDirectionEnum.DOWN, selectedColumnGroupEntries, selectedColumnEntries, reversed, toPositions);
            }
        }
    }

    private boolean columnGroupMoved(List<Integer> fromPositions, List<ColumnGroupEntry> movedColumnGroupEntries) {
        for (ColumnGroupEntry columnGroupEntry : movedColumnGroupEntries) {
            if (fromPositions.contains(columnGroupEntry.getFirstElementPosition())) {
                return true;
            }
        }
        return false;
    }

    private ColumnEntry getColumnEntryForPosition(Tree tree, int columnEntryPosition) {
        List<ColumnEntry> allColumnEntries = getColumnEntriesIncludingNested(this.selectedTree.getItems());

        for (ColumnEntry columnEntry : allColumnEntries) {
            if (columnEntry.getPosition().intValue() == columnEntryPosition) {
                return columnEntry;
            }
        }
        return null;
    }

    private ColumnEntry getPreviousColumnEntryForPosition(Tree tree, int columnEntryPosition) {
        ColumnEntry result = null;
        while (result == null && columnEntryPosition >= getUpperMostPosition()) {
            result = getColumnEntryForPosition(tree, columnEntryPosition);
            if (result == null) {
                columnEntryPosition--;
            }
        }
        return result;
    }

    private ColumnEntry getNextColumnEntryForPosition(Tree tree, int columnEntryPosition) {
        ColumnEntry result = null;
        while (result == null && columnEntryPosition <= getLowerMostPosition()) {
            result = getColumnEntryForPosition(tree, columnEntryPosition);
            if (result == null) {
                columnEntryPosition++;
            }
        }
        return result;
    }

    // Leaf related methods

    /**
     * Get Leaf index of the selected leaves in the tree
     */
    /**
     * Return the indexes of the selected leaves in the tree.
     *
     * @param tree
     *            The {@link Tree} to inspect.
     * @return The indexes of the selected leaves.
     */
    protected List<Integer> getIndexesOfSelectedLeaves(Tree tree) {
        List<TreeItem> allSelectedLeaves = ArrayUtil.asList(tree.getSelection());
        List<Integer> allSelectedIndexes = new ArrayList<Integer>();

        for (TreeItem selectedLeaf : allSelectedLeaves) {
            allSelectedIndexes.add(Integer.valueOf(tree.indexOf(selectedLeaf)));
        }

        return allSelectedIndexes;
    }

    /**
     * Expands all leaves.
     */
    public void expandAllLeaves() {
        List<TreeItem> allLeaves = ArrayUtil.asList(this.selectedTree.getItems());

        for (TreeItem leaf : allLeaves) {
            if (isColumnGroupLeaf(leaf)) {
                ColumnGroupEntry columnGroupEntry = (ColumnGroupEntry) leaf.getData();
                leaf.setExpanded(!columnGroupEntry.isCollapsed());
            }
        }
    }

    private boolean isColumnGroupLeaf(TreeItem treeItem) {
        if (ObjectUtils.isNotNull(treeItem)) {
            return treeItem.getData() instanceof ColumnGroupEntry;
        } else {
            return false;
        }
    }

    private boolean isLastLeafSelected(Tree tree) {
        TreeItem[] selectedLeaves = tree.getSelection();
        for (int i = 0; i < selectedLeaves.length; i++) {
            if (tree.indexOf(selectedLeaves[i]) + 1 == tree.getItemCount()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFirstLeafSelected(Tree tree) {
        TreeItem[] selectedLeaves = tree.getSelection();
        for (int i = 0; i < selectedLeaves.length; i++) {
            if (this.selectedTree.indexOf(selectedLeaves[i]) == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnyLeafSelected(Tree tree) {
        TreeItem[] selectedLeaves = tree.getSelection();
        return selectedLeaves != null && selectedLeaves.length > 0;
    }

    private ColumnEntry getColumnEntryInLeaf(TreeItem leaf) {
        if (!isColumnGroupLeaf(leaf)) {
            return (ColumnEntry) leaf.getData();
        } else {
            return null;
        }
    }

    /**
     * Removes all items from the available and the selected tree.
     */
    public void removeAllLeaves() {
        this.selectedTree.removeAll();
        this.availableTree.removeAll();
    }

    // Leaf Selection

    public void setSelectionIncludingNested(List<Integer> indexes) {
        setSelectionIncludingNested(this.selectedTree, indexes);
    }

    /**
     * Marks the leaves in the tree as selected
     *
     * @param tree
     *            containing the leaves
     * @param indexes
     *            index of the leaf in the tree
     */
    protected void setSelection(Tree tree, List<Integer> indexes) {
        List<TreeItem> selectedLeaves = new ArrayList<TreeItem>();

        for (Integer leafIndex : indexes) {
            selectedLeaves.add(tree.getItem(leafIndex.intValue()));
        }
        tree.setSelection(selectedLeaves.toArray(new TreeItem[] {}));
        tree.showSelection();
    }

    /**
     * Mark the leaves with matching column entries as selected. Also checks all
     * the children of the column group leaves
     *
     * @param columnEntryIndexes
     *            index of the ColumnEntry in the leaf
     */
    private void setSelectionIncludingNested(Tree tree, List<Integer> columnEntryIndexes) {
        Collection<TreeItem> allLeaves = ArrayUtil.asCollection(tree.getItems());
        List<TreeItem> selectedLeaves = new ArrayList<TreeItem>();

        for (TreeItem leaf : allLeaves) {
            if (!isColumnGroupLeaf(leaf)) {
                int index = getColumnEntryInLeaf(leaf).getIndex().intValue();
                if (columnEntryIndexes.contains(Integer.valueOf(index))) {
                    selectedLeaves.add(leaf);
                }
            } else {
                // Check all children in column groups
                Collection<TreeItem> columnGroupLeaves = ArrayUtil.asCollection(leaf.getItems());
                for (TreeItem columnGroupLeaf : columnGroupLeaves) {
                    int index = getColumnEntryInLeaf(columnGroupLeaf).getIndex().intValue();
                    if (columnEntryIndexes.contains(Integer.valueOf(index))) {
                        selectedLeaves.add(columnGroupLeaf);
                    }
                }
            }
        }
        tree.setSelection(selectedLeaves.toArray(new TreeItem[] {}));
        setGroupsSelectionIfRequired(tree, columnEntryIndexes);
        tree.showSelection();
    }

    /**
     * If all the leaves in a group are selected the group is also selected
     */
    private void setGroupsSelectionIfRequired(Tree tree, List<Integer> columnEntryIndexes) {
        Collection<TreeItem> allLeaves = ArrayUtil.asCollection(tree.getItems());
        Collection<TreeItem> selectedLeaves = ArrayUtil.asCollection(tree.getSelection());

        for (TreeItem leaf : allLeaves) {
            if (isColumnGroupLeaf(leaf)) {
                boolean markSelected = true;
                Collection<TreeItem> nestedLeaves = ArrayUtil.asCollection(leaf.getItems());

                for (TreeItem nestedLeaf : nestedLeaves) {
                    ColumnEntry columnEntry = getColumnEntryInLeaf(nestedLeaf);
                    if (!columnEntryIndexes.contains(columnEntry.getIndex())) {
                        markSelected = false;
                    }
                }
                if (markSelected) {
                    selectedLeaves.add(leaf);
                }
            }
        }
        tree.setSelection(selectedLeaves.toArray(new TreeItem[] {}));
    }

    protected Tree getSelectedTree() {
        return this.selectedTree;
    }

    /**
     * With this option, the dialog can be configure to either allow removing
     * all columns from the set of visible columns or prevent such a state by
     * disabling the "remove from selection" button if the selection contains
     * all remaining visible columns.
     *
     * @param preventHidingAllColumns
     *            if true, the dialog will prevent that the user selects no
     *            column at all, by disabling the "remove from selection" button
     *            appropriately.
     */
    public void setPreventHidingAllColumns(boolean preventHidingAllColumns) {
        this.preventHidingAllColumns = preventHidingAllColumns;
    }

    private boolean isSelectedTreeCompletelySelected() {
        final TreeItem[] selection = this.selectedTree.getSelection();
        final TreeItem[] items = this.selectedTree.getItems();

        for (TreeItem item : items) {
            // if any one item is NOT completely selected, the tree is NOT
            // completely selected.
            if (!isItemCompletelySelected(item, ArrayUtil.asCollection(selection))) {
                return false;
            }
        }

        return true;
    }

    private boolean isItemCompletelySelected(TreeItem item, Collection<TreeItem> selectedItems) {
        if (selectedItems.contains(item)) {
            // sub-root is selected: need not look any further, the whole
            // subtree is thereby selected
            return true;
        } else {
            TreeItem[] children = item.getItems();
            if (children.length == 0) {
                return false;
            } else {
                // if there are children: check if all children are selected
                // (which means the sub-root is implicitly selected)
                for (TreeItem child : item.getItems()) {
                    if (!isItemCompletelySelected(child, selectedItems)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    private int getUpperMostPosition() {
        List<ColumnEntry> entries = getColumnEntriesIncludingNested(this.selectedTree.getItems());
        return entries.get(0).getPosition();
    }

    private int getLowerMostPosition() {
        List<ColumnEntry> entries = getColumnEntriesIncludingNested(this.selectedTree.getItems());
        return entries.get(entries.size() - 1).getPosition();
    }

}
