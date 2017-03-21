import java.util.ArrayList;

/**
 * Created by lauraedelson on 3/2/17.
 */
public class ViterbiTree {
    TreeNode root;
    ArrayList<TreeNode> leaves;

    public ViterbiTree() {
        root = new TreeNode(new Word("","start"), 1.0);
        leaves = new ArrayList<>();
        leaves.add(root);
    }

    public ArrayList<TreeNode> getLeaves() {
        return leaves;
    }

    public void setLeaves(ArrayList<TreeNode> leaves) {
        this.leaves = leaves;
    }
}
