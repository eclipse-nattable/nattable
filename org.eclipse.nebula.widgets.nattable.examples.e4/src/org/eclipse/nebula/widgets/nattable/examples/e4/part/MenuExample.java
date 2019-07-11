/*****************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4.part;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.e4.AbstractE4NatExamplePart;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class MenuExample extends AbstractE4NatExamplePart {

    @Inject
    EMenuService menuService;

    @PostConstruct
    public void postConstruct(Composite parent, Shell shell) {
        parent.setLayout(new GridLayout());

        // property names of the Person class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "money",
                "birthday",
                "description" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("description", "Description");

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(
                        PersonService.getPersons(10),
                        new ReflectiveColumnPropertyAccessor<Person>(propertyNames));

        DefaultGridLayer gridLayer =
                new DefaultGridLayer(bodyDataProvider,
                        new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));

        NatTable natTable = new NatTable(parent, gridLayer, false);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        // application model menu configuration
        menuService.registerContextMenu(
                natTable,
                "org.eclipse.nebula.widgets.nattable.examples.e4.popupmenu.0");

        // get the menu registered by EMenuService
        final Menu e4Menu = natTable.getMenu();

        // remove the menu reference from NatTable instance
        natTable.setMenu(null);

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                // add NatTable menu items
                // and register the DisposeListener
                new PopupMenuBuilder(natTable, e4Menu)
                        .withInspectLabelsMenuItem()
                        .build();

                // register the UI binding
                uiBindingRegistry.registerMouseDownBinding(
                        new MouseEventMatcher(
                                SWT.NONE,
                                GridRegion.BODY,
                                MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(e4Menu));
            }
        });

        natTable.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        showSourceLinks(parent, getClass().getName());
    }

}