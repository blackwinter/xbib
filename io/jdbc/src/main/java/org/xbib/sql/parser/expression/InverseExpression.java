package org.xbib.sql.parser.expression;

public class InverseExpression implements Expression {
	private Expression expression;
	
	public InverseExpression() {
	}

	public InverseExpression(Expression expression) {
		setExpression(expression);
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

}
