package aldisp.ast;

import java.util.List;

/**
 * AST of ALDiSP (just starting with a core), names from the thesis, page 26
 *
 * grammar is made an interface so all definitions are automatically public
 */
public interface Grammar {

    record Aprogram(List<Adecl> definitions, List<Adecl> net, Aexpr expr) { }

    interface Adecl {}
    record Asimpledecl(String id, List<Aheader> headers, Aexpr expr) implements Adecl {}
    record Aheader(String id, Atype type){}

    interface Aexpr {}
    record Aapp(Aexpr func, List<Aexpr> args) implements Aexpr {}
    record Acond(Aexpr cond, Aexpr trueExpr, Aexpr falseExpr)implements Aexpr {}
    record Avar(String id) implements Aexpr {}
    record Aconst(Object value) implements Aexpr {}
    record Adelay(Aexpr expr) implements Aexpr {}
    record Asuspend(Aexpr cond, Aexpr expr, Aexpr min, Aexpr max) implements Aexpr {}

    interface Atype{}
    record Aexprtype(Aexpr expr) implements Atype {}


}
