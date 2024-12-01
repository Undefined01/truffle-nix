package website.lihan.trufflenix.parser;

import java.util.HashMap;
import java.util.Map;

public class LocalScope {
    public final LocalScope parent;
    private final Map<String, Integer> variablesId = new HashMap<>();

    public LocalScope(LocalScope parent) {
        this.parent = parent;
    }

    public boolean newVariable(String name, int id) {
        return variablesId.put(name, id) == null;
    }

    public Integer getVariableId(String name) {
        Integer id = variablesId.get(name);
        if (id != null) {
            return id;
        }
        if (parent != null) {
            id = parent.getVariableId(name);
        }
        return id;
    }
}
