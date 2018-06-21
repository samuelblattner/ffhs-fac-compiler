package ch.samuelblattner.ffhs.fac.emotica.interpreter.actors;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.valueobjects.Scope;


abstract class AbstractScopedActor implements ifInstructionVisitor {

    Scope scope;

    AbstractScopedActor() {
        scope = new Scope(null);
    }

    void createInnerScope() {
        this.scope = new Scope(this.scope);
    }

    void destroyCurrentScope() {
        this.scope = this.scope.getOuterScope();
    }
}
