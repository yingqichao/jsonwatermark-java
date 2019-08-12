package GeneralHelper;

import java.util.*;

public class BlockGraph{
    //Graph on which we run Belief Propagation to resolve source node data

    public Map<Integer,List<CheckNode>> checks = new HashMap<>();
    public Map<Integer,Integer> eliminated = new HashMap<>();
    public int num_blocks;

    public BlockGraph(int num_blocks) {
        this.num_blocks = num_blocks;
    }

    public boolean add_block(Set<Integer> nodes,int data){
        //Adds a new check node and edges between that node and all source nodes it connects, resolving all message passes that become possible as a result.
        Deque<CheckNode> to_eliminate = new LinkedList<>();
        if(nodes.size()==1){
            //Recursively eliminate all nodes that can now be resolved
            to_eliminate = eliminate(nodes,data);
            while(to_eliminate.size()!=0){
                to_eliminate = eliminate(nodes,data);
                while(to_eliminate.size()>0){
                    //Recursively eliminate all nodes that can now be resolved
                    CheckNode checkNode = to_eliminate.pollFirst();
                    to_eliminate.addAll(eliminate(checkNode.src_nodes,checkNode.check));
                }
            }
        }else{
            //Pass messages from already-resolved source nodes
            List<Integer> templist = new LinkedList<>(nodes);int index = 0;
            while(index<templist.size()){
                int node = templist.get(index);
                if(eliminated.containsKey(node)){
                    templist.remove(templist.indexOf(node));
                    data^=eliminated.get(node);
                }else{
                    index++;
                }
            }
            nodes = new HashSet<>(templist);
            //Resolve if we are left with a single non-resolved source node
            if(nodes.size()==1){
                return add_block(nodes,data);
            }else{
                //Add edges for all remaining nodes to this check
                CheckNode check = new CheckNode(nodes,data);
                for(int node:nodes){
                    List<CheckNode> list = checks.getOrDefault(node,new LinkedList<>());
                    list.add(check);
                    checks.put(node,list);
                }
            }
        }

        //Are we done yet?
        return eliminated.size() >= num_blocks;
    }

    public Deque<CheckNode> eliminate(Set<Integer> nodes,int data){
        Deque<CheckNode> res = new LinkedList<>();
        //Resolves a source node, passing the message to all associated checks
        List<CheckNode> others = new LinkedList<>();
        //Cache resolved value
        for(int node:nodes){
            eliminated.put(node,data);
            others = checks.get(node);
            checks.remove(node);
        }

        System.out.println("-- Eliminated: "+eliminated.size());

        //Pass messages to all associated checks
        if(others!=null) {
            for (CheckNode check : others) {
                check.check ^= data;
                check.src_nodes.removeAll(nodes);

                //Yield all nodes that can now be resolved
                res.add(check);
            }
        }

        return res;

    }


}