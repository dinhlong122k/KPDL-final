package com.example.demo.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class MyPdfPageEventHelper extends PdfPageEventHelper{

    public MyPdfPageEventHelper() {
    }

    Font font =new Font(Font.FontFamily.HELVETICA, 52, Font.BOLD, new GrayColor(0.85f));

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        ColumnText.showTextAligned(writer.getDirectContentUnder(), 
                                    Element.ALIGN_CENTER,
                                    new Phrase("@COPYRIGHT BY GROUP 2",font),
                                    297.5f, 421, writer.getPageNumber() %2 == 1 ?45 : -45);
    }
    
}
