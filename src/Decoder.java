import java.util.*;

public class Decoder {
    public int success_time = 0;
    public LtDecoder decoder = null;
    public List<String> secret_data = new LinkedList<>();

    public void decode(Map<String,String> JSON,int filesize){
        decoder = new LtDecoder(Util.DEFAULT_C,Util.DEFAULT_DELTA);

        for(String key:JSON.keySet()){
            String lt_block = JSON.get(key);
            if(decoder.consume_block(filesize,key,lt_block,1)){
                decoder.received_packs++;
                if(decoder.is_done()){
                    success_time++;
                    System.out.println("Decoded Successfully... "+success_time);
                    List<Integer> buff = decoder.bytes_dump();
                    String str = "";
                    for(int i=0;i<buff.size();i++) {
                        int tmp = buff.get(i);
                        if(tmp==-1)
                            str+="?";
                        else
                            str += (char)('a'+tmp);
                    }
                    secret_data.add(str);
                    decoder.succeed_and_init();
                }else{
                    System.out.println("Need more Packs...Received: "+decoder.received_packs);
                }
            }
        }

    }

    public List<String> run(Map<String,String> JSON,int filesize){
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        System.out.println("-----------------------------Extraction---------------------------------------");
        Map<String,String> modified_json = new HashMap<>();
//        modified_json= Util.eliminateLevels(JSON, "");
        decode(modified_json, filesize);

        return this.secret_data;
    }



}
