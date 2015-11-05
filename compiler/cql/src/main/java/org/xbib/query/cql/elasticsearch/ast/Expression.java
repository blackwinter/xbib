package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.cql.elasticsearch.Visitor;

/**
 * Elasticsearch expression
 */
public class Expression implements Node {

    private Operator op;

    private Node[] args;

    private TokenType type;

    private boolean visible;

    /**
     * Constructor for folding nodes.
     *
     * @param expr the expression
     * @param arg  the new argument
     */
    public Expression(Expression expr, Node arg) {
        this.type = TokenType.EXPRESSION;
        this.op = expr.getOperator();
        if (arg instanceof Expression) {
            Expression expr2 = (Expression) arg;
            this.args = new Node[expr.getArgs().length + expr2.getArgs().length];
            System.arraycopy(expr.getArgs(), 0, this.args, 0, expr.getArgs().length);
            System.arraycopy(expr2.getArgs(), 0, this.args, expr.getArgs().length, expr2.getArgs().length);
        } else {
            Node[] exprargs = expr.getArgs();
            this.args = new Node[exprargs.length + 1];
            // to avoid copy, organization of the argument list is reverse, the most recent arg is at position 0
            this.args[0] = arg;
            System.arraycopy(exprargs, 0, this.args, 1, exprargs.length);
        }
        this.visible = false;
        for (Node node : args) {
            if (node instanceof Name || node instanceof Expression) {
                this.visible = visible || arg.isVisible();
            }
        }
    }

    public Expression(Operator op, Node... args) {
        this.op = op;
        this.type = TokenType.EXPRESSION;
        this.args = args;
        if (args != null && args.length > 0) {
            this.visible = false;
            for (Node arg : args) {
                if (arg instanceof Name || arg instanceof Expression) {
                    this.visible = visible || arg.isVisible();
                }
            }
        } else {
            this.visible = true;
        }
    }

    public Operator getOperator() {
        return op;
    }

    public Node[] getArgs() {
        return args;
    }

    public Node getArg1() {
        return args[0];
    }

    public Node getArg2() {
        return args[1];
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (!visible) {
            return "";
        }
        StringBuilder sb = new StringBuilder(op.toString());
        sb.append('(');
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) {
                sb.append(',');
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
