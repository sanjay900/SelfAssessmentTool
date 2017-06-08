package Filter;

import org.junit.Test;
import sat.compiler.java.annotations.Hidden;
import sat.compiler.java.annotations.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Using streams, complete the two methods to return a list of Contact objects which name or phone number starts
 * with the given String prefix. The implementation of the Contact class is given for you.
 */
@Task(name="B4. Filtering an address book")
public abstract class Streams5 {

    @Hidden
    private final Contact[] contacts = new Contact[] {
            new Contact("Bob Bobbs", "0211234567"),
            new Contact("John Smith", "0274439810"),
            new Contact("John Doe", "0211112222"),
            new Contact("James Jameson", "0277220691"),
            new Contact("Captain Morgan", "0219991010")
    };

    private class Contact {
        private final String name;
        private final String phoneNumber;

        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return this.name;
        }

        public String getPhoneNumber() {
            return this.phoneNumber;
        }

        @Hidden
        @Override
        public boolean equals(Object other) {
            if (other instanceof Contact) {
                Contact c = (Contact)other;
                return c.name.equals(this.name) && c.phoneNumber.equals(this.phoneNumber);
            }
            return false;
        }
    }

    @Test
    @Hidden(showFunctionSignature=false)
    public void testNames() {
        List<Contact> returned = filterByName(Arrays.asList(contacts), "John");
        assertEquals(2, returned.size());
        assertTrue(returned.contains(contacts[1]));
        assertTrue(returned.contains(contacts[2]));
    }

    @Test
    @Hidden(showFunctionSignature=false)
    public void testPhoneNumbers() {
        List<Contact> returned = filterByPhoneNumber(Arrays.asList(contacts), "021");
        assertEquals(3, returned.size());
        assertTrue(returned.contains(contacts[0]));
        assertTrue(returned.contains(contacts[2]));
        assertTrue(returned.contains(contacts[5]));
    }

    @Test
    @Hidden(showFunctionSignature=false)
    public void testEmptyList() {
        List<Contact> returnedName = filterByName(new ArrayList<Contact>(), "James");
        List<Contact> returnedNumber = filterByPhoneNumber(new ArrayList<Contact>(), "027");
        assertTrue(returnedName.isEmpty());
        assertTrue(returnedNumber.isEmpty());
    }

    public abstract List<Contact> filterByName(List<Contact> original, String prefix);
    public abstract List<Contact> filterByPhoneNumber(List<Contact> original, String prefix);
}