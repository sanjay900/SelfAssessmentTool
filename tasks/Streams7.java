import org.junit.Test;
import sat.util.*;
import java.util.stream.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;
import static sat.util.AssertUtils.*;
import java.util.*;

/**
 * Using streams, particularly the flatMap function, return all unique languages that a Set of Developers has. There cannot
 * be any repeats as the return type for your method is a Set. The implementation of Developer is given for you.
 */
@Task(name="Streams 7: Using Flatmaps", restricted={"addAll"})
public abstract class Streams7 {

    @Hidden(shouldWriteComment = false)
    private static final Set<String> LANGUAGE_GROUP_1 = new HashSet<String>();
    @Hidden(shouldWriteComment = false)
    private static final Set<String> LANGUAGE_GROUP_2 = new HashSet<String>();

    static {
        LANGUAGE_GROUP_1.add("Java");
        LANGUAGE_GROUP_1.add("C");
        LANGUAGE_GROUP_1.add("Perl");
        LANGUAGE_GROUP_1.add("Python");

        LANGUAGE_GROUP_2.add("C#");
        LANGUAGE_GROUP_2.add("Java");
        LANGUAGE_GROUP_2.add("Python");
        LANGUAGE_GROUP_2.add("JavaScript");
    }

    private class Developer {
        private final String name;
        private final Set<String> languages;

        public Developer(String name, Set<String> languages) {
            this.name = name;
            this.languages = languages;
        }

        public String getName() {
            return this.name;
        }

        public Set<String> getLanguages() {
            return this.languages;
        }

        @Hidden
        public boolean equals(Object other) {
            if (other instanceof Developer) {
                Developer d = (Developer)other;
                return d.name.equals(this.name) && d.languages.equals(this.languages);
            }
            return false;
        }
    }

    @Test
    public void testFlatMap() {
        Developer bob = new Developer("Bob", LANGUAGE_GROUP_1);
        Developer jerry = new Developer("Jerry", LANGUAGE_GROUP_2);
        Set<Developer> team = new HashSet<Developer>();
        team.add(bob);
        team.add(jerry);
        Set<String> knownLanguages = getLanguagesForTeam(team);
        assertEquals(6, knownLanguages.size()); // set should contain only 6 languages
        assertTrue(knownLanguages.contains("Java"));
        assertTrue(knownLanguages.contains("Python"));
        assertTrue(knownLanguages.contains("JavaScript"));
        Developer bobTwin = new Developer("Rob", LANGUAGE_GROUP_1); // add Bob's twin - Rob.
        team.add(bobTwin);
        knownLanguages = getLanguagesForTeam(team);
        assertEquals(6, knownLanguages.size()); // size shouldn't change
        assertTrue(knownLanguages.contains("C"));
    }

    public abstract Set<String> getLanguagesForTeam(Set<Developer> team);
}