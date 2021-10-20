package sample;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.itextpdf.text.*;
import com.itextpdf.text.List;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Projections.*;
import static java.util.Arrays.asList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane root = new BorderPane();
        primaryStage.setTitle("Test");

        GenerateReport generateReport = new GenerateReport();

        String filePath = "D:\\StudentDB\\BELsample.txt";

        String[] pathAttributes = filePath.split("\\\\");

        String fileName = pathAttributes[pathAttributes.length-1];

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/");
        MongoDatabase database = mongoClient.getDatabase("Student");
        MongoCollection<org.bson.Document> studentsCollection = database.getCollection("studentInfo");
        ArrayList<String> key_details = new ArrayList<String>();
        ArrayList<Object> value_details = new ArrayList<Object>();

        String keys[] = new String[]{"SNo", "Standard","Name","Age","Roll_No", "Percentile"};
        for(int i = 0 ; i<keys.length ; i++)
        {
            key_details.add(keys[i]);
        }
        Object row = "";
        BufferedReader txtReader = new BufferedReader(new FileReader(filePath));
        while((row = txtReader.readLine()) != null)
        {
            Object[] data = ((String)row).split("\t");
            int index = 0;
            for(Object value:data){
                value_details.add(value);
            }
            Document doc = new Document();
            for(int i=0 ; i<key_details.size() ; i++)
            {
                if(key_details.get(i).equals("Percentile"))
                    doc.append(key_details.get(i),Float.parseFloat((String) value_details.get(i)));
                else
                    doc.append(key_details.get(i), value_details.get(i));
            }
            studentsCollection.insertOne(doc);

            value_details.clear();

        }

        MongoCollection<org.bson.Document> fileCollection = database.getCollection("FileInfo");


        Path path = Paths.get(filePath);
        long bytes = Files.size(path);
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

        Document doc = new Document("File Name", fileName);
        doc.append("Path", filePath);
        doc.append("File Size", bytes+" Bytes");
        doc.append("Creation Time", attr.creationTime().toString());

//        docs.add(d1);

        fileCollection.insertOne(doc);


        generateReport.pdfGeneration("StudentReport.pdf", filePath);

        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }






    public static void main(String[] args) {
        launch(args);
    }
}
