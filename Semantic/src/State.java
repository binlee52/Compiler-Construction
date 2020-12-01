import java.util.*;

public class State extends HashMap<Variable, Value> { 
    // Defines the set of variables and their associated values 
    // that are active during interpretation
    
    public State( ) { }
    
    public State(Variable key, Value val) {
        put(key, val);
    }
    
    public State onion(Variable key, Value val) {
        put(key, val);
        return this;
    }
    
    public State onion (State t) {
        for (Variable key : t.keySet( ))
            put(key, t.get(key));
        return this;
    }

    public void display()
    {
        int idx = 0;
        System.out.print("{ ");
        for(Variable v : this.keySet())
        {
            System.out.print("<"+v.toString() + ", " +this.get(v)+">");
            idx++;
            if (idx < this.size())
                System.out.print(", ");
        }
        System.out.println(" }");
    }

}
