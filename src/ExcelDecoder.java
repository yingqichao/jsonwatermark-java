/**
 * @author Qichao Ying
 * @date 2019/8/12 16:26
 * @Description DEFAULT
 */
import ExcelWatermarkHelper.excel.ExcelUtil;
import GeneralHelper.LtDecoder;
import GeneralHelper.Sampler;
import Setting.Settings;
import Utils.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Math.min;

public class ExcelDecoder extends AbstractDecoder{

    Map<Integer,Integer> map = new HashMap<>();
    TreeMap<Integer,Integer> treemap = new TreeMap<>();
    List<String> keyCol_values;
    File file;
    FileOutputStream out;
    Workbook wb ;
    String fileVersion;
    ExcelUtil excl = new ExcelUtil();
    List<Integer> keyCols = new LinkedList<>();
    int max_allowed_modi_digits = 3;
    TreeSet<Integer> keyIndex = new TreeSet<>();
    int startRow = 0;

    Set<Integer> row_contain_len = new HashSet<>();
    Map<Integer,Set<Integer>> row_contain_len_map = new HashMap<>();

    //for CSV only
    List<String> csvData = new ArrayList<>();
    String[][] csvArray;

    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;
    int filesize = 0;
    Set<Integer> banColList = new HashSet<>();
    Set<Integer> redundant = new HashSet<>();
    int length_need = 0;
    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    boolean MODE;
    List<List<Integer>> data = new LinkedList<>();

    public ExcelDecoder(File file, double max_allowed_modification,boolean isLong) throws Exception{
        this.file = file;
        this.MODE = isLong;
        this.decoder= new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);
        this.max_allowed_modi_digits = (int)Math.max(Math.ceil(Math.log10(1/max_allowed_modification)),3);
        System.out.println("[Accepted Modification Length] "+ this.max_allowed_modi_digits);

        this.fileVersion = file.getName().substring(file.getName().lastIndexOf("."));

        if(fileVersion.equals(".csv")){
            // CSV
            csvData = Utils.CsvUtil.readCsv(this.file.getPath());
        }else{
            // EXCEL
            this.wb = excl.getWorkbook(file);
        }
        this.getSheetsRowAndCol();

//        filesize = Integer.parseInt((this.wb).getProperties().getCoreProperties().getDescription());
//        ByteBuffer buf = ByteBuffer.allocateDirect(10) ;
//        UserDefinedFileAttributeView userDefined = Files.getFileAttributeView(Paths.get(file.getPath()), UserDefinedFileAttributeView.class);
//        try {
//            userDefined.read("num_packages", buf);
//        }catch(Exception e){
//            throw new Exception("The file does not contain watermark.");
//        }
//        buf.flip();
//        filesize = Integer.parseInt(Charset.defaultCharset().decode(buf).toString());
//        if(filesize==0){
//            throw new Exception("Filesize equals to 0. Please check!");
//        }


        if(fileVersion.equals(".csv")){
            // CSV
//            filesize = Integer.parseInt(csvData.get(startRow-1).split(",")[exclCol[0]]);
            csvArray = new String[exclRow[0]][exclCol[0]];
            for(int i=0;i<exclRow[0];i++){
                String[] strs1 = csvData.get(i).split(",");String[] strs2 = csvData.get(i).split("\t");

                String[] strs = (strs1.length>=strs2.length)?strs1:strs2;
                for(int j=0;j<exclCol[0];j++)
                    if(j>=strs.length)
                        csvArray[i][j] = "";
                    else
                        csvArray[i][j] = strs[j];
            }
        }

    }

    public String getExactValue(int row,int col){
        if(csvData.size()==0){
            // EXCEL
            int sheet = 0;
            Object str = this.excl.getExactValue(this.wb, sheet, row,col);
            return ((str==null)?"":str).toString();
        }else{
            // CSV
            return (csvArray[row].length>col)?csvArray[row][col]:"";
        }
    }

    /*
     * 获得workbook的最大行列
     */
    private void getSheetsRowAndCol(){
        this.sheetNum = 1;
        exclRow = new int[this.sheetNum];
        exclCol = new int[this.sheetNum];
        if(this.csvData.size()==0) {
            // EXCEL
            for (int i = 0; i < this.sheetNum; i++) {
                exclRow[i] = this.wb.getSheetAt(i).getPhysicalNumberOfRows();
                if (0 != exclRow[i]) {
                    for (int j = startRow; j < min(exclRow[i], 20); j++) {
                        exclCol[i] = max(this.wb.getSheetAt(i).getRow(j).getPhysicalNumberOfCells(), exclCol[i]);
                    }
                }
            }
        }else{
            // CSV
            exclRow[0] = csvData.size();
            for (int j = startRow; j < min(exclRow[0], 20); j++) {
                exclCol[0] = max(csvData.get(j).split(",").length, exclCol[0]);
                exclCol[0] = max(csvData.get(j).split("\t").length, exclCol[0]);
            }
        }
    }

    public void getFileSize(){

        for(int i=startRow;i<exclRow[0];i++){
            decode(i,Settings.LENGTH);
        }
        for(int key:map.keySet()){
            treemap.put(map.get(key),key);
        }
        while(treemap.get(treemap.lastKey())==0||treemap.get(treemap.lastKey())==15)
            treemap.remove(treemap.lastKey());
        filesize = (treemap.get(treemap.lastKey())+1)*4;
        if(filesize==2) {
            treemap.remove(treemap.lastKey());
            filesize = (treemap.get(treemap.lastKey()) + 1) * 4;
        }
        System.out.println(">> Filesize: " + filesize + " ("+treemap.lastKey()+")");
        row_contain_len = row_contain_len_map.get(treemap.get(treemap.lastKey()));
    }

    public void decode(int row,boolean isNormal){
        if(row_contain_len.contains(row)){
            //说明是隐藏水印长度的行，已经检测过了
            System.out.println("Skipped");
            return;
        }
        List<Integer> src_blocks = new LinkedList<>();
//        String key = "";
//        for(int k:keyIndex){
//            key+=getExactValue(row, k);
//        }
        String name = keyCol_values.get(row);
        //init src_blocks and key for pseudo-random
//        decoder = new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);
        if(isNormal)
            src_blocks = decoder.getSrcBlocks(filesize,name,1);
        else
            decoder.buildPrng(name);
//        else
//            decoder.prng = new Sampler(Settings.rand_K, Settings.DEFAULT_DELTA, Settings.DEFAULT_C);
        //get dynamically embedment: calculate total sum
        // A-Z a-z同样去除

        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>(Util.comparator);
//        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>
//                ((a,b)->(b.getValue().replaceAll("[^A-Za-z0-9]", "").length()-a.getValue().replaceAll("[^A-Za-z0-9]", "").length()));
        int totalLen = 0;List<Integer> eachLen = new LinkedList<>();
        for(int col=0;col<exclCol[0];col++){
            if(!keyIndex.contains(col) && !banColList.contains(col)) {
                String str = getExactValue(row, col);
                if(Util.isNumeric(str)) {//Util.isInteger(str) &&
                    //新规定要求只能在float或者int中嵌入数据
                    //数据为0不做嵌入，且修改幅度不可以超过0.05，也即前两位不考虑嵌入
                    if(Double.parseDouble(str)!=0 && Util.lengthQualify(str,this.max_allowed_modi_digits)>0) {
                        totalLen += Util.lengthQualify(str,this.max_allowed_modi_digits);
                        pq.offer(new AbstractMap.SimpleEntry<>(col, str));
                    }
                }
            }
        }
        //data extraction
        int validLen = (!isNormal && MODE)?Settings.DEFAULT_EMBEDLEN_LONGMODE:Settings.DEFAULT_EMBEDLEN;
        if(totalLen<validLen){
            //表示当前行可以用来嵌入信息的总长度不够，一般都不会执行的
            System.out.println("[SKIPPED PACK] Total length of row "+row+" is not enough! Length:"+totalLen);

            return;
        }
        String debug = new String();int decodeInt = 0;
        int remainLen = validLen;
        while(pq.size()!=0){
            Map.Entry<Integer,String> entry = pq.poll();
            //data embedment according to length of value
            int len = (int)Math.ceil(validLen*Util.lengthQualify(entry.getValue().toString(),this.max_allowed_modi_digits)/(double)totalLen);
            if(remainLen-len<0)
                len = remainLen;

             List<Object> list = decoder.extract_excel(((Integer)row).toString(),entry.getValue(),len,this.max_allowed_modi_digits);
             int retrieve = (int)list.get(0);
             debug += (String)list.get(1);


            remainLen -= len;

            retrieve <<= Math.max(0,remainLen);

            decodeInt += retrieve;

            if(remainLen<=0)    break;
        }
        int real_embed_data = decodeInt>>((!isNormal && MODE)?Settings.DEFAULT_EMBEDLEN_LONGMODE-Settings.DEFAULT_DATALEN_LONGMODE:Settings.DEFAULT_EMBEDLEN-Settings.DEFAULT_DATALEN);
        String crc_code = Util.dec2bin(decodeInt,(!isNormal&&MODE)?Settings.DEFAULT_EMBEDLEN_LONGMODE:Settings.DEFAULT_EMBEDLEN);
        if(!isNormal)
            System.out.println(crc_code);
        if(isNormal) System.out.println("Debug Extract: EmbeddedAt-> "+debug+"  origin->"+ decodeInt+" data->" + crc_code + " sourceBlock->" + src_blocks.get(0) + " ROW: "+row);

        if (Utils.cyclic.CyclicCoder.decode_without_correction(decodeInt,isNormal,MODE) != -1) {

            if(isNormal) {
                //隐藏水印数据
                System.out.println("Valid Package.");
                if (decoder.consume_block_excel(src_blocks, real_embed_data)) {
                    decoder.received_packs++;
                    if (decoder.is_done()) {
                        success_time++;
                        System.out.println("--> Decoded Successfully <--... The ExcelWatermarkHelper is now successfully retrieved. Time: " + success_time);
                        List<Integer> buff = decoder.bytes_dump();

                        data.add(new LinkedList<>(buff));
//                        List<String> res = Utils.StrBinaryTurn.stream2String(buff);
//                        //分别保存中英文的可能结果
//                        secret_data.add(res.get(0));
//                        secret_data_chinese.add(res.get(1));
                        decoder.succeed_and_init();
                    } else {
                        System.out.println("Need more Packs...Received: " + decoder.received_packs);
                    }
                }
            }else{
                //隐藏水印长度
                System.out.println("[Detected length/row] " + real_embed_data+" / "+row+" "+crc_code);
                map.put(real_embed_data,map.getOrDefault(real_embed_data,0)+1);
//                row_contain_len.add(row);
                Set<Integer> contain_len = row_contain_len_map.getOrDefault(real_embed_data,new HashSet<>());
                contain_len.add(row);
                row_contain_len_map.put(real_embed_data,contain_len);
            }
        }else{
            if(isNormal)
                System.out.println("Invalid Package.Skipped...");
//            else
//                System.out.println(crc_code);
        }

    }

    public void findKeyIndex() throws Exception{
        //第一个数是作为键值的
        int keyCol = -1;//int firstThresh = -1;
        int sheetIndex = 0;//当前只允许嵌入在一页里，不考虑多页的情况
        //double thresh = 0.0;//int valid = 0;
        double maxMatch = 0;//double firstMatch = 0;
        double matchLen = 0;
        TreeMap<Double,List<Integer>> match_map = new TreeMap<>();
        int sumLen = 0;TreeMap<Integer,Integer> shortest = new TreeMap<>();

        for (int colIndex = 0; colIndex < this.exclCol[sheetIndex]; colIndex++) {
            Set<String> objCol = new HashSet<>();
            Set<String> objColwithoutLen = new HashSet<>();
            List<Object> col = new LinkedList<>();
            if (csvData.size() == 0) {
                // EXCEL
                col = this.excl.getColValues(this.wb, sheetIndex, colIndex);
            } else {
                // CSV
                for (int i = startRow; i < exclRow[0]; i++)
                    col.add(csvArray[i][colIndex]);
            }
            for (Object object : col) {
                if (object == null)
                    continue;
                int validLen = object.toString().replaceAll("[^A-Za-z0-9]", "").length();
//                totalLen += validLen;
                if (validLen >= Setting.Settings.DEFAULT_MINLEN_EXCEL)
                    //不足以嵌入信息，并且当前value没有出现过
                    objCol.add(object.toString());
                objColwithoutLen.add((object.toString().length() <= this.max_allowed_modi_digits) ? object.toString() : object.toString().substring(0, this.max_allowed_modi_digits));
                sumLen+=object.toString().length();
            }

            shortest.put(sumLen,colIndex);

            double match = ((double) objColwithoutLen.size()) / col.size();
            double matchWithLen = ((double) objCol.size()) / col.size();//暂时不用

            List<Integer> list = match_map.getOrDefault(match,new LinkedList<>());
            list.add(colIndex);
            match_map.put(match,list);
//            if (match >= maxMatch) {
//                if (match > maxMatch || matchWithLen < matchLen) {
//                    matchLen = matchWithLen;
//                    keyCol = colIndex;
//                }
//                maxMatch = match;
//            }

        }

        //##########Strategy 1
//        if(firstMethod) {

//            if (keyCol == -1) {
//                System.out.println("[Warning] Using a longer key");
//                throw new Exception("[Warning] No valid key index found...Embedding was aborted...");
////            keyCol = firstThresh;
////            maxMatch = firstMatch;
//
//            }

//            System.out.println("The selected col is COL: " + keyCol + " with maxMatch " + maxMatch);
//        }

        //##########Strategy 2:最多找两列重复最小的，合并起来
        List<Integer> list = match_map.get(match_map.lastKey());double firstMatch = match_map.lastKey();
        int first = list.get(0);int second;
        //关键列添加第一项：重复概率最小的那个
        keyIndex.add(first);keyCol_values = new ArrayList<>(exclRow[0]-startRow);
        System.out.println("> The first selected col is COL: " + first + " with maxMatch " + firstMatch);
        if(list.size()>1)   second = list.get(1);
        else    second = match_map.get(match_map.lowerKey(match_map.lastKey())).get(0);
        for (int i = startRow; i < exclRow[0]; i++)
            if (csvData.size() == 0)
                // EXCEL
                keyCol_values.add(getExactValue(i, first));
            else
                //CSV
                keyCol_values.add(csvArray[i][first]);
        //如果第一个关键列重复程度不超过设定的阈值，则添加第二个关键列
        double thresh = Settings.keyCol_thresh;
        if(firstMatch<thresh){
            for (int i = startRow; i < exclRow[0]; i++) {
                if (csvData.size() == 0)
                    // EXCEL
                    keyCol_values.set(i, keyCol_values.get(i) + getExactValue(i, second));
                else
                    //CSV
                    keyCol_values.set(i, keyCol_values.get(i) + csvArray[i][second]);
            }
            keyIndex.add(second);
            Set<String> newset = new HashSet<>(keyCol_values);
            System.out.println("> The second selected col is COL: " + second + " with maxMatch " + (double)newset.size()/exclRow[0]);
        }

        //##########Strategy 3:适当修改关联列
//        Map<String,Integer> objCol = new HashMap<>();
//        if(!banColList.contains(keyCol)){
//            List<Object> col = new LinkedList<>();
//            if (csvData.size() == 0) {
//                // EXCEL
//                col = this.excl.getColValues(this.wb, sheetIndex, keyCol);
//            } else {
//                // CSV
//                for (int i = startRow; i < exclRow[0]; i++)
//                    col.add(csvArray[i][keyCol]);
//            }
//            for (Object object : col) {
//                if (object == null)
//                    continue;
//                objCol.put(object.toString(),objCol.getOrDefault(object.toString(),0)+1);
//
//            }
//            int modified = 0;
//            for (int i = startRow; i < exclRow[0]; i++) {
//                String str = getExactValue(i, keyCol);
//                if(objCol.containsKey(str) && objCol.get(str)>1 && Util.isNumeric(str) && Util.lengthQualify(str,this.max_allowed_modi_digits)>0){
//                    //遇到重复，尝试修改
//                    StringBuilder builder = new StringBuilder(str);
//                    for(int j=this.max_allowed_modi_digits;j<str.length();j++){
//                        //对于数字：统一向下取结果
//                        char ori = builder.charAt(j);int k = (ori%2==0)?1:0;
//                        builder.setCharAt(j, (char) (ori - ori % 2 + k));
//                        String newstr = builder.toString();
//                        if(!objCol.containsKey(newstr)){
//                            objCol.put(str,objCol.get(str)-1);
//                            objCol.put(newstr,1);
//                            if(csvData.size()==0) {
//                                this.excl.writeWorkBookAt(this.wb, 0, i, keyCol, newstr);
//                            }else{
//                                csvArray[i][keyCol] = newstr;
//                            }
//                            modified++;
//                            break;
//                        }
//                        builder.setCharAt(j, ori);
//                    }
//                }
//            }
//            System.out.println("After Modification, maxMatch: " + ((double)objCol.size())/exclRow[0]+", modified: "+modified);
//
//        }


        int name_collide = 0;int hash_collide = 0;int length_collide = 0;int last_used_redundant = -1;
        length_need = Math.max(1,exclRow[0]/Settings.row_for_water_len);
        //计算有多少可以嵌入的数据包
        Set<Integer> seeds = new HashSet<>();Set<String> names = new HashSet<>();
        for(int i=startRow;i<exclRow[0];i++) {
//            String name = "";
//            for(int index:keyIndex)
//                name+=getExactValue(i, index);
            String name = keyCol_values.get(i);
            int seed = Util.BKDRHash(name, 131);
//            if(i%Settings.row_for_water_len==0)
//                continue;
            if(names.contains(name) || seeds.contains(seed)){
                if(names.contains(name))
                    name_collide++;
                else
                    hash_collide++;

                if(length_need>0) {
//                    if(last_used_redundant==-1 || i-last_used_redundant>=Settings.row_for_water_len*0.75){

                        //需要两个用作嵌入水印长度的冗余行之间空开足够多距离
                        last_used_redundant = i;
                        redundant.add(i);
                        length_need--;
//                    }
                }
                continue;
            }

            int totalLen = 0;
            for (int col = 0; col < exclCol[0]; col++) {
                //不是关键列，也不在banList里
                if (!keyIndex.contains(col) && !banColList.contains(col)) {
                    String str = getExactValue(i, col);
                    if (Util.isNumeric(str)) {//Util.isInteger(str) &&
                        //新规定要求只能在float或者int中嵌入数据
                        //数据为0不做嵌入，且修改幅度不可以超过0.05，也即前两位不考虑嵌入
                        if (Double.parseDouble(str) != 0 && Util.lengthQualify(str, this.max_allowed_modi_digits) > 0) {
                            totalLen += Util.lengthQualify(str, this.max_allowed_modi_digits);
                        }
                    }
                }
            }
            int validLen = Settings.DEFAULT_EMBEDLEN;
            if (totalLen >= validLen) {
                //长度足够
                names.add(name);seeds.add(seed);
            }else{
                length_collide++;
            }
        }
        System.out.println("---------------------------");
        System.out.println("[Num of valid packages] "+seeds.size()+"/"+exclRow[0]);
        System.out.println("Name Collide: "+name_collide+"/ Seed_Collide: "+hash_collide+"/ Length_Collide: "+length_collide);
        System.out.println("---------------------------");

    }

    public List<String> run(String filePath,int[] args) throws Exception{
        for(int arg:args)
            banColList.add(arg);
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        System.out.println("-----------------------------Extraction---------------------------------------");


//        WatermarkUtils watermarkUtils = new WatermarkUtils(new File(filePath));
        findKeyIndex();
        getFileSize();
        //Embedment
        for(int i=startRow;i<exclRow[0];i++){//固定第一个sheet
            //Embedment;i++){
            decode(i,Settings.NORMAL);
        }

        //printer.print(getEnglishResult(),getChineseResult());


        if(data.size()>0) {
            //根据概率得到最终结果
            List<Integer> res = new LinkedList<>();
            for (int i = 0; i <data.get(0).size(); i++) {
                Map<Integer,Integer> calc = new HashMap<>();
                for (int j = 0; j < data.size(); j++) {
                    calc.put(data.get(j).get(i),calc.getOrDefault(data.get(j).get(i),0)+1);
                }
                TreeMap<Integer,Integer> time = new TreeMap<>();
                for (int k:calc.keySet()){
                    time.put(calc.get(k),k);
                }
                res.add(time.get(time.lastKey()));
            }
            System.out.println("成功提取次数为 "+data.size());
            return Utils.StrBinaryTurn.stream2String(res);
//                        //分别保存中英文的可能结果
//                        secret_data.add(res.get(0));
//                        secret_data_chinese.add(res.get(1));
        }

        //否则返回空结果
        List<String> result = new LinkedList<>();result.add("");result.add("");
        System.out.println("成功提取次数为 0， 认为不含水印（或者嵌入的水印过长）");
        return result;
    }

}
