/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * @author Dirk Fauth
 *
 */
public class DarkGroupByThemeExtension extends ModernGroupByThemeExtension {

    {
        this.groupByHeaderBgColor = GUIHelper.COLOR_BLACK;

        this.groupByBgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;

        this.groupByHintBgColor = GUIHelper.COLOR_BLACK;
        this.groupByHintFgColor = GUIHelper.COLOR_WIDGET_DARK_SHADOW;
    }
}
