package apps;

import structures.*;
import java.util.ArrayList;

public class MST {
	
	/**
	 * Initializes the algorithm by building single-vertex partial trees
	 * 
	 * @param graph Graph for which the MST is to be found
	 * @return The initial partial tree list
	 */
	public static PartialTreeList initialize(Graph graph) {
		PartialTreeList L = new PartialTreeList();
		
		for(Vertex v : graph.vertices)
		{
			PartialTree T = new PartialTree(v);
			MinHeap<PartialTree.Arc> P = new MinHeap<PartialTree.Arc>();
			Vertex.Neighbor tmp = v.neighbors;
			
			while(tmp != null)
			{
				P.insert(new PartialTree.Arc(v, tmp.vertex, tmp.weight));
				tmp = tmp.next;
			}
			T.getArcs().merge(P);
			L.append(T);
		}
		
		return L;
	}

	/**
	 * Executes the algorithm on a graph, starting with the initial partial tree list
	 * 
	 * @param ptlist Initial partial tree list
	 * @return Array list of all arcs that are in the MST - sequence of arcs is irrelevant
	 */
	public static ArrayList<PartialTree.Arc> execute(PartialTreeList ptlist) {
		ArrayList<PartialTree.Arc> ans = new ArrayList<PartialTree.Arc>();
		int k=0;
		while(ptlist.size() != 1)
		{	
			PartialTree add = ptlist.remove();
			PartialTree.Arc a = null;
			while(k<add.getArcs().size())
			{
				boolean c = true;
				a = add.getArcs().getMin();
				add.getArcs().deleteMin();
				MinHeap<PartialTree.Arc> deleted = new MinHeap<PartialTree.Arc>();
				while(!add.getArcs().isEmpty())
				{
					PartialTree.Arc b = add.getArcs().getMin();
					add.getArcs().deleteMin();
					deleted.insert(b);
					if(a.v2.name.equals(b.v1.name))
					{
						c = false;
						break;
					}
				}
				add.getArcs().merge(deleted);
				if(c == true)
					break;
				k++;
			}

			if(a == null)
			{
				System.out.println("There are no arcs to this element. It cannot be part of the MST.");
				continue;
			}
			ans.add(a);
			PartialTree m = ptlist.removeTreeContaining(a.v2);	
			add.merge(m);
			ptlist.append(add);
		}
		return ans;
	}
}