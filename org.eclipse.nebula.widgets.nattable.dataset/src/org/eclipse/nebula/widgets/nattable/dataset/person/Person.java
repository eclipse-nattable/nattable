/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.person;

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
    protected Double money;
    protected String description;

    public Person(int id) {
        this.id = id;
    }

    public Person(
            int id,
            String firstName,
            String lastName,
            Gender gender,
            boolean married,
            Date birthday) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.married = married;
        this.birthday = birthday;
    }

    public Person(
            int id,
            String firstName,
            String lastName,
            Gender gender,
            boolean married,
            Date birthday,
            Double money) {
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

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
