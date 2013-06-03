/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Adds hooks to the onStart() and onStop() methods to save the settings to a
 * properties file.
 * <p>
 * The following state is persisted out of the box</br>
 * <ol>
 * 		<li>Column widths</li>
 * 		<li>Column order</li>
 * 		<li>Hidden columns</li>
 * 		<li>Row heights</li>
 * 		<li>Column groups</li>
 * </ol>
 *
 * Any additional state can be persisted by registering a IPersistable
 * on an {@link ILayer}.
 *
 * @see PersistentStyleGridExample
 */
public class PersistentNatExampleWrapper extends AbstractNatExample {

	private static final String PROPERTIES_FILE = "natTable.properties";
	private final INatExample example;
	private NatTable natTable;

	public PersistentNatExampleWrapper(INatExample example) {
		this.example = example;
	}

	@Override
	public String getName() {
		return example.getName() + " (Persisted)";
	}

	@Override
	public String getDescription() {
		return example.getDescription();
	}

	@Override
	public String getShortDescription() {
		return example.getShortDescription();
	}

	public Control createExampleControl(Composite parent) {
		natTable = (NatTable) example.createExampleControl(parent);
		return natTable;
	}

	@Override
	public void onStart() {
		Properties properties = new Properties();

		try {
			System.out.println("Loading NatTable state from " + PROPERTIES_FILE);
			properties.load(new FileInputStream(new File(PROPERTIES_FILE)));
			natTable.loadState("", properties);
		} catch (FileNotFoundException e) {
			// No file found, oh well, move along
			System.out.println(PROPERTIES_FILE + " not found, skipping load");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		example.onStart();
	}

	@Override
	public void onStop() {
		example.onStop();

		Properties properties = new Properties();

		natTable.saveState("", properties);

		try {
			System.out.println("Saving NatTable state to " + PROPERTIES_FILE);
			properties.store(new FileOutputStream(new File(PROPERTIES_FILE)), "NatTable state");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
