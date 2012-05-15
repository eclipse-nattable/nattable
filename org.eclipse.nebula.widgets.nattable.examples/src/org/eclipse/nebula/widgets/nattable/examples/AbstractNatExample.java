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
package org.eclipse.nebula.widgets.nattable.examples;

import java.io.IOException;
import java.io.InputStream;


import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractNatExample implements INatExample {

	private Text outputArea;

	public String getName() {
		return getClass().getSimpleName().replaceAll("^_[0-9]*_", "").replace('_', ' ');
	}

	public String getShortDescription() {
		String description = getDescription();
		return description.substring(0, description.indexOf('.') + 1);
	}

	public String getDescription() {
		String description = getResourceAsString(getClass().getSimpleName() + ".txt");
		if (description != null) {
			return description;
		} else {
			return "";
		}
	}

	public void onStart() {
	}

	public void onStop() {
	}

	private String getResourceAsString(String resource) {
		InputStream inStream = getClass().getResourceAsStream(resource);

		if (inStream != null) {
			StringBuffer strBuf = new StringBuffer();
			try {
				int i = -1;
				while ((i = inStream.read()) != -1) {
					strBuf.append((char) i);
				}

				return strBuf.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Text area at the bottom
	 */
	public Text setupTextArea(Composite parent) {
		outputArea = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		outputArea.setEditable(false);

		final GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.heightHint = 100;

		outputArea.setLayoutData(layoutData);
		return outputArea;
	}

	public void log(String msg) {
		if (ObjectUtils.isNotNull(outputArea)) {
			outputArea.append(msg + "\n");
			System.out.println(msg);
		}
	}
}
