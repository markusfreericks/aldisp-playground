package aldisp.ast;

import aldisp.ast.Grammar.*;

import java.util.List;

/**
 * pretty printer for AST
 */
public class Printer {

    StringBuilder sb = new StringBuilder();
    int indent = 0;

    public void addComment(String comment){
        app("\\\\ ");
        app(comment.replace("\n","\n\\\\ "));
        nl();
    }

    void enter(){
        indent += 2;
        nl();
    }

    void leave(){
        indent -= 2;
        nl();
    }

    private void nl() {
        app("\n");
        for(int i=0; i<indent;i++){
            app(" ");
        }
    }

    private void app(String text) {
        sb.append(text);
    }

    // append quoted
    private void appQ(String text) {
        sb.append('"');
        app(text);
        sb.append('"');
    }

    public void app(Aprogram x){
        addComment("--- program start ---");
        app(x.definitions());
        app("net");
        enter();
        app(x.net());
        leave();
        app("in");
        enter();
        app(x.expr());
        leave();
        addComment("--- program end ---");
    }

    public void app(Aexpr x){
        if(x instanceof Aapp a){
            app(a.func());
            app("(");
            int n=0;
            for(Aexpr e : a.args()){
                if(n > 0){
                    app(", ");
                }
                app(e);
                n++;
            }
            app(")");
        }

        if(x instanceof Aconst c){
            Object v = c.value();
            if(v == null){
                app("null");
                return;
            }
            if(v instanceof String s){
                appQ(s);
                return;
            }
            // default: print as self
            app(sb.toString());
            return;
        }

        addComment("unable to append " + x);
    }

    public void app(List<Adecl> ds){
        if(ds.isEmpty()){
            addComment("no decls");
        } else {
            for (Adecl d : ds) {
                app(d);
                nl();
            }
        }
    }

    private void app(Adecl d) {
        if(d instanceof Asimpledecl s){
            app(s.id());
            app(" = ");
            app(s.expr());
            return;
        }
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
