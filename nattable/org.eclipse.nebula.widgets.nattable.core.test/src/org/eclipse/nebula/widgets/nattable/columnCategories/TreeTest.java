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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.columnCategories.Tree;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TreeTest {

	private Tree tree;

	@Before
	public void setup() {
		tree = new Tree();

		Node root = newNode("R");
		tree.setRootElement(root);

		root.addChild(newNode("a"));

		Node b = newNode("b");
		root.addChild(b);
		b.addChild(newNode("b1"));
		b.addChild(newNode("b2"));
		Node b3 = newNode("b3");
		b.addChild(b3);

		b3.addChild(newNode("b3a"));
		b3.addChild(newNode("b3b"));

		root.addChild(newNode("c"));
	}

	@Test
	public void basics() throws Exception {
		Assert.assertEquals("R", tree.getRootElement().getData());
		Assert.assertEquals(3, tree.getRootElement().getNumberOfChildren());
		Assert.assertEquals(9, tree.toList().size());
		Assert.assertEquals("[{UNKNOWN,R,[a,b,c]}, {UNKNOWN,a,[]}, {UNKNOWN,b,[b1,b2,b3]}, {UNKNOWN,b1,[]}, {UNKNOWN,b2,[]}, {UNKNOWN,b3,[b3a,b3b]}, {UNKNOWN,b3a,[]}, {UNKNOWN,b3b,[]}, {UNKNOWN,c,[]}]", tree.toString());
	}

	@Test
	public void findParent() throws Exception {
		Node found = tree.find("c");
		Assert.assertNotNull(found);
		Assert.assertEquals("R", found.getParent().getData());

		found = tree.find("b3b");
		Assert.assertEquals("b3", found.getParent().getData());
	}

	@Test
	public void findElements() throws Exception {
		Node found = tree.find(tree.getRootElement(), "b2");
		Assert.assertNotNull(found);
		Assert.assertEquals("b2", found.getData());

		found = tree.find(tree.getRootElement(), "b3b");
		Assert.assertNotNull(found);
	}

	@Test
	public void insertChild() throws Exception {
		Node a = tree.find("a");
		a.addChild(newNode("a1"));
		a.addChild(newNode("a2"));
		Assert.assertEquals(2, a.getNumberOfChildren());
		Assert.assertEquals("{UNKNOWN,a,[a1,a2]}", a.toString());

		a.insertChildAt(1, newNode("a11"));
		Assert.assertEquals(3, a.getNumberOfChildren());
		Assert.assertEquals("{UNKNOWN,a,[a1,a11,a2]}", a.toString());
	}

	@Test
	public void remove() throws Exception {
		Node root = tree.getRootElement();
		assertEquals(3, root.getNumberOfChildren());

		assertTrue(tree.remove(root.getChildren().get(1).getData()));
		assertEquals(2, root.getNumberOfChildren());

		assertFalse(tree.remove("Non Existent Node"));
	}

	private Node newNode(String data) {
		return new Node(data);
	}
}
