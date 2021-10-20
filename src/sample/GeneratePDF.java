package sample;

import com.itextpdf.text.*;
import com.itextpdf.text.List;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Projections.*;
import static java.util.Arrays.asList;

public class GenerateReport {

    static String FILE, filePath;
    Document document;

    static MongoClient mongoClient;
    static MongoDatabase database;
    static MongoCollection<org.bson.Document> collection;


    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font subTextFont = new Font(Font.FontFamily.TIMES_ROMAN, 9);
    private static Font subTableTextFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);
    private static Font subHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private static Font subSideHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);

    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);



    private static void addMetaData (com.itextpdf.text.Document document){
        document.addTitle("My first PDF");
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("suhas");
        document.addCreator("suhas");
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private static void addFileData(Document document) throws DocumentException
    {

        collection = database.getCollection("FileInfo");
        MongoCursor<org.bson.Document> cursor = collection.find().iterator();
        String fileSize=null, fileCreationTime=null;

        while (cursor.hasNext()) {
            org.bson.Document docx = cursor.next();
            fileSize = docx.get("File Size").toString();
            fileCreationTime = docx.get("Creation Time").toString();
        }

        Paragraph preface = new Paragraph();
        Chunk c1 = new Chunk("File Name: ", subSideHeadingFont);

//        DateFormat df = new SimpleDateFormat("MMM dd YYYY HH:mm:ss");
//        Date dateObj = new Date();

        Chunk c2 = new Chunk(filePath, subTextFont);
        preface.add(c1);
        preface.add(c2);

        document.add(preface);

        preface = new Paragraph();
        c1 = new Chunk("File Size: ", subSideHeadingFont);
        c2 = new Chunk(fileSize, subTextFont);
        preface.add(c1);
        preface.add(c2);

        document.add(preface);

        preface = new Paragraph();
        c1 = new Chunk("File Creation Time: ", subSideHeadingFont);
        c2 = new Chunk(fileCreationTime, subTextFont);
        preface.add(c1);
        preface.add(c2);

        document.add(preface);

        preface = new Paragraph();
        addEmptyLine(preface, 1);

        document.add(preface);


    }

    private static void addReportHeading(Document document) throws DocumentException
    {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("Student Report", catFont));

        addEmptyLine(preface, 2);

        preface.setAlignment(Element.ALIGN_CENTER);


        document.add(preface);
    }

    private static void addTable(Document document)
            throws DocumentException, JSONException {
        PdfPTable table = new PdfPTable(5);

        float[] columnWidths = new float[]{50f, 100f, 90f, 100f, 90f};

        collection = database.getCollection("studentInfo");

        table.setWidths(columnWidths);

        PdfPCell c1 = new PdfPCell(new Phrase("SNo", subTableTextFont));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c1);

        ArrayList<String> columnsList = new ArrayList<String>();

        columnsList.add("");

        String Keys[] = new String[]{"Name","Age","Roll_No", "Percentile"};
        String columns[] = new String[]{"SNo","Name","Age","Roll_No", "Percentile"};

        ArrayList <String> arrayList = new ArrayList<String>();

        for(int i=0; i<columns.length;i++){
            arrayList.add(columns[i]);
        }

        for(int i=0;i<Keys.length;i++)
        {

            c1 = new PdfPCell(new Phrase(Keys[i], subTableTextFont));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);
        }

        table.setHeaderRows(1);

        MongoCursor<org.bson.Document> cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            org.bson.Document docx = cursor.next();
            for(int i = 0;i<columns.length;i++){
                table.addCell(new Phrase(docx.get(arrayList.get(i)).toString(), subTableTextFont));
            }
        }

        document.add(table);

    }

    public static void addStudentData(Document document) throws DocumentException {
        collection = database.getCollection("studentInfo");
        MongoCursor<org.bson.Document> cursor = collection.find().iterator();
        int count=0;
        while (cursor.hasNext()) {
            org.bson.Document docx = cursor.next();
            count++;
        }

        Bson group = group("$Standard", Accumulators.max("Percentile", "$Percentile"));
        Bson projection = project(fields(include("Percentile"), excludeId()));
        String percentile=null;

        cursor = collection.aggregate(asList(group, projection)).iterator();

        while (cursor.hasNext()) {
            org.bson.Document docx = cursor.next();
            percentile = docx.get("Percentile").toString();
        }

        Paragraph preface = new Paragraph();
        Chunk c1 = new Chunk("Student Info ", subHeadingFont);

        Chunk c2 = new Chunk();
        preface.add(c1);

        document.add(preface);

        preface = new Paragraph();

        c1 = new Chunk("No. of Students: ", subSideHeadingFont);
        c2 = new Chunk(count+"", subTextFont);

        preface.add(c1);
        preface.add(c2);

        document.add(preface);

        preface = new Paragraph();

        c1 = new Chunk("Highest Percentile: ", subSideHeadingFont);
        c2 = new Chunk(percentile+"%", subTextFont);

        preface.add(c1);
        preface.add(c2);

        document.add(preface);

        preface = new Paragraph();
        addEmptyLine(preface, 1);

        document.add(preface);

    }

    public static void pdfGeneration(String fileName, String path){
        try {
            Document document = new Document();

            mongoClient = MongoClients.create("mongodb://localhost:27017/");
            database = mongoClient.getDatabase("Student");



            FILE = fileName;
            filePath = path;
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            document.open();

            addMetaData(document);
            addReportHeading(document);
            addFileData(document);
            addStudentData(document);
            addTable(document);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
