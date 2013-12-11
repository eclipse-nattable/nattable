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
package org.eclipse.nebula.widgets.nattable.examples.data.person;

import java.util.Date;


public class PersonWithAddress extends Person {

	private Address address;
	
	public PersonWithAddress(int id, String firstName, String lastName, Gender gender,
			boolean married, Date birthday, Address address) {
		super(id, firstName, lastName, gender, married, birthday);
		this.address = address;
	}

	public PersonWithAddress(Person person, Address address) {
		super(person.getId(), person.getFirstName(), person.getLastName(), person.getGender(), 
				person.isMarried(), person.getBirthday());
		this.address = address;
	}
	
	/**
	 * @param address the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

}
