import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

public class TaoParser
{
    public Tao parse(String str) throws Exception {
        return tao(new Input(str));
    }
    Tao tao(Input input) throws Exception {
        var tao = new Tao();
        while (true) {
            if (input.atBound()) return tao;
            var part = tree(input);
            if (part.tag == "other") {
                part = op(input);
                if (part.tag == "other") {
                    part = note(input);
                }
            }
            tao.push(part);
        }
    }
    Tagged tree(Input input) throws Exception {
        if (input.at('[')) {
            input.next();
            input.bound(']');
            var tree = tao(input);
            input.unbound();
            input.next();
            return new Tree(tree);
        }
        return new Other();
    }
    Tagged op(Input input) throws Exception {
        if (input.at('`')) {
            input.next();
            if (input.done()) input.error("op");
            return new Op(input.next());
        }
        return new Other();
    }
    Tagged note(Input input) throws Exception {
        if (meta(input)) input.error("note");
        String note = "" + input.next();
        while (true) {
            if (meta(input) || input.done()) return new Note(note);
            note += input.next();
        }
    }
    boolean meta(Input input) {
        return input.at('[') || input.at('`') || input.at(']');
    }
    public class Tagged {
        String tag;
        public String getTag() {
            return tag;
        }
        protected Tagged(String tag) {
            this.tag = tag;
        }
    }
    public class Tao extends Tagged {
        public Tao() { super("tao"); }
        List<Tagged> parts = new ArrayList<Tagged>();
        public List<Tagged> getParts() {
            return parts;
        }
        public void push(Tagged tree) {
            parts.add(tree);
        }
        @Override
        public String toString() {
            var str = "";
            for (Tagged p: parts) {
                str += p.toString();
            }
            return str;
        }
    }
    public class Tree extends Tagged {
        Tao tao;
        public Tao getTao() {
            return tao;
        }
        public Tree(Tao tao) {
            super("tree");
            this.tao = tao;
        }
        @Override
        public String toString() {
            return "[" + tao + "]";
        }
    }
    public class Note extends Tagged {
        String note;
        public String getNote() {
            return note;
        }
        public Note(String note) {
            super("note");
            this.note = note;
        }
        @Override 
        public String toString() {
            return note;
        }
    }
    public class Op extends Tagged {
        char op;
        public char getOp() {
            return op;
        }
        public Op(char op) {
            super("op");
            this.op = op;
        }
        @Override 
        public String toString() {
            return "`" + op;
        }
    }
    public class Other extends Tagged {
        public Other() { super("other"); }
    }
    class Input {
        Integer length;
        Integer position;
        String str;
        Stack<Bound> bounds = new Stack<Bound>();
        public Input(String str) {
            this.length = str.length();
            this.position = 0;
            this.str = str;
        }
        public boolean done() { return position >= length; }
        public boolean at(char symbol) { return str.charAt(position) == symbol; }
        public char next() { return str.charAt(position++); }
        public void error(String name) throws Exception {
            throw new Exception("Error: malformed " + name + " at " + position);
        }
        public void bound(char symbol) { bounds.push(new Bound(position, symbol)); }
        public void unbound() { bounds.pop(); }
        public boolean atBound() throws Exception {
            if (bounds.size() > 0) {
                Bound pair = bounds.peek();
                Integer position = pair.position;
                char symbol = pair.symbol;
                if (done()) throw new Exception(
                    "ERROR: since " + position + " expected \"" + symbol + "\" before end of input"
                );
                return at(symbol);
            }
            return done();
        }
    }
    class Bound {
        public Integer position;
        public char symbol;

        public Bound(Integer position, char symbol) {
            this.position = position;
            this.symbol = symbol;
        }
    }
}