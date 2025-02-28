import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        String repoPath = "neo";
        Neo repo = new Neo(repoPath);

        while (true) {
            if (args.length > 0) {
                if (args[0].equals("init")) {
                    repo.init();
                    return;
                }
                if (args[0].equals("add")) {
                    int numberOfArgs = args.length;
                    if (numberOfArgs < 2) {
                        System.out.println("Usage: neo add <file>");
                        return;
                    }
                    if (numberOfArgs >= 2) {
                        if (args[1].equals(".")) {
                            try {
                                repo.addAll();
                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            return;
                        } else {
                            for (int i = 1; i < numberOfArgs; i++) {
                                try {
                                    repo.add(args[i]);
                                } catch (IOException | NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                            }
                            return;
                        }

                    }
                }
                if (args[0].equals("commit")) {
                    try {
                        repo.commit(args[1]);
                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (args[0].equals("log")) {
                    try {
                        repo.log();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (args[0].equals("diff")) {
                    try {
                        repo.showCommitDiff(args[1]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (args[0].equals("help")) {
                    System.out.println("Usage: neo <command> [<args>]");
                    System.out.println("Commands:");
                    System.out.println("  init      Create an empty Neo repository or reinitialize an existing one");
                    System.out.println("  add       Add file contents to the index");
                    System.out.println("  commit    Record changes to the repository");
                    System.out.println("  log       Show commit logs");
                    System.out.println("  diff      Show changes between commits, commit and working tree, etc");
                    return;
                }
            }
            if (args.length == 0) {
                System.out.println("Usage: neo <command> [<args>]");
                return;
            }
        }
    }
}
