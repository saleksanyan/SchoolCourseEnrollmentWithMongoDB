package org.example;
import com.mongodb.client.*;

import org.bson.Document;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class StudentManagement {

    public static void createStudent(MongoCollection<Document> studentsCollection, String id, String name, int age, List<String> courses) {
        Document studentDocument = new Document("studentId", id)
                .append("age", age)
                .append("name", name)
                .append("enrolledCourses", courses);

        studentsCollection.insertOne(studentDocument);
    }

    // Check if the given studentId exists in the collection
    public static boolean studentIdExists(MongoCollection<Document> studentsCollection, String studentId) {

        AtomicInteger count = new AtomicInteger();
        studentsCollection.find().forEach((Consumer<? super Document>) document -> {
            String id = document.getString("studentId");
            if(id!=null && id.equals(studentId)){
                count.getAndIncrement();
            }
        });
        if(count.intValue() == 0){
            return false;
        }
        return true;
    }

    public static void listAllStudents(MongoCollection<Document> studentCollection) {
        // Find all documents in the courseCollection
        studentCollection.find().forEach((Consumer<? super Document>)
                document -> {
            String id = document.getString("studentId");
            String name = document.getString("name");
                    System.out.println("Student ID: "+ id+ "   Name: "+ name);
        });
    }
    public static void studentsInCourse(MongoCollection<Document> studentsCollection, String courseId) {
        Document query = new Document("enrolledCourses", courseId);
        for (Document document : studentsCollection.find(query)) {
            String id = document.getString("studentId");
            String name = document.getString("name");
            System.out.println("Student ID: "+ id+ "   Name: "+ name);
        }
    }

    public static void addCourses(MongoCollection<Document> studentsCollection, String id, String courseId) {
        // Update example: Add a new course to a student's enrolledCourses
        Document filter = new Document("studentId", id);
        Document update = new Document("$push", new Document("enrolledCourses", courseId));
        studentsCollection.updateOne(filter, update);
    }

    public static void deleteStudent(MongoCollection<Document> studentsCollection, String id) {
        // Delete example: Remove a student from the collection
        Document query = new Document("studentId", id);
        studentsCollection.deleteOne(query);
    }

}
