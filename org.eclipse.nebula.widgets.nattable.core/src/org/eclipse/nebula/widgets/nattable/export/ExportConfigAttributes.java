/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
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
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 */
public interface ExportConfigAttributes {

    /**
     * The configuration attribute for specifying the concrete implementation
     * instance of {@link ILayerExporter} that should be used for exporting the
     * NatTable.
     */
    ConfigAttribute<ILayerExporter> EXPORTER = new ConfigAttribute<ILayerExporter>();
    /**
     * The configuration attribute for specifying a formatter that should be
     * used to format the values for the export.
     */
    ConfigAttribute<IExportFormatter> EXPORT_FORMATTER = new ConfigAttribute<IExportFormatter>();
    /**
     * The configuration attribute for specifying the format that should be used
     * for exporting Calendar or Date objects appropriately.
     */
    ConfigAttribute<String> DATE_FORMAT = new ConfigAttribute<String>();
    /**
     * The configuration attribute for specifying the concrete implementation
     * instance of ITableExporter that should be used for exporting the
     * NatTable.
     *
     * @since 1.5
     */
    ConfigAttribute<ITableExporter> TABLE_EXPORTER = new ConfigAttribute<ITableExporter>();
}
