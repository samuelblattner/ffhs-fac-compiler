package ch.samuelblattner.ffhs.fac.emotica.interpreter.valueobjects;

import ch.samuelblattner.ffhs.fac.emotica.interpreter.instructions.AbstractInstruction;

import java.util.HashMap;

public class Scope {

    private Scope outerScope;
    private HashMap<String, AbstractInstruction> innerScope;

    public Scope(Scope outerScope) {
        this.innerScope = new HashMap<>();
        this.outerScope = outerScope;
    }

    public Scope getOuterScope() {
        return this.outerScope;
    }

    public AbstractInstruction getVariable(String varName) {
        if (innerScope.containsKey(varName)) {
            return innerScope.get(varName);
        }
        if (outerScope != null) {
            return outerScope.getVariable(varName);
        }
        return null;
    }

    public AbstractInstruction setVariable(String varName, AbstractInstruction value) {
        AbstractInstruction var = getVariable(varName);
        if (var == null) {
            innerScope.put(varName, value);
            var = value;
        } else {
            var = value;
        }
        return var;
    }
}
