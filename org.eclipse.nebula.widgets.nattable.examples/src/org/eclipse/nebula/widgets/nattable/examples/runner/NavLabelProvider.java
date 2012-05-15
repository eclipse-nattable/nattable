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
package org.eclipse.nebula.widgets.nattable.examples.runner;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;

public class NavLabelProvider extends LabelProvider {

	private final NavContentProvider contentProvider;

	public NavLabelProvider(NavContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}
	
	@Override
	public String getText(Object element) {
		String str = (String) element;
		if (!contentProvider.hasChildren(element)) {
			INatExample example = TabbedNatExampleRunner.getExample(str);
			return example.getName();
		}
		
		int lastSlashIndex = str.lastIndexOf('/');
		if (lastSlashIndex < 0) {
			return format(str);
		} else {
			return format(str.substring(lastSlashIndex + 1));
		}
	}
	
	private String format(String str) {
		return str.replaceAll("^_[0-9]*_", "").replace('_', ' ');
	}
	
}
