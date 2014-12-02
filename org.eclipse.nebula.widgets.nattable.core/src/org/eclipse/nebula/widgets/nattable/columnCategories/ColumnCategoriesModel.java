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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.columnCategories.Node.Type;

public class ColumnCategoriesModel implements Serializable {

    private static final long serialVersionUID = 4550L;

    /** Tree of the column category names */
    private final Tree tree = new Tree();

    public Node addRootCategory(String rootCategoryName) {
        if (isNotNull(this.tree.getRootElement())) {
            throw new IllegalStateException("Root has been set already. Clear using (clear()) to reset."); //$NON-NLS-1$
        }
        Node root = new Node(rootCategoryName, Type.ROOT);
        this.tree.setRootElement(root);
        return root;
    }

    public Node addCategory(Node parentCategory, String newCategoryName) {
        if (this.tree.getRootElement() == null) {
            throw new IllegalStateException("Root node must be set (using addRootNode()) before children can be added"); //$NON-NLS-1$
        }
        Node newNode = new Node(newCategoryName, Node.Type.CATEGORY);
        parentCategory.addChild(newNode);
        return newNode;
    }

    public void addColumnsToCategory(Node parentCategory, int... columnIndexes) {
        if (parentCategory.getType() != Type.CATEGORY) {
            throw new IllegalStateException("Columns can be added to a category node only."); //$NON-NLS-1$
        }

        for (Integer columnIndex : columnIndexes) {
            parentCategory.addChild(new Node(String.valueOf(columnIndex),
                    Type.COLUMN));
        }
    }

    public void removeColumnIndex(Integer hiddenColumnIndex) {
        this.tree.remove(String.valueOf(hiddenColumnIndex));
    }

    public Node getRootCategory() {
        return this.tree.getRootElement();
    }

    @Override
    public String toString() {
        return this.tree.toString();
    }

    public void dispose() {
        this.tree.clear();
    }

    public void clear() {
        this.tree.clear();
    }

}
