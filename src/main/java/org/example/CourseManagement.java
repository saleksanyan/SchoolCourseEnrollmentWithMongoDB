package org.example;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * this class manages courses by providing some functionalities
 */

public class CourseManagement {

    //creates course
    public static void createCourse(MongoCollection<Document> courseCollection, String courseName, String id, String department) {
        Document studentDocument = new Document("courseId", id)
                .append("courseName", courseName)
                .append("department", department);
        courseCollection.insertOne(studentDocument);
    }

    // check if the given courseId exists in the collection
    public static boolean courseIdExists(MongoCollection<Document> courseCollection, String courseId) {
        AtomicInteger count = new AtomicInteger();
        courseCollection.find().forEach((Consumer<? super Document>) document -> {
            String id = document.getString("courseId");
            if(id!=null && id.equals(courseId)){
                count.getAndIncrement();
            }
        });
        if(count.intValue() == 0){
            return false;
        }
        return true;
    }

    //lists all courses
    public static void listAllCourses(MongoCollection<Document> courseCollection) {
        // Find all documents in the courseCollection
        courseCollection.find().forEach((Consumer<? super Document>) document -> {
            String id = document.getString("courseId");
            String courseName = document.getString("courseName");
            System.out.println("Course ID: "+ id+ "   Course Name: "+ courseName);
        });
    }

    //calculates average age of students in each course
    public static void calculateAverageAgeInEachCourse(MongoCollection<Document> studentsCollection) {
        AggregateIterable<Document> result = studentsCollection.aggregate(Arrays.asList(
                new Document("$unwind", "$enrolledCourses"),
                new Document("$group", new Document("_id", "$enrolledCourses")
                        .append("averageAge", new Document("$avg", "$age")))
        ));

        for (Document document : result) {
            System.out.println(document);
        }
    }

    //lists all enrolled courses
    public static void displayEnrolledCourses(MongoDatabase database, String studentId) {
        // Data Modeling example: Display all courses a student is enrolled in using referencing
        MongoCollection<Document> studentsCollection = database.getCollection("Students");
        Document student = studentsCollection.find(new Document("studentId", studentId)).first();
        if (student != null) {
            System.out.println("Enrolled Courses for Student " + studentId + ": " + student.get("enrolledCourses"));
        }
    }

    //finds the number of students that attend each course
    public static void findNumberOfStudentsInEachCourse(MongoCollection<Document> studentsCollection) {
        // Aggregation example: Find the number of students enrolled in each course
        AggregateIterable<Document> result = studentsCollection.aggregate(Arrays.asList(
                new Document("$unwind", "$enrolledCourses"),
                new Document("$group", new Document("_id", "$enrolledCourses")
                        .append("count", new Document("$sum", 1)))
        ));

        for (Document document : result) {
            System.out.println(document);
        }
    }
}
