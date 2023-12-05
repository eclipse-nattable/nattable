/*******************************************************************************
 * Copyright (c) 2020, 2023 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

public class LabelStackTest {

    @Test
    public void shouldCorrectlyInitializeLabelStack() {
        LabelStack labels = new LabelStack("One", "Two", "Three");

        assertTrue(labels.hasLabel("One"), "One is not contained");
        assertTrue(labels.hasLabel("Two"), "Two is not contained");
        assertTrue(labels.hasLabel("Three"), "Three is not contained");

        Iterator<String> iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());
    }

    @Test
    public void shouldCorrectlyAddLabelsToLabelStack() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertTrue(labels.hasLabel("One"), "One is not contained");
        assertTrue(labels.hasLabel("Two"), "Two is not contained");
        assertTrue(labels.hasLabel("Three"), "Three is not contained");

        Iterator<String> iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());
    }

    @Test
    public void shouldNotAddLabelsTwiceToLabelStack() {
        LabelStack labels = new LabelStack("One", "Two", "Three");

        assertFalse(labels.addLabel("One"), "One was added twice");
        assertEquals(3, labels.size());

        Iterator<String> iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());
    }

    @Test
    public void shouldAddLabelOnTopToLabelStack() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertEquals(3, labels.size());

        Iterator<String> iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());

        labels.addLabelOnTop("Four");

        assertEquals(4, labels.size());

        iterator = labels.iterator();
        assertEquals("Four", iterator.next());
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());
    }

    @Test
    public void shouldMoveLabelToTop() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertEquals(3, labels.size());

        Iterator<String> iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());

        labels.addLabelOnTop("Three");

        assertEquals(3, labels.size());

        iterator = labels.iterator();
        assertEquals("Three", iterator.next());
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
    }

    @Test
    public void shouldRemoveLabel() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertEquals(3, labels.size());

        Iterator<String> iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Two", iterator.next());
        assertEquals("Three", iterator.next());

        labels.removeLabel("Two");

        assertEquals(2, labels.size());

        iterator = labels.iterator();
        assertEquals("One", iterator.next());
        assertEquals("Three", iterator.next());
    }

    @Test
    public void shouldFindSingleLabel() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertTrue(labels.hasLabel("One"));
        assertTrue(labels.hasLabel("Two"));
        assertTrue(labels.hasLabel("Three"));
    }

    @Test
    public void shouldNotFindSingleLabel() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertFalse(labels.hasLabel("Four"));
        assertFalse(labels.hasLabel("Five"));
        assertFalse(labels.hasLabel("Six"));
    }

    @Test
    public void shouldFindAllLabels() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        assertTrue(labels.hasAllLabels());
        assertTrue(labels.hasAllLabels("Two"));
        assertTrue(labels.hasAllLabels("One", "Two"));
        assertTrue(labels.hasAllLabels("One", "Three"));
        assertTrue(labels.hasAllLabels("One", "Two", "Three"));
    }

    @Test
    public void shouldFindAllLabelsList() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Three");

        ArrayList<String> toCheck = new ArrayList<>();
        toCheck.add("One");
        toCheck.add("Two");
        toCheck.add("Three");
        assertTrue(labels.hasAllLabels(toCheck));
    }

    @Test
    public void shouldNotFindAllLabels() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Four");

        assertFalse(labels.hasAllLabels("One", "Two", "Three"));
    }

    @Test
    public void shouldNotFindAllLabelsList() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Four");

        ArrayList<String> toCheck = new ArrayList<>();
        toCheck.add("One");
        toCheck.add("Two");
        toCheck.add("Three");
        assertFalse(labels.hasAllLabels(toCheck));
    }

    @Test
    public void shouldHandleNull() {
        LabelStack labels = new LabelStack();
        labels.add("One");
        labels.add("Two");
        labels.add("Trhee");

        assertFalse(labels.hasAllLabels("One", null, "Two"));

        labels.add(null);

        assertTrue(labels.hasAllLabels("One", null, "Two"));

        ArrayList<String> toCheck = null;
        assertFalse(labels.hasAllLabels(toCheck));
    }
}