package org.broker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * TestSequence represents a sequence of test steps to be executed in order.
 * A TestSequence consists of a list of Tests to be executed in order, where
 * each TestStep is a single test ste[.  A TestSequence is the concrete expression
 * of an abstract Sequence, in that each TestStep includes the expected Input and
 * Output values.  A TestSequence records the names of the expected source and
 * target States traversed in the Machine.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class TestSequence extends Sequence implements Comparable<TestSequence> {

	/**
	 * Constructs an empty TestSequence.
	 */
	public TestSequence() {
		super();
	}
	
	/**
	 * Constructs an empty TestSequence covering the same state and path as
	 * the coverage Sequence.
	 * @param sequence a coverage Sequence.
	 */
	public TestSequence(Sequence sequence) {
		super();
		state = sequence.state;
		path = sequence.path;
	}

	/**
	 * Returns the ordered list of TestSteps.  Provided for convenience, since
	 * the inherited method returns an ordered list of Events.
	 * @return the list of TestSteps.
	 */
	public List<TestStep> getTestSteps() {
		List<TestStep> result = new ArrayList<TestStep>();
		for (Event event : events) 
			result.add((TestStep) event);
		return result;
	}
	
	/**
	 * Reports whether this TestSequence contains the other as a prefix.  The
	 * other must be an initial subsequence of this, and shorter than this.
	 * Returns false if this and the other are equal or identical.
	 * @param other the other TestSequence
	 * @return true, if the other is a prefix of this TestSequence.
	 */
	public boolean hasPrefix(Sequence other) {
		if (this == other || size() <= other.size())
			return false;
		else {
			List<Event> prefix = other.getEvents();
			for (int i = 0; i < prefix.size(); ++i)
				if (! events.get(i).equals(prefix.get(i)))
					return false;
			return true;
		}
	}
	
	/**
	 * Merges a prefix TestSequence with this TestSequence.  If the prefix is
	 * in fact a prefix of this TestSequence, merges its verified obligations
	 * with this TestSequence.  This TestSequence becomes a multi-objective
	 * test, with more than one verified TestStep.
	 * @param prefix the prefix TestSequence.
	 * @return this TestSequence after merging.
	 */
	public Sequence mergePrefix(TestSequence prefix) {
		if (hasPrefix(prefix)) {
			List<TestStep> prefixTests = prefix.getTestSteps();
			List<TestStep> mainTests = getTestSteps();
			for (int index = 0; index < prefix.size(); ++index) {
				if (prefixTests.get(index).isVerify())
					mainTests.get(index).setVerify(true);
			}
		}
		else
			semanticError("cannot merge a non-prefix sequence.");
		return this;
	}
	
	/**
	 * Verify the outcome of this TestSequence.  Sets the checked property of
	 * the final TestStep in this TestSequence to true.  Does nothing if this
	 * TestSequence is empty.
	 * @return this TestSequence.
	 */
	public Sequence doVerify() {
		if (! isEmpty()) {
			TestStep last = (TestStep) getLastEvent();
			last.setVerify(true);
		}
		return this;
	}

	/**
	 * Returns the name of the expected source State.  Returns the name of 
	 * the State reached by the first initialising TestStep.
	 * @return the name of the source State.
	 */
	public String getSource() {
		if (! isEmpty()) {
			TestStep first = (TestStep) getFirstEvent();
			return first.getState();
		}
		else
			return null;
	}

	/**
	 * Returns the name of the expected target State.  Returns the name of
	 * the State reached by the last TestStep in this TestSequence.
	 * @return the name of the target State.
	 */
	public String getTarget() {
		if (! isEmpty()) {
			TestStep last = (TestStep) getLastEvent();
			return last.getState();
		}
		else
			return null;
	}
	
	/**
	 * Counts the number of verified TestSteps in this TestSequence.
	 * @return the number of verified TestSteps.
	 */
	public int countVerify() {
		int count = 0;
		for (TestStep testStep : getTestSteps()) {
			if (testStep.isVerify())
				++count;
		}
		return count;
	}

	/**
	 * Compares one TestSequence with another, as part of a sorting algorithm.
	 * By default, a shorter TestSequence precedes every longer TestSequence,
	 * of which it could be a prefix.  However, when sorting multi-objective
	 * tests, a TestSequence with multiple verified steps should precede a
	 * TestSequence with fewer verified steps, since the sequence with more
	 * verified steps contains its own prefix tests.  This rule takes priority
	 * over the default rule, which applies when two TestSequences have the
	 * same number of verified steps.
	 * @param other the other TestSequence.
	 * @return a negative, zero or positive value, denoting respectively that
	 * this TestSequence precedes, is equal in rank to, or follows the other.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TestSequence other) {
		int myCount = countVerify();
		int otherCount = other.countVerify();
		if (myCount == otherCount)
			return size() - other.size();
		else
			return otherCount - myCount;
	}
	
	/**
	 * Receive a Grounding generator for grounding this TestSequence.  The
	 * actual generator is any class that implements the Grounding interface.
	 * Asks the grounding reciprocally to ground this TestSequence.
	 * @param grounding a Grounding generator.
	 */
	public void receive(Grounding grounding) {
		grounding.groundTestSequence(this);
	}



}
