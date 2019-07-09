import java.util.*;

public class LtDecoder {
    public int received_packs = 0;
    public double c = 0.0;
    public double delta = 0.0;
    public int K = 0;
    public int filesize = 0;
    public int blocksize = 0;
    public BlockGraph block_graph = null;
    public Sampler prng = null;
    public boolean initialized = false;
    public boolean done = false;

    public LtDecoder(double c, double delta) {
        this.c = c;
        this.delta = delta;
    }

    public void succeed_and_init(){
        received_packs = 0;
        K = 0;
        block_graph = null;
        prng = null;
        initialized = false;
    }

    public boolean is_done(){
        return done;
    }

    public boolean consume_block(int filesize,String key,String lt_block,Integer blocksize){
        if(blocksize==null) blocksize = 1;
        int blockseed = Util.BKDRHash(key,null);

        if(!initialized){
            this.filesize = filesize;
            this.blocksize = blocksize;
            this.K = (int)Math.ceil(filesize / blocksize);
            this.block_graph = new BlockGraph(K);
            this.prng = new Sampler(K,Util.DEFAULT_DELTA,Util.DEFAULT_C);
            this.initialized = true;

        }

        this.prng.setSeed(blockseed);
        // Run PRNG with given seed to figure out which blocks were XORed to make received data
        List<Integer> src_blocks = prng.get_src_blocks(null);// or seed=blockseed
        src_blocks.remove(0);//blockseed
        src_blocks.remove(0);//d
        List<Object> blockAndVerify = extract(lt_block);
        if(Util.crc_check((String)blockAndVerify.get(1),null)){
            // If BP is done, stop
            System.out.println("Valid Package.");
            this.done = handle_block(src_blocks, (int)blockAndVerify.get(0));
            return this.done;
        }else{
            System.out.println("Invalid Package.Skip...");
            return false;
        }

    }

    public List<Object> extract(String ori_block){
        List<Object> list = new LinkedList<>();
        int strlen = Util.DEFAULT_STRLEN;boolean negative;String verify = "";
        int extracted = 0;StringBuilder lt_block = new StringBuilder(ori_block);

        //preprocess
        if(Util.isInteger(ori_block)){
            int value_int = Integer.parseInt(ori_block);
            negative = value_int<0;
            if(negative)    lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(0);
        }else if(Util.isNumeric(ori_block)){
            double value_double = Double.parseDouble(ori_block);
            negative = value_double<0;
            if(negative)    lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(0);
            lt_block.deleteCharAt(lt_block.indexOf("."));
        }

        Set<Integer> duplicateSet = new HashSet<>(0);int ind = 0;
        while(ind<strlen){
            int num = prng.get_next() % lt_block.length();
            if(!duplicateSet.contains(num)){
                duplicateSet.add(num);
                if(ind<Util.DEFAULT_DATALEN){
                    extracted *= 2;
                    extracted += (lt_block.charAt(num) % 2);// * pow(2, ind)
                }
                verify += lt_block.charAt(num) % 2;
                ind += 1;
            }

        }

        System.out.println("Debug Extract: " + extracted + " " + ori_block);
        list.add(extracted);list.add(verify);
        return list;
    }

    public List<Integer> bytes_dump(){
        return stream_dump();
    }

    public List<Integer> stream_dump(){
        List<Integer> res = new LinkedList<>();
        int[] data = new int[filesize];
        for(int key:block_graph.eliminated.keySet()){
            data[key] = block_graph.eliminated.get(key);
        }
        for(int i=0;i<filesize;i++)
            res.add(data[i]);
        return res;

    }

    public boolean handle_block(List<Integer> src_blocks, int block){
        // What to do with new block: add check and pass messages in graph
        return block_graph.add_block(new HashSet<>(src_blocks), block);
    }

}
