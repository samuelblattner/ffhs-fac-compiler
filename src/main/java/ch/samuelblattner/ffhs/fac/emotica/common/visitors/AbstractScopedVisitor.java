package ch.samuelblattner.ffhs.fac.emotica.common.visitors;

import ch.samuelblattner.ffhs.fac.emotica.common.interfaces.ifInstructionVisitor;
import ch.samuelblattner.ffhs.fac.emotica.common.valueobjects.Scope;


public abstract class AbstractScopedVisitor implements ifInstructionVisitor {

    protected Scope scope;

    public AbstractScopedVisitor() {
        scope = new Scope(null);
    }

    protected void createInnerScope() {
        this.scope = new Scope(this.scope);
    }

    protected void destroyCurrentScope() {
        this.scope = this.scope.getOuterScope();
    }
}
