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
package org.eclipse.nebula.widgets.nattable.examples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.examples.runner.TabbedNatExampleRunner;

public class NatTableExamples {

    public static void main(String[] args)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        if (args.length == 0) {
            List<String> examples;

            InputStream inputStream = NatTableExamples.class.getResourceAsStream("/examples.index");
            if (inputStream != null) {
                examples = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    String line = reader.readLine();
                    while (line != null) {
                        examples.add(line);
                        line = reader.readLine();
                    }
                }
            } else {
                System.out.println("examples.index not found, reconstructing");
                examples = createExamplesIndex(null);
            }

            TabbedNatExampleRunner.run(examples.toArray(new String[0]));
        } else if (args.length == 2 && "--createIndex".equals(args[0])) {
            System.out.println("Creating examples.index");
            System.out.println("basedir: " + args[1]);
            createExamplesIndex(args[1]);
        } else {
            System.out.println("Usage: NatTableExamples [--createIndex <basedir>]");
        }
    }

    public static List<String> createExamplesIndex(String basedir)
            throws IOException {
        List<String> examples = new ArrayList<>();

        File examplesDir = new File(basedir, "src" + INatExample.BASE_PATH);
        findTutorialExamples(examplesDir, examples);

        examplesDir = new File(basedir, "src" + INatExample.CLASSIC_BASE_PATH);
        findExamples(examplesDir, examples, INatExample.CLASSIC_EXAMPLES_PREFIX);

        File examplesIndexFile = new File(new File(basedir, "src"), "examples.index");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(examplesIndexFile))) {
            for (String example : examples) {
                writer.write(example + "\n");
            }
            writer.flush();
        }

        return examples;
    }

    private static void findTutorialExamples(File dir, List<String> examples)
            throws IOException {
        FilenameFilter packageFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches("_\\d{3}.*");
            }
        };

        List<String> sortedList = Arrays.asList(dir.list(packageFilter));
        Collections.sort(sortedList);
        for (String packageName : sortedList) {
            File f = new File(dir, packageName);
            if (f.isDirectory()) {
                findExamples(f, examples, INatExample.TUTORIAL_EXAMPLES_PREFIX);
            }
        }
    }

    public static void findExamples(File dir, List<String> examples, String prefix)
            throws IOException {
        List<String> sortedList = Arrays.asList(dir.list());
        Collections.sort(sortedList);
        for (String s : sortedList) {
            File f = new File(dir, s);
            if (f.isDirectory()) {
                findExamples(f, examples, prefix);
            } else {
                String examplePath = dir.getCanonicalPath() + File.separator + s;
                examplePath = examplePath.replace(File.separator, "/"); // Convert
                                                                        // to
                                                                        // /-delimited
                                                                        // path
                if (examplePath.endsWith(".java")) {
                    examplePath = examplePath.replaceAll("^.*/src/", "").replaceAll("\\.java$", "");
                    examples.add(prefix + examplePath);
                }
            }
        }
    }

}
