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
package org.eclipse.nebula.widgets.nattable.examples.runner;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.nattable.examples.INatExample;

public class NavLabelProvider extends LabelProvider {

    protected final NavContentProvider contentProvider;

    public NavLabelProvider(NavContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }

    @Override
    public String getText(Object element) {
        String str = (String) element;
        if (!this.contentProvider.hasChildren(element)) {
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

    protected String format(String str) {
        return str.replaceAll("^_[0-9]*_", "").replace('_', ' ');
    }

}
