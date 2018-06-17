/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.examples._105_Persistence.PersistentColumnGroupGridExample;
import org.eclipse.nebula.widgets.nattable.examples.examples._105_Persistence.PersistentDefaultGridExample;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Adds hooks to the <code>onStart()</code> and <code>onStop()</code> methods to
 * save the settings to a properties file.
 * <p>
 * The following state is persisted out of the box
 * <ol>
 * <li>Column widths</li>
 * <li>Column order</li>
 * <li>Hidden columns</li>
 * <li>Row heights</li>
 * <li>Column groups</li>
 * </ol>
 *
 * Any additional state can be persisted by registering a IPersistable on an
 * {@link ILayer}.
 *
 * @see PersistentDefaultGridExample
 * @see PersistentColumnGroupGridExample
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
        return this.example.getName() + " (Persisted)";
    }

    @Override
    public String getDescription() {
        return this.example.getDescription();
    }

    @Override
    public String getShortDescription() {
        return this.example.getShortDescription();
    }

    @Override
    public Control createExampleControl(Composite parent) {
        this.natTable = (NatTable) this.example.createExampleControl(parent);
        return this.natTable;
    }

    @Override
    public void onStart() {
        Properties properties = new Properties();

        try (InputStream in = new FileInputStream(new File(PROPERTIES_FILE))) {
            System.out.println("Loading NatTable state from " + PROPERTIES_FILE);
            properties.load(in);
            this.natTable.loadState("", properties);
        } catch (FileNotFoundException e) {
            // No file found, oh well, move along
            System.out.println(PROPERTIES_FILE + " not found, skipping load");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.example.onStart();
    }

    @Override
    public void onStop() {
        this.example.onStop();

        Properties properties = new Properties();
        this.natTable.saveState("", properties);

        try (OutputStream out = new FileOutputStream(new File(PROPERTIES_FILE))) {
            System.out.println("Saving NatTable state to " + PROPERTIES_FILE);
            properties.store(out, "NatTable state");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
