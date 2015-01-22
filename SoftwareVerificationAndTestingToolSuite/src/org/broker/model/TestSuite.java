package org.broker.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * TestSuite represents a set of generated high-level test testSequences.  A
 * TestSuite is the more concrete counterpart of a Language, which contains
 * testSequences of events.  A TestSuite contains testSequences of tests, populated
 * with the inputs, outputs, conditions and effects.
 * 
 * @author Anthony J H Simons
 * @version Broker@Cloud 0.1
 */
public class TestSuite extends Annotated {
	
	/**
	 * The system-under-test, which may be used as a name, or as the type of the
	 * object returned when creating an instance of the system.
	 */
	private String system;

	/**
	 * The maximum transition path length to be explored from each covered
	 * state.  All paths from zero up to the maximum test depth will be
	 * explored and verified.
	 */
	private int testDepth;  // 0 by default
	
	/**
	 * True if this TestSuite is compressed and contains multi-objective 
	 * tests with intermediate verification; false if this TestSuite contains
	 * single-objective tests, each with a final verified step.
	 */
	private boolean multiObjective;  // false by default
	
	/**
	 * The set of TestSequences that constitute this TestSuite.  These are
	 * presented in the order in which they should be executed, to ensure
	 * that prior properties are verified before the same steps are used as
	 * part of the set-up for longer sequences.
	 */
	private Set<TestSequence> testSequences;
	
	/**
	 * Creates an empty TestSuite.
	 */
	public TestSuite() {
		testSequences = new LinkedHashSet<TestSequence>();
	}
	
	/**
	 * Creates an empty TestSuite for the given system and test depth.
	 */
	public TestSuite(String system, int depth) {
		this.system = system;
		this.testDepth = depth;
		testSequences = new LinkedHashSet<TestSequence>();
	}
	
	/**
	 * Returns the system-under-test.  This is the name taken from a Service
	 * specification, understood to be the type name of the Service.
	 * @return the type name of the system-under-test.
	 */
	public String getSystem() {
		return system;
	}
	
	/**
	 * Returns the system-under-test.  This is the name taken from a Service
	 * specification, understood to be the type name of the Service.
	 * @param system the type name of the system-under-test.
	 */
	public TestSuite setSystem(String system) {
		this.system = system;
		return this;
	}
	
	/**
	 * Returns the maximum test depth. Depth zero indicates that the state
	 * cover is tested, and higher values indicate that transition paths up
	 * to that length are tested from every covered state.
	 * @return the maximum test depth.
	 */
	public int getTestDepth() {
		return testDepth;
	}
	
	/**
	 * Sets the maximum test depth.  Depth zero indicates that the state
	 * cover is tested, and higher values indicate that transition paths up
	 * to that length are tested from every covered state.
	 * @return this TestSuite.
	 */
	public TestSuite setTestDepth(int depth) {
		testDepth = depth;
		return this;
	}
	
	/**
	 * Reports whether this TestSuite contains multi-objective tests.  If 
	 * true, indicates that some of the contained TestSequences satisfy more
	 * than one test objective (test more than one path).
	 * @return true, if tests are multi-objective, otherwise false.
	 */
	public boolean isMultiObjective() {
		return multiObjective;
	}
	
	/**
	 * Returns whether this TestSuite contains multi-objective tests.  If
	 * true, returns a Boolean object, otherwise returns null.
	 * @return true or null.
	 */
	public Boolean getMultiObjective() {
		return multiObjective ? true : null;
	}
	
	/**
	 * Sets whether this TestSuite contains multi-objective tests.  If true,
	 * indicates that some of the contained TestSequences satisfy more than
	 * one test objective (test more than one path).
	 * @param value the value to set.
	 * @return this TestSuite.
	 */
	public TestSuite setMultiObjective(boolean value) {
		multiObjective = value;
		return this;
	}
	
	/**
	 * Returns the size of this TestSuite.  Counts the number of TestSequences
	 * in this TestSuite.
	 * @return the number of TestSequences in this TestSuite.
	 */
	public int size() {
		return testSequences.size();
	}

	/**
	 * Returns the set of TestSequences that constitute this TestSuite.  These
	 * are in the order in which the tests should be executed, which ensures
	 * that earlier properties are verified before they are used as part of
	 * the set-up in longer TestSequences.
	 * @return a set of TestSequences.
	 */
	public Set<TestSequence> getTestSequences() {
		return testSequences;
	}
	
	/**
	 * Adds a sequence to the set of TestSequences in this TestSuite.  If this
	 * TestSuite does not already contain a TestSequence equal to the added
	 * TestSequence, it includes the new sequence in its set of TestSequences.
	 * @param sequence the TestSequence to add.
	 * @return this TestSuite.
	 */
	public TestSuite addTestSequence(TestSequence sequence) {
		testSequences.add(sequence);
		return this;
	}
	
	/**
	 * Compress this TestSuite, returning a more compact TestSuite in which
	 * single TestSequences may verify multiple test objectives.  Without any
	 * compression, each TestSequence consists of a number of TestSteps that
	 * are part of the set-up, followed by a verified TestStep which is the
	 * test objective.  Each TestSequence is by default a single-objective
	 * test.  With compression, all TestSequences that are prefixes of longer
	 * TestSequences are merged with the longer sequences, resulting in some
	 * TestSequences that verify multiple test objectives.  The resulting
	 * smaller TestSuite is re-ordered, so that the properties of shorter
	 * paths are verified before the longer paths of which they are the
	 * prefix.
	 * @return a compact TestSuite.
	 */
	public TestSuite compress() {
		List<TestSequence> list = new ArrayList<TestSequence>();
		for (TestSequence prefix : getTestSequences()) {
			boolean isPrefix = false;
			for (TestSequence sequence : getTestSequences()) {
				if (sequence.hasPrefix(prefix)) {
					isPrefix = true;
					sequence.mergePrefix(prefix);
					break;
				}
			}
			if (! isPrefix)
				list.add(prefix);
		}
		Collections.sort(list);
		multiObjective = true;
		testSequences = new LinkedHashSet<TestSequence>(list);
		return this;
	}
	
	/**
	 * Receive a Grounding generator for grounding this TestSuite.  The actual
	 * generator is any class that implements the Grounding interface.  Asks
	 * the grounding reciprocally to ground this TestSuite.
	 * @param grounding a Grounding generator.
	 * @throws IOException if the file storing the grounded output cannot be
	 * created due to an I/O fault.
	 */
	public void receive(Grounding grounding) throws IOException {
		grounding.groundTestSuite(this);
	}

}
