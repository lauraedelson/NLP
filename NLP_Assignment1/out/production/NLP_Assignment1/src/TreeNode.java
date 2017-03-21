import java.util.List;
import java.util.LinkedList;


public class TreeNode  {

    Word data;
    Double prob;
    TreeNode parent;
    List<TreeNode> children;

    public TreeNode() {
        this.children = new LinkedList<TreeNode>();
    }
    public TreeNode(Word data, Double prob) {
        this.data = data;
        this.prob = prob;
        this.children = new LinkedList<TreeNode>();
    }

    public TreeNode addChild(Word child, Double prob) {
        TreeNode childNode = new TreeNode(child, prob);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
}