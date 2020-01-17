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


    int keyIndex = 0;

    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;
    int startRow = 0;
    int max_allowed_modi_digits = 3;
    Set<Integer> banColList = new HashSet<>();


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

    public ExcelEncoder(String f_bytes,String file,int startRow,double max_allowed_modification) {
        super(f_bytes);
        this.max_allowed_modi_digits = (int)Math.max(Math.ceil(Math.log10(1/max_allowed_modification)),3);
        System.out.println("[Accepted Modification Length] "+ this.max_allowed_modi_digits);
        this.startRow = startRow;
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

    public boolean run(String filePath, String outpath,int[] args) throws Exception{
        for(int arg:args)
            banColList.add(arg);
//        POIXMLProperties.ExtendedProperties expProps = (this.wb).getProperties().getExtendedProperties();
//        (this.wb).getProperties().getCoreProperties().setDescription("Qichao Ying");
        System.out.println("-----------------------------Embedding---------------------------------------");
        try {

//            WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));


            keyIndex = findKeyIndex();
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

    public int findKeyIndex() throws Exception{
        //第一个数是作为键值的
        int keyCol = -1;//int firstThresh = -1;
        int sheetIndex = 0;//当前只允许嵌入在一页里，不考虑多页的情况
        double thresh = 0.0;//int valid = 0;
        double maxMatch = 0;//double firstMatch = 0;
        double matchLen = 0;

        for(int colIndex = 0; colIndex < this.exclCol[sheetIndex]; colIndex++){
            Set<String> objCol = new HashSet<>();Set<String> objColwithoutLen = new HashSet<>();
            List<Object> col = new LinkedList<>();
            if(csvData.size()==0) {
                // EXCEL
                col = this.excl.getColValues(this.wb, sheetIndex, colIndex, 20);
            }else{
                // CSV
                for(int i=startRow;i<exclRow[0];i++)
                    col.add(csvArray[i][colIndex]);
            }
            for(Object object:col){
                if(object==null)
                    continue;
                int validLen = object.toString().replaceAll("[^A-Za-z0-9]","").length();
//                totalLen += validLen;
                if(validLen >= Setting.Settings.DEFAULT_MINLEN_EXCEL)
                    //不足以嵌入信息，并且当前value没有出现过
                    objCol.add(object.toString());
                objColwithoutLen.add((object.toString().length()<=this.max_allowed_modi_digits)?object.toString():object.toString().substring(0,this.max_allowed_modi_digits));
            }

            double match = ((double)objColwithoutLen.size())/col.size();double matchWithLen = ((double)objCol.size())/col.size();
            //maxMatch = Math.max(maxMatch,match);
            if(match>=thresh && match>=maxMatch){

                if(match>maxMatch || matchWithLen<matchLen){
                    matchLen = matchWithLen;
                    keyCol = colIndex;
                }

                maxMatch = match;
            }

//            if(match<thresh && match1>=thresh && firstThresh==-1){
//                firstThresh = colIndex;
//                firstMatch = match1;
//            }
        }

        if(keyCol==-1){
            System.out.println("[Warning] Using a longer key");
            throw new Exception("[Warning] No valid key index found...Embedding was aborted...");
//            keyCol = firstThresh;
//            maxMatch = firstMatch;

        }


        System.out.println("The selected col is COL: "+keyCol+" with maxMatch "+maxMatch);


        return keyCol;
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
        this.seed = Util.BKDRHash(getExactValue(row,keyIndex),131);
//        if(row%Settings.row_for_water_len==0)
//            this.solitionGenerator.setK(Settings.rand_K);
        this.solitionGenerator.setSeed(this.seed);

        int block_data = 0;int key = 0;
        if(row%Settings.row_for_water_len!=0) {
            //正常嵌入数据的行
            list = this.solitionGenerator.get_src_blocks(null);
            key = Settings.DATA_POLYNOMIAL;
            for (int i = 2; i < list.size(); i++)
                block_data ^= this.blocks[list.get(i)];
        }else{
            //否则是隐藏水印长度的行，水印长度也由4bit构成
            list = this.solitionGenerator.get_src_blocks(null);
            key = Settings.KEY_POLYNOMIAL;
            block_data = this.solitionGenerator.K/2-1;
        }

        String crc_text = Util.dec2bin(Utils.cyclic.CyclicCoder.encode(block_data,key),Settings.DEFAULT_EMBEDLEN);
        // dynamically embedment: calculate total sum
        // A-Z a-z同样去除

        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>(Util.comparator);
//        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>
//                ((a,b)->(b.getValue().replaceAll("[^A-Za-z0-9]", "").length()-a.getValue().replaceAll("[^A-Za-z0-9]", "").length()));
        int totalLen = 0;List<Integer> eachLen = new LinkedList<>();
        for(int col=0;col<exclCol[0];col++){
            //不是关键列，也不在banList里
            if(col!=keyIndex && ! banColList.contains(col)) {
                String str = getExactValue(row, col);
                if(Util.isNumeric(str)) {//Util.isInteger(str) &&
                    //新规定要求只能在float或者int中嵌入数据
                    //数据为0不做嵌入，且修改幅度不可以超过0.05，也即前两位不考虑嵌入
                    if(Double.parseDouble(str)!=0 && Util.lengthQualify(str,3)>0) {
                        totalLen += Util.lengthQualify(str,3);
                        pq.offer(new AbstractMap.SimpleEntry<>(col, str));
                    }
                }
            }
        }
        if(totalLen<Settings.DEFAULT_EMBEDLEN){
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
            int len = (int)Math.ceil(crc_text.length()*Util.lengthQualify(entry.getValue().toString(),3)/(double)totalLen);

            debug += modify(row,entry.getKey(),entry.getValue(),crc_text.substring(beginInd,Math.min(beginInd+len,crc_text.length())));

            beginInd += len;
            if(beginInd>=crc_text.length())    break;
        }

//        if(row%Settings.row_for_water_len==0)
//            this.solitionGenerator.setK(this.K);
        //if(row%Settings.row_for_water_len!=0)
            System.out.println(((row%Settings.row_for_water_len==0)?">":"")+"Debug Embed: EmbeddedAt-> "+debug+" origin->"+crc_text+" data->" + Util.bin2dec(crc_text.substring(0,Settings.DEFAULT_DATALEN)) + " ROW: "+row);

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