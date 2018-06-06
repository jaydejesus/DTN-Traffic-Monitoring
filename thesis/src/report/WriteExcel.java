package report;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import core.DTNHost;
import core.TripProperties;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class WriteExcel {

    private WritableCellFormat timesBoldUnderline;
    private WritableCellFormat times;
    private String inputFile;

    private Workbook workbook;
    private WorkbookSettings wbSettings;
    private WritableWorkbook writableWorkbook; 
    private WritableSheet excelSheet;
    private File file;
    public void setOutputFile(String inputFile) {
    	this.inputFile = inputFile;
    }

    public void initialize(List<String> headers) throws IOException, WriteException, BiffException {
        file = new File(inputFile);
        wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "EN"));

        if(file.exists()) {
        	workbook = Workbook.getWorkbook(file, wbSettings);
        	writableWorkbook = Workbook.createWorkbook(file, workbook);
        	excelSheet = writableWorkbook.getSheet(0);
        	initLabel(excelSheet);
        }
        else {
        	writableWorkbook = Workbook.createWorkbook(file, wbSettings);
        	writableWorkbook.createSheet("Report", 0);
        	excelSheet = writableWorkbook.getSheet(0);
        	initLabel(excelSheet);
        	createLabel(excelSheet, headers);
        }
        
//        excelSheet = writableWorkbook.getSheet(0);
        
        

    }

    public void write() throws IOException, WriteException {
    	writableWorkbook.write();
        writableWorkbook.close();
    }
    
    public WritableSheet getExcelSheet() {
    	return excelSheet;
    }
    
    private void initLabel(WritableSheet sheet) throws WriteException {
    	// Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        // Define the cell format
        times = new WritableCellFormat(times10pt);
        // Lets automatically wrap the cells
        times.setWrap(true);

        // create create a bold font with unterlines
        WritableFont times10ptBoldUnderline = new WritableFont(
                WritableFont.TIMES, 10, WritableFont.BOLD, false,
                UnderlineStyle.SINGLE);
        timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
        // Lets automatically wrap the cells
        timesBoldUnderline.setWrap(true);

        CellView cv = new CellView();
        cv.setFormat(times);
        cv.setFormat(timesBoldUnderline);
        cv.setAutosize(true);
    }
    
    private void createLabel(WritableSheet sheet, List<String> headers)
            throws WriteException {

        for(int i = 0; i < headers.size(); i++) {
        	addCaption(sheet, i, 0, headers.get(i));
        }

    }

    public void addCaption(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, timesBoldUnderline);
        sheet.addCell(label);
//        System.out.println("added " + s);
    }

    public void addNumber(WritableSheet sheet, int column, int row, Integer integer) throws WriteException, RowsExceededException {
        Number number;
        number = new Number(column, row, integer, times);
        sheet.addCell(number);
    }

    public void addDouble(WritableSheet sheet, int column, int row, Double integer) throws WriteException, RowsExceededException {
        Number number;
        number = new Number(column, row, integer, times);
        sheet.addCell(number);
    }
    
    public void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, times);
        sheet.addCell(label);
    }

	public boolean fileExists() {
		return file.exists();
	}
}