/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.columnCategories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TreeTest {

    private Tree tree;

    @BeforeEach
    public void setup() {
        this.tree = new Tree();

        Node root = newNode("R");
        this.tree.setRootElement(root);

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
        assertEquals("R", this.tree.getRootElement().getData());
        assertEquals(3, this.tree.getRootElement().getNumberOfChildren());
        assertEquals(9, this.tree.toList().size());
        assertEquals(
                "[{UNKNOWN,R,[a,b,c]}, {UNKNOWN,a,[]}, {UNKNOWN,b,[b1,b2,b3]}, {UNKNOWN,b1,[]}, {UNKNOWN,b2,[]}, {UNKNOWN,b3,[b3a,b3b]}, {UNKNOWN,b3a,[]}, {UNKNOWN,b3b,[]}, {UNKNOWN,c,[]}]",
                this.tree.toString());
    }

    @Test
    public void findParent() throws Exception {
        Node found = this.tree.find("c");
        assertNotNull(found);
        assertEquals("R", found.getParent().getData());

        found = this.tree.find("b3b");
        assertEquals("b3", found.getParent().getData());
    }

    @Test
    public void findElements() throws Exception {
        Node found = this.tree.find(this.tree.getRootElement(), "b2");
        assertNotNull(found);
        assertEquals("b2", found.getData());

        found = this.tree.find(this.tree.getRootElement(), "b3b");
        assertNotNull(found);
    }

    @Test
    public void insertChild() throws Exception {
        Node a = this.tree.find("a");
        a.addChild(newNode("a1"));
        a.addChild(newNode("a2"));
        assertEquals(2, a.getNumberOfChildren());
        assertEquals("{UNKNOWN,a,[a1,a2]}", a.toString());

        a.insertChildAt(1, newNode("a11"));
        assertEquals(3, a.getNumberOfChildren());
        assertEquals("{UNKNOWN,a,[a1,a11,a2]}", a.toString());
    }

    @Test
    public void remove() throws Exception {
        Node root = this.tree.getRootElement();
        assertEquals(3, root.getNumberOfChildren());

        assertTrue(this.tree.remove(root.getChildren().get(1).getData()));
        assertEquals(2, root.getNumberOfChildren());

        assertFalse(this.tree.remove("Non Existent Node"));
    }

    private Node newNode(String data) {
        return new Node(data);
    }
}
