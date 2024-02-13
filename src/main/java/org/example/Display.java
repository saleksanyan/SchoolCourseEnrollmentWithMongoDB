package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Scanner;

/**
this class creates the menu and combine all actions
 */

public class Display {

    private static final String CREATE_STUDENT = "1";
    private static final String ADD_COURSE_TO_STUDENT = "2";
    private static final String DELETE_STUDENT = "3";
    private static final String LIST_STUDENTS_IN_SPECIFIC_COURSES = "4";
    private static final String LIST_STUDENTS = "9";
    private static final String FIND_NUMBER_OF_STUDENTS_IN_CLASS = "5";
    private static final String CALCULATE_AVERAGE_AGE_IN_EACH_COURSE = "6";
    private static final String DISPLAY_ENROLLED_COURSES = "7";
    private static final String LIST_COURSES = "8";
    private static final String CREATE_COURSE = "10";
    private static final String NUMBER_OF_STUDENTS_IN_A_COURSE = "11";
    private static final String EXIT_CODE = "0";


    //the start of the app

    public static void start() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("SchoolDB");
            MongoCollection<Document> coursesCollection = database.getCollection("Courses");
            MongoCollection<Document> studentsCollection = database.getCollection("Students");
            // Indexing
            studentsCollection.createIndex(new Document("studentId", 1), new IndexOptions().unique(true));

            Scanner scanner = new Scanner(System.in);
            menu();
            String choice = scanner.nextLine().trim();
            while (!choice.equals(EXIT_CODE)) {
                choice = menuChoice(choice, scanner, studentsCollection, coursesCollection, database);
            }
        }
    }


    //gives user the manu bar and gies the required data

    private static String menuChoice(String choice, Scanner scanner, MongoCollection<Document> studentsCollection, MongoCollection<Document> coursesCollection, MongoDatabase database) {
        if (choice.equals(CREATE_STUDENT)) {
            createStudent(scanner, studentsCollection, coursesCollection);
        } else if (choice.equals(ADD_COURSE_TO_STUDENT)) {
            String studentID = getStudentIDThatExists(studentsCollection, scanner);
            String courseID = getCourseIDThatExists(scanner, coursesCollection);
            StudentManagement.addCourses(studentsCollection, studentID, courseID);
            System.out.println("Course has been added successfully!");
        } else if (choice.equals(DELETE_STUDENT)) {
            String id = getNameOrID("Enter student ID: ", scanner);
            StudentManagement.deleteStudent(studentsCollection, id);
            System.out.println("Student has been deleted successfully!");

        } else if (choice.equals(LIST_STUDENTS_IN_SPECIFIC_COURSES)) {
            String courseID = getCourseIDThatExists(scanner, coursesCollection);
            StudentManagement.studentsInCourse(studentsCollection, courseID);
        } else if (choice.equals(FIND_NUMBER_OF_STUDENTS_IN_CLASS)) {
            String courseID = getCourseIDThatExists(scanner, coursesCollection);
            StudentManagement.studentsInCourse(studentsCollection, courseID);
        } else if (choice.equals(CALCULATE_AVERAGE_AGE_IN_EACH_COURSE)) {
            CourseManagement.calculateAverageAgeInEachCourse(studentsCollection);
        } else if (choice.equals(DISPLAY_ENROLLED_COURSES)) {
            String studentID = getStudentIDThatExists(studentsCollection, scanner);
            CourseManagement.displayEnrolledCourses(database, studentID);
        } else if (choice.equals(LIST_COURSES)) {
            CourseManagement.listAllCourses(coursesCollection);
        } else if (choice.equals(LIST_STUDENTS)) {
            StudentManagement.listAllStudents(studentsCollection);
        } else if (choice.equals(CREATE_COURSE)) {
            createCourse(scanner, coursesCollection);
        }else if (choice.equals(NUMBER_OF_STUDENTS_IN_A_COURSE)) {
            CourseManagement.findNumberOfStudentsInEachCourse(studentsCollection);
        }
        menu();
        choice = scanner.nextLine().trim();
        return choice;
    }

    //creates courses
    private static void createCourse(Scanner scanner, MongoCollection<Document> coursesCollection) {
        String courseName = getNameOrID("Enter course name: ", scanner);
        String courseID = getCourseIDThatDoesNotExists(scanner, coursesCollection);
        System.out.println("Enter department name: ");

        String department = scanner.nextLine().trim();
        CourseManagement.createCourse(coursesCollection, courseName, courseID, department);
        System.out.println("Course has been added successfully!");
    }


    //returns the id if the course exists in the collection
    private static String getCourseIDThatExists(Scanner scanner, MongoCollection<Document> coursesCollection) {
        CourseManagement.listAllCourses(coursesCollection);
        String courseID = getNameOrID("Enter course ID: ", scanner);
        while (!CourseManagement.courseIdExists(coursesCollection, courseID)) {
            System.out.println("This course ID does not exist in the database!");
            System.out.println("Enter one that exists: ");
            courseID = scanner.nextLine().trim();
        }
        return courseID;
    }

    //returns the id if the course does not exist in the collection
    private static String getCourseIDThatDoesNotExists(Scanner scanner, MongoCollection<Document> coursesCollection) {
        String courseID = getNameOrID("Enter course ID: ", scanner);
        while (CourseManagement.courseIdExists(coursesCollection, courseID)) {
            System.out.println("This course ID exist in the database!");
            System.out.println("Enter one that does not exist: ");
            courseID = scanner.nextLine().trim();
        }
        return courseID;
    }

    //returns the id if the student exists in the collection
    private static String getStudentIDThatExists(MongoCollection<Document> studentsCollection, Scanner scanner) {
        StudentManagement.listAllStudents(studentsCollection);
        String studentID = getNameOrID("Enter student ID: ", scanner);
        while (!StudentManagement.studentIdExists(studentsCollection, studentID)) {
            System.out.println("This student ID does not exist in the database!");
            System.out.println("Enter one that exists: ");
            studentID = scanner.nextLine().trim();
        }
        return studentID;
    }

    //creates students
    private static void createStudent(Scanner scanner, MongoCollection<Document> studentsCollection, MongoCollection<Document> coursesCollection) {
        String name = getNameOrID("Enter student name: ", scanner);
        String id = getStudentIDThatDoesNotExist(scanner, studentsCollection);
        System.out.println("Enter student age: ");
        int age = isNumeric(scanner);
        CourseManagement.listAllCourses(coursesCollection);
        System.out.println("\nEnter course ID's that the student take\n" +
                "(enter 'done' if you want to exit): ");
        String courseID = scanner.nextLine().trim();
        ArrayList<String> courses = new ArrayList<>();
        while (!courseID.equalsIgnoreCase("done")){
            if (CourseManagement.courseIdExists(coursesCollection, courseID))
                courses.add(courseID);
            else {
                System.out.println("This course ID does not exist in the database");
                System.out.println("Enter one that exists: ");
                courseID = scanner.nextLine().trim();
            }
            courseID = scanner.nextLine().trim();
        }
        StudentManagement.createStudent(studentsCollection, id, name, age, courses);
        System.out.println("Student has been added successfully!");

    }

    //returns the id if the student does not exist in the collection


    private static String getStudentIDThatDoesNotExist(Scanner scanner, MongoCollection<Document> studentsCollection) {
        String studentID = getNameOrID("Enter student ID: ", scanner);
        while (StudentManagement.studentIdExists(studentsCollection, studentID)) {
            System.out.println("This ID already exists in the database!");
            System.out.println("Enter one that does not exist: ");

            studentID = scanner.nextLine().trim();
        }
        return studentID;
    }

    //this method just return the input of the user
    private static String getNameOrID(String text, Scanner scanner) {
        System.out.println(text);
        return scanner.nextLine().trim();
    }

    //checking if the input is numeric or not and return the value
    private static int isNumeric(Scanner scanner){
        String s = scanner.nextLine().trim();
        while(!s.matches("\\d+")){
            System.out.println("Enter digits: ");
            s = scanner.nextLine().trim();
        }
        return Integer.parseInt(s);
    }

    //the menu bar

    private static void menu() {
        System.out.println("\n1. Create Student");
        System.out.println("2. Add Courses To Student");
        System.out.println("3. Delete Student");
        System.out.println("4. List Students In Course");
        System.out.println("5. Find Number Of Students In Each Course");
        System.out.println("6. Calculate Average Age In Each Course");
        System.out.println("7. Display Enrolled Courses");
        System.out.println("8. List Courses");
        System.out.println("9. List Students");
        System.out.println("10. Create Courses");
        System.out.println("11. Find Number Of Students In Each Course");
        System.out.println("0. Exit\n");
        System.out.println("Please choose one of this actions(enter only digits): ");

    }
}
