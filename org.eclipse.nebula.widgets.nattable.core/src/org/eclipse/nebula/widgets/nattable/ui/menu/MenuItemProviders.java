/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;


import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnCategories.ChooseColumnsFromCategoriesCommand;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommand;
import org.eclipse.nebula.widgets.nattable.columnRename.DisplayColumnRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.group.command.OpenCreateColumnGroupDialog;
import org.eclipse.nebula.widgets.nattable.group.command.UngroupColumnCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommand;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.style.editor.command.DisplayColumnStyleEditorCommand;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Widget;

/**
 * Helper class that provides several {@link IMenuItemProvider} for menu items that can be
 * used within a popup menu in the NatTable to execute NatTable specific actions.
 */
public class MenuItemProviders {

	/**
	 * Walk up the MenuItems (in case they are nested) and find the parent {@link Menu}
	 *
	 * @param selectionEvent
	 *            on the {@link MenuItem}
	 * @return data associated with the parent {@link Menu}
	 */
	public static NatEventData getNatEventData(SelectionEvent selectionEvent) {
		Widget widget = selectionEvent.widget;
		if (widget == null || !(widget instanceof MenuItem)) {
			return null;
		}

		MenuItem menuItem = (MenuItem) widget;
		Menu parentMenu = menuItem.getParent();
		Object data = null;
		while (parentMenu != null) {
			if (parentMenu.getData() == null) {
				parentMenu = parentMenu.getParentMenu();
			} else {
				data = parentMenu.getData();
				break;
			}
		}

		return data != null ? (NatEventData) data : null;
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link ColumnHideCommand} to a popup menu. This command is intended to hide the current selected
	 * column immediately.
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link ColumnHideCommand}.
	 * 			The {@link MenuItem} will be shown with the localized default text configured in NatTable core.
	 */
	public static IMenuItemProvider hideColumnMenuItemProvider() {
		return hideColumnMenuItemProvider(Messages.getString("MenuItemProviders.hideColumn")); //$NON-NLS-1$
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link ColumnHideCommand} to a popup menu. This command is intended to hide the current selected
	 * column immediately.
	 * <p>
	 * The {@link MenuItem} will be shown with the given menu label.
	 * @param menuLabel The text that will be showed for the generated {@link MenuItem}
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link ColumnHideCommand}.
	 */
	public static IMenuItemProvider hideColumnMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setImage(GUIHelper.getImage("hide_column")); //$NON-NLS-1$
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new ColumnHideCommand(natTable, columnPosition));
					}
				});
			}
		};
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link ShowAllColumnsCommand} to a popup menu. This command is intended to show all columns of 
	 * the NatTable and is used to unhide previous hidden columns.
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link ShowAllColumnsCommand}.
	 * 			The {@link MenuItem} will be shown with the localized default text configured in NatTable core.
	 */
	public static IMenuItemProvider showAllColumnsMenuItemProvider() {
		return showAllColumnsMenuItemProvider(Messages.getString("MenuItemProviders.showAllColumns")); //$NON-NLS-1$
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link ShowAllColumnsCommand} to a popup menu. This command is intended to show all columns of 
	 * the NatTable and is used to unhide previous hidden columns.
	 * <p>
	 * The {@link MenuItem} will be shown with the given menu label.
	 * @param menuLabel The text that will be showed for the generated {@link MenuItem}
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link ShowAllColumnsCommand}.
	 */
	public static IMenuItemProvider showAllColumnsMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem showAllColumns = new MenuItem(popupMenu, SWT.PUSH);
				showAllColumns.setText(menuLabel);
				showAllColumns.setImage(GUIHelper.getImage("show_column")); //$NON-NLS-1$
				showAllColumns.setEnabled(true);

				showAllColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ShowAllColumnsCommand());
					}
				});
			}
		};
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link RowHideCommand} to a popup menu. This command is intended to hide the current selected
	 * row immediately.
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link RowHideCommand}.
	 * 			The {@link MenuItem} will be shown with the localized default text configured in NatTable core.
	 */
	public static IMenuItemProvider hideRowMenuItemProvider() {
		return hideRowMenuItemProvider(Messages.getString("MenuItemProviders.hideRow")); //$NON-NLS-1$
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link RowHideCommand} to a popup menu. This command is intended to hide the current selected
	 * row immediately.
	 * <p>
	 * The {@link MenuItem} will be shown with the given menu label.
	 * @param menuLabel The text that will be showed for the generated {@link MenuItem}
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link RowHideCommand}.
	 */
	public static IMenuItemProvider hideRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setImage(GUIHelper.getImage("hide_row")); //$NON-NLS-1$
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int rowPosition = getNatEventData(event).getRowPosition();
						natTable.doCommand(new RowHideCommand(natTable, rowPosition));
					}
				});
			}
		};
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link ShowAllRowsCommand} to a popup menu. This command is intended to show all rows of 
	 * the NatTable and is used to unhide previous hidden rows.
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link ShowAllRowsCommand}.
	 * 			The {@link MenuItem} will be shown with the localized default text configured in NatTable core.
	 */
	public static IMenuItemProvider showAllRowsMenuItemProvider() {
		return showAllRowsMenuItemProvider(Messages.getString("MenuItemProviders.showAllRows")); //$NON-NLS-1$
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link ShowAllRowsCommand} to a popup menu. This command is intended to show all rows of 
	 * the NatTable and is used to unhide previous hidden rows.
	 * <p>
	 * The {@link MenuItem} will be shown with the given menu label.
	 * @param menuLabel The text that will be showed for the generated {@link MenuItem}
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the {@link ShowAllRowsCommand}.
	 */
	public static IMenuItemProvider showAllRowsMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem showAllRows = new MenuItem(popupMenu, SWT.PUSH);
				showAllRows.setText(menuLabel);
				showAllRows.setImage(GUIHelper.getImage("show_row")); //$NON-NLS-1$
				showAllRows.setEnabled(true);

				showAllRows.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ShowAllRowsCommand());
					}
				});
			}
		};
	}

	public static IMenuItemProvider autoResizeColumnMenuItemProvider() {
		return autoResizeColumnMenuItemProvider(Messages.getString("MenuItemProviders.autoResizeColumn")); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider autoResizeColumnMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem autoResizeColumns = new MenuItem(popupMenu, SWT.PUSH);
				autoResizeColumns.setText(menuLabel);
				autoResizeColumns.setImage(GUIHelper.getImage("auto_resize")); //$NON-NLS-1$
				autoResizeColumns.setEnabled(true);

				autoResizeColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new InitializeAutoResizeColumnsCommand(natTable, columnPosition, natTable.getConfigRegistry(), new GCFactory(natTable)));
					}
				});
			}
		};
	}

	public static IMenuItemProvider autoResizeRowMenuItemProvider() {
		return autoResizeRowMenuItemProvider(Messages.getString("MenuItemProviders.autoResizeRow")); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider autoResizeRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem autoResizeRows = new MenuItem(popupMenu, SWT.PUSH);
				autoResizeRows.setText(menuLabel);
				autoResizeRows.setEnabled(true);

				autoResizeRows.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int rowPosition = getNatEventData(event).getRowPosition();
						natTable.doCommand(new InitializeAutoResizeRowsCommand(natTable, rowPosition, natTable.getConfigRegistry(), new GCFactory(natTable)));
					}
				});
			}
		};
	}

	public static IMenuItemProvider autoResizeAllSelectedColumnMenuItemProvider() {
		return autoResizeAllSelectedColumnMenuItemProvider(Messages.getString("MenuItemProviders.autoResizeAllSelectedColumns")); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider autoResizeAllSelectedColumnMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem autoResizeColumns = new MenuItem(popupMenu, SWT.PUSH);
				autoResizeColumns.setText(menuLabel); 
				autoResizeColumns.setEnabled(true);

				autoResizeColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new InitializeAutoResizeColumnsCommand(natTable, columnPosition, natTable.getConfigRegistry(), new GCFactory(natTable)));
					}
				});
			}

		};
	}

	public static IMenuItemProvider columnChooserMenuItemProvider() {
		return columnChooserMenuItemProvider(Messages.getString("MenuItemProviders.chooseColumns")); //$NON-NLS-1$
	}

	public static IMenuItemProvider columnChooserMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnChooser = new MenuItem(popupMenu, SWT.PUSH);
				columnChooser.setText(menuLabel);
				columnChooser.setImage(GUIHelper.getImage("column_chooser")); //$NON-NLS-1$
				columnChooser.setEnabled(true);

				columnChooser.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new DisplayColumnChooserCommand(natTable));
					}
				});
			}
		};
	}

	public static IMenuItemProvider columnStyleEditorMenuItemProvider() {
		return columnStyleEditorMenuItemProvider(Messages.getString("MenuItemProviders.editStyles")); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider columnStyleEditorMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
				columnStyleEditor.setText(menuLabel);
				columnStyleEditor.setImage(GUIHelper.getImage("preferences")); //$NON-NLS-1$
				columnStyleEditor.setEnabled(true);

				columnStyleEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int rowPosition = getNatEventData(event).getRowPosition();
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new DisplayColumnStyleEditorCommand(natTable, natTable.getConfigRegistry(), columnPosition, rowPosition));
					}
				});
			}

		};
	}

	public static IMenuItemProvider renameColumnMenuItemProvider() {
		return renameColumnMenuItemProvider(Messages.getString("MenuItemProviders.renameColumn")); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider renameColumnMenuItemProvider(final String label) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(label);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						natTable.doCommand(new DisplayColumnRenameDialogCommand(natTable, getNatEventData(event).getColumnPosition()));
					}
				});
			}
		};
	}

	public static IMenuItemProvider createColumnGroupMenuItemProvider() {
		return createColumnGroupMenuItemProvider(Messages.getString("MenuItemProviders.createColumnGroup")); //$NON-NLS-1$
	}

	public static IMenuItemProvider createColumnGroupMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
				columnStyleEditor.setText(menuLabel);
				columnStyleEditor.setEnabled(true);

				columnStyleEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new OpenCreateColumnGroupDialog(natTable.getShell()));
					}
				});
			}
		};
	}

	public static IMenuItemProvider ungroupColumnsMenuItemProvider() {
		return ungroupColumnsMenuItemProvider(Messages.getString("MenuItemProviders.ungroupColumns")); //$NON-NLS-1$
	}

	public static IMenuItemProvider ungroupColumnsMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
				columnStyleEditor.setText(menuLabel);
				columnStyleEditor.setEnabled(true);

				columnStyleEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new UngroupColumnCommand());
					}
				});
			}
		};
	}
	
	public static IMenuItemProvider inspectLabelsMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(NatTable natTable, Menu popupMenu) {
				MenuItem inspectLabelsMenuItem = new MenuItem(popupMenu, SWT.PUSH);
				inspectLabelsMenuItem.setText(Messages.getString("MenuItemProviders.debugInfo")); //$NON-NLS-1$
				inspectLabelsMenuItem.setEnabled(true);

				inspectLabelsMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						NatEventData natEventData = getNatEventData(e);
						NatTable natTable = natEventData.getNatTable();
						int columnPosition = natEventData.getColumnPosition();
						int rowPosition = natEventData.getRowPosition();

						String msg = "Display mode: " + natTable.getDisplayModeByPosition(columnPosition, rowPosition) + "\nConfig labels: " //$NON-NLS-1$ //$NON-NLS-2$
								+ natTable.getConfigLabelsByPosition(columnPosition, rowPosition) + "\nData value: " //$NON-NLS-1$
								+ natTable.getDataValueByPosition(columnPosition, rowPosition) + "\n\nColumn position: " + columnPosition + "\nColumn index: " //$NON-NLS-1$ //$NON-NLS-2$
								+ natTable.getColumnIndexByPosition(columnPosition) + "\n\nRow position: " + rowPosition + "\nRow index: " //$NON-NLS-1$ //$NON-NLS-2$
								+ natTable.getRowIndexByPosition(rowPosition);

						MessageBox messageBox = new MessageBox(natTable.getShell(), SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText(Messages.getString("MenuItemProviders.debugInformation")); //$NON-NLS-1$
						messageBox.setMessage(msg);
						messageBox.open();
					}
				});
			}
		};
	}

	public static IMenuItemProvider categoriesBasedColumnChooserMenuItemProvider() {
		return categoriesBasedColumnChooserMenuItemProvider(Messages.getString("MenuItemProviders.columnCategoriesChooser")); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider categoriesBasedColumnChooserMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnChooser = new MenuItem(popupMenu, SWT.PUSH);
				columnChooser.setText(menuLabel);
				columnChooser.setImage(GUIHelper.getImage("column_categories_chooser")); //$NON-NLS-1$
				columnChooser.setEnabled(true);

				columnChooser.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ChooseColumnsFromCategoriesCommand(natTable));
					}
				});
			}
		};
	}

	public static IMenuItemProvider clearAllFiltersMenuItemProvider() {
		return clearAllFiltersMenuItemProvider(Messages.getString("MenuItemProviders.clearAllFilters")); //$NON-NLS-1$
	}

	public static IMenuItemProvider clearAllFiltersMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setImage(GUIHelper.getImage("remove_filter")); //$NON-NLS-1$
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ClearAllFiltersCommand());
					}
				});
			}
		};
	}

	public static IMenuItemProvider clearToggleFilterRowMenuItemProvider() {
		return clearToggleFilterRowMenuItemProvider(Messages.getString("MenuItemProviders.toggleFilterRow")); //$NON-NLS-1$
	}

	public static IMenuItemProvider clearToggleFilterRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setImage(GUIHelper.getImage("toggle_filter")); //$NON-NLS-1$
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ToggleFilterRowCommand());
					}
				});
			}
		};
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link DisplayPersistenceDialogCommand} to a popup menu. This command is intended to open the 
	 * DisplayPersistenceDialog for managing NatTable states (also called view management).
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the 
	 * 			{@link DisplayPersistenceDialogCommand}
	 * 			The {@link MenuItem} will be shown with the localized default text configured in NatTable core.
	 */
	public static IMenuItemProvider stateManagerMenuItemProvider() {
		return stateManagerMenuItemProvider(Messages.getString("MenuItemProviders.stateManager")); //$NON-NLS-1$
	}

	/**
	 * Will create and return the {@link IMenuItemProvider} that adds the action for executing the
	 * {@link DisplayPersistenceDialogCommand} to a popup menu. This command is intended to open the 
	 * DisplayPersistenceDialog for managing NatTable states (also called view management).
	 * <p>
	 * The {@link MenuItem} will be shown with the given menu label.
	 * @param menuLabel The text that will be showed for the generated {@link MenuItem}
	 * @return The {@link IMenuItemProvider} for the {@link MenuItem} that executes the 
	 * 			{@link DisplayPersistenceDialogCommand}
	 * 			The {@link MenuItem} will be shown with the localized default text configured in NatTable core.
	 */
	public static IMenuItemProvider stateManagerMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem saveState = new MenuItem(popupMenu, SWT.PUSH);
				saveState.setText(menuLabel);
				saveState.setImage(GUIHelper.getImage("table_icon")); //$NON-NLS-1$
				saveState.setEnabled(true);

				saveState.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new DisplayPersistenceDialogCommand(natTable));
					}
				});
			}
		};
	}

	/**
	 * @return An {@link IMenuItemProvider} for adding a separator to the popup menu.
	 */
	public static IMenuItemProvider separatorMenuItemProvider() {
		return new IMenuItemProvider() {
			public void addMenuItem(NatTable natTable, Menu popupMenu) {
				 new MenuItem(popupMenu, SWT.SEPARATOR);
			}
		};
	}

}
