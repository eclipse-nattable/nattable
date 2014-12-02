/*******************************************************************************
 * Copyright (c) 2012, 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
import org.eclipse.nebula.widgets.nattable.persistence.PersistenceHelper;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommand;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.persistence.command.IStateChangedListener;
import org.eclipse.nebula.widgets.nattable.persistence.command.StateChangeEvent;
import org.eclipse.nebula.widgets.nattable.persistence.command.StateChangeEvent.StateChangeType;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that allows to save and load NatTable state. It will operate on the
 * the specified NatTable and Properties instances. If the Properties need to be
 * persisted e.g. in the file system, the developer has to take care of that
 * himself.
 *
 * <p>
 * It is possible to listen for state change events on the view configurations.
 * Rather than adding listeners to this dialog yourself, you should register the
 * listeners to the {@link DisplayColumnChooserCommandHandler}, as it will
 * handle propagating the listeners to newly created instances of this dialog.
 *
 * @author Dirk Fauth
 *
 * @see DisplayPersistenceDialogCommand
 * @see DisplayPersistenceDialogCommandHandler
 */
public class PersistenceDialog extends Dialog {

    /**
     * Key under which the name of the active view configuration is stored
     * within the properties. Used to indicate which view configuration is
     * currently active and to be able to restore a view based on the last
     * active one, when persisting the states to a file or database.
     */
    public static final String ACTIVE_VIEW_CONFIGURATION_KEY = "PersistenceDialog.activeViewConfiguration"; //$NON-NLS-1$

    /**
     * Constant ID for the save button of this dialog.
     */
    public static final int SAVE_ID = 2;

    /**
     * Constant ID for the load button of this dialog.
     */
    public static final int LOAD_ID = 3;

    /**
     * Constant ID for the delete button of this dialog.
     */
    public static final int DELETE_ID = 4;

    /**
     * The NatTable instance to apply the save/load operations.
     */
    private NatTable natTable;

    /**
     * The Properties instance that should be used for saving and loading.
     */
    private Properties properties;

    /**
     * Viewer containing the state configurations.
     */
    private TableViewer viewer;

    /**
     * The decoration for the configNameText field. Needed for showing an error
     * when trying to invoke save with an empty name.
     */
    private ControlDecoration configNameDeco;

    /**
     * Text input field for specifying the name of a configuration. If there is
     * no input in this field when saving a state configuration the default
     * state configuration will be used.
     */
    private Text configNameText;

    /**
     * List of {@link IStateChangedListener}s that will be notified if states
     * are changed using this dialog.
     */
    private List<IStateChangedListener> stateChangeListeners = new ArrayList<IStateChangedListener>();

    /**
     * Create a new dialog for handling NatTable state.
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     * @param natTable
     *            The NatTable instance to apply the save/load operations.
     * @param properties
     *            The Properties instance that should be used for saving and
     *            loading.
     */
    public PersistenceDialog(Shell parentShell, NatTable natTable,
            Properties properties) {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);

        if (natTable == null) {
            throw new IllegalArgumentException("natTable can not be null!"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties can not be null!"); //$NON-NLS-1$
        }

        this.natTable = natTable;
        this.properties = properties;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite control = (Composite) super.createDialogArea(parent);

        Label viewerLabel = new Label(control, SWT.NONE);
        viewerLabel
                .setText(Messages.getString("PersistenceDialog.viewerLabel")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().grab(true, false).applyTo(viewerLabel);

        this.viewer = new TableViewer(control);
        this.viewer.setContentProvider(new ArrayContentProvider());
        this.viewer.setLabelProvider(new ViewConfigurationNameLabelProvider());

        // sort in alphabetical order
        this.viewer.setComparator(new ViewerComparator());

        GridDataFactory.fillDefaults().grab(true, true)
                .applyTo(this.viewer.getControl());

        // layout textbox
        Composite nameContainer = new Composite(control, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        nameContainer.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(nameContainer);
        Label label = new Label(nameContainer, SWT.NONE);
        label.setText(Messages.getString("PersistenceDialog.nameLabel")); //$NON-NLS-1$
        this.configNameText = new Text(nameContainer, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false)
                .applyTo(this.configNameText);

        this.configNameText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.keyCode == SWT.CR && event.stateMask == 0)
                        || (event.keyCode == SWT.KEYPAD_CR && event.stateMask == 0)) {
                    buttonPressed(SAVE_ID);
                }
            }
        });

        this.configNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (PersistenceDialog.this.configNameText.getText().length() != 0) {
                    PersistenceDialog.this.configNameDeco.hide();
                }
            }
        });

        this.configNameDeco = new ControlDecoration(this.configNameText,
                SWT.RIGHT);
        Image image = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                .getImage();
        this.configNameDeco.setDescriptionText(Messages
                .getString("PersistenceDialog.nameErrorText")); //$NON-NLS-1$
        this.configNameDeco.setImage(image);
        this.configNameDeco.hide();

        // add click listener on viewer
        this.viewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        ISelection selection = event.getSelection();
                        if (selection != null
                                && selection instanceof IStructuredSelection) {
                            String configName = ((IStructuredSelection) selection)
                                    .getFirstElement().toString();
                            PersistenceDialog.this.configNameText.setText(configName);
                        }
                    }
                });

        // add double click listener
        this.viewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                buttonPressed(LOAD_ID);
            }
        });

        this.viewer.add(PersistenceHelper.getAvailableStates(this.properties)
                .toArray());

        return control;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, DELETE_ID,
                Messages.getString("PersistenceDialog.buttonDelete"), false); //$NON-NLS-1$
        createButton(parent, SAVE_ID,
                Messages.getString("PersistenceDialog.buttonSave"), false); //$NON-NLS-1$
        createButton(parent, LOAD_ID,
                Messages.getString("PersistenceDialog.buttonLoad"), false); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID,
                Messages.getString("PersistenceDialog.buttonDone"), false); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == SAVE_ID) {
            String configName = this.configNameText.getText();
            if (configName == null || configName.length() == 0) {
                // it is not possible to store an empty configuration with this
                // dialog
                // this is because the configuration with an empty name is the
                // default
                // configuration
                this.configNameDeco.show();
                return;
            } else {
                this.configNameDeco.hide();
            }
            this.natTable.saveState(configName, this.properties);
            String oldActiveName = getActiveViewConfigurationName();
            setActiveViewConfigurationName(configName);
            this.viewer.refresh(oldActiveName, true);

            this.configNameText.setText(""); //$NON-NLS-1$

            for (int i = 0; i < this.viewer.getTable().getItemCount(); i++) {
                String element = this.viewer.getElementAt(i).toString();
                if (configName.equals(element)) {
                    // fire event for a changed view configuration
                    fireStateChange(new StateChangeEvent(configName,
                            StateChangeType.CHANGE));
                    return;
                }
            }

            this.viewer.add(configName);
            // fire event for a newly created view configuration
            fireStateChange(new StateChangeEvent(configName,
                    StateChangeType.CREATE));
        } else if (buttonId == DELETE_ID) {
            ISelection selection = this.viewer.getSelection();
            if (selection != null && selection instanceof IStructuredSelection) {
                String configName = ((IStructuredSelection) selection)
                        .getFirstElement().toString();
                PersistenceHelper.deleteState(configName, this.properties);
                // remove the state name out of the viewer
                this.viewer.getTable().deselectAll();
                this.viewer.remove(configName);
                this.configNameText.setText(""); //$NON-NLS-1$
                // fire event for a deleted view configuration
                fireStateChange(new StateChangeEvent(configName,
                        StateChangeType.DELETE));
            }
        } else if (buttonId == LOAD_ID) {
            ISelection selection = this.viewer.getSelection();
            if (selection != null && selection instanceof IStructuredSelection) {
                String configName = ((IStructuredSelection) selection)
                        .getFirstElement().toString();
                this.natTable.loadState(configName, this.properties);
                setActiveViewConfigurationName(configName);
            }
            super.okPressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
     * .Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("PersistenceDialog.title")); //$NON-NLS-1$
        newShell.setImage(GUIHelper.getImage("table_icon")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        return new Point(500, 300);
    };

    /**
     * @return The Properties instance that is used for saving and loading.
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * @param properties
     *            The Properties instance that should be used for saving and
     *            loading.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @return The name of the current active view configuration
     */
    public String getActiveViewConfigurationName() {
        return this.properties.getProperty(ACTIVE_VIEW_CONFIGURATION_KEY);
    }

    /**
     * Sets the name of the current active view configuration. Note that this
     * method does not set the active view configuration programmatically. It is
     * just used to support highlighting the current active view configuration
     * in the viewer of this dialog.
     *
     * @param name
     *            The name of the current active view configuration
     */
    public void setActiveViewConfigurationName(String name) {
        this.properties.setProperty(ACTIVE_VIEW_CONFIGURATION_KEY, name);
    }

    /**
     * Add the given {@link IStateChangedListener} to the local list of
     * listeners.
     *
     * @param listener
     *            The listener to add.
     */
    public void addStateChangeListener(IStateChangedListener listener) {
        this.stateChangeListeners.add(listener);
    }

    /**
     * Adds the given {@link IStateChangedListener}s to the local list of
     * listeners.
     *
     * @param listeners
     *            The listeners to add.
     */
    public void addAllStateChangeListener(List<IStateChangedListener> listeners) {
        this.stateChangeListeners.addAll(listeners);
    }

    /**
     * Removes the given {@link IStateChangedListener} from the local list of
     * listeners.
     *
     * @param listener
     *            The listener to remove.
     */
    public void removeStateChangeListener(IStateChangedListener listener) {
        this.stateChangeListeners.remove(listener);
    }

    /**
     * Removes the given {@link IStateChangedListener}s from the local list of
     * listeners.
     *
     * @param listeners
     *            The listeners to remove.
     */
    public void removeAllStateChangeListener(
            List<IStateChangedListener> listeners) {
        this.stateChangeListeners.removeAll(listeners);
    }

    /**
     * Inform all registered listeners about the state change.
     *
     * @param event
     *            The {@link StateChangeEvent} object.
     */
    public void fireStateChange(StateChangeEvent event) {
        for (IStateChangedListener listener : this.stateChangeListeners) {
            listener.handleStateChange(event);
        }
    }

    /**
     * Special StyledCellLabelProvider that will render the default view
     * configuration italic, so a user will know that there is something special
     * about it. Will also add a leading '*' to the current active view
     * configuration.
     */
    class ViewConfigurationNameLabelProvider extends StyledCellLabelProvider {
        private Font italicFont;
        private Styler italicStyler;

        ViewConfigurationNameLabelProvider() {
            this.italicFont = GUIHelper.getFont(new FontData[] { new FontData("Arial", 8, SWT.ITALIC) }); //$NON-NLS-1$
            this.italicStyler = new Styler() {
                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.font = ViewConfigurationNameLabelProvider.this.italicFont;
                }
            };
        }

        @Override
        public void update(ViewerCell cell) {
            Object element = cell.getElement();
            String result = element == null ? "" : element.toString();//$NON-NLS-1$
            String prefix = ""; //$NON-NLS-1$
            if (result.equals(getActiveViewConfigurationName())) {
                prefix = "* "; //$NON-NLS-1$
            }
            Styler styler = null;
            if (result.length() == 0) {
                result = Messages
                        .getString("PersistenceDialog.defaultStateConfigName"); //$NON-NLS-1$
                styler = this.italicStyler;
            }

            StyledString styledString = new StyledString(prefix + result,
                    styler);
            cell.setText(styledString.toString());
            cell.setStyleRanges(styledString.getStyleRanges());

            super.update(cell);
        }
    }
}
