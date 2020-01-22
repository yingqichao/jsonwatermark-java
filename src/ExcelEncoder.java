import ExcelWatermarkHelper.excel.ExcelVersion;
import Setting.Settings;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import ExcelWatermarkHelper.excel.ExcelUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.*;

import Utils.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static java.lang.Integer.max;
import static java.lang.Math.min;

public class ExcelEncoder extends AbstractEncoder {

    File file;
//    FileOutputStream out;
    Workbook wb ;
    String fileVersion;
    ExcelUtil excl = new ExcelUtil();
    List<Integer> keyCols = new LinkedList<>();

    //for CSV only
    List<String> csvData = new ArrayList<>();
    List<String> csvData_embedded = new ArrayList<>();

    String[][] csvArray;

    //显示包数量的数组，8bit，如16表示为15的二进制，为00001111，则进一步表示为
    int[] length_represent = new int[4];

    TreeSet<Integer> keyIndex = new TreeSet<>();

    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;
    int startRow = 0;
    int max_allowed_modi_digits = 3;
    Set<Integer> banColList = new HashSet<>();

    boolean MODE;
    TreeSet<Integer> redundant = new TreeSet<>();
    List<String> keyCol_values;
    int length_need = 0;


//    public ExcelEncoder(int seed, double c, double delta, String f_bytes,File file,int startRow) {
//        this.startRow = startRow;
//        super(seed, c, delta, f_bytes);
//        this.file = file;
//        this.fileVersion = file.getName().substring(file.getName().lastIndexOf("."));
//        if(this.fileVersion.equals(".csv")){
//            // CSV
//            csvData = Utils.CsvUtil.readCsv(file.getPath());
//        }else{
//            //EXCEL
//            this.wb = excl.getWorkbook(file);
//        }
//
//        this.getSheetsRowAndCol();
//    }

    public ExcelEncoder(String f_bytes,String file,double max_allowed_modification,boolean isLong) {
        super(f_bytes);
        this.MODE = isLong;
        this.max_allowed_modi_digits = (int)Math.max(Math.ceil(Math.log10(1/max_allowed_modification)),3);
        System.out.println("[Accepted Modification Length] "+ this.max_allowed_modi_digits);
        this.file = new File(file);
        this.fileVersion = this.file.getName().substring(this.file.getName().lastIndexOf("."));
        if(this.fileVersion.equals(".csv")){
            // CSV
            csvData = Utils.CsvUtil.readCsv(this.file.getPath());
        }else{
            //EXCEL
            this.wb = excl.getWorkbook(this.file);

        }
        this.getSheetsRowAndCol();
        if(this.fileVersion.equals(".csv")){
            // CSV
            csvArray = new String[exclRow[0]][exclCol[0]];
            for(int i=startRow;i<exclRow[0];i++){
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
                    for (int j = startRow; j < exclRow[i]; j++) {
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

    public boolean run(String filePath, String outpath,int[] args) throws Exception{
        for(int arg:args)
            banColList.add(arg);
//        POIXMLProperties.ExtendedProperties expProps = (this.wb).getProperties().getExtendedProperties();
//        (this.wb).getProperties().getCoreProperties().setDescription("Qichao Ying");
        System.out.println("-----------------------------Embedding---------------------------------------");
        try {

//            WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));


            findKeyIndex();
            System.out.println("len: "+(this.solitionGenerator.K/4-1));
//            if(endRow<this.minRequire){
//                throw new Exception("[Error] Not enough valid packages for watermarking! Please shorter the watermark sequence...");
//            }
//            keyIndex = keyCols.get(0);
//            keyCols.remove(0);

            int endRow = exclRow[0];//固定第一个sheet

            if(endRow<this.minRequire){
                throw new Exception("[Error] Not enough valid packages for watermarking! Please shorter the watermark sequence...");
            }

            valid = endRow;

            //Embedment
            for(int i=startRow;i<endRow;i++){
                encoder(i);
            }

//            try {
                if (csvData.size() == 0) {
                    // EXCEL
                    // filesize原来是写在数据中的
//                    this.excl.writeWorkBookAt(this.wb,0, startRow-1, exclCol[0], ((Integer)this.solitionGenerator.K).toString());
                    FileOutputStream out = new FileOutputStream(outpath);

                    this.excl.write2Excel(this.wb, out);
                } else {
                    // CSV
                    for(int i=0;i<exclRow[0];i++){
                        StringBuilder str = new StringBuilder();
                        for(int j=0;j<exclCol[0];j++){
                            str.append(csvArray[i][j]);str.append(',');
                        }
                        str.deleteCharAt(str.length()-1);
                        csvData_embedded.add(str.toString());
                    }

                    // filesize原来是写在数据中的
//                    csvData_embedded.set(startRow-1,csvData.get(startRow-1)+","+this.solitionGenerator.K);
                    CsvUtil.writeCSV(outpath,csvData_embedded);
                }

//                (this.wb).getProperties().getCoreProperties().setDescription(((Integer)this.solitionGenerator.K).toString());
//                ByteBuffer buf = ByteBuffer.allocateDirect(10) ;
//                buf.put(((Integer)this.solitionGenerator.K).toString().getBytes()) ;
//                buf.flip();
//                UserDefinedFileAttributeView userDefined = Files.getFileAttributeView(Paths.get(outpath), UserDefinedFileAttributeView.class);
//                userDefined.write("num_packages",buf);

//

                System.out.println("-----------------Embedding was conducted successfully...--------------------");
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("-----------------[Warning] Embedding was not conducted...--------------------");
            }

            return true;
//        }catch(Exception e){
//            System.out.println("Embedding message into Excel Failed...");
//            return false;
//        }
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
                    if(last_used_redundant==-1 || i-last_used_redundant>=Settings.row_for_water_len*0.75){

                        //需要两个用作嵌入水印长度的冗余行之间空开足够多距离
                        last_used_redundant = i;
                        redundant.add(i);
                        length_need--;
                    }
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

    public String getExactValue(int row,int col){
        if(csvData.size()==0){
            // EXCEL
            int sheet = 0;
            Object str = this.excl.getExactValue(this.wb, sheet, row,col);
            return ((str==null)?"":str).toString();
        }else{
            // CSV
            return csvArray[row][col];
        }
    }

    public boolean encoder(int row) throws Exception{
        //prepare
        List<Integer> list = new LinkedList<>();
//        String name = "";
//        for(int key:keyIndex)
//            name+=getExactValue(row,key);
        String name = keyCol_values.get(row);
        this.seed = Util.BKDRHash(name,131);
//        if(row%Settings.row_for_water_len==0)
//            this.solitionGenerator.setK(Settings.rand_K);
        this.solitionGenerator.setSeed(this.seed);

        int block_data = 0;boolean isNormal;
        if(redundant.contains(row) || (length_need>0 && row%Settings.row_for_water_len==0)) {
            //否则是隐藏水印长度的行，水印长度也由4bit构成
            if(redundant.add(row)){
                length_need--;
            }
            list = this.solitionGenerator.get_src_blocks(null);
            isNormal = Settings.LENGTH;
            block_data = this.solitionGenerator.K/4-1;

        }else{
            //正常嵌入数据的行
            list = this.solitionGenerator.get_src_blocks(null);
            isNormal = Settings.NORMAL;
            for (int i = 2; i < list.size(); i++)
                block_data ^= this.blocks[list.get(i)];
        }

        String crc_text = Util.dec2bin(Utils.cyclic.CyclicCoder.encode(block_data,isNormal,MODE),(!isNormal&&MODE)?Settings.DEFAULT_EMBEDLEN_LONGMODE:Settings.DEFAULT_EMBEDLEN);
        // dynamically embedment: calculate total sum
        // A-Z a-z同样去除

        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>(Util.comparator);
//        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>
//                ((a,b)->(b.getValue().replaceAll("[^A-Za-z0-9]", "").length()-a.getValue().replaceAll("[^A-Za-z0-9]", "").length()));
        int totalLen = 0;List<Integer> eachLen = new LinkedList<>();
        for(int col=0;col<exclCol[0];col++){
            //不是关键列，也不在banList里
            if(!keyIndex.contains(col) && ! banColList.contains(col)) {
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
        int validLen = (!isNormal && MODE)?Settings.DEFAULT_EMBEDLEN_LONGMODE:Settings.DEFAULT_EMBEDLEN;
        if(totalLen<validLen){
            //表示当前行可以用来嵌入信息的总长度不够，一般都不会执行的
            System.out.println("[SKIPPED PACK] Total length of row "+row+" is not enough!");
            valid--;
            if(valid<this.minRequire){
                throw new Exception("[Error] Not enough valid packages for watermarking! Please shorter the watermark sequence...");
            }
            return false;
        }
        //data embedding
        String debug = new String();
        int beginInd = 0;
        while(pq.size()!=0){
            Map.Entry<Integer,String> entry = pq.poll();
            //data embedment according to length of value
            int len = (int)Math.ceil(crc_text.length()*Util.lengthQualify(entry.getValue().toString(),this.max_allowed_modi_digits)/(double)totalLen);

            debug += modify(row,entry.getKey(),entry.getValue(),crc_text.substring(beginInd,Math.min(beginInd+len,crc_text.length())));

            beginInd += len;
            if(beginInd>=crc_text.length())    break;
        }

//        if(row%Settings.row_for_water_len==0)
//            this.solitionGenerator.setK(this.K);
        //if(row%Settings.row_for_water_len!=0)
            System.out.println(((isNormal)?"":">")+"Debug Embed: EmbeddedAt-> "+debug+" origin->"+crc_text+" data->" + Util.bin2dec(crc_text.substring(0,Settings.DEFAULT_DATALEN)) + " ROW: "+row);

        return true;
    }

    public String modify(int row,int col,String value,String waterSeq){
        //modify the value to embed data
        Set<Integer> duplicateSet = new HashSet<>();
//        boolean negative = false;
        StringBuilder newvalue = new StringBuilder();String buffer = "";
        int startFrom = 0;
        //preprocess
        if(Util.isInteger(value)){
            long value_int = Long.parseLong(value);
            //去除前缀0
            while(value.charAt(startFrom)=='-' || value.charAt(startFrom)=='.' || value.charAt(startFrom)=='0'){
//                newvalue.deleteCharAt(0);
                startFrom++;
            }

            //前几位暂存
            buffer = value.substring(0,startFrom+this.max_allowed_modi_digits);
            newvalue = new StringBuilder(value.substring(startFrom+this.max_allowed_modi_digits));

//            first = value.charAt(startFrom);newvalue.deleteCharAt(0);
        }else if(Util.isNumeric(value)){
            double value_double = Double.parseDouble(value);
//            negative = value_double<0;
//            if(negative)    {
////                newvalue.deleteCharAt(0);
//                startFrom++;
//            }

            //去除前缀0
            while(value.charAt(startFrom)=='-' || value.charAt(startFrom)=='.' || value.charAt(startFrom)=='0'){
//                newvalue.deleteCharAt(0);
                startFrom++;
            }

            //前几位暂存
            buffer = value.substring(0,startFrom+this.max_allowed_modi_digits);
            newvalue = new StringBuilder(value.substring(startFrom+this.max_allowed_modi_digits));

//            first = value.charAt(startFrom);newvalue.deleteCharAt(0);
//            dotIndex = newvalue.indexOf(".");
//            if(dotIndex>=0) {
//                newvalue.deleteCharAt(dotIndex);
//            }
        }

//        Set<Integer> duplicateSet = new HashSet<>(0);
        int embedded = 0;

        String crc_text = waterSeq;

        String debug = new String();

        while(embedded<crc_text.length()){
            int num = this.solitionGenerator.get_next() % newvalue.length();

            if(!duplicateSet.contains(num)){
                duplicateSet.add(num);
                char ori = newvalue.charAt(num);

                if ((ori >= 'a' && ori <= 'z') || (ori >= 'A' && ori <= 'Z')) {
                    //对于小写大写字母：统一向上取结果
                    newvalue.setCharAt(num, (char) (ori + ori % 2 - crc_text.charAt(embedded) + '0'));
                    embedded ++;
                }
                else if(ori >= '0' && ori <= '9') {
                    //对于数字：统一向下取结果
                    newvalue.setCharAt(num, (char) (ori - ori % 2 + crc_text.charAt(embedded) - '0'));
                    embedded ++;
                }
                else if(ori == '.'){
                    //遇到.直接跳过
                    continue;
                }
                debug += newvalue.charAt(num);
            }
        }

        //recovery
//        if(first!=null) newvalue.insert(0,first);
//        if(dotIndex!=-1) newvalue.insert(dotIndex,'.');
//        if(negative)    newvalue.insert(0,'-');
        newvalue.insert(0,buffer);


        if(csvData.size()==0) {
            this.excl.writeWorkBookAt(this.wb, 0, row, col, newvalue.toString());
        }else{
            csvArray[row][col] = newvalue.toString();
        }

        return debug;

    }

}