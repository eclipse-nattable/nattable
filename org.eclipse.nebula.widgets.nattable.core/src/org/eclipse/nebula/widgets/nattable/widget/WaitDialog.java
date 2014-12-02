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

import static org.eclipse.swt.layout.GridData.CENTER;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class WaitDialog extends Dialog {

    private String msg;
    private Image iconImage;
    private Label textLabel;

    public WaitDialog(Shell parent, int shellStyle, String msg, Image iconImg) {
        super(parent);
        this.msg = msg;
        this.iconImage = iconImg;
        setShellStyle(shellStyle | SWT.APPLICATION_MODAL);
    }

    private void centerDialogOnScreen(Shell shell) {
        shell.setSize(250, 75);
        Rectangle parentSize = getParentShell().getBounds();
        Rectangle mySize = shell.getBounds();
        int locationX, locationY;
        locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
        locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;
        shell.setLocation(locationX, locationY);
    }

    @Override
    protected Control createContents(Composite parent) {
        centerDialogOnScreen(getShell());

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(CENTER, CENTER, true, true));
        composite.setRedraw(true);

        Label imgLabel = new Label(composite, SWT.NONE);
        imgLabel.setImage(this.iconImage);

        this.textLabel = new Label(composite, SWT.NONE);
        this.textLabel.setLayoutData(new GridData(CENTER, CENTER, true, true));
        this.textLabel
                .setFont(GUIHelper.getFont(new FontData("Arial", 9, SWT.BOLD))); //$NON-NLS-1$
        this.textLabel.setRedraw(true);
        this.textLabel.setText(this.msg);

        return composite;
    }

    public void setMsg(String msg) {
        this.msg = msg;
        this.textLabel.setText(msg);
        getShell().layout(new Control[] { this.textLabel });
    }

    @Override
    public boolean close() {
        if (ObjectUtils.isNotNull(this.iconImage)) {
            this.iconImage.dispose();
        }
        return super.close();
    }
}
