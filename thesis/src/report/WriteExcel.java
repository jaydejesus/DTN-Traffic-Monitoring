package report;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import core.DTNHost;
import core.TripProperties;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
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

    private WorkbookSettings wbSettings;
    private WritableWorkbook workbook; 
    private WritableSheet excelSheet;
    private File file;
    public void setOutputFile(String inputFile) {
    	this.inputFile = inputFile;
    }

    public void initialize(List<String> headers, TreeMap<DTNHost, List<TripProperties>> hash) throws IOException, WriteException {
        file = new File(inputFile);
        wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "EN"));

        workbook = Workbook.createWorkbook(file, wbSettings);
        workbook.createSheet("Report", 0);
        excelSheet = workbook.getSheet(0);
        
        createLabel(excelSheet, headers);
//        createContent(excelSheet, hash);

    }

    public void write() throws IOException, WriteException {
    	workbook.write();
        workbook.close();
    }
    
    public WritableSheet getExcelSheet() {
    	return excelSheet;
    }
    
    private void createLabel(WritableSheet sheet, List<String> headers)
            throws WriteException {
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

        // Write a few headers
//        addCaption(sheet, 0, 0, "Header 1");
//        addCaption(sheet, 1, 0, "This is another header");

        for(int i = 0; i < headers.size(); i++) {
        	addCaption(sheet, i, 0, headers.get(i));
        }

    }

    private void createContent(WritableSheet sheet, HashMap<DTNHost, List<TripProperties>> hash) throws WriteException,
            RowsExceededException {
        // Write a few number
    	int column = 1;
    	int row;
        for(DTNHost h : hash.keySet()) {
        	row = 0;
        	for(TripProperties t : hash.get(h)) {
        		addLabel(sheet, row++, column, h.toString());
        		addLabel(sheet, row++, column, "" + t.getTripStart()+"->" + t.getTripDestination());
        		addDouble(sheet, row++, column, t.getTripStartTime());
        		addDouble(sheet, row++, column, t.getTripEndTime());
        		addDouble(sheet, row++, column, t.getTravelTime());
        		addNumber(sheet, row++, column, t.getRerouteCtr());
        	}
        	column++;
        }
        // Lets calculate the sum of it
//        StringBuffer buf = new StringBuffer();
//        buf.append("SUM(A2:A10)");
//        Formula f = new Formula(0, 10, buf.toString());
//        sheet.addCell(f);
//        buf = new StringBuffer();
//        buf.append("SUM(B2:B10)");
//        f = new Formula(1, 10, buf.toString());
//        sheet.addCell(f);

        // now a bit of text
//        for (int i = 12; i < 20; i++) {
//            // First column
//            addLabel(sheet, 0, i, "Boring text " + i);
//            // Second column
//            addLabel(sheet, 1, i, "Another text");
//        }
    }

    public void addCaption(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, timesBoldUnderline);
        sheet.addCell(label);
        System.out.println("added " + s);
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