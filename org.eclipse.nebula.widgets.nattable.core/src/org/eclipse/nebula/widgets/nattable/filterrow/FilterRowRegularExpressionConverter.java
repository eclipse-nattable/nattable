/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;

/**
 * {@link IDisplayConverter} that is intended to be used for filter row editors
 * that are configured for regular expression evaluation. It will add simplified
 * usage of wildcards for end users by replacing * to the regular expression
 * (.*) and ? to the regular expression (.?).
 * <p>
 * <b><u>Note:</u></b><br>
 * As the characters * and ? are replaced to match the corresponding regular
 * expression using this {@link IDisplayConverter} will suppress the ability to
 * search for those characters completely. If it should be supported to search
 * for those wildcards, implement a custom converter that supports to mask the
 * icons or stick with the default regular expression syntax.
 * </p>
 *
 * <b><u>Note:</u></b><br>
 * The {@link FilterRowRegularExpressionConverter} needs to be registered as
 * {@link CellConfigAttributes#DISPLAY_CONVERTER}!
 * 
 * <pre>
 * configRegistry.registerConfigAttribute(
 *         CellConfigAttributes.DISPLAY_CONVERTER,
 *         new FilterRowRegularExpressionConverter(),
 *         DisplayMode.NORMAL,
 *         FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
 *                 + DataModelConstants.FIRSTNAME_COLUMN_POSITION);
 * </pre>
 *
 * @since 1.4
 */
public class FilterRowRegularExpressionConverter extends DisplayConverter {

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        if (displayValue != null) {
            displayValue = displayValue.toString().replaceAll("\\*", "(.\\*)"); //$NON-NLS-1$ //$NON-NLS-2$
            displayValue = displayValue.toString().replaceAll("\\?", "(.\\?)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return displayValue;
    }

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        if (canonicalValue != null) {
            canonicalValue = canonicalValue.toString().replaceAll("\\(\\.\\*\\)", "\\*"); //$NON-NLS-1$ //$NON-NLS-2$
            canonicalValue = canonicalValue.toString().replaceAll("\\(\\.\\?\\)", "\\?"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return canonicalValue;
    }
}
