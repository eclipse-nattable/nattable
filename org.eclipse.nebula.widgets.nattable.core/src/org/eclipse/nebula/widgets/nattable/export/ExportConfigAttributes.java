/*******************************************************************************
 * Copyright (c) 2013, 2023 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.export;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * Configuration attributes that are used to configure the export functionality.
 */
public final class ExportConfigAttributes {

    private ExportConfigAttributes() {
        // private default constructor for constants class
    }

    /**
     * The configuration attribute for specifying the concrete implementation
     * instance of {@link ILayerExporter} that should be used for exporting the
     * NatTable.
     */
    public static final ConfigAttribute<ILayerExporter> EXPORTER = new ConfigAttribute<>();
    /**
     * The configuration attribute for specifying a formatter that should be
     * used to format the values for the export.
     */
    public static final ConfigAttribute<IExportFormatter> EXPORT_FORMATTER = new ConfigAttribute<>();
    /**
     * The configuration attribute for specifying the format that should be used
     * for exporting Calendar or Date objects appropriately.
     */
    public static final ConfigAttribute<String> DATE_FORMAT = new ConfigAttribute<>();
    /**
     * The configuration attribute for specifying the format that should be used
     * for exporting Number objects appropriately.
     *
     * @since 2.1
     */
    public static final ConfigAttribute<String> NUMBER_FORMAT = new ConfigAttribute<>();
    /**
     * The configuration attribute for specifying the concrete implementation
     * instance of ITableExporter that should be used for exporting the
     * NatTable.
     *
     * @since 1.5
     */
    public static final ConfigAttribute<ITableExporter> TABLE_EXPORTER = new ConfigAttribute<>();
}
