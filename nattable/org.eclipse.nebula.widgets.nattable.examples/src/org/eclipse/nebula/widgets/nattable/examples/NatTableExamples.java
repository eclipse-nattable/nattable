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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.examples.runner.TabbedNatExampleRunner;


public class NatTableExamples {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		List<String> examples = new ArrayList<String>();

		InputStream inputStream = NatTableExamples.class.getResourceAsStream("/examples.index");
		if (inputStream != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = reader.readLine();
			while (line != null) {
				examples.add(line);
				line = reader.readLine();
			}
			reader.close();
		} else {
			System.out.println("examples.index not found, reconstructing");
			
			File examplesDir = new File("src" + INatExample.BASE_PATH);
			findExamples(examplesDir, examples);
			
			File examplesIndexFile = new File("src", "examples.index");
			BufferedWriter writer = new BufferedWriter(new FileWriter(examplesIndexFile));
			for (String example : examples) {
				writer.write(example + "\n");
			}
			writer.flush();
			writer.close();
		}

		TabbedNatExampleRunner.run(examples.toArray(new String[] {}));
	}
	
	private static void findExamples(File dir, List<String> examples) throws IOException {
		for (String s : dir.list()) {
			File f = new File(dir, s);
			if (f.isDirectory()) {
				findExamples(f, examples);
			} else {
				String examplePath = dir.getCanonicalPath() + File.separator + s;
				examplePath = examplePath.replace(File.separator, "/");  // Convert to /-delimited path
				if (examplePath.endsWith(".java")) {
					examplePath = examplePath.replaceAll("^.*" + INatExample.BASE_PATH, "").replaceAll("\\.java$", "");
					Class<? extends INatExample> exampleClass = TabbedNatExampleRunner.getExampleClass(examplePath);
					if (exampleClass != null) {
						examples.add(examplePath);
					}
				}
			}
		}
	}

}
