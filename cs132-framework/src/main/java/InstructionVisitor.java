import minijava.syntaxtree.*;
import minijava.visitor.GJDepthFirst;

import java.util.ArrayList;

import IR.SparrowParser;
import IR.token.Identifier;
import IR.token.Label;
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
        result.setTemp(content.temp_name);

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
    public InstrContainer visit(TrueLiteral n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(temp, 1));  // true = 1
        result.setTemp(temp);

        return result;
    }

    @Override
    public InstrContainer visit(FalseLiteral n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        Identifier temp = new Identifier(generateTemp());
        result.addInstr(new Move_Id_Integer(temp, 0));  // false = 0
        result.setTemp(temp, null);

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
        result.setTemp(var);
        return result;
    }

    @Override
    public InstrContainer visit(CompareExpression n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Evaluate both sides
        InstrContainer left = n.f0.accept(this, s_table);
        InstrContainer right = n.f2.accept(this, s_table);

        // Combine instructions from both sides
        result.append(left);
        result.append(right);

        // Generate a new temp for the result
        Identifier temp = new Identifier(generateTemp());

        // Add the < instruction
        result.addInstr(new LessThan(temp, left.temp_name, right.temp_name));

        // Set the result temp
        result.setTemp(temp);

        return result;
    }

    @Override
    public InstrContainer visit(IfStatement n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        // Generate labels
        Label elseLabel = new Label("L" + generateTemp() + "_Else");
        Label endLabel = new Label("L" + generateTemp() + "_End");

        // Evaluate condition
        InstrContainer cond = n.f2.accept(this, s_table);
        result.append(cond);

        // Conditional jump
        result.addInstr(new IfGoto(cond.temp_name, elseLabel)); // if0 cond goto elseLabel

        // THEN block
        InstrContainer thenBlock = n.f4.accept(this, s_table);
        result.append(thenBlock);
        result.addInstr(new Goto(endLabel)); // jump to end after then block

        // ELSE block
        result.addInstr(new LabelInstr(elseLabel));
        InstrContainer elseBlock = n.f6.accept(this, s_table);
        result.append(elseBlock);

        // End label
        result.addInstr(new LabelInstr(endLabel));

        return result;
    }

    @Override // doin this for multiple statements (like in IfStatement)
    public InstrContainer visit(minijava.syntaxtree.Block n, SymbolTable s_table) {
        InstrContainer result = new InstrContainer();

        for (Node stmtNode : n.f1.nodes) {
            InstrContainer inner = stmtNode.accept(this, s_table);
            if (inner != null) {
                result.append(inner);
            }
        }

        return result;
    }
}
