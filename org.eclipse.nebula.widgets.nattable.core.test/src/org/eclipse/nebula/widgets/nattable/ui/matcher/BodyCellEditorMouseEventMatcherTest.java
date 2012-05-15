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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.ui.matcher.BodyCellEditorMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.junit.Assert;


import org.junit.Test;

public class BodyCellEditorMouseEventMatcherTest {

	@Test
	public void testEquals() {
		IMouseEventMatcher matcher1 = new BodyCellEditorMouseEventMatcher(TextCellEditor.class);
		IMouseEventMatcher matcher2 = new BodyCellEditorMouseEventMatcher(TextCellEditor.class);
		Assert.assertEquals(matcher1, matcher2);
	}
	
	@Test
	public void testNotEqual() {
		IMouseEventMatcher matcher = new BodyCellEditorMouseEventMatcher(TextCellEditor.class);
		Assert.assertFalse(matcher.equals(new BodyCellEditorMouseEventMatcher(CheckBoxCellEditor.class)));
	}
	
	@Test
	public void testMap() {
		Map<IMouseEventMatcher, String> map = new HashMap<IMouseEventMatcher, String>();
		map.put(new BodyCellEditorMouseEventMatcher(TextCellEditor.class), "ABC");
		Assert.assertEquals(1, map.size());
		map.remove(new BodyCellEditorMouseEventMatcher(TextCellEditor.class));
		Assert.assertEquals(0, map.size());
	}
	
}
