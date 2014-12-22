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
package org.eclipse.nebula.widgets.nattable.test.data;

import java.util.Date;

public class Person {
    public enum Gender {
        MALE, FEMALE
    }

    private final int id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private boolean married;
    private Date birthday;
    private Double money;

    public Person(int id) {
        this.id = id;
    }

    public Person(int id, String firstName, String lastName, Gender gender,
            boolean married, Date birthday, Double money) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.married = married;
        this.birthday = birthday;
        this.money = money;
    }

    /**
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the gender
     */
    public Gender getGender() {
        return this.gender;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * @return the married
     */
    public boolean isMarried() {
        return this.married;
    }

    /**
     * @param married
     *            the married to set
     */
    public void setMarried(boolean married) {
        this.married = married;
    }

    /**
     * @return the birthday
     */
    public Date getBirthday() {
        return this.birthday;
    }

    /**
     * @param birthday
     *            the birthday to set
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * @return the money
     */
    public Double getMoney() {
        return this.money;
    }

    /**
     * @param money
     *            the money to set
     */
    public void setMoney(Double money) {
        this.money = money;
    }
}
