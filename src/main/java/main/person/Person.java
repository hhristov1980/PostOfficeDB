package main.person;

import main.postOffice.PostOffice;

import java.util.Objects;

public abstract class Person extends Thread{
    private static int uniqueId = 1;
    protected int personId;
    private String firstName;
    private String lastName;
    protected PostOffice postOffice;

    public Person(String firstName, String lastName, PostOffice postOffice){
        if(firstName.length()>0){
            this.firstName = firstName;
        }
        if(lastName.length()>0){
            this.lastName = lastName;
        }
        personId = uniqueId++;
        this.postOffice = postOffice;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getPersonId() {
        return personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return personId == person.personId && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName) && Objects.equals(postOffice, person.postOffice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, firstName, lastName, postOffice);
    }
}
