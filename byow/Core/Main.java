package byow.Core;

/** This is the main entry point for the program. This class simply parses
 *  the command line inputs, and lets the byow.Core.Engine class take over
 *  in either keyboard or input string mode.
 *  @author Junliang Lu, Kunkai Lin
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("Can only have one argument - the input string");
            System.exit(0);
        } else if (args.length == 1) {
            Engine engine = new Engine();
            engine.interactWithInputString(args[0]);
            //engine.interactWithInputString("n8702095859193238354ssswadswwds");
            //engine.interactWithInputString("n8832930857124999723ssaws");
            //engine.interactWithInputString("lwsd");
            //engine.interactWithInputString("n7193300625454684331saaawasdaawdwsd");
            //engine.interactWithInputString("n7193300625454684331saaawasdaawd:q");
            //engine.interactWithInputString("lwsd");
            //System.out.println(engine.toString());
        } else {
            Engine engine = new Engine();
            engine.interactWithKeyboard();
        }
    }
}
