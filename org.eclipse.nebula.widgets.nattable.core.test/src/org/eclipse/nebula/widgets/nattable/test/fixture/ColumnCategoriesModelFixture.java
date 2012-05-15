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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;

public class ColumnCategoriesModelFixture extends ColumnCategoriesModel {

	private static final long serialVersionUID = 1001L;

	public static final String CATEGORY_A_LABEL = "a";
	public static final String CATEGORY_B_LABEL = "b";
	public static final String CATEGORY_B1_LABEL = "b1";
	public static final String CATEGORY_B2_LABEL = "b2";
	public static final String CATEGORY_C_LABEL = "c";

	/**
	 * NOTE: Do not auto format !
	 *
	 * Root
	 * --a
	 * |  -- 0 .. 6
	 * --b
	 * | --b1
	 * | |	-- 7, 8
	 * | --b2
	 * | 	-- 9, 10, 11
	 * --c
	 * |  -- 12 .. 16
	 * -- 17 .. 19
	 */
	public ColumnCategoriesModelFixture() {
		Node root = addRootCategory("Root");
		root.addChildColumnIndexes(17, 18, 19);

		// a
		Node A = addCategory(root, CATEGORY_A_LABEL);
		A.addChildColumnIndexes(0, 2, 3, 4 , 5, 6);

		// b
		Node B = root.addChildCategory(CATEGORY_B_LABEL);
		B.addChildCategory(CATEGORY_B1_LABEL).addChildColumnIndexes(7, 8);
		B.addChildCategory(CATEGORY_B2_LABEL).addChildColumnIndexes(9, 10, 11);

		// c
		Node C = root.addChildCategory(CATEGORY_C_LABEL);
		addColumnsToCategory(C, 12, 13, 14 , 15, 16);
	}

}
