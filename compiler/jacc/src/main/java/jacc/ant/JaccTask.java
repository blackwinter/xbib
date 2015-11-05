package jacc.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import jacc.CommandLine;

public class JaccTask extends Task {

    private String packageName;
    private String infile;
    private String outdir;
    private String runparser;
    private String errors;
    private boolean noparser;
    private boolean notoken;
    private boolean verbose;
    private boolean showstatenumbers;
    private boolean html;
    private boolean firstfollow;
    private ArrayList args = new ArrayList();

    public JaccTask() {
        super();
    }

    public void execute() throws BuildException {
        if (infile == null) {
            throw new BuildException("Input file needed");
        }

        if (!new File(infile).canRead()) {
            throw new BuildException("Cannot read input file " + infile);
        }

        try {
            findPackage();
            findOutdir();
            args.add(infile);
            args.add("-d");
            args.add(outdir);
            if (noparser) {
                args.add("-p");
            }
            if (notoken) {
                args.add("-t");
            }
            if (runparser != null) {
                args.add("-r");
                args.add(runparser);
            }
            if (verbose) {
                args.add("-v");
            }
            if (html) {
                args.add("-h");
            }
            if (firstfollow) {
                args.add("-f");
            }
            if (errors != null) {
                args.add("-e");
                args.add(errors);
            }
            System.out.println("Running Jacc: infile = " + infile
                    + " outdir=" + outdir);
            CommandLine.main((String[]) args.toArray(new String[]{}));
        } catch (Exception e) {
            throw new BuildException("Exception: " + e.toString());
        }
    }

    public void setFile(File file) {
        this.infile = file.toString();
    }

    public void setDestDir(File file) {
        this.outdir = file.toString() + "/";
    }

    public void setNoParser(boolean b) {
        this.noparser = b;
    }

    public void setNoToken(boolean b) {
        this.notoken = b;
    }

    public void setRunParser(File file) {
        this.runparser = file.toString();
    }

    public void setVerbose(boolean b) {
        this.verbose = b;
    }

    public void setShowStateNumbers(boolean b) {
        this.showstatenumbers = b;
    }

    public void setHtml(boolean b) {
        this.html = b;
    }

    public void setFirstFollow(boolean b) {
        this.firstfollow = b;
    }

    public void setErrors(File file) {
        this.errors = file.toString();
    }

    /**
     * Peek into .jacc file to get package name
     *
     * @throws IOException if there is a problem reading the .jacc file
     */
    private void findPackage() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        while (packageName == null) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            int index = line.indexOf("package");
            if (index >= 0) {
                packageName = line.substring(index + 7).trim();
            }
        }
    }

    /**
     * Find output directory and add package name, if given
     */
    private void findOutdir() {
        File destDir;
        if (outdir != null) {
            if (packageName == null) {
                destDir = new File(outdir);
            } else {
                destDir = new File(outdir,
                        packageName.replace('.', File.separatorChar));
            }
        } else {
            destDir = new File(new File(infile).getParent());
        }
        this.outdir = destDir.toString() + "/";
    }
}
