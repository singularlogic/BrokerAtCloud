/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * This file (along with RepeatTokenParser) extend Pebble engine with 'repeat' tag
 * For more information see RepeatTokenParser
 */

package eu.brokeratcloud.opt.engine.sim;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.NodeVisitor;
import com.mitchellbosecke.pebble.node.AbstractRenderableNode;
import com.mitchellbosecke.pebble.node.BodyNode;
import com.mitchellbosecke.pebble.node.expression.Expression;
import com.mitchellbosecke.pebble.node.expression.LiteralLongExpression;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl;

/**
 * Represents a "repeat" loop within the template.
 */
public class RepeatNode extends AbstractRenderableNode {
	
	private final String variableName;
	private final Expression<?> lowerBoundExpression;
	private final Expression<?> upperBoundExpression;
	private final BodyNode body;

	public RepeatNode(int lineNumber, String variableName, Expression<?> lowerBoundExpression, 
						Expression<?> upperBoundExpression, BodyNode body)
	{
		super(lineNumber);
		this.variableName = variableName;
		this.lowerBoundExpression = lowerBoundExpression;
		this.upperBoundExpression = upperBoundExpression;
		this.body = body;
	}

	@Override
	public void render(PebbleTemplateImpl self, Writer writer, EvaluationContext context) throws PebbleException, IOException {
		
		Object lowerEvaluation = lowerBoundExpression.evaluate(self, context);
		Object upperEvaluation = upperBoundExpression.evaluate(self, context);
		if (lowerEvaluation == null || upperEvaluation == null) {
			return;
		}
		
		Long lowerBound = toLong(lowerEvaluation);
		Long upperBound = toLong(upperEvaluation);
		if (lowerBound == null || upperBound == null) {
			return;
		}
		
		long lower = lowerBound.longValue();
		long upper = upperBound.longValue();
		
		boolean newScope = false;
		long length = upper-lower+1;
		
		if (length > 0) {
			/*
			 * Only if there is a variable name conflict between one of the
			 * variables added by the for loop construct and an existing
			 * variable do we push another scope, otherwise we reuse the current
			 * scope for performance purposes.
			 */
			if (context.currentScopeContainsVariable("loop") || context.currentScopeContainsVariable(variableName)) {
				context.pushScope();
				newScope = true;
			}
			
			int index = 0;
			
			for (long i = lower; i <= upper; i++) {
				/*
				 * Must create a new map with every iteration instead of
				 * re-using the same one just in case there is a "parallel" tag
				 * within this repeat loop; it's imperative that each thread would
				 * get it's own distinct copy of the context.
				 */
				Map<String, Object> loop = new HashMap<String, Object>();
				loop.put("index", index++);
				loop.put("length", length);
				context.put("loop", loop);
				
				context.put(variableName, new Long(i));
				body.render(self, writer, context);
			}
			
			if(newScope){
				context.popScope();
			}
		}
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	public String getIterationVariable() {
		return variableName;
	}

	public Expression<?> getLowerBoundExpression() {
		return lowerBoundExpression;
	}
	
	public Expression<?> getUpperBoundExpression() {
		return upperBoundExpression;
	}
	
	public BodyNode getBody() {
		return body;
	}
	
	@SuppressWarnings("unchecked")
	private Long toLong(final Object obj) {
		Long result = null;
		
		if (obj instanceof Long) {
			result = (Long) obj;
		} else
		if (obj instanceof Integer) {
			result = new Long( ((Integer)obj).intValue() );
		}

		return result;
	}
}
