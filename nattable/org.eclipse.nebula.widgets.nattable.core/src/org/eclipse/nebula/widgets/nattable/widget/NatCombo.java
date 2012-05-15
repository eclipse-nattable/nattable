/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.widget;

import java.util.Arrays;


import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NatCombo extends Composite {

	public static final int DEFAULT_NUM_OF_VISIBLE_ITEMS = 5;

	private final IStyle cellStyle;

	private int maxVisibleItems = 10;

	private String[] items;

	private Text text;

	private Shell dropdownShell;

	private List dropdownList;

	private Image iconImage;

	private EditModeEnum editMode;
	private boolean initialResize = false;

	public NatCombo(Composite parent, IStyle cellStyle) {
		this(parent, cellStyle, DEFAULT_NUM_OF_VISIBLE_ITEMS);
	}

	public NatCombo(Composite parent, IStyle cellStyle, int maxVisibleItems) {
		super(parent, SWT.NONE);

		this.cellStyle = cellStyle;

		this.maxVisibleItems = maxVisibleItems;

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent event) {
				if (editMode == EditModeEnum.MULTI && !initialResize) {
					initialResize = true; 	 
                    return;
				}
				resizeDropdownControl();
			}

		});

		addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				dropdownShell.dispose();
				text.dispose();
			}

		});

		createTextControl();
		createDropdownControl();
	}
	
	public void setEditMode(EditModeEnum editMode) {
		this.editMode = editMode;
	}

	public void setItems(String[] items) {
		if (items != null) {
			this.items = items;
			if (!dropdownList.isDisposed() && items != null && items.length > 0) {
				dropdownList.setItems(items);
			}
			resizeDropdownControl();
		}
	}

	public void setSelection(String[] items) {
		if(items != null){
			if (!dropdownList.isDisposed()) {
				dropdownList.setSelection(items);
			}
		}
		if(items[0] != null){
			text.setText(items[0]);
		}
	}

	public int getSelectionIndex() {
		if (!dropdownList.isDisposed()) {
			return dropdownList.getSelectionIndex();
		} else {
			return Arrays.asList(items).indexOf(text.getText());
		}
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
		dropdownList.addKeyListener(listener);
	}

	@Override
	public void addTraverseListener(TraverseListener listener) {
		text.addTraverseListener(listener);
		dropdownList.addTraverseListener(listener);
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		text.addMouseListener(listener);
		dropdownList.addMouseListener(listener);
	}

	@Override
	public void notifyListeners(int eventType, Event event) {
		dropdownList.notifyListeners(eventType, event);
	}

	private void createTextControl() {
		text = new Text(this, HorizontalAlignmentEnum.getSWTStyle(cellStyle));
		text.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		text.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		text.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		text.setLayoutData(gridData);
		text.forceFocus();

		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ARROW_DOWN || event.keyCode == SWT.ARROW_UP) {
					showDropdownControl();

					int selectionIndex = dropdownList.getSelectionIndex();
					selectionIndex += event.keyCode == SWT.ARROW_DOWN ? 1 : -1;
					if (selectionIndex < 0) {
						selectionIndex = 0;
					}

					dropdownList.select(selectionIndex);
					text.setText(dropdownList.getSelection()[0]);
				}
			}

		});

		iconImage = GUIHelper.getImage("down_2"); //$NON-NLS-1$
		final Canvas iconCanvas = new Canvas(this, SWT.NONE) {

			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Rectangle iconImageBounds = iconImage.getBounds();
				return new Point(iconImageBounds.width + 2, iconImageBounds.height + 2);
			}

		};

		gridData = new GridData(GridData.BEGINNING, SWT.FILL, false, true);
		iconCanvas.setLayoutData(gridData);

		iconCanvas.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent event) {
				GC gc = event.gc;

				Rectangle iconCanvasBounds = iconCanvas.getBounds();
				Rectangle iconImageBounds = iconImage.getBounds();
				int horizontalAlignmentPadding = CellStyleUtil.getHorizontalAlignmentPadding(HorizontalAlignmentEnum.CENTER, iconCanvasBounds, iconImageBounds.width);
				int verticalAlignmentPadding = CellStyleUtil.getVerticalAlignmentPadding(VerticalAlignmentEnum.MIDDLE, iconCanvasBounds, iconImageBounds.height);
				gc.drawImage(iconImage, horizontalAlignmentPadding, verticalAlignmentPadding);

				Color originalFg = gc.getForeground();
				gc.setForeground(GUIHelper.COLOR_WIDGET_BORDER);
				gc.drawRectangle(0, 0, iconCanvasBounds.width - 1, iconCanvasBounds.height - 1);
				gc.setForeground(originalFg);
			}

		});

		iconCanvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				showDropdownControl();
			}

		});
	}

	private void showDropdownControl() {
		if (dropdownShell.isDisposed()) {
			createDropdownControl();
		}
		resizeDropdownControl();
	}

	public void hideDropdownControl() { 	 
        if (!dropdownShell.isDisposed()) {
            dropdownShell.setVisible(false);
        } 	 
	}
	
	private void createDropdownControl() {
		dropdownShell = new Shell(getShell(), SWT.MODELESS);
		dropdownShell.setLayout(new FillLayout());

		dropdownList = new List(dropdownShell, SWT.V_SCROLL | HorizontalAlignmentEnum.getSWTStyle(cellStyle));
		dropdownList.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		dropdownList.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		dropdownList.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));

		dropdownList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (dropdownList.getSelectionCount() > 0) {
					text.setText(dropdownList.getSelection()[0]);
				}
			}

		});

		setItems(items);
		dropdownList.setSelection(new String[] { text.getText() });
	}

	private void resizeDropdownControl() {
		if (dropdownShell != null && !dropdownShell.isDisposed() && !dropdownShell.isVisible()) {
			Point size = getSize();
			int itemCount = dropdownList.getItemCount();
			if (itemCount > 0 && size.x > 0 && size.y > 0) {
				int listHeight = Math.min(itemCount, maxVisibleItems) * dropdownList.getItemHeight();
				int listWidth = dropdownList.computeSize(SWT.DEFAULT, listHeight).x;
				if (listWidth < size.x) {
					listWidth = size.x;
				}
				dropdownList.setSize(listWidth, listHeight);
				
				//the following code to calculate the coordinates to display the combo is copied from 
				//org.eclipse.swt.custom.CCombo 
				Display display = getDisplay ();
				Rectangle listRect = dropdownList.getBounds ();
				Rectangle parentRect = display.map (getParent (), null, getBounds ());
				Point comboSize = getSize ();
				Rectangle displayRect = getMonitor ().getClientArea ();
				int width = Math.max (comboSize.x, listRect.width + 2);
				int height = listRect.height + 2;
				int x = parentRect.x;
				int y = parentRect.y + comboSize.y;
				if (y + height > displayRect.y + displayRect.height) y = parentRect.y - height;
				if (x + width > displayRect.x + displayRect.width) x = displayRect.x + displayRect.width - listRect.width;
				dropdownShell.setBounds (x, y, width, height);
				dropdownShell.open();
			}
		}
	}

	public void select(int index) {
		dropdownList.select(index);
	}
	
	public void addFocusListener(FocusListener listener) {
		dropdownList.addFocusListener(listener);
	}

	public void addShellListener(ShellListener listener) {
		dropdownShell.addShellListener(listener);
	}
}
