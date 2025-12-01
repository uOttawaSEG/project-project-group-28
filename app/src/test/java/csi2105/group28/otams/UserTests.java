package csi2105.group28.otams;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;

//Test Cases of all domain model classes using local tests with the JUnit library
public class UserTests {
    @Test
    public void checkEmail(){// checks the email criteria by creating a student account with an invalid email, and seeing if it returns the correct error message
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
          Student Joe = new Student("Joe","Andrews","@joeAndrews","1wert56rthy","12345678912","Computer Engineering");
                }
        );
        assertEquals("email",exception.getMessage());
    }
    @Test
    public void checkPasswordLength(){// Checks the password criteria checker by creating a student (User) instance with a password
        // length less than the minimum password length, and seeing if it will return the correct error
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
                    Student Sam = new Student("Sam","Andrews","samandrews@gmail.com","1e","12345678912","Computer Engineering");
                }
        );
        assertEquals("passwordl",exception.getMessage());
    }
    @Test
    public void chekPasswordContent(){// Checks the password criteria by creating a Student (User) password with an incorrect format (Does not contains letters and numbers)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
                    Student Mike = new Student("Mike","Andrews","joeandrews@gmail.com","ertyuyiop","12345678912","Computer Engineering");
                }
        );
        assertEquals("passwordc",exception.getMessage());

    }

//    public void checkCorrectStudentFormat(){// Check the student class by creating a valiod instance
//        assertThrows(IllegalArgumentException.class,()->{
//            Student Uche = new Student("Uche","mike","uchemikejohn@gmail.com","wert12345rt","1234571919","Electrical Engineering");
//        });
//    }
    @Test
    public void checkTutorAddingCourse(){// Checks the course addition function by adding a new course to the coursesOffered of a Tutor instance
        ArrayList<String> coursesOffered = new ArrayList<String>();
        coursesOffered.add("ENG2104");
        coursesOffered.add("MAT1234");
        coursesOffered.add("QUA5678");

        Tutor Johnson = new Tutor("Johnson","Quake","johnsonquake@gmail.com","wert12345rt","1234571919","PhD",coursesOffered);
        Johnson.addCourse("ADM1506");
        coursesOffered.add("ADM1506");
        assertArrayEquals(coursesOffered.toArray(),Johnson.getCoursesOffered().toArray());
    }

    @Test
    public void checkRating(){//Checks the rating by creating an invalid rating class
        TutorRating TestRating = new TutorRating("Mike","Rate45rt","Testtutor","TestTutorName", 7, "Good course", "Session", "12Nov","creative_course");
        TestRating.setRating(8);
        assertEquals(7,TestRating.getRating());
        TestRating.setRating(3);
        assertEquals(3,TestRating.getRating());
    }
}
