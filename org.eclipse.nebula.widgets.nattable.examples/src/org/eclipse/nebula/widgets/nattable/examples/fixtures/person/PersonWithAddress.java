package org.eclipse.nebula.widgets.nattable.examples.fixtures.person;

import java.util.Date;

public class PersonWithAddress extends Person {

	private Address address;
	
	public PersonWithAddress(String firstName, String lastName, Gender gender,
			boolean married, Date birthday, Address address) {
		super(firstName, lastName, gender, married, birthday);
		this.address = address;
	}

	public PersonWithAddress(Person person, Address address) {
		super(person.getFirstName(), person.getLastName(), person.getGender(), 
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
