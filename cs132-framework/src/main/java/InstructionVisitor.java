import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

import java.util.ArrayList;

import IR.SparrowParser;
import IR.token.Identifier;
import sparrow.*;
// import sparrowv.Print;

public class InstructionVisitor extends GJDepthFirst < InstrContainer, SymbolTable > {

    // global counter for our identifier name generator
    int id_name_counter = 0;
    ArrayList<Instruction> global_instr_list = new ArrayList<Instruction> ();

    public String generateTemp(){
        String name = "v" + id_name_counter;
        id_name_counter++;
        return name;
    }

    @Override
    public InstrContainer visit(Goal n, SymbolTable s_table) {
        return n.f0.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(MainClass n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        for (Node node : n.f15.nodes){
            InstrContainer statement = node.accept(this, s_table);
            result.instr_list.addAll(statement.instr_list);
        }
        return result;
    }

    @Override
    public InstrContainer visit(Statement n, SymbolTable s_table) {
        InstrContainer result = n.f0.accept(this, s_table);
        return result;
    }

    @Override
    public InstrContainer visit(PrintStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        InstrContainer content = n.f2.accept(this, s_table);
        result.instr_list.addAll(content.instr_list);

        Print p = new Print(content.temp_name);
        result.instr_list.add(p);
        result.setTemp(content.temp_name, null);

        return result;
    }

    @Override
    public InstrContainer visit(Expression n, SymbolTable s_table) {
        return n.f0.choice.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(PrimaryExpression n, SymbolTable s_table) {
        return n.f0.choice.accept(this, s_table);
    }

    @Override
    public InstrContainer visit(IntegerLiteral n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        Identifier temp_id = new Identifier(generateTemp());
        int int_val = Integer.parseInt(n.f0.toString());        
        result.instr_list.add(new Move_Id_Integer(temp_id, int_val)); // set instr list
        result.temp_name = (temp_id); // set temp name
        return result;
    }

    @Override
    public InstrContainer visit(PlusExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        InstrContainer left = n.f0.accept(this, s_table);
        InstrContainer right = n.f2.accept(this, s_table);
        
        result.instr_list=left.instr_list;
        result.instr_list.addAll(right.instr_list);
        result.temp_name = new Identifier(generateTemp());
        result.instr_list.add(new Add(result.temp_name, left.temp_name, right.temp_name));
        return result;
    }

    @Override
    public InstrContainer visit(AssignmentStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // the var name being assigned to
        String varName = n.f0.f0.toString();

        // rhs
        InstrContainer rhs = n.f2.accept(this, s_table);
        result.instr_list.addAll(rhs.instr_list);
    
        // wrap var as an Identifier
        Identifier id = new Identifier(varName);
    
        // add assignment: id = rhs.temp_name
        result.addInstr(new Move_Id_Id(id, rhs.temp_name));
    
        // we don’t need to set temp_name for statements
        return result;
    }

    @Override
    public InstrContainer visit(minijava.syntaxtree.Identifier n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();
        Identifier var = new Identifier(n.f0.toString());
        result.setTemp(var, null);
        return result;
    }
}
