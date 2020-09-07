/*******************************************************************************
 * Copyright (c) 2014, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.summaryrow;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;

/**
 * Special display converter that will render a default value in case there is
 * no summary value calculated yet. If there is a value calculated, it will be
 * converted using the wrapped display converter.
 * <p>
 * By default "..." will be used as default value.
 * </p>
 */
public class SummaryDisplayConverter extends DisplayConverter {

    private Object defaultSummaryValue = ISummaryProvider.DEFAULT_SUMMARY_VALUE;

    private final IDisplayConverter wrappedConverter;

    /**
     * @param wrappedConverter
     *            The IDisplayConverter that is wrapped by this
     *            GroupBySummaryDisplayConverter. Will be used to convert
     *            calculated values.
     */
    public SummaryDisplayConverter(IDisplayConverter wrappedConverter) {
        this.wrappedConverter = wrappedConverter;
    }

    /**
     * @param wrappedConverter
     *            The IDisplayConverter that is wrapped by this
     *            GroupBySummaryDisplayConverter. Will be used to convert
     *            calculated values.
     * @param defaultSummaryValue
     *            The value that will be shown in case the summary value is not
     *            calculated yet.
     */
    public SummaryDisplayConverter(IDisplayConverter wrappedConverter,
            Object defaultSummaryValue) {
        this.wrappedConverter = wrappedConverter;
        this.defaultSummaryValue = defaultSummaryValue;
    }

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        if (canonicalValue == null) {
            return this.defaultSummaryValue;
        }
        return this.wrappedConverter.canonicalToDisplayValue(canonicalValue);
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        return this.wrappedConverter.displayToCanonicalValue(displayValue);
    }

}
