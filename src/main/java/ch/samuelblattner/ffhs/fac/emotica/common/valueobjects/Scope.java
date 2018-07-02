package ch.samuelblattner.ffhs.fac.emotica.common.valueobjects;

import ch.samuelblattner.ffhs.fac.emotica.common.instructions.AbstractInstruction;

import java.util.HashMap;

public class Scope {

    private Scope outerScope;
    private HashMap<String, AbstractInstruction> innerScope;
    private HashMap<String, Integer> scopeIndex;
    private int index = 0;
    private int loopId;

    public Scope(Scope outerScope) {
        this.loopId = 0;
        this.innerScope = new HashMap<>();
        this.scopeIndex = new HashMap<>();
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

    public int getIndexForVariable(String varName) {
        if (scopeIndex.containsKey(varName)) {
            return scopeIndex.get(varName);
        }
        return -1;
    }

    public AbstractInstruction setVariable(String varName, AbstractInstruction value) {
        AbstractInstruction var = getVariable(varName);
        if (var == null) {
            innerScope.put(varName, value);
//            if (outerScope != null) {
                scopeIndex.put(varName, index);
                index += 1;
//            }
            var = value;
        } else {
            var = value;
        }
        return var;
    }

    public int pushLoop() {
        return loopId++;
    }

    public void popLoop() {
        loopId--;
    }
}
