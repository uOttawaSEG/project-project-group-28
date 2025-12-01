package csi2105.group28.otams;

import static org.junit.Assert.*;

import java.util.ArrayList;

//Test Cases of all domain model classes using local tests
public class UserTests {
    public void checkEmail(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
          Student Joe = new Student("Joe","Andrews","@joeAndrews","1wert56rthy","12345678912","Computer Engineering");
                }
        );
        assertEquals("email",exception.getMessage());
    }

    public void checkPasswordLength(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
                    Student Sam = new Student("Sam","Andrews","samandrews@gmail.com","1e","12345678912","Computer Engineering");
                }
        );
        assertEquals("passwordl",exception.getMessage());
    }

    public void chekPasswordContent(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()->{
                    Student Mike = new Student("Mike","Andrews","joeandrews@gmail.com","ertyuyiop","12345678912","Computer Engineering");
                }
        );
        assertEquals("passwordc",exception.getMessage());

    }

    public void checkCorrectStudentFormat(){
        assertThrows(IllegalArgumentException.class,()->{
            Student Uche = new Student("Uche","mike","uchemikejohn@gmail.com","wert12345rt","1234571919","Electrical Engineering");
        });
    }

    public void checkTutorAddingCourse(){
        ArrayList<String> coursesOffered = new ArrayList<String>();
        coursesOffered.add("ENG2104");
        coursesOffered.add("MAT1234");
        coursesOffered.add("QUA5678");

        Tutor Johnson = new Tutor("Johnson","Quake","johnsonquake@gmail.com","wert12345rt","1234571919","PhD",coursesOffered);
        Johnson.addCourse("ADM1506");
        coursesOffered.add("ADM1506");
        assertArrayEquals(coursesOffered.toArray(),Johnson.getCoursesOffered().toArray());
    }

    public void checkRating(){
        TutorRating TestRating = new TutorRating("Mike","Rate45rt","Testtutor","TestTutorName", 7, "Good course", "Session", "12Nov","creative_course");
        TestRating.setRating(8);
        assertEquals(0,TestRating.getRating());
        TestRating.setRating(3);
        assertEquals(3,TestRating.getRating());
    }
}
