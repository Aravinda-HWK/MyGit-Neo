import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        String repoPath = "neo";
        Neo repo = new Neo(repoPath);
        repo.init();

        try {
            repo.showCommitDiff("80d7971848c81773b0a3b789c68855d1a17169ae");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
