/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.test.data.Person;
import org.eclipse.nebula.widgets.nattable.test.data.PersonService;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;

/**
 * @author Dirk Fauth
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GlazedListTreeDataTest {

	private TreeList treeList;
	private GlazedListTreeData treeData;
	
	@Before
	public void setup() {
		EventList<Person> eventList = GlazedLists.eventList(PersonService.getFixedPersons());
		
		treeList = new TreeList(eventList, new PersonTreeFormat(), TreeList.nodesStartExpanded());

		treeData = new GlazedListTreeData(treeList);
	}
	
	@Test
	public void testInitialExpanded() {
		for (int i = 0; i < treeList.size(); i++) {
			assertTrue("Node is not expanded", treeData.isExpanded(i));
		}
	}
	
	@Test
	public void testCollapseAllStepByStep() {
		for (int i = treeList.size()-1; i >= 0; i--) {
			treeData.collapse(i);
		}
		
		for (int i = 0; i < treeList.size(); i++) {
			assertFalse(MessageFormat.format("Node at index {0} is expanded", i), treeData.isExpanded(i));
		}
	}
	
	@Test
	public void testCollapseAll() {
		treeData.collapseAll();
		
		for (int i = 0; i < treeList.size(); i++) {
			assertFalse(MessageFormat.format("Node at index {0} is expanded", i), treeData.isExpanded(i));
		}
	}
	
	@Test
	public void testCollapseExpandAll() {
		treeData.collapseAll();
		treeData.expandAll();
		
		for (int i = 0; i < treeList.size(); i++) {
			assertTrue(MessageFormat.format("Node at index {0} is collapsed", i), treeData.isExpanded(i));
		}
	}
	
	@Test
	public void testCollapseAllNonUnique() {
		treeList.addAll(PersonService.getFixedMixedPersons());
		
		treeData.collapseAll();
		
		for (int i = 0; i < treeList.size(); i++) {
			assertFalse(MessageFormat.format("Node at index {0} is expanded", i), treeData.isExpanded(i));
		}
	}
	
	@Test
	public void testCollapseExpandAllNonUnique() {
		treeList.addAll(PersonService.getFixedMixedPersons());
		
		treeData.collapseAll();
		treeData.expandAll();
		
		for (int i = 0; i < treeList.size(); i++) {
			assertTrue(MessageFormat.format("Node at index {0} is collapsed", i), treeData.isExpanded(i));
		}
	}
	
	@Test
	public void testGetChildrenByIndex() {
		List flandersChildren = treeData.getChildren(0);
		
		assertEquals(4, flandersChildren.size());
	}
	
	@Test
	public void testExpandCollapseByIndex() {
		assertTrue("Flanders is not expanded", treeData.isExpanded(0));
		
		//collapse Flanders
		treeData.collapse(0);

		assertFalse("Flanders is not expanded", treeData.isExpanded(0));
		
		//expand Flanders
		treeData.expand(0);
		
		assertTrue("Flanders is not expanded", treeData.isExpanded(0));
	}
	
	@Test
	public void testExpandCollapseByObject() {
		assertTrue("Flanders is not expanded", treeData.isExpanded("Flanders"));
		
		//collapse Flanders
		treeData.collapse("Flanders");

		assertFalse("Flanders is not expanded", treeData.isExpanded("Flanders"));
		
		//expand Flanders
		treeData.expand("Flanders");
		
		assertTrue("Flanders is not expanded", treeData.isExpanded("Flanders"));
	}
	
	private class PersonTreeFormat implements TreeList.Format<Object> {
		
		@Override
		public void getPath(List<Object> path, Object element) {
			if (element instanceof Person) {
				Person ele = (Person) element;
				path.add(ele.getLastName());
				path.add(ele.getFirstName());
			}
			path.add(element);
		}
		
		@Override
		public boolean allowsChildren(Object element) {
			return true;
		}

		@Override
		public Comparator<? super Object> getComparator(final int depth) {
			return new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {
					String e1 = (o1 instanceof Person) ? 
							(depth == 0 ? ((Person)o1).getLastName() : ((Person)o1).getFirstName()) 
								: o1.toString();
					String e2 = (o2 instanceof Person) ? 
							(depth == 0 ? ((Person)o2).getLastName() : ((Person)o2).getFirstName()) 
								: o2.toString();
					return e1.compareTo(e2);
				}
				
			};
		}
	}

}
