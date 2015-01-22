package org.broker.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Language represents a set of sequences of various lengths.  Formally, a
 * language may consist of all sequences of uniform finite length, such as 
 * L0, L1, L2, ..., Ln, or may consist of sequences of mixed lengths, such 
 * as L*, the infinite set of all sequences of all possible lengths.  In 
 * practice, we use languages that are finite subsets of L*, and denote
 * these as Ln*, the language containing all finite sequences from L0..Ln, 
 * for some finite length n.  We also refer to Ln+, the language containing
 * all non-empty sequences up to length n.  Ln* is the union of L0, the 
 * language containing just the empty sequence, and Ln+.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class Language extends Element {
	
	/**
	 * Creates a language containing all sequences of a given length, chosen
	 * from an alphabet.  A factory method, provided for convenience to
	 * create languages such as L1, L2, L3, ... Ln for some finite n.
	 * @param alphabet the alphabet.
	 * @param length the sequence length.
	 * @return the language containing all sequences of this length.
	 */
	public static Language createExactly(Alphabet alphabet, int length) {
		Language result = new Language().addSequence(new Sequence());
		for (int i = length; i > 0; --i) {
			result = result.product(alphabet);
		}
		return result;
	}
	
	/**
	 * Creates a language containing all non-empty sequences up to a given
	 * length, chosen from an alphabet.  A factory method, provided for
	 * convenience to create Ln+, for some finite n.
	 * @param alphabet the alphabet.
	 * @param length the maximum sequence length.
	 * @return the language containing all non-empty sequences up to this
	 * length.
	 */
	public static Language createBoundedPlus(Alphabet alphabet, int length) {
		Language result = new Language();
		Language language = new Language().addSequence(new Sequence());
		for (int i = length; i > 0; --i) {
			language = language.product(alphabet);
			result.addLanguage(language);
		}
		return result;	
	}
	
	/**
	 * Creates a language containing all sequences up to a given length, 
	 * chosen from an alphabet.  A factory method, provided for convenience
	 * to create Ln*, for some finite n.
	 * @param alphabet the alphabet.
	 * @param length the maximum sequence length
	 * @return the language containing all sequences up to this length,
	 * including the empty sequence.
	 */
	public static Language createBoundedStar(Alphabet alphabet, int length) {
		Language result = new Language().addSequence(new Sequence());
		Language language = new Language().addSequence(new Sequence());
		for (int i = length; i > 0; --i) {
			language = language.product(alphabet);
			result.addLanguage(language);
		}
		return result;	
	}

	/**
	 * The set of sequences that constitute this language.
	 */
	private Set<Sequence> sequences;
	
	/**
	 * Creates an empty language.
	 */
	public Language() {
		sequences = new LinkedHashSet<Sequence>();
	}
	
	/**
	 * Returns the size of this language.
	 * @return the number of sequences in this language.
	 */
	public int size() {
		return sequences.size();
	}

	/**
	 * Returns the set of sequences that constitute this language.
	 * @return a set of sequences.
	 */
	public Set<Sequence> getSequences() {
		return sequences;
	}
	
	/**
	 * Adds a sequence to the set of sequences in this language.  If this
	 * language does not already contain a sequence equal to the added
	 * sequence, it includes the new sequence in its set of sequences.
	 * @param sequence the sequence to add.
	 * @return this language.
	 */
	public Language addSequence(Sequence sequence) {
		sequences.add(sequence);
		return this;
	}
	
	/**
	 * Includes the set of sequences from another language in the set of 
	 * sequences in this language.  Modifies this language, such that it
	 * also includes all of the sequences in the other language.
	 * @param language the language to include.
	 * @return this language.
	 */
	public Language addLanguage(Language language) {
		for (Sequence sequence : language.getSequences()) {
			addSequence(sequence);
		}
		return this;
	}
	
	/**
	 * Creates the product of this language and every event in an alphabet.
	 * Creates a new language, in which every sequence from this language is 
	 * extended by every event from the alphabet.  This is a pure functional
	 * operation that does not modify any of its operands.
	 * @param alphabet the alphabet.
	 * @return the created product language.
	 */
	public Language product(Alphabet alphabet) {
		Language result = new Language();
		for (Sequence prefix : getSequences()) {
			for (Event event : alphabet.getEvents()) {
				Sequence newSequence = new Sequence(prefix).addEvent(event);
				newSequence.setPath(prefix.getPath() + 1);
				result.addSequence(newSequence);
			}
		}
		return result;
	}

	/**
	 * Creates the product of this language and every sequence in another 
	 * language.  Creates a new language, in which every sequence from this
	 * language is extended by every sequence in the other language.  This is
	 * a pure functional operation that does not modify any of its operands.
	 * @param other the other language.
	 * @return the created product language.
	 */
	public Language product(Language other) {
		Language result = new Language();
		for (Sequence prefix : getSequences()) {
			for (Sequence extra : other.getSequences()) {
				Sequence newSequence = new Sequence(prefix).addSequence(extra);
				newSequence.setPath(prefix.getPath() + extra.getPath());
				result.addSequence(newSequence);
			}
		}
		return result;
	}

}
