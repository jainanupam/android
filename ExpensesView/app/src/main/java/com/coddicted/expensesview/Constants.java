package com.coddicted.expensesview;

/**
 * Created by anupamjain on 27/09/15.
 */
public class Constants {

    // Request URLs
    private static String APP_ROOT = "http://192.168.0.101:8080/ExpenseServices";
    public static String ADD_EXPENSE_URL = Constants.APP_ROOT + "/expenses/addexpensestr";
    public static String REGISTER_USER_URL = Constants.APP_ROOT + "/register/registerUser";
    public static String LOGIN_USER_URL = Constants.APP_ROOT + "/login/loginUser";

    // Data fields

    // Data fields for Main Activity
    public static String USER_ID = "userid";
    public static String AMOUNT = "amount";
    public static String PARTICULARS = "particulars";
    public static String GROUP = "group";
    public static String DATED = "dated";

    // Data fields for Register Activity
    public  static String USER_NAME = "userName";
    public  static String PASSWORD = "password";

}
