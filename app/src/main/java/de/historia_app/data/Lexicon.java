package de.historia_app.data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

public class Lexicon {

    private static final Logger LOGGER = Logger.getLogger(Lexicon.class.getName());

    private final TreeMap<Character, List<LexiconEntry>> lettersToEntries;

    private static final Character DEFAULT_CHAR = '-';

    public Lexicon() {
        lettersToEntries = new TreeMap<>();
    }

    public void addEntry(LexiconEntry entry) {
        if (entry != null && entry.getTitle() != null) {
            Character letter = getStartLetterUppercase(entry.getTitle());

            List<LexiconEntry> entries = lettersToEntries.get(letter);
            if (entries == null) {
                entries = new ArrayList<>();
            }
            entries.add(entry);

            lettersToEntries.put(letter, entries);
        } else {
            LOGGER.warning("Not adding invalid lexicon entry.");
        }
    }

    public TreeSet<Character> getLetters() {
        return new TreeSet<>(lettersToEntries.keySet());
    }

    public List<LexiconEntry> getEntriesForLetter(Character letter) {
        return lettersToEntries.get(letter);
    }

    private Character getStartLetterUppercase(String s) {
        if (s != null && s.length() > 0) {
            return s.toUpperCase().charAt(0);
        } else {
            LOGGER.warning("Cannot determine start letter for string: " + s);
            return DEFAULT_CHAR;
        }
    }

    public boolean hasEntries() {
        return (lettersToEntries.size() > 0);
    }

}
