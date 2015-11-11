package jacc;

import compiler.SimpleHandler;

import java.io.PrintWriter;

public class CommandLine {
    private static class NameList {

        public String name;
        public NameList names;

        public static void visit(NameList namelist, Visitor visitor) {
            if (namelist != null) {
                visit(namelist.names, visitor);
                visitor.visit(namelist.name);
            }
        }

        public String getFirst() {
            NameList namelist;
            for (namelist = this; namelist.names != null; namelist = namelist.names) {
                ;
            }
            return namelist.name;
        }

        public NameList(String s, NameList namelist) {
            name = s;
            names = namelist;
        }

        interface Visitor {
            void visit(String s);
        }
    }


    public CommandLine() {
    }

    public static void main(String args[]) {
        NameList namelist = null;
        String s = ".jacc";
        Settings settings = new Settings();
        boolean flag = true;
        boolean flag1 = true;
        boolean flag2 = false;
        boolean flag3 = false;
        boolean flag4 = false;
        NameList namelist1 = null;
        NameList namelist2 = null;
        boolean flag5 = false;
        String dir = null;
        PrintWriter printwriter = new PrintWriter(System.out, true);
        label0:
        for (int i = 0; i < args.length; i++) {
            String s1 = args[i];
            if (s1.startsWith("-")) {
                if (s1.length() == 1) {
                    usage("Missing command line options");
                }
                int j = 1;
                do {
                    if (j >= s1.length()) {
                        continue label0;
                    }
                    switch (s1.charAt(j)) {
                        case 102: // 'f'
                            flag4 = true;
                            break;

                        case 112: // 'p'
                            flag = false;
                            break;

                        case 116: // 't'
                            flag1 = false;
                            break;

                        case 118: // 'v'
                            flag2 = true;
                            break;

                        case 104: // 'h'
                            flag3 = true;
                            break;

                        case 48: // '0'
                            settings.setMachineType(0);
                            break;

                        case 115: // 's'
                            settings.setMachineType(1);
                            break;

                        case 97: // 'a'
                            settings.setMachineType(2);
                            break;

                        case 101: // 'e'
                            if (i + 1 >= args.length) {
                                usage("Missing filename for -e option");
                            }
                            namelist1 = new NameList(args[++i], namelist1);
                            break;

                        case 114: // 'r'
                            if (i + 1 >= args.length) {
                                usage("Missing filename for -r option");
                            }
                            namelist2 = new NameList(args[++i], namelist2);
                            break;

                        case 110: // 'n'
                            flag5 = true;
                            break;

                        case 'd':
                            if (i + 1 >= args.length) {
                                usage("Missing directory for -d option");
                            }
                            dir = args[++i];
                            break;

                        default:
                            usage("Unrecognized command line option " + s1.charAt(j));
                            break;
                    }
                    j++;
                } while (true);
            }
            if (!s1.endsWith(s)) {
                usage("Input file must have \"" + s + "\" suffix");
            } else {
                namelist = new NameList(s1, namelist);
            }
        }

        if (namelist == null) {
            usage("No input file(s) specified");
        }
        SimpleHandler simplehandler = new SimpleHandler();
        String s2 = namelist.getFirst();
        int k = 1 + Math.max(s2.lastIndexOf('\\'), s2.lastIndexOf('/'));
        //String s3 = s2.substring(0, k);
        dir = dir == null ? s2.substring(0, k) : dir;
        String s4 = s2.substring(k, s2.length() - s.length());
        final JaccJob job = new JaccJob(simplehandler, printwriter, settings);
        NameList.visit(namelist, new NameList.Visitor() {

            public void visit(String s5) {
                job.parseGrammarFile(s5);
            }

        });
        job.buildTables();
        settings.fillBlanks(s4);
        NameList.visit(namelist1, new NameList.Visitor() {

            public void visit(String s5) {
                job.readErrorExamples(s5);
            }

        });
        if (simplehandler.getNumFailures() > 0) {
            return;
        }
        if (flag) {
            (new ParserOutput(simplehandler, job)).write(dir + settings.getClassName() + ".java");
        }
        if (flag1) {
            (new TokensOutput(simplehandler, job)).write(dir + settings.getInterfaceName() + ".java");
        }
        if (flag2) {
            (new TextOutput(simplehandler, job, flag4)).write(dir + s4 + ".output");
        }
        if (flag3) {
            (new HTMLOutput(simplehandler, job, flag4)).write(dir + s4 + "Machine.html");
        }
        final boolean showState = flag5;
        NameList.visit(namelist2, new NameList.Visitor() {

            public void visit(String s5) {
                job.readRunExample(s5, showState);
            }

        });
    }

    private static void usage(String s) {
        System.err.println(s);
        System.err.println("usage: jacc [options] file.jacc ...");
        System.err.println("options (individually, or in combination):");
        System.err.println(" -p        do not generate parser");
        System.err.println(" -t        do not generate token specification");
        System.err.println(" -v        output text description of machine");
        System.err.println(" -h        output HTML description of machine");
        System.err.println(" -f        show first/follow sets (with -h or -v)");
        System.err.println(" -a        treat as LALR(1) grammar (default)");
        System.err.println(" -s        treat as SLR(1) grammar");
        System.err.println(" -0        treat as LR(0) grammar");
        System.err.println(" -r file   run parser on input in file");
        System.err.println(" -n        show state numbers in parser output");
        System.err.println(" -e file   read error cases from file");
        System.err.println(" -d dir    output files to directory");
        System.exit(1);
    }
}
