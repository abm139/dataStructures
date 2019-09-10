package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 * 
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {
		String str = this.sc.nextLine();
		root = new TagNode("html", null, null);

		Stack<String> L = new Stack<String>();
		Stack<String> subLines = new Stack<String>();
		for(str = this.sc.nextLine(); str != null && !str.equals("</html>"); str = this.sc.nextLine())
			L.push(str);
		for(; !L.isEmpty(); subLines.push(L.pop())) {}
		root.firstChild = createCnodes(subLines);
	}
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		rt(root, oldTag, newTag);
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		TagNode t = isThereTable(root);
		if(t != null)
		{
			t = t.firstChild;
			for(int i = row-1; i != 0; i--)
				t = t.sibling;
			for(t = t.firstChild; t != null; t = t.sibling)
			{
				TagNode boldTag = new TagNode("b", t.firstChild, null);
				t.firstChild = boldTag;
			}
		}
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		delete(root, tag);
	}
	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		add(root, word, tag);
	}
	
	private TagNode createCnodes(Stack<String> sLines) {
		if(sLines.isEmpty())
			return null;
		String str = sLines.pop();
		if(str.charAt(0) == '<')
		{
			str = str.substring(1, str.length() - 1);	
			TagNode nTag = new TagNode(str, null, null);
			if(sLines.isEmpty())
				return nTag;
			
			Stack<String> sublines = new Stack<String>();
			for(int tCount = 0; !sLines.isEmpty();)
			{
				str = sLines.pop();
				if(str.equals("<" + nTag.tag + ">"))
					tCount++;
				if(str.equals("</" + nTag.tag + ">"))
				{
					if(tCount == 0)
						break;
					tCount--;
				}
				sublines.push(str);
			}
			if (!sLines.isEmpty())
				nTag.sibling = createCnodes(sLines);
			for( ;!sublines.isEmpty(); sLines.push(sublines.pop())) {}
			if(!sLines.isEmpty())
				nTag.firstChild = createCnodes(sLines);
			return nTag;	
		}
		TagNode nTag = new TagNode(str, null, null);
		nTag.sibling = createCnodes(sLines);
		return nTag;
	}
	
	private void rt(TagNode t, String oTag, String nTag){
		if(t == null)
			return;
		
		rt(t.sibling, oTag, nTag);
		rt(t.firstChild, oTag, nTag);
		
		if(t.tag.equals(oTag))
			t.tag = nTag;
	}
	
	private TagNode delete(TagNode t, String rTag) {
		if(t == null)
			return null;
		t.sibling = delete(t.sibling, rTag);
		t.firstChild = delete(t.firstChild, rTag);
		
		if(!t.tag.equals(rTag))
			return t;
		if(t.firstChild == null)
			return t.sibling;
		TagNode sib;
		for(sib = t.firstChild; sib.sibling != null; sib = sib.sibling)
			if((rTag.equals("ol") || rTag.equals("ul")) && sib.tag.equals("li"))
				sib.tag = "p";
		if((rTag.equals("ol") || rTag.equals("ul")) && sib.tag.equals("li"))
			sib.tag = "p";
		sib.sibling = t.sibling;
		return t.firstChild;

	}
	
	private TagNode isThereTable(TagNode t) {
		if(t == null)
			return null;
		TagNode s = isThereTable(t.sibling);
		TagNode c = isThereTable(t.firstChild);
		if(t.tag.equals("table"))
			return t;
		if(s != null)
			return s;
		return c;
	}
	
	private TagNode add(TagNode node, String word, String tag) {
		TagNode n = node;
		String w = word;
		String t = tag;
		if(n == null)
			return null;
		n.sibling = add(n.sibling, w, t);
		n.firstChild = add(n.firstChild, w, t);
		
		if(t.equals("html"))
			return n;
		if(t.equals("body"))
			return n;
		if(t.equals("p"))
			return n;
		if(t.equals("em"))
			return n;
		if(t.equals("b"))
			return n;
		if(t.equals("table"))
			return n;
		if(t.equals("tr"))
			return n;
		if(t.equals("td"))
			return n;
		if(t.equals("ol"))
			return n;
		if(t.equals("ul"))
			return n;
		if(t.equals("li"))
			return n;
		
		String theTag = n.tag;
		if(theTag.equals(w))
		{
			n.firstChild = new TagNode(theTag, null, null);
			n.tag = t;
			return n;
		}
		
		for(int index = theTag.indexOf(w); index != -1; )
		{
			if(index != 0)
			{
				int charAfter = -1;
				if(index + w.length() + 1 > theTag.length())
					charAfter = 0;
				else if(index + w.length() + 2 > theTag.length()  && isValid(theTag.charAt(index + w.length()) + " "))
					charAfter = 1;
				if(theTag.charAt(index - 1) == ' ' && (charAfter != -1 || isValid(theTag.substring(index + w.length(), index + w.length() + 2))))
				{
					n.tag = theTag.substring(0, index);
					n.sibling = add(new TagNode(theTag.substring(index), null, n.sibling), w, t);
					break;
				}
				else
				{
					index = theTag.indexOf(w, index + 1);
					continue;
				}
			}
			else
			{
				int charAfter = -1;
				if(w.length() + 1 > theTag.length())
					charAfter = 0;
				else if(w.length() + 2 > theTag.length()  && isValid(theTag.charAt(w.length()) + " "))
					charAfter = 1;
				if(charAfter != -1 || isValid(theTag.substring(w.length(), w.length() + 2)))
				{
					if(charAfter == 0 || theTag.charAt(w.length()) == ' ')
					{
						n.firstChild = new TagNode(theTag.substring(0, w.length()), null, null);
						n.sibling = add(new TagNode(theTag.substring(w.length()), null, n.sibling), w, t);
						n.tag = t;
						break;
					}
					else
					{
						n.firstChild = new TagNode(theTag.substring(0, w.length() + 1), null, null);
						n.sibling = add(new TagNode(theTag.substring(w.length() + 1), null, n.sibling), w, t);
						n.tag = t;
						break;
					}
				}
				else
				{
					index = theTag.indexOf(w, index + 1);
					continue;
				}
			}
		}
		
		return n;
	}
	
	private boolean isValid(String x) {
		if(x.charAt(0) == ' ')
			return true;
		if(x.charAt(0) == '.' && x.charAt(1) == ' ')
			return true;
		if(x.charAt(0) == '!' && x.charAt(1) == ' ')
			return true;
		if(x.charAt(0) == ',' && x.charAt(1) == ' ')
			return true;
		if(x.charAt(0) == '?' && x.charAt(1) == ' ')
			return true;
		if(x.charAt(0) == ':' && x.charAt(1) == ' ')
			return true;
		if(x.charAt(0) == ';' && x.charAt(1) == ' ')
			return true;
		return false;
	}
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}
	
	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|---- ");
			} else {
				System.out.print("      ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
}