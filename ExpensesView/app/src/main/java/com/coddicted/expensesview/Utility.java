package com.coddicted.expensesview;

/**
 * Created by anupamjain on 27/09/15.
 */
public class Utility {
    /**
     * Checks for Null String object
     *
     * @param txt
     * @return true for not null and false for null String object
     */
    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0;
    }
}
