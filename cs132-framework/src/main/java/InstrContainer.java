import java.util.*;

import IR.token.Identifier;
import sparrow.*;

public class InstrContainer{
    public ArrayList<Instruction> instr_list;
    public Identifier temp_name; // v0, v1, etc.
    public String class_name; // the temp's type

    public InstrContainer (){
        instr_list = new ArrayList<Instruction>();
        temp_name = null;
        class_name = "";
    }

    public InstrContainer (ArrayList<Instruction> instr, Identifier name){
        instr_list = instr;
        temp_name = name;
    }


    // Adds a single instruction
    public void addInstr(Instruction instr) {
        instr_list.add(instr);
    }

    // Appends instructions from another InstrContainer
    public void append(InstrContainer other) {
        if (other != null) {
            instr_list.addAll(other.instr_list);
        }
    }

    // Sets a new temp and optionally the type
    public void setTemp(Identifier temp) {
        this.temp_name = temp;
    }
    public void setTemp(Identifier temp, String type) {
        this.temp_name = temp;
        this.class_name = type;
    }

    // Checks if this is a "void" result (like print)
    public boolean hasResult() {
        return temp_name != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InstrContainer(temp=").append(temp_name)
        .append(", class=").append(class_name)
        .append(")\nInstructions:\n");

        for (Instruction instr : instr_list) {
            sb.append("  ").append(instr.toString()).append("\n");
        }

        return sb.toString();
    }
}