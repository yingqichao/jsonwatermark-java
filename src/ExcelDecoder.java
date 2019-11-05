/**
 * @author Qichao Ying
 * @date 2019/8/12 16:26
 * @Description DEFAULT
 */
import ExcelWatermarkHelper.excel.ExcelUtil;
import GeneralHelper.LtDecoder;
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

import static Setting.Settings.DEFAULT_EMBEDLEN;
import static java.lang.Integer.max;
import static java.lang.Math.min;

public class ExcelDecoder extends AbstractDecoder{

    File file;
    FileOutputStream out;
    XSSFWorkbook wb ;
    String fileVersion;
    ExcelUtil excl = new ExcelUtil();
    List<Integer> keyCols = new LinkedList<>();
    int keyIndex = 0;
    int startRow = 0;


    //for CSV only
    List<String> csvData = new ArrayList<>();

    String[][] csvArray;

    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;
    int filesize = 0;
    Set<Integer> banColList = new HashSet<>();

    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    public ExcelDecoder(File file, int startRow) throws Exception{
        this.file = file;
        this.startRow = startRow;
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
        ByteBuffer buf = ByteBuffer.allocateDirect(10) ;
        UserDefinedFileAttributeView userDefined = Files.getFileAttributeView(Paths.get(file.getPath()), UserDefinedFileAttributeView.class);
        try {
            userDefined.read("num_packages", buf);
        }catch(Exception e){
            throw new Exception("The file does not contain watermark.");
        }
        buf.flip();
        filesize = Integer.parseInt(Charset.defaultCharset().decode(buf).toString());
        if(filesize==0){
            throw new Exception("Filesize equals to 0. Please check!");
        }


        if(fileVersion.equals(".csv")){
            // CSV
//            filesize = Integer.parseInt(csvData.get(startRow-1).split(",")[exclCol[0]]);
            csvArray = new String[exclRow[0]][exclCol[0]];
            for(int i=0;i<exclRow[0];i++){
                String[] strs = csvData.get(i).split(",");
                csvArray[i] = strs;
            }
        }else{
            // EXCEL
//            filesize = Integer.parseInt(getExactValue(startRow-1,exclCol[0]));
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
            }
        }
    }

    public void decode(int row,int filesize){
        String key = getExactValue(row, keyIndex).toString();
        //init src_blocks and key for pseudo-random
//        decoder = new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);
        List<Integer> src_blocks = decoder.getSrcBlocks(filesize,key,1);
        //get dynamically embedment: calculate total sum
        // A-Z a-z同样去除

        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>(Util.comparator);
//        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>
//                ((a,b)->(b.getValue().replaceAll("[^A-Za-z0-9]", "").length()-a.getValue().replaceAll("[^A-Za-z0-9]", "").length()));
        int totalLen = 0;List<Integer> eachLen = new LinkedList<>();
        for(int col=0;col<exclCol[0];col++){
            if(col!=keyIndex && !banColList.contains(col)) {
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
        //data extraction
        if(totalLen<Settings.DEFAULT_EMBEDLEN){
            //表示当前行可以用来嵌入信息的总长度不够，一般都不会执行的
            System.out.println("[SKIPPED PACK] Total length of row "+row+" is not enough!");

            return;
        }
        String debug = new String();
        int remainLen = DEFAULT_EMBEDLEN;int decodeInt = 0;
        while(pq.size()!=0){
            Map.Entry<Integer,String> entry = pq.poll();
            //data embedment according to length of value
            int len = (int)Math.ceil(DEFAULT_EMBEDLEN*Util.lengthQualify(entry.getValue().toString(),3)/(double)totalLen);
            if(remainLen-len<0)
                len = remainLen;

             List<Object> list = decoder.extract_excel(((Integer)row).toString(),entry.getValue(),len);
             int retrieve = (int)list.get(0);
             debug += (String)list.get(1);


            remainLen -= len;

            retrieve <<= Math.max(0,remainLen);

            decodeInt += retrieve;

            if(remainLen<=0)    break;
        }
        int real_embed_data = decodeInt>>(Settings.DEFAULT_EMBEDLEN-Settings.DEFAULT_DATALEN);

        System.out.println("Debug Extract: EmbeddedAt-> "+debug+"  origin->"+ decodeInt+" data->" + real_embed_data + " sourceBlock->" + src_blocks.get(0) + " ROW: "+row);

        if (Utils.cyclic.CyclicCoder.decode(decodeInt) != -1) {
            System.out.println("Valid Package.");

            if(decoder.consume_block_excel(src_blocks,real_embed_data)) {
                decoder.received_packs++;
                if (decoder.is_done()) {
                    success_time++;
                    System.out.println("--> Decoded Successfully <--... The ExcelWatermarkHelper is now successfully retrieved. Time: " + success_time);
                    List<Integer> buff = decoder.bytes_dump();

                    List<String> res = Utils.StrBinaryTurn.stream2String(buff);
                    //分别保存中英文的可能结果
                    secret_data.add(res.get(0));
                    secret_data_chinese.add(res.get(1));
                    decoder.succeed_and_init();
                } else {
                    System.out.println("Need more Packs...Received: " + decoder.received_packs);
                }
            }
        }else{
            System.out.println("Invalid Package.Skipped...");
        }

    }

    public int findKeyIndex() throws Exception{
        //第一个数是作为键值的
        int keyCol = -1;int firstThresh = -1;
        int sheetIndex = 0;//当前只允许嵌入在一页里，不考虑多页的情况
        double thresh = 0.5;int valid = 0;double maxMatch = 0;double firstMatch = 0;

        for(int colIndex = 0; colIndex < this.exclCol[sheetIndex]; colIndex++){
            Set<String> objCol = new HashSet<>();Set<String> objColwithoutLen = new HashSet<>();
            List<Object> col = new LinkedList<>();
            if(csvData.size()==0) {
                // EXCEL
                col = this.excl.getColValues(this.wb, sheetIndex, colIndex, 20);
            }else{
                // CSV
                for(int i=startRow;i<exclRow[0];i++)
                    //每一行的长度可能不一样
                    if(csvArray[i].length>colIndex)
                        col.add(csvArray[i][colIndex]);
            }
            for(Object object:col){
                if(object==null)
                    continue;
                int validLen = object.toString().replaceAll("[^A-Za-z0-9]","").length();
//                totalLen += validLen;
//                if(validLen <= Setting.Settings.DEFAULT_MINLEN_EXCEL)
                    //不足以嵌入信息，并且当前value没有出现过
                objCol.add(object.toString());

                objColwithoutLen.add(object.toString());
            }

            double match = ((double)objCol.size())/col.size();
            if(match>=thresh){
                valid++;
                if(match>maxMatch){
                    keyCol = colIndex;maxMatch = match;
                }
            }
            double match1 = ((double)objColwithoutLen.size())/col.size();
            if(match<thresh && match1>=thresh && firstThresh==-1){
                firstThresh = colIndex;
                firstMatch = match1;
            }
        }

        if(keyCol==-1){
//            System.out.println("[Warning] Using a longer key");
            throw new Exception("[Warning] No valid key index found...Extraction was aborted...");
//            keyCol = firstThresh;
//            maxMatch = firstMatch;

        }


        System.out.println("The selected col is COL: "+keyCol+" with maxMatch "+maxMatch);


        return keyCol;
    }

    public void run(String filePath,int[] args) throws Exception{
        for(int arg:args)
            banColList.add(arg);
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        System.out.println("-----------------------------Extraction---------------------------------------");


//        WatermarkUtils watermarkUtils = new WatermarkUtils(new File(filePath));
        keyIndex = findKeyIndex();

        //Embedment
        decoder = new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);
        for(int i=startRow;i<exclRow[0];i++){//固定第一个sheet
            //Embedment;i++){
            decode(i, filesize);
        }

//        return this.secret_data;
    }

}
