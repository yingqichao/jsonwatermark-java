package watermark.excel;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;


import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static java.lang.Math.min;

public class ExcelUtil {

    /*
     * 获得文件流 file的 Workbook，兼容处理2003、2007两个版本的Excel
     * @param file : 需要读取的文件流
     * @return : file的workbook
     */
    public static Workbook getWorkbook(File file){
        try {
            String fileName = file.getName();
            String extName = fileName.substring(fileName.lastIndexOf("."));
            Workbook wb = null;
            FileInputStream excelFileInputStream = new FileInputStream(file);
            if (ExcelVersion.V2003.getSuffix().equals(extName)) {
                wb = new HSSFWorkbook(excelFileInputStream);
            } else if (ExcelVersion.V2007.getSuffix().equals(extName)) {
                wb = new XSSFWorkbook(excelFileInputStream);
            }
            excelFileInputStream.close();
            return wb;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 提取cell
     * @param wb : workbook
     * @param cell : 数据元
     * @return : 提取得到的cell中的信息
     */
    private static Object getCellValue(Workbook wb, Cell cell) {
        Object columnValue = null;
        if (cell != null) {
            DecimalFormat df = new DecimalFormat("0");// 格式化 number
            // String
            // 字符
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
            DecimalFormat nf = new DecimalFormat("0");// 格式化数字
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    columnValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                        columnValue = cell.getNumericCellValue();
//                        columnValue = df.format(cell.getNumericCellValue());
                    } else if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                        columnValue = cell.getNumericCellValue();
//                        columnValue = nf.format(cell.getNumericCellValue());
                    } else {
                        columnValue = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    columnValue = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    columnValue = "";
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    // 格式单元格
                    FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                    evaluator.evaluateFormulaCell(cell);
                    CellValue cellValue = evaluator.evaluate(cell);
                    columnValue = cellValue.getNumberValue();
                    break;
                default:
                    columnValue = cell.toString();
            }
        }
        return columnValue;
    }


    /*
     * 将 workbook wb 写入文件流 file 中
     * @param wb : 需要保存的workbook
     * @param file : 需要写入的文件file流
     */
    public static void write2Excel(Workbook wb, File file){
        try {
            OutputStream outstream = new FileOutputStream(file);
            wb.write(outstream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /*
     * 读取某一列的元数据
     * @param wb : woorkbook
     * @param sheetIndex : sheet的索引
     * @param posCol : 需要读取的浮点数列索引
     * @return : 提取出的列元数据
     */
    public List<Object> getColValues(Workbook wb, int sheetIndex, int posCol){
        int row = wb.getSheetAt(sheetIndex).getPhysicalNumberOfRows();
        List<Object> colValue = new LinkedList<Object>();

        for(int i = 0; i < row; i++) {
            Cell cell = wb.getSheetAt(sheetIndex).getRow(i).getCell(posCol);
            colValue.add(getCellValue(wb, cell));
        }
        return colValue ;
    }

    /*
     * 读取某一列的前cellNum个元数据
     * @param wb : woorkbook
     * @param sheetIndex : sheet的索引
     * @param posCol : 需要读取的浮点数列索引
     * @param cellNum : 需要读取的前cellNum行元数据
     * @return : 提取出的前cellNum个元数据
     */
    public List<Object> getColValues(Workbook wb, int sheetIndex, int posCol, int cellNum){
        int row = min(wb.getSheetAt(sheetIndex).getPhysicalNumberOfRows(), cellNum);
        List<Object> colValue = new LinkedList<Object>();

        for(int i = 0; i < row; i++) {
            Cell cell = wb.getSheetAt(sheetIndex).getRow(i).getCell(posCol);
            colValue.add(getCellValue(wb, cell));
        }
        return colValue ;
    }

    /*
     * 对Excel指定位置进行修改
     * @param wb : woorkbook
     * @param sheetIndex : sheet的索引
     * @param posRow : 行索引
     * @param posCol : 列索引
     * @param val : 需要赋值的值
     */
    public static void writeWorkBookAt(Workbook wb, int sheetIndex, int posRow, int posCol, String val){
        Cell cell = wb.getSheetAt(sheetIndex).getRow(posRow).getCell(posCol);
        cell.setCellValue(val);
    }
}