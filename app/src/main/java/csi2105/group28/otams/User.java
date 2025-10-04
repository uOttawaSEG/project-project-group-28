/* @author: group28
 * CSI2105:intro to software engineering
 * Project
 * Due: Oct 13 2025
 * a class containing all the attributes about the users of an otams
 */

package csi2105.group28.otams;

import java.io.Serializable;

public class User implements Serializable {

    private String email, password,userType, username;

    /*
     * Constructor for the User class. creates a firebase username by replacing the '.' in the email with an '@'
     * @param usertype is the type of user: Student or Tutor
     * @param email is the email of the user
     * @param password is their password
     */
    public User(String userType, String email, String password){
        this.userType = userType;
        setEmail(email);
        setPassword(password);
        username = this.email.replace(".","@"); //creates a firebase username by replacing the '.' in the email with an '@'
    }

    /*
     * Empty constructor class for firebase to create a class
     */
    public User(){

    }

    /*
     *@return the email of the user
     */
    public String getEmail(){
        return email;
    }

    /*sets the email of the user if it matches specifications
     *@param the email of the user
     *@throws IllegalArgumentException if the email is <6, contains multiple '@', contains no '.' after an '@', if it starts with '@'
     */
    private void setEmail(String email){
        boolean valid = false, first=false;
        if(email.length()<6){
            throw new IllegalArgumentException("email");
        }

        for(int i=0;i<email.length();i++){
            if(email.charAt(i)=='@' && !first){
                if(i==0){
                    throw new IllegalArgumentException("email");
                }
                first=true;
            }else if(email.charAt(i)=='@' && first){
                throw new IllegalArgumentException("email");
            }
            if(first & email.charAt(i)=='.'){
                valid =true;
            }
        }
        if (valid) {
            this.email = email;
        }else{
            throw new IllegalArgumentException("email");
        }
    }

    /*
     *@return the email of the user
     */
    public String getPassword(){
        return password;
    }

    /*sets the password of the user if it matches specifications
     *@param the password of the user
     *@throws IllegalArgumentException if the password is <7, amd does not contain a letter and number
     */
    private void setPassword(String password){

        if(password.length()<7){
            throw new IllegalArgumentException("passwordl");
        }
        boolean letter = false, number = false;
        for(int i=0;i<password.length();i++) {
            if (!letter && Character.isLetter(password.charAt(i))){
                letter=true;
            }
            if (!number && Character.isDigit(password.charAt(i))){
                number=true;
            }
        }

        if(letter & number) {
            this.password = password;
        }else{
            throw new IllegalArgumentException("passwordc");
        }
    }

    /*
     *@return the type of the user
     */
    public String getUserType(){
        return userType;
    }

    /* sets the usertype of the use
     *@param is the type of the user
     */
    public void setUserType(String userType){
        this.userType = userType;
    }


    /*
     *@return the firebase username of the user
     */
    public String getUsername(){
        return username;
    }
    /*sets the firebase username of the user
     *@param is the firebase username of the user
     */
    public void setUsername(String username){
         this.username=username;
    }
}
