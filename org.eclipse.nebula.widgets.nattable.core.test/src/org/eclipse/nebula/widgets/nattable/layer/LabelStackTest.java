package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

public class LabelStackTest {

	@Test
	public void shouldCorrectlyInitializeLabelStack() {
		LabelStack labels = new LabelStack("One", "Two", "Three");
		
		assertTrue("One is not contained", labels.hasLabel("One"));
		assertTrue("Two is not contained", labels.hasLabel("Two"));
		assertTrue("Three is not contained", labels.hasLabel("Three"));
		
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
		
		assertTrue("One is not contained", labels.hasLabel("One"));
		assertTrue("Two is not contained", labels.hasLabel("Two"));
		assertTrue("Three is not contained", labels.hasLabel("Three"));
		
		Iterator<String> iterator = labels.iterator();
		assertEquals("One", iterator.next());
		assertEquals("Two", iterator.next());
		assertEquals("Three", iterator.next());
	}

	@Test
	public void shouldNotAddLabelsTwiceToLabelStack() {
		LabelStack labels = new LabelStack();
		labels.add("One");
		labels.add("Two");
		labels.add("Three");
		
		assertFalse("One was added twice", labels.addLabel("One"));
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
}
