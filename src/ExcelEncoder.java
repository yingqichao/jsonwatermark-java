import Setting.Settings;
import org.apache.poi.ss.usermodel.Workbook;
import watermark.excel.ExcelUtil;
import watermark.utils.Utils;
import watermark.utils.WatermarkUtils;

import java.io.File;
import java.util.*;

import Utils.Util;

import static java.lang.Integer.max;
import static java.lang.Math.min;

public class ExcelEncoder extends Encoder{

    File file;
    Workbook wb ;
    String fileVersion;
    ExcelUtil excl = new ExcelUtil();
    List<Integer> keyCols = new LinkedList<>();
    int keyIndex = 0;

    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;

    public ExcelEncoder(int seed, double c, double delta, String f_bytes,File file) {
        super(seed, c, delta, f_bytes);
        this.file = file;
        this.fileVersion = file.getName().substring(file.getName().lastIndexOf("."));
        this.wb = excl.getWorkbook(file);
        this.getSheetsRowAndCol();
    }

    public ExcelEncoder(String f_bytes,File file) {
        super(f_bytes);
        this.file = file;
        this.fileVersion = file.getName().substring(file.getName().lastIndexOf("."));
        this.wb = excl.getWorkbook(file);
        this.getSheetsRowAndCol();
    }

    /*
     * 获得workbook的最大行列
     */
    private void getSheetsRowAndCol(){
        this.sheetNum = this.wb.getNumberOfSheets();
        exclRow = new int[this.sheetNum];
        exclCol = new int[this.sheetNum];
        for(int i = 0; i < this.sheetNum; i++){
            exclRow[i] = this.wb.getSheetAt(i).getPhysicalNumberOfRows();
            if(0 != exclRow[i]){
                for(int j = 0; j < min(exclRow[i], 20); j++){
                    exclCol[i] =  max(this.wb.getSheetAt(i).getRow(j).getPhysicalNumberOfCells(), exclCol[i]);
                }
            }
        }
    }

//    public void embed2OneRow(int colNo){
//        int rown = exclRow[0];//固定第一个sheet
//
//        List<Object> rowv = this.excl.getRowValues(this.wb, sheetNum, colNo);
//        Utils ut = new Utils();
//
////        // 跳过不是浮点数的行
////        int k = 0;
////        for ( Object t : rowv ) {
////            if (null != t) {
////                if((ut.isDouble(rowv.get(k).toString()))){
////                    break;
////                }
////            }
////            k++;
////        }
//
//        // 将水印嵌入到workbook指定位置
////        for(int i = k; i < rown; i++){
////            String t = colv.get(i).toString() + wm.get((i-k)%wm.size());
////            this.excl.writeWorkBookAt(this.wb,sheetNum, i, colNo, t);
////        }
////        this.excl.write2Excel(this.wb, this.file);
//    }

    /*
     * 对filePath的Excel文件的所有sheet中的浮点数列进行嵌入
     * @param filePath : Excel 文件路径
     * @param wmBin : 二进制的水印信息
     * @return : 返回嵌入的水印信息长度，嵌入失败返回 -1
     */
    public boolean run(String filePath,int startRow){

//        try {


            WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));


            keyIndex = embeddingUint.findKeyIndex();
//            keyIndex = keyCols.get(0);
//            keyCols.remove(0);

            int endRow = exclRow[0];//固定第一个sheet

            //Embedment
            for(int i=startRow;i<endRow;i++){
                encoder(i);
            }

            System.out.println("Embedding message into Excel Succeeded...");

            return true;
//        }catch(Exception e){
//            System.out.println("Embedding message into Excel Failed...");
//            return false;
//        }
    }

    public boolean encoder(int row){
        //prepare
        int sheet = 0;
        this.seed = Util.BKDRHash(this.excl.getExactValue(this.wb, sheet, row,keyIndex).toString(),131);
        this.solitionGenerator.setSeed(this.seed);
        List<Integer> list = this.solitionGenerator.get_src_blocks(null);
        int block_data = 0;
        for(int i=2;i<list.size();i++)
            block_data ^= this.blocks[list.get(i)];

        String crc_text = Util.dec2bin(cyclic.CyclicCoder.encode(block_data),Settings.DEFAULT_EMBEDLEN);
        // dynamically embedment: calculate total sum
        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>((a,b)->(b.getValue().length()-a.getValue().length()));
        int totalLen = 0;List<Integer> eachLen = new LinkedList<>();
        for(int col=0;col<exclCol[0];col++){
            if(col!=keyIndex) {
                String str = this.excl.getExactValue(this.wb, sheet, row, col).toString().replaceAll("[^A-Za-z0-9]", "");
                totalLen += str.length();
                pq.offer(new AbstractMap.SimpleEntry<>(col,str));
            }
        }
        if(totalLen<Settings.DEFAULT_MINLEN){
            System.out.println("[SKIPPED PACK] Total length of row "+row+" is not enough!");
            return false;
        }
        //data embedding
        int beginInd = 0;
        while(pq.size()!=0){
            Map.Entry<Integer,String> entry = pq.poll();
            //data embedment according to length of value
            int len = (int)Math.ceil(crc_text.length()*entry.getValue().toString().length()/(double)totalLen);

            String modified = modify(row,entry.getKey(),entry.getValue(),crc_text.substring(beginInd,Math.min(beginInd+len,crc_text.length())));

            beginInd += len;
            if(beginInd>=crc_text.length())    break;
        }

        this.excl.write2Excel(this.wb, this.file);

        return true;
    }

    public String modify(int row,int col,String value,String waterSeq){
        //modify the value to embed data
        Set<Integer> duplicateSet = new HashSet<>();
        Character first = null;boolean negative = false;int dotIndex = -1;
        StringBuilder newvalue = new StringBuilder(value);
        int startFrom = 0;
        //preprocess
        if(Util.isInteger(value)){
            long value_int = Long.parseLong(value);
            negative = value_int<0;
            if(negative)  {
                newvalue.deleteCharAt(startFrom);
                startFrom++;
            }
            first = value.charAt(startFrom);newvalue.deleteCharAt(0);
        }else if(Util.isNumeric(value)){
            double value_double = Double.parseDouble(value);
            negative = value_double<0;
            if(negative)    {
                newvalue.deleteCharAt(0);
                startFrom++;
            }
            first = value.charAt(startFrom);newvalue.deleteCharAt(0);
            dotIndex = newvalue.indexOf(".");
            newvalue.deleteCharAt(dotIndex);
        }

//        Set<Integer> duplicateSet = new HashSet<>(0);
        int embedded = 0;

//        String crc_text = Util.dec2bin(cyclic.CyclicCoder.encode(waterSeq),Settings.DEFAULT_EMBEDLEN);
        String crc_text = waterSeq;

        int debug = 0;

        while(embedded<crc_text.length()){
            int num = this.solitionGenerator.get_next() % newvalue.length();
            if(embedded==0)
                debug = num;
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
                //其他的字符全都直接跳过
            }
        }

        //recovery
        if(dotIndex!=-1) newvalue.insert(dotIndex,'.');
        if(first!=null) newvalue.insert(0,first);
        if(negative)    newvalue.insert(0,'-');


        this.excl.writeWorkBookAt(this.wb,0, row, col, newvalue.toString());

        System.out.println("Debug Embed: data->" + waterSeq + " seed->" + debug +" ROWNUMBER: " + row + " " + newvalue);
        return newvalue.toString();

    }

    /*
     * 对filePath的Excel文件的指定sheet中的前几个浮点数列进行嵌入
     * @param filePath : Excel 文件路径
     * @param wmBin : 二进制的水印信息
     * @param sheetIndex : 需要嵌入的sheet的索引下标，从0开始
     * @param embedColNum : 需要嵌入的浮点数列的个数，选择最前面的开始嵌入
     * @return : 返回嵌入的水印信息长度，嵌入失败返回 -1
     */
//    public int embed(String filePath, String wmStr, int sheetIndex, int embedColNum, String [] Keys){
//        int msgLen = -1;
//
//        WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));
//        List<Integer> wmBin = embeddingUint.String2Bin(wmStr);
//        List<Integer> wmInt = embeddingUint.bin2Int(wmBin);
//        //int msgLen = wmBin.size();
//        //List<Integer> wmInt = embeddingUint.bin2Int(wmBin); // 将二进制水印转化成数字，方便嵌入
//        msgLen = wmBin.size();
//
//        List<List<Integer>> validCol = embeddingUint.findEmbeddingCols(Keys);
//        for(int col = 0; col < min(embedColNum, validCol.get(sheetIndex).size()); col++){
//            embeddingUint.embed2OneCol(sheetIndex, validCol.get(sheetIndex).get(col), wmInt, 1);
//        }
//
//        return  msgLen;
//    }
//
//    public List<Integer> getRandomMsg(int seed, int length){
//        return WatermarkUtils.geneRandom(seed, length, 2);
//    }



}