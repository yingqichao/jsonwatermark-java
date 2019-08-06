package watermark.utils;

import watermark.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Math.min;

public class WatermarkUtils {
    File file;
    Workbook wb ;
    String fileVersion;
    ExcelUtil excl = new ExcelUtil();
    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;

    public WatermarkUtils(File file){
        this.file = file;
        this.fileVersion = file.getName().substring(file.getName().lastIndexOf("."));
        this.wb = excl.getWorkbook(file);
        this.getSheetsRowAndCol();
    }


    /*
     * 对某一个sheet的某一列进行提取
     * @param sheetNum : sheet的索引
     * @param colNo : 需要提取的浮点数列索引
     * @param wmLen : 水印长度
     * @return : 返回提取的水印信息
     */
    public List<Integer> extractFromOneCol(int sheetNum, int colNo, int wmLen){
        List<Object> colv = this.excl.getColValues(this.wb, sheetNum, colNo);
        Utils ut = new Utils();
        List<List<Integer>> wms = new LinkedList<>();
        List<Integer> pt = new LinkedList<>();
        int wmIntLen = (int)(wmLen / 3) + ((wmLen%3 == 0) ? 0 : 1);

        // 跳过不是浮点数的行
        int k = 0;
        for ( Object t : colv ) {
            if (null != t) {
                if((ut.isDouble(colv.get(k).toString()))){
                    break;
                }
            }
            k++;
        }

        //将89之间的数据当作备选水印保存
        boolean flag = false;
        int wmsC = -1;
        for(int i = k; i < colv.size(); i++){
            String s = colv.get(i).toString();
            if(!(ut.isDouble(s))){
                break;
            }
            int t = Integer.valueOf(s.substring(s.length()-1));
            if(t == 8){
                flag = true;
                wmsC++;
                wms.add(new LinkedList<>());
            }else if(t == 9){
                flag = false;
            }else{
                if (flag == true){
                    wms.get(wmsC).add(t);
                }
            }
        }

        //删除不满足容量的块
        int cnt = 0;
        for(int i = 0; i < wms.size(); i++){
            if(wms.get(i).size() != wmIntLen+2){
                wms.remove(i);
                i--;
                cnt++;
            }
            else{
                int seed = wms.get(i).get(0) + wms.get(i).get(1)*10;
                wms.get(i).remove(0);
                wms.get(i).remove(0);
                // 置乱
                List<Integer> indx = new ArrayList<>();
                List<Integer> extWmInt = new ArrayList<>();
                for(int m = 0; m < wms.get(i).size(); m++){
                    indx.add(m);
                    extWmInt.add(0);
                }
                Random rand = new Random(seed);
                Collections.shuffle(indx, rand);

                for(int m = 0; m < wms.get(i).size(); m++){
                    extWmInt.set(indx.get(m), wms.get(i).get(m));
                }
                List<Integer> tmp = this.int2Bin(extWmInt, wmLen);
                wms.get(i).clear();
                wms.get(i).addAll(tmp);
            }
        }

//        System.out.println(cnt + " watermark pattern is invalied");

        // 判断完整水印阈值
        //int th = (int)(colv.size() / (wmIntLen+3) * 0.3);
        // 投票
        if(wms.size() > 0){
            if(wms.size() < 5){ // 没有水印
                pt.clear();
            }else{
                // 水印投票
                pt.addAll(wms.get(0));
                for(int i = 1; i < wms.size(); i++){
                    for(int j = 0; j < wms.get(i).size(); j++){
                        pt.set(j, pt.get(j)+wms.get(i).get(j));
                    }
                }
                for(int j = 0; j < pt.size(); j++){
                    double t = (double)pt.get(j)/ (double)wms.size();
                    pt.set(j, (int) Math.ceil(t));
                }
            }
        }

        return  pt;
    }

    /*
     * 对某一个sheet的某一列进行嵌入
     * @param sheetNum : sheet的索引
     * @param colNo : 需要提取的浮点数列索引
     * @param wmInt : 十进制的水印信息
     */
    public void embed2OneCol(int sheetNum, int colNo, List<Integer> wmIntO, int seed){
        int rown = exclRow[sheetNum];
        int ptLen = wmIntO.size()+2+2;
        List<Object> colv = this.excl.getColValues(this.wb, sheetNum, colNo);
        Utils ut = new Utils();

        // 置乱
        List<Integer> indx = new ArrayList<>();
        for(int i = 0; i < wmIntO.size(); i++){
            indx.add(i);
        }
        Random rand = new Random(seed);
        Collections.shuffle(indx, rand);
        List<Integer> wmInt = new ArrayList<>();
        for(int i = 0; i < wmIntO.size(); i++){
            wmInt.add(wmIntO.get(indx.get(i)));
        }
        wmInt.add(0,seed/10);
        wmInt.add(0,seed%10);

        List<Integer> wm = this.addHeader(wmInt);

        // 跳过不是浮点数的行
        int k = 0;
        for ( Object t : colv ) {
            if (null != t) {
                if((ut.isDouble(colv.get(k).toString()))){
                    break;
                }
            }
            k++;
        }

        // 将水印嵌入到workbook指定位置
        for(int i = k; i < rown; i++){
            String t = colv.get(i).toString() + wm.get((i-k)%wm.size());
            this.excl.writeWorkBookAt(this.wb,sheetNum, i, colNo, t);
        }
        this.excl.write2Excel(this.wb, this.file);
    }

    /*
     * 添加定位信息，将十进制水印放置到8和9之间
     * @param pt : 水印
     * @param wmInt : 返回包含定位信息的水印
     */
    public List<Integer> addHeader(List<Integer> pt){
        int len = pt.size() + 2;
        List<Integer> newPt = new LinkedList<>();

        for(int i = 0; i < len; i++){
            if(i == 0){
                newPt.add(8);
            }
            else if(i == len-1)
                newPt.add(9);
            else
                newPt.add(pt.get(i-1));
        }

        return newPt;
    }


    /*
     * 十进制信息转二进制
     * @param intWm: 十进制水印
     * @param wmLen : 二进制水印长度
     * @retuen : 二进制水印
     */
    public List<Integer> int2Bin(List<Integer> intWm, int wmLen){ //
        List<Integer> wm = new LinkedList<>();

        for(int i = 0; i < intWm.size(); i++){
            int t = intWm.get(i);
            int idx = wm.size();
            for (int j = 0; j < 3; j++){
                if(t == 0)
                    wm.add(idx,0);
                else{
                    wm.add(idx,t % 2);
                    t /= 2;
                }
            }
        }

        int s = wm.size();
        if(s > wmLen){
            int over = s - wmLen;
            if(over == 1)
                wm.remove(s - 3);
            else if(over == 2){
                wm.remove(s - 3);
                wm.remove(s - 3);
            }
        }

        return wm;
    }


    /*
     * 二进制信息转十进制
     * @param binWm: 二进制水印
     * @retuen : 十进制水印
     */
    public List<Integer> bin2Int(List<Integer>  binWm){ // 将二进制数转换成十进制数：十进制范围是0-8
        List<Integer> wm = new LinkedList<>();

        for(int i = 0; i < binWm.size(); i+=3){
            int maxShift = min(3, binWm.size()-i);
            int wm_t = 0;
            for(int j = 0; j < maxShift; j++){
                wm_t = wm_t * 2 + binWm.get(i+j);
            }
            wm.add(wm_t);
        }

        return wm;
    }

    /*
     * 生成随机数
     * @param seed: 随机种子
     * @param length : 生成随机数长度
     * @param bound : 生成随机数边界( 每个随机数 < Bound)
     * @retuen : 生成的随机数
     */
    public static List<Integer> geneRandom(long seed, int length, int bound){
        Random rand = new Random(seed);
        List<Integer> wm = new LinkedList<>();
        for(int i = 0; i < length; i++){
            wm.add(rand.nextInt(bound));
        }
        return wm;
    }

    /*
     * 寻找所有可嵌入的浮点数列
     * @retuen : 所有sheet可以嵌入的浮点数列的索引
     */
    public List<List<Integer>> findEmbeddingCols(String[] Keys){
        List<List<Integer>> validCols = new LinkedList<>();
        for(int sheetIndex = 0; sheetIndex < this.sheetNum; sheetIndex++){
            validCols.add(findEmbeddingCol(sheetIndex, Keys));
        }
        return validCols;
    }

    /*
     * 在一个sheet中寻找可以嵌入的浮点数列
     * @param sheetIndex : sheet 索引
     * @retuen : 当前sheet的可嵌入列索引
     */
    private List<Integer> findEmbeddingCol(int sheetIndex, String[] Keys){
        List<Integer> objCol = new LinkedList<Integer>();
        for(int colIndex = 0; colIndex < this.exclCol[sheetIndex]; colIndex++){
            List<Object> col = this.excl.getColValues(this.wb, sheetIndex, colIndex, 20);
            if(isEmbeddingCol(col, Keys)){
                objCol.add(colIndex);
            }
        }
        return objCol;
    }

    /*
     * 判断当前列是否可嵌入
     * @param col: 列元数据
     * @retuen : 是否可以嵌入，true表示可以，false不可以
     */
    private static boolean isEmbeddingCol(List<Object> col, String [] Keys){
        boolean isOk = true;
        Utils ut = new Utils();

        if ( col.size() < 1){
            isOk = false;
        } else{
            int k =  0;
            for ( Object t : col ){
                // 包含 id name time phone date 的列不作嵌入
                if(null != t){
                    int isEmbed = 0;
                    for(int i = 0; i < Keys.length; i++){
                        isEmbed = isEmbed + (t.toString().toLowerCase().contains(Keys[i]) == true ? 1 : 0);
                    }
                    if (isEmbed != 0) {
                        isOk = false;
                        return isOk;
                    }else if ( ( ut.isDouble(t.toString()) ) )
                    {
                        break;
                    }
                }
                k++;
            }
            if (k == col.size()){
                isOk = false;
                return isOk;
            }
            int m = 0;
            for(Object t : col){
                if (m < k){
                    m++;
                }else{
                    boolean a = ut.isDouble(t.toString());
                    if(null == t || !( ut.isDouble(t.toString())) ){ // 含有空白cell或者非浮点数的行，则当前列也不作嵌入
                        isOk = false;
                        break;
                    }
                }
            }
        }

        return  isOk;
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


    /*
     * 字符串转二进制
     * @param s: 字符串信息
     * @retuen : 信息的二进制形式
     */
    public List<Integer> Bytes2Bin(byte[] s){
        List<Integer> msg = new LinkedList<>();

        for(int i = 0; i < s.length; i++){
            byte b = s[i];
            for(int j = 0; j < 8; j++)
                msg.add( (b>>(7-j) ) & 0x01 );
        }

        return msg;
    }

    /*
     * 二进制信息转字符串
     * @param msg: 二进制信息
     * @retuen : 信息的字符串形式
     */
    public byte[] Bin2Bytes(List<Integer> msg){
        byte[] str = new byte[msg.size()/8];

        for(int i = 0; i < msg.size(); i+=8){
            byte t = 0;
            for(int j = 0; j < 8; j++){
                t = (byte)((t << 1) + msg.get(i+j));
            }
            str[i/8] = t;
        }

        return str;
    }

    /*
     * 二进制信息转字符串
     * @param msg: 二进制信息
     * @retuen : 信息的字符串形式
     */
    public String Bin2String(List<Integer> msg){
        byte[] b = Bin2Bytes(msg);
        String str = null;
        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        char[] str = new char[msg.size()/8];
//
//        for(int i = 0; i < msg.size(); i+=8){
//            char t = 0;
//            for(int j = 0; j < 8; j++){
//                t = (char) ((t << 1) + msg.get(i+j));
//            }
//            str[i/8] = t;
//        }

        return str;
    }

    /*
     * 字符串转二进制
     * @param s: 字符串信息
     * @retuen : 信息的二进制形式
     */
    public List<Integer> String2Bin(String s){
        List<Integer> msg = null;
        try {
            msg = Bytes2Bin(s.getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        for(int i = 0; i < s.length(); i++){
//            char b = s.charAt(i);
//            for(int j = 0; j < 8; j++)
//                msg.add( (b>>(7-j) ) & 0x01 );
//        }

        return msg;
    }

    public int[] getRows(){
        return this.exclRow;
    }

    public int[] getCols(){
        return this.exclCol;
    }

    public int getSheetNum(){
        return this.sheetNum;
    }

    public String getSheetName(int sheetIndex){
        return this.wb.getSheetName(sheetIndex);
    }
}
