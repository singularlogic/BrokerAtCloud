/**
 * Extends Pebble engine with 'repeat' tag token parser
 *
 * Syntax:
 * {% repeat <var> from <lower-bound> to <upper-bound> %}
 *   .... your content goes here ....
 * {% endrepeat %}
 *
 * Description:
 * Iterates through the integer values from <lower-bound> to <upper-bound> (i.e. iterates "<upper-bound> - <lower-bound> + 1" times)
 * and sets iteration value to variable <var>.
 *
 * Example:
 * {% repeat ii from 1 to 100 %}
 *   .... you content goes here ....
 * {% endrepeat %}
 */

package eu.brokeratcloud.opt.engine.sim;

import com.mitchellbosecke.pebble.error.ParserException;
import com.mitchellbosecke.pebble.lexer.Token;
import com.mitchellbosecke.pebble.lexer.TokenStream;
import com.mitchellbosecke.pebble.node.BodyNode;
import com.mitchellbosecke.pebble.node.ForNode;
import com.mitchellbosecke.pebble.node.RenderableNode;
import com.mitchellbosecke.pebble.node.expression.Expression;
import com.mitchellbosecke.pebble.parser.StoppingCondition;
import com.mitchellbosecke.pebble.tokenParser.AbstractTokenParser;

public class RepeatTokenParser extends AbstractTokenParser {
	@Override
	public RenderableNode parse(Token token) throws ParserException {
		TokenStream stream = this.parser.getStream();
		int lineNumber = token.getLineNumber();
		
		// skip the 'repeat' token
		stream.next();
		
		// get the iteration variable
		String iterationVariable = this.parser.getExpressionParser().parseNewVariableName();
		
		stream.expect(Token.Type.NAME, "from");
		
		// get the lower bound
		Expression<?> lowerBound = this.parser.getExpressionParser().parseExpression();
		
		stream.expect(Token.Type.NAME, "to");
		
		// get the lower bound
		Expression<?> upperBound = this.parser.getExpressionParser().parseExpression();
		
		stream.expect(Token.Type.EXECUTE_END);
		
		BodyNode body = this.parser.subparse(decideForEnd);
		
		// skip the 'endrepeat' token
		stream.next();
		stream.expect(Token.Type.EXECUTE_END);
		
		return new RepeatNode(lineNumber, iterationVariable, lowerBound, upperBound, body);
	}
	
	private StoppingCondition decideForEnd = new StoppingCondition() {
		@Override
		public boolean evaluate(Token token) {
			return token.test(Token.Type.NAME, "endrepeat");
		}
	};
	
	@Override
	public String getTag() {
		return "repeat";
	}
}