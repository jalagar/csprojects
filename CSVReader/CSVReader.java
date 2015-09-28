import java.util.HashMap;
import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.Calendar;
/*
 * Program to read through CSV files and print out data analysis, such as column sums or per month sums
 * or selecting certain rows, using easy keywords. This program was built just to make my life easier so 
 * instead of having to manually search through Excel, it could tailor it to specific files and examine 
 * across files. But because of this fact, some of the functions might not work and I had to delete all 
 * confidential data content. All commented code was used for only specific CSV file analysis and won't
 * work for generic files. 
 * @Author: James Uejio
 * Summer 2015
 */
public class CSVReader {
    String dates;
    static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    ArrayList<String> columns;
    ArrayList<row> rows;
    Date minDate;
    Date maxDate; 
    String file;
    HashMap<String, Integer> observationCount = new HashMap<String, Integer>();
    static HashMap<String, String> vehicleIdToCategory;
    static HashMap<String, String> vehicleIdToDescription;
    public CSVReader(String file1, String dates) {
        System.out.println("Loading " + file1 + "...");
        this.dates = dates;
        columns = new ArrayList<String>();
        rows = new ArrayList<row>();
        this.file = file1;
        In in = new In(file);
        int start = 0;
        int end = 0;
        minDate = null;
        maxDate = null;
        String s = in.readLine();
        while (end < s.length()) {
            while(s.charAt(end)!='|') {
                if(end+1 == s.length()) {
                    end += 1;
                    break;
                }
                end += 1;
            }
            String col = s.substring(start,end).trim();
            if(!col.equals("")){
                columns.add(s.substring(start,end).trim());
            }
            end += 1;
            start = end;           
        }
        while (in.hasNextLine()) {
            start = 0;
            end = 0;
            s = in.readLine();
            ArrayList<String> values = new ArrayList<String>();
            while(end <= s.length()) {
                if (end != s.length()) {
                    while(s.charAt(end)!='|') {
                        if(end + 1 == s.length()) {
                            end += 1;
                            break;
                        }
                        end += 1;
                    }
                }
                if(values.size() < columns.size()){
                    String substring = s.substring(start, end);
                    if(substring.equals("")) {
                        substring = " ";
                    } else {
                        substring = substring.trim();
                    }
                    int substringLength = substring.length();
                    if(substringLength > 0) {
                        if(substring.charAt(0) == '"') {
                            substring = substring.substring(1, substringLength).trim();
                        }
                        substringLength = substring.length();
                        if(substring.charAt(substringLength - 1) == '"') {
                            substring = substring.substring(0, substringLength - 1).trim();
                        }
                    }
                    if(substringLength == 0) {
                        substring = " ";
                    }
                    values.add(substring);
                }                
                end += 1;
                start = end;         
            }
            row temp = new row(columns, values);
            rows.add(temp);
        }
//        if(file.equals("/callins10-15.csv") || file.equals("/observations.csv") || file.equals("/callins12-15.csv") || file.equals("/incidentswithDriverNameandId.csv")) {
//            int i = 0;
//            while (i < rows.size()) {
//                row r = rows.get(i);
//                if (!file.equals("/incidentswithDriverNameandId.csv") && !r.columnVal.get("Incident Type".toUpperCase()).equals("Complaint")) {
//                    rows.remove(r);
//                } else {
//                    String vehicleId = "";
//                    if (file.equals("/incidentswithDriverNameandId.csv")) {
//                        vehicleId = r.columnVal.get("pg&E vehicle number".toUpperCase());
//                    } else {
//                        vehicleId = r.columnVal.get("Vehicle No.".toUpperCase());
//                    }
//                    String category = vehicleIdToCategory.get(vehicleId);
//                    String value = vehicleIdToDescription.get(vehicleId);
//                    if (category == null) {
//                        category = "null";
//                    }
//                    if (value == null) {
//                        value = "null";
//                    }
//                    r.columnVal.put("Vehicle Category".toUpperCase(), category);
//                    r.columnVal.put("Vehicle Description".toUpperCase(), value);
//                    i++;
//                }
//            } 
//            columns.add("Vehicle Category");
//            columns.add("Vehicle Description");
//        }
        //to find min and max dates
        if(!dates.equals("")) {
            TreeSet<Date> datesInOrder = new TreeSet<Date>(new dateComparator());
            for (row r: rows) {
                String val = r.columnVal.get(dates).toUpperCase();
                try {
                    datesInOrder.add(sdf.parse(val));
                } catch (ParseException e) {
                }
            }
            minDate = datesInOrder.first();
            maxDate = datesInOrder.last();
        }
    }
    public class dateComparator implements Comparator<Date>{
        public dateComparator() {
        }
        @Override
        public int compare(Date a, Date b) {
            if (a.before(b)) {
                return -1;
            } else if (a.equals(b)) {
                return 0;
            } else {
                return 1;
            }
        }
    }
    public int sum() {
        return rows.size();
    }
    public ArrayList<String> columns(){
        return columns;
    }
    public int twoDates(String start, String end) {
        try {
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            int total = 0;
            for (row r: rows) {
                Date incidentDate = sdf.parse(r.columnVal.get(dates));
                if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                    total += 1;
                }
            }
            return total;
        } catch (ParseException e) {
            return 0;
        }
    }
    public int count(String column, String value) {
        int total = 0;
        for (row r: rows) {
            String val = r.columnVal.get(column.toUpperCase()).toUpperCase();
            if(val.equals(value.toUpperCase())) {
                total += 1;
            }
        }
        return total;
    }
    public void sumOfTwoDates(String start, String end) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] splitBy = start.split("/");
        System.out.println(months[Integer.parseInt(splitBy[0]) - 1] + ", " + splitBy[2] + "| " + twoDates(start, end)); 
    }
    public void sumOfTwoDates(String col1, String val1, String start, String end) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] splitBy = start.split("/");
        System.out.println(months[Integer.parseInt(splitBy[0]) - 1] + ", " + splitBy[2] + "| " + colVal(col1, val1, start, end, true)); 
    }
    public void sumOfTwoDates(String col1, String val1, String col2, String val2, String start, String end) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] splitBy = start.split("/");
        System.out.println(months[Integer.parseInt(splitBy[0]) - 1] + ", " + splitBy[2] + "| " + colValColVal(col1, val1, col2, val2, start, end, true)); 
    }
    public void sumOfTwoDates(String col1, String val1, String col2, String val2, String col3, String val3, String start, String end) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] splitBy = start.split("/");
        System.out.println(months[Integer.parseInt(splitBy[0]) - 1] + ", " + splitBy[2] + "| " + colValColValColVal(col1, val1, col2, val2, col3, val3, start, end, true)); 
    }
    public void allValuesAndCounts(String column) {
        allValuesCompact(column, "0", false, "");
    }
    public void allValuesAndCounts(String column, String min) {
        allValuesCompact(column, min, false, "");
    }
    public void allValuesAndCounts(String column, String start, String end) {
        try {
            rows.get(0).columnVal.get(column.toUpperCase()).equals("");
            System.out.println(column + " " + start + " - " + end);
            System.out.println("--------------");
            HashMap<String, Integer> valCount = new HashMap<String, Integer>();
            TreeMap<Integer, ArrayList<String>> valCol = 
                  new TreeMap<Integer, ArrayList<String>>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val = r.columnVal.get(column.toUpperCase()).toUpperCase();
                int count = 0;
                if (valCount.containsKey(val)) {
                    count = valCount.get(val);
                }
                try {
                    Date incidentDate = sdf.parse(r.columnVal.get(dates));
                    if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                        count += 1;
                    }
                } catch (ParseException x) {
                    System.out.println("Invalid date");
                    break;
                }
                valCount.put(val, count);
            }
            Iterable<Integer> keySet = valCol.keySet();
            for (String val: valCount.keySet()) {
                ArrayList<String> temp = new ArrayList<String>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(String c: valCol.get(k)) {
                    if (k != 0) {                    
                        totalrows += 1;
                        totalsum += k;
                        System.out.println(c + "| " +  k);
                    }
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        } catch (ParseException x) {
            System.out.println("Invalid date");
        }
    }
    public void allValuesCompact(String column, String min, Boolean x, String v) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column.toUpperCase()).equals("");
            System.out.println(column);
            System.out.println("--------------");
            HashMap<String, Integer> valCount = new HashMap<String, Integer>();
            TreeMap<Integer, ArrayList<String>> valCol = 
                  new TreeMap<Integer, ArrayList<String>>();
            for (row r: rows) {
                String val = r.columnVal.get(column.toUpperCase()).toUpperCase();
                int count = 0;
                if (x) {
                    if(val.equals(v.toUpperCase())) {
                        if (valCount.containsKey(val)) {
                            count = valCount.get(val);
                        }
                        count += 1;
                        valCount.put(val, count);
                    }
                } else {
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }
            }
            for (String val: valCount.keySet()) {
                ArrayList<String> temp = new ArrayList<String>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            int totalrows = 0;
            int totalsum = 0;
            Iterable<Integer> keySet = valCol.keySet();
            for(Integer k: keySet) {
                for(String c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(c + "| " +  k);
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colVal(String column, String v) {
        allValuesCompact(column, "0", true, v);
    }
    public Integer colVal(String column, String v, String start, String end, Boolean month) {
        try {
            rows.get(0).columnVal.get(column.toUpperCase()).equals("");
            HashMap<String, Integer> valCount = new HashMap<String, Integer>();
            TreeMap<Integer, ArrayList<String>> valCol = 
                  new TreeMap<Integer, ArrayList<String>>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val = r.columnVal.get(column.toUpperCase()).toUpperCase();
                int count = 0;
                if (val.equals(v.toUpperCase())) {
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    try {
                        Date incidentDate = sdf.parse(r.columnVal.get(dates));
                        if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                            count += 1;
                        }
                    } catch (ParseException x) {
                        System.out.println("Invalid date");
                        break;
                    }
                    valCount.put(val, count);
                }
            }
            for (String val: valCount.keySet()) {
                ArrayList<String> temp = new ArrayList<String>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            for(Integer k: keySet) {
                for(String c: valCol.get(k)) { 
                    if(!month) {
                        System.out.println(c + "| " +  k);
                        System.out.println();
                    }
                    return k;
                }
            }
            return null;
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. "
                    + "Make sure it is spelled exactly the same as the spreadsheet.");
            return null;
        } catch (ParseException x) {
            System.out.println("Invalid date");
            return null;
        }
    }
    public void colValColVal(String column1, String v1, String column2, String v2) {
        colColCompact(column1, column2, "0", true, v1, v2);
    }
    public Integer colValColVal(String column1, String v1, String column2, String v2, String start, String end, Boolean month) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            TreeMap<Integer, ArrayList<val1val2>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2>>();
            HashMap<val1val2, Integer> valCount = new HashMap<val1val2, Integer>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                int count = 0;
                if (val1.equals(v1.toUpperCase()) && val2.equals(v2.toUpperCase())) {
                    val1val2 val = new val1val2(val1, val2);
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    try {
                        Date incidentDate = sdf.parse(r.columnVal.get(dates));
                        if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                            count += 1;
                        }
                    } catch (ParseException x) {
                        System.out.println("Invalid date");
                        break;
                    }
                    valCount.put(val, count);
                }
            }
            for (val1val2 val: valCount.keySet()) {
                ArrayList<val1val2> temp = new ArrayList<val1val2>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            for(Integer k: keySet) {
                for(val1val2 c: valCol.get(k)) {
                    if(!month) {
                        System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + "| " 
                                + k); 
                        System.out.println();
                    }
                    return k;
                }
            }
            return null;
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. "
                    + "Make sure it is spelled exactly the same as the spreadsheet.");
            return null;
        } catch (ParseException x) {
            System.out.println("Invalid date");
            return null;
        }
    }
    public void colCol(String column1, String column2) {
        colColCompact(column1, column2, "0", false, "", "");
    }
    public void colCol(String column1, String column2, String min) {
        colColCompact(column1, column2, min, false, "", "");
    }
    public void colCol(String column1, String column2, String start, String end) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + start + " - " + end);
            System.out.println("--------------");  
            TreeMap<Integer, ArrayList<val1val2>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2>>();
            HashMap<val1val2, Integer> valCount = new HashMap<val1val2, Integer>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                int count = 0;
                val1val2 val = new val1val2(val1, val2);
                if (valCount.containsKey(val)) {
                    count = valCount.get(val);
                }
                try {
                    Date incidentDate = sdf.parse(r.columnVal.get(dates));
                    if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                        count += 1;
                    }
                } catch (ParseException x) {
                    System.out.println("Invalid date");
                    break;
                }
                valCount.put(val, count);
            }
            for (val1val2 val: valCount.keySet()) {
                ArrayList<val1val2> temp = new ArrayList<val1val2>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2 c: valCol.get(k)) {
                    if(k != 0) {
                        totalrows += 1;
                        totalsum += k;
                        System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + "| " 
                                + k); 
                    }
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        } catch (ParseException x) {
            System.out.println("Invalid date");
        }

    }
    public void colColCompact(String column1, String column2, String min, Boolean x, String v1, String v2) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2);
            System.out.println("--------------");  
            TreeMap<Integer, ArrayList<val1val2>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2>>();
            HashMap<val1val2, Integer> valCount = new HashMap<val1val2, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                val1val2 val = new val1val2(val1, val2);
                int count = 0;
                if(x) {
                    if(val1.equals(v1.toUpperCase()) && val2.equals(v2.toUpperCase())) {
                        if(valCount.containsKey(val)) {
                            count = valCount.get(val);
                        }
                        count += 1;
                        valCount.put(val, count);
                    }
                } else {
                    if(valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }
            }
            for (val1val2 val: valCount.keySet()) {
                ArrayList<val1val2> temp = new ArrayList<val1val2>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2 c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + "| " 
                            + k); 
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column/Value does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        } 
    }
    public void colColVal(String column1, String column2, String v) {
        colColVal(column1, column2, v, "0");
    }
    public void colColVal(String column1, String column2, String v, String start, String end) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + start + " - " + end);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2>>();
            HashMap<val1val2, Integer> valCount = new HashMap<val1val2, Integer>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                int count = 0;
                if (val2.equals(v.toUpperCase())) {
                    val1val2 val = new val1val2(val1, val2);
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    try {
                        Date incidentDate = sdf.parse(r.columnVal.get(dates));
                        if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                            count += 1;
                        }
                    } catch (ParseException x) {
                        System.out.println("Invalid date");
                        break;
                    }
                    valCount.put(val, count);
                }
            }
            for (val1val2 val: valCount.keySet()) {
                ArrayList<val1val2> temp = new ArrayList<val1val2>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2 c: valCol.get(k)) {
                    if(k != 0) {
                        totalrows += 1;
                        totalsum += k;
                        System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + "| " 
                                + k); 
                    }
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        } catch (ParseException x) {
            System.out.println("Invalid date");
        }
    }
    public void colColVal(String column1, String column2, String v, String min) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2);
            System.out.println("--------------");  
            TreeMap<Integer, ArrayList<val1val2>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2>>();
            HashMap<val1val2, Integer> valCount = new HashMap<val1val2, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                val1val2 val = new val1val2(val1, val2);
                int count = 0;
                if(val2.equals(v.toUpperCase())) {
                    if(valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }                
            }
            for (val1val2 val: valCount.keySet()) {
                ArrayList<val1val2> temp = new ArrayList<val1val2>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2 c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + "| " 
                            + k); 
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column/Value does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colColCol(String column1, String column2, String column3) {
        colColCol(column1, column2, column3, "0");
    }
    public void colColCol(String column1, String column2, String column3, String min) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + column3);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2val3>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3>>();
            HashMap<val1val2val3, Integer> valCount = new HashMap<val1val2val3, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                val1val2val3 val = new val1val2val3(val1, val2, val3);
                int count = 0;
                if (valCount.containsKey(val)) {
                    count = valCount.get(val);
                }
                count += 1;
                valCount.put(val, count);
            }
            for (val1val2val3 val: valCount.keySet()) {
                ArrayList<val1val2val3> temp = new ArrayList<val1val2val3>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2val3 c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + 
                            column3 + "-" + c.val3 + "| " + k);
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colColColVal(String column1, String column2, String column3, String v) {
        colColColVal(column1, column2, column3, v, "0");
    }
    public void colColColVal(String column1, String column2, String column3, String v, String min) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + column3);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2val3>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3>>();
            HashMap<val1val2val3, Integer> valCount = new HashMap<val1val2val3, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                val1val2val3 val = new val1val2val3(val1, val2, val3);
                int count = 0;
                if(val3.equals(v.toUpperCase())) {
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }
            }
            for (val1val2val3 val: valCount.keySet()) {
                ArrayList<val1val2val3> temp = new ArrayList<val1val2val3>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2val3 c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + 
                            column3 + "-" + c.val3 + "| " + k);
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colColColVal(String column1, String column2, String column3, String v, String start, String end) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + column3 + " " + start + " - " + end);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2val3>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3>>();
            HashMap<val1val2val3, Integer> valCount = new HashMap<val1val2val3, Integer>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                int count = 0;
                if (val3.equals(v.toUpperCase())) {
                    val1val2val3 val = new val1val2val3(val1, val2, v);
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    try {
                        Date incidentDate = sdf.parse(r.columnVal.get(dates));
                        if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                            count += 1;
                        }
                    } catch (ParseException x) {
                        System.out.println("Invalid date");
                        break;
                    }
                    valCount.put(val, count);
                }
            }
            for (val1val2val3 val: valCount.keySet()) {
                ArrayList<val1val2val3> temp = new ArrayList<val1val2val3>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2val3 c: valCol.get(k)) {
                    if(k != 0) {
                        totalrows += 1;
                        totalsum += k;
                        System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + column3 + "-" + v + "| " 
                                + k); 
                    }
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        } catch (ParseException x) {
            System.out.println("Invalid date");
        }
    }
    public void colColColCol(String column1, String column2, String column3, String column4) {
        colColColCol(column1, column2, column3, column4, "0");
    }
    public void colColColCol(String column1, String column2, String column3, String column4, String min) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + column3 + " " + column4);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2val3val4>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3val4>>();
            HashMap<val1val2val3val4, Integer> valCount = new HashMap<val1val2val3val4, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                String val4 = r.columnVal.get(column4.toUpperCase()).toUpperCase();
                val1val2val3val4 val = new val1val2val3val4(val1, val2, val3, val4);
                int count = 0;
                if (valCount.containsKey(val)) {
                    count = valCount.get(val);
                }
                count += 1;
                valCount.put(val, count);
            }
            for (val1val2val3val4 val: valCount.keySet()) {
                ArrayList<val1val2val3val4> temp = new ArrayList<val1val2val3val4>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2val3val4 c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + 
                            column3 + "-" + c.val3 + " " + column4 + "-" + c.val4 + "| " + k);
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colColColColVal(String column1, String column2, String column3, String column4, String v) {
        colColColColVal(column1, column2, column3, column4, v, "0");
    }
    public void colColColColVal(String column1, String column2, String column3, String column4, String v, String min) {
        try {
            int m = Integer.parseInt(min);
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + column3 + " " + column4);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2val3val4>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3val4>>();
            HashMap<val1val2val3val4, Integer> valCount = new HashMap<val1val2val3val4, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                String val4 = r.columnVal.get(column4.toUpperCase()).toUpperCase();
                if (val4.equals(v.toUpperCase())) {
                val1val2val3val4 val = new val1val2val3val4(val1, val2, val3, v);
                int count = 0;
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }
            }
            for (val1val2val3val4 val: valCount.keySet()) {
                ArrayList<val1val2val3val4> temp = new ArrayList<val1val2val3val4>();
                int count = valCount.get(val);
                if (count >= m) {
                    if(valCol.containsKey(count)) {
                        temp = valCol.get(count);
                    }
                    temp.add(val);
                    valCol.put(count, temp);
                }
            }
            Iterable<Integer> keySet = valCol.keySet();
            int totalrows = 0;
            int totalsum = 0;
            for(Integer k: keySet) {
                for(val1val2val3val4 c: valCol.get(k)) {
                    totalrows += 1;
                    totalsum += k;
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + 
                            column3 + "-" + c.val3 + " " + column4 + "-" + c.val4 + "| " + k);
                }
            }
            System.out.println("Total rows = " + totalrows + "*");
            System.out.println("Total sum = " + totalsum + "*");
            System.out.println("*takes into account unknowns and blanks");
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colValColValColVal(String column1, String v1, String column2, String v2, String column3, String v3) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            TreeMap<Integer, ArrayList<val1val2val3>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3>>();
            HashMap<val1val2val3, Integer> valCount = new HashMap<val1val2val3, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                if (val1.equals(v1.toUpperCase()) && val2.equals(v2.toUpperCase()) && val3.equals(v3.toUpperCase())) {
                    val1val2val3 val = new val1val2val3(val1, val2, val3);
                    int count = 0;
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }
            }
            for (val1val2val3 val: valCount.keySet()) {
                ArrayList<val1val2val3> temp = new ArrayList<val1val2val3>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            for(Integer k: keySet) {
                for(val1val2val3 c: valCol.get(k)) {
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + 
                            column3 + "-" + c.val3 + "| " + k);
                }
            }
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public void colValColValColVal(String column1, String v1, String column2, String v2, String column3, String v3, String start, String end, String month) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            System.out.println(column1 + " " + column2 + " " + column3);
            System.out.println("--------------");
            TreeMap<Integer, ArrayList<val1val2val3>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3>>();
            HashMap<val1val2val3, Integer> valCount = new HashMap<val1val2val3, Integer>();
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                if (val1.equals(v1.toUpperCase()) && val2.equals(v2.toUpperCase()) && val3.equals(v3.toUpperCase())) {
                    val1val2val3 val = new val1val2val3(val1, val2, val3);
                    int count = 0;
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    count += 1;
                    valCount.put(val, count);
                }
            }
            for (val1val2val3 val: valCount.keySet()) {
                ArrayList<val1val2val3> temp = new ArrayList<val1val2val3>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            for(Integer k: keySet) {
                for(val1val2val3 c: valCol.get(k)) {
                    System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + " " + 
                            column3 + "-" + c.val3 + "| " + k);
                }
            }
            System.out.println();
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
        }
    }
    public Integer colValColValColVal(String column1, String v1, String column2, String v2, String column3, 
            String v3, String start, String end, Boolean month) {
        try {
            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
            TreeMap<Integer, ArrayList<val1val2val3>> valCol = 
                    new TreeMap<Integer, ArrayList<val1val2val3>>();
            HashMap<val1val2val3, Integer> valCount = new HashMap<val1val2val3, Integer>();
            Date s= sdf.parse(start);
            Date e = sdf.parse(end);
            for (row r: rows) {
                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
                String val3 = r.columnVal.get(column3.toUpperCase()).toUpperCase();
                int count = 0;
                if (val1.equals(v1.toUpperCase()) && val2.equals(v2.toUpperCase()) && val3.equals(v3.toUpperCase())) {
                    val1val2val3 val = new val1val2val3(val1, val2, val3);
                    if (valCount.containsKey(val)) {
                        count = valCount.get(val);
                    }
                    try {
                        Date incidentDate = sdf.parse(r.columnVal.get(dates));
                        if((incidentDate.after(s) || incidentDate.equals(s)) && incidentDate.before(e)) {
                            count += 1;
                        }
                    } catch (ParseException x) {
                        System.out.println("Invalid date");
                        break;
                    }
                    valCount.put(val, count);
                }
            }
            for (val1val2val3 val: valCount.keySet()) {
                ArrayList<val1val2val3> temp = new ArrayList<val1val2val3>();
                int count = valCount.get(val);
                if(valCol.containsKey(count)) {
                    temp = valCol.get(count);
                }
                temp.add(val);
                valCol.put(count, temp);
            }
            Iterable<Integer> keySet = valCol.keySet();
            for(Integer k: keySet) {
                for(val1val2val3 c: valCol.get(k)) {
                    if(!month) {
                        System.out.println(column1 + "-" + c.val1 + " " + column2 + "-" + c.val2 + "| " 
                                + k); 
                        System.out.println();
                    }
                    return k;
                }
            }
            return null;
        } catch (NullPointerException e) {
            System.out.println("Column does not exist. "
                    + "Make sure it is spelled exactly the same as the spreadsheet.");
            return null;
        } catch (ParseException x) {
            System.out.println("Invalid date");
            return null;
        }
    }
    public class val1val2 {
        //to sort colcol numerically
        String val1;
        String val2;
        public val1val2 (String val1, String val2) {
            this.val1 = val1;
            this.val2 = val2;
        }
        @Override
        public int hashCode() {
            return ("v1" + val1 + "v2" + val2).hashCode();
        }
        @Override
        public boolean equals(Object other) {
            return ("v1" + val1 + "v2" + val2).equals
                    ("v1" + ((val1val2)other).val1 + "v2" + ((val1val2)other).val2);
        }
    }
    public class val1val2val3 {
        //to sort colcolcol numerically
        String val1, val2, val3;
        public val1val2val3 (String val1, String val2, String val3) {
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
        }
        @Override
        public int hashCode() {
            return ("v1" + val1 + "v2" + val2 + "v3" + val3).hashCode();
        }
        @Override
        public boolean equals(Object other) {
            return ("v1" + val1 + "v2" + val2 + "v3" + val3).equals
                    ("v1" + ((val1val2val3)other).val1 + "v2" 
                            + ((val1val2val3)other).val2 + "v3" + ((val1val2val3)other).val3);
        }
    }
    public class val1val2val3val4 {
      //to sort colcolcolcol numerically
        String val1, val2, val3, val4;
        public val1val2val3val4(String val1, String val2, String val3, String val4) {
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
            this.val4 = val4;
        }
        @Override
        public int hashCode() {
            return ("v1" + val1 + "v2" + val2 + "v3" + val3 + "v4" + val4).hashCode();
        }
        @Override
        public boolean equals(Object other) {
            return ("v1" + val1 + "v2" + val2 + "v3" + val3 + "v4" + val4).equals
                    ("v1" + ((val1val2val3val4)other).val1 + "v2" 
                            + ((val1val2val3val4)other).val2 + "v3" + ((val1val2val3val4)other).val3 + "v4" + ((val1val2val3val4)other).val4);
        }
    }
    public void rows(String col, String val) {
        int space = 25;
        for (String column: columns) {
            int colLength = column.length();
            int numSpaces = space - column.length(); 
            if(colLength > space) {
                System.out.print(column.substring(0, space - 1));
                numSpaces = 1;
            } else {
                System.out.print(column);
            }
            for (int i = 0; i < numSpaces; i++) {
                System.out.print(" ");
            }
        }
        for (row r: rows) {
            if (r.columnVal.get(col.toUpperCase()).toUpperCase().equals(val.toUpperCase())) {
                System.out.println();
                for (String column: columns) {
                    String value = r.columnVal.get(column.toUpperCase());
                    int length = value.length();
                    int numSpaces = space - value.length(); 
                    if(length > space) {
                        String substring = value.toUpperCase().substring(0, space - 1);
                        System.out.print(substring);
                        numSpaces = space - substring.length();
                    } else {
                        System.out.print(value.toUpperCase());
                    }
                    for (int i = 0; i < numSpaces; i++) {
                        System.out.print(" ");
                    }
                }
            }
        }
        System.out.println();
    }
    public void rows(String col1, String val1, String col2, String val2) {
        int space = 25;
        for (String column: columns) {
            int colLength = column.length();
            int numSpaces = space - column.length(); 
            if(colLength >= space) {
                System.out.print(column.substring(0, space - 1));
                numSpaces = 1;
            } else {
                System.out.print(column);
            }

            for (int i = 0; i < numSpaces; i++) {
                System.out.print(" ");
            }
        }
        for (row r: rows) {
            if (r.columnVal.get(col1.toUpperCase()).toUpperCase().equals(val1.toUpperCase()) &&
                    r.columnVal.get(col2.toUpperCase()).toUpperCase().equals(val2.toUpperCase())) {
                System.out.println();
                for (String column: columns) {
                    String value = r.columnVal.get(column.toUpperCase());
                    int length = value.length();
                    int numSpaces = space - value.length();
                    if(length >= space) {
                        String substring = value.toUpperCase().substring(0, space - 1);
                        System.out.print(substring);
                        numSpaces = space - substring.length();
                    } else {
                        System.out.print(value.toUpperCase());
                    }
                    for (int i = 0; i < numSpaces; i++) {
                        System.out.print(" ");
                    }
                }
            }
        }
        System.out.println();
    }
//    public void vehicleTypeCategoryCount(String column1, String value1) {
//        try {
//            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
//            System.out.println(column1 + " " + "-" + " " + value1);
//            System.out.println("--------------");  
//            TreeMap<Integer, ArrayList<String>> valCol = 
//                    new TreeMap<Integer, ArrayList<String>>();
//            HashMap<String, Integer> valCount = new HashMap<String, Integer>();
//            for (row r: rows) {
//                String val = r.columnVal.get(column1.toUpperCase()).toUpperCase();
//                int count = 0;
//                if (valCount.containsKey(val)) {
//                    count = valCount.get(val);
//                }
//                count += 1;
//                valCount.put(val, count);
//            }
//            for (String val: valCount.keySet()) {
//                ArrayList<String> temp = new ArrayList<String>();
//                int count = valCount.get(val);
//                if(valCol.containsKey(count)) {
//                    temp = valCol.get(count);
//                }
//                temp.add(val);
//                valCol.put(count, temp);
//            }
//            Iterable<Integer> keySet = valCol.keySet();
//            for(Integer k: keySet) {
//                for(String c: valCol.get(k)) {
//                    //accounts for NAN and None for multiple offenders
//                    if(k < 30) {
//                        this.observations(column1, c);
//                    }
//                }
//            }
//            TreeMap<Integer, ArrayList<String>> valCol1 = 
//                    new TreeMap<Integer, ArrayList<String>>();
//            for (String val: observationCount.keySet()) {
//                ArrayList<String> temp = new ArrayList<String>();
//                int count = observationCount.get(val);
//                if(valCol1.containsKey(count)) {
//                    temp = valCol1.get(count);
//                }
//                temp.add(val);
//                valCol1.put(count, temp);
//            }
//            Iterable<Integer> keySet1 = valCol1.keySet();
//            for(Integer k: keySet1) {
//                for(String c: valCol1.get(k)) {
//                    System.out.println(c + "| " + k);
//                }
//            }
//            observationCount = new HashMap<String, Integer>();
//        } catch (NullPointerException e) {
//            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
//        }
//    }
//    //counts the observations for the multiple offenders
//    public void multipleOffendersCount(String column1, String column2, String min) {
//        try {
//            int m = Integer.parseInt(min);
//            rows.get(0).columnVal.get(column1.toUpperCase()).equals("");
//            System.out.println(column1 + " " + column2);
//            System.out.println("--------------");  
//            TreeMap<Integer, ArrayList<val1val2>> valCol = 
//                    new TreeMap<Integer, ArrayList<val1val2>>();
//            HashMap<val1val2, Integer> valCount = new HashMap<val1val2, Integer>();
//            for (row r: rows) {
//                String val1 = r.columnVal.get(column1.toUpperCase()).toUpperCase();
//                String val2 = r.columnVal.get(column2.toUpperCase()).toUpperCase();
//                val1val2 val = new val1val2(val1, val2);
//                int count = 0;
//                if (valCount.containsKey(val)) {
//                    count = valCount.get(val);
//                }
//                count += 1;
//                valCount.put(val, count);
//            }
//            for (val1val2 val: valCount.keySet()) {
//                ArrayList<val1val2> temp = new ArrayList<val1val2>();
//                int count = valCount.get(val);
//                if (count >= m) {
//                    if(valCol.containsKey(count)) {
//                        temp = valCol.get(count);
//                    }
//                    temp.add(val);
//                    valCol.put(count, temp);
//                }
//            }
//            Iterable<Integer> keySet = valCol.keySet();
//            for(Integer k: keySet) {
//                for(val1val2 c: valCol.get(k)) {
//                    //accounts for NAN and None for multiple offenders
//                    if(k < 30) {
//                        this.observations(column1, c.val1, column2, c.val2);
//                    }
//                }
//            }
//            TreeMap<Integer, ArrayList<String>> valCol1 = 
//                    new TreeMap<Integer, ArrayList<String>>();
//            for (String val: observationCount.keySet()) {
//                ArrayList<String> temp = new ArrayList<String>();
//                int count = observationCount.get(val);
//                if(valCol1.containsKey(count)) {
//                    temp = valCol1.get(count);
//                }
//                temp.add(val);
//                valCol1.put(count, temp);
//            }
//            Iterable<Integer> keySet1 = valCol1.keySet();
//            for(Integer k: keySet1) {
//                for(String c: valCol1.get(k)) {
//                    System.out.println(c + "| " + k);
//                }
//            }
//            observationCount = new HashMap<String, Integer>();
//        } catch (NullPointerException e) {
//            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
//        }
//    }
//    //Counts total observations
//    public void observation() {
//        try {
//            System.out.println("observations");
//            System.out.println("--------------");
//            HashMap<String, Integer> observationCount = new HashMap<String, Integer>();
//            for (row r: rows) {
//                String[] observations = {"OBSERVATION1","OBSERVATION2","OBSERVATION3","OBSERVATION4","OBSERVATION5"};
//                //TODO can change this to put whatever columns you want to add up for multiple callins
//                for (String o: observations) {
//                    String value = r.columnVal.get(o);
//                    int count = 0;
//                    if (observationCount.containsKey(value)) {
//                        count = observationCount.get(value);
//                    }
//                    count += 1;
//                    observationCount.put(value, count);
//                }
//            }
//            System.out.println();
//            TreeMap<Integer, ArrayList<String>> valCol = 
//                    new TreeMap<Integer, ArrayList<String>>();
//            for (String val: observationCount.keySet()) {
//                ArrayList<String> temp = new ArrayList<String>();
//                int count = observationCount.get(val);
//                if(valCol.containsKey(count)) {
//                    temp = valCol.get(count);
//                }
//                temp.add(val);
//                valCol.put(count, temp);
//            }
//            Iterable<Integer> keySet = valCol.keySet();
//            for(Integer k: keySet) {
//                for(String c: valCol.get(k)) {
//                    System.out.println(c + "| " + k);
//                }
//            }
//        } catch (NullPointerException e) {
//            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
//        }
//    }
//    public void observations(String col1, String val1, String col2, String val2) {
//        for (row r: rows) {
//            if (r.columnVal.get(col1.toUpperCase()).toUpperCase().equals(val1.toUpperCase()) &&
//                    r.columnVal.get(col2.toUpperCase()).toUpperCase().equals(val2.toUpperCase())) {
//                String[] observations = {"OBSERVATION1","OBSERVATION2","OBSERVATION3","OBSERVATION4","OBSERVATION5"};
//                for (String o: observations) {
//                    String value = r.columnVal.get(o);
//                    int count = 0;
//                    if (observationCount.containsKey(value)) {
//                        count = observationCount.get(value);
//                    }
//                    count += 1;
//                    observationCount.put(value, count);
//                }
//            }
//        }
//    }
//    public void observations(String col1, String val1) {
//        for (row r: rows) {
//            if (r.columnVal.get(col1.toUpperCase()).toUpperCase().equals(val1.toUpperCase())) {
//                String[] observations = {"OBSERVATION1","OBSERVATION2","OBSERVATION3","OBSERVATION4","OBSERVATION5"};
//                for (String o: observations) {
//                    String value = r.columnVal.get(o);
//                    int count = 0;
//                    if (observationCount.containsKey(value)) {
//                        count = observationCount.get(value);
//                    }
//                    count += 1;
//                    observationCount.put(value, count);
//                }
//            }
//        }
//    }
    public class row {
        HashMap<String, String> columnVal;
        row(ArrayList<String> columns, ArrayList<String> values) {
            columnVal = new HashMap<String, String>();
            for (int i = 0; i< columns.size(); i++) {
                String column = columns.get(i).toUpperCase();
                String value = values.get(i);
                if(column.equals("DRIVER ID")) {
                    columnVal.put(column, value.replaceFirst("^0+", ""));
                } else {
                    columnVal.put(column, value);
                }
            }
        }
    }
//    //Only for Pool Mileage Data. ColVal but adds up the mileage
//    public void pool(String column) {
//        try {
//            rows.get(0).columnVal.get(column.toUpperCase()).equals("");
//            System.out.println(column);
//            System.out.println("--------------");
//            TreeMap<String, Integer> valCount = new TreeMap<String, Integer>();
//            for (row r: rows) {
//                String val = r.columnVal.get(column.toUpperCase()).toUpperCase();
//                int count = 0;
//                if (valCount.containsKey(val)) {
//                    count = valCount.get(val);
//                }
//                count += Integer.parseInt(r.columnVal.get("USAGE"));
//                valCount.put(val, count);
//            }
//
//            Iterable<String> keySet = valCount.keySet();
//            for(String k: keySet) {
//                System.out.println(k + "| " +  valCount.get(k));
//            }
//            System.out.println();
//        } catch (NullPointerException e) {
//            System.out.println("Column does not exist. Make sure it is spelled exactly the same as the spreadsheet.");
//        }
//    }
    public static void sort(CSVReader incidents, ArrayList<String> tokens) {
        int tokensSize = tokens.size();
        if (tokensSize == 2) {
            //col
            incidents.allValuesAndCounts(tokens.get(1));
        }
        if (tokensSize == 3) {
            //NOTE if a value is the same as a column, there will be error!
            try {
                //col min
                incidents.allValuesAndCounts(tokens.get(1), tokens.get(2));
            } catch (NumberFormatException e) {
                int count = incidents.count(tokens.get(1), tokens.get(2));
                if(count == 0) {
                    incidents.colCol(tokens.get(1), tokens.get(2));
                } else {
                    incidents.colVal(tokens.get(1), tokens.get(2));
                }
            }
        }
        if (tokensSize == 4) {
            try {
                //col col min
                incidents.colCol(tokens.get(1), tokens.get(2), tokens.get(3));
            } catch (NumberFormatException e) {
                //col col col
                int count = incidents.count(tokens.get(2), tokens.get(3));
                if (count == 0) {
                    incidents.colColCol(tokens.get(1), tokens.get(2), tokens.get(3));
                } else {
                    incidents.colColVal(tokens.get(1), tokens.get(2), tokens.get(3));
                }
            }
        }
        if (tokensSize == 5) {
            try {
                int count = incidents.count(tokens.get(2), tokens.get(3));
                if (count == 0) {
                    int count23 = incidents.count(tokens.get(3), tokens.get(4));
                    if (count23 == 0) {
                        //Note doesn't work when min is a value!
                        //col col col min
                        incidents.colColCol(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
                    } else {
                        incidents.colColColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
                    }
                } else {
                    incidents.colColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
                }
            } catch (NumberFormatException | NullPointerException e) {
                int count12 = incidents.count(tokens.get(1), tokens.get(2));
                if (count12 == 0) {
                    incidents.colColColCol(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
                } else {
                    incidents.colValColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
                }
            }
        }
        if (tokensSize == 6) {
            String token1 = tokens.get(1);
            String token2 = tokens.get(2);
            String token3 = tokens.get(3);
            String token4 = tokens.get(4);
            String token5 = tokens.get(5);
            int count34 = incidents.count(token3, token4);
            if (count34 == 0) {
                try {
                    incidents.colColColCol(token1, token2, token3, token4, token5);
                } catch (IllegalArgumentException e) {
                    incidents.colColColColVal(token1, token2, token3, token4, token5);
                }
            } else {
                incidents.colColColVal(token1, token2, token3, token4, token5);
            }
        }
        if(tokensSize == 7) {
            int count12 = incidents.count(tokens.get(1), tokens.get(2));
            if(count12 == 0) {
                incidents.colColColColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), tokens.get(5), tokens.get(6));
            } else {
                incidents.colValColValColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), tokens.get(5), tokens.get(6));
            }
        }     
    }
    //Specify start and end dates
    public static void dates(CSVReader incidents, ArrayList<String> tokens) {
        if (tokens.size() == 3) {
            incidents.sumOfTwoDates(tokens.get(1), tokens.get(2));
        }
        if (tokens.size() == 4) {
            incidents.allValuesAndCounts(tokens.get(1), tokens.get(2), tokens.get(3));
        }
        if (tokens.size() == 5) {
            //NOTE if a value is the same as a column, there will be error!
            String token1 = tokens.get(1);
            String token2 = tokens.get(2);
            String token3 = tokens.get(3);
            String token4 = tokens.get(4);
            int count = incidents.count(token1, token2);
            if(count == 0) {
                incidents.colCol(token1, token2, token3, token4);
            } else {
                incidents.colVal(token1, token2, token3, token4, false);
            }
        }
        if (tokens.size() == 6) {
            incidents.colColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), tokens.get(4));
        }
        if (tokens.size() == 7) {
            incidents.colValColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), tokens.get(5), tokens.get(6), false);
        }
    }
    public static void months(CSVReader incidents, ArrayList<String> tokens) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(incidents.minDate);
        int minYear = cal.get(Calendar.YEAR);
        int minMonth = cal.get(Calendar.MONTH) + 1;
        int countYear = minYear;
        int countMonth = minMonth;
        String countstartDate = countMonth + "/" + 1 + "/" + countYear;
        String countendDate = countMonth + "/" + 1 + "/" + countYear;
        int tokenSize = tokens.size();
        try {
            while (sdf.parse(countstartDate).before(incidents.maxDate)) {
                countstartDate = countMonth + "/" + 1 + "/" + countYear;
                if (countMonth + 1 == 13) {
                    countendDate = 1 + "/" + 1 + "/" + (countYear + 1);
                } else {
                    countendDate = (countMonth + 1) + "/" + 1 + "/" + countYear;
                }
                if(tokenSize == 2) {
                    incidents.allValuesAndCounts(tokens.get(1), countstartDate, countendDate);
                } else if (tokenSize == 3) {
                    String token1 = tokens.get(1);
                    String token2 = tokens.get(2);
                    int count = incidents.count(token1, token2);
                    if(count == 0) {
                        incidents.colCol(token1, token2, countstartDate, countendDate);
                    } else {
                        incidents.sumOfTwoDates(token1, token2, countstartDate, countendDate);
                    }
                } else if (tokenSize == 4) {
                    incidents.colColVal(tokens.get(1), tokens.get(2), tokens.get(3), countstartDate, countendDate);
                } else if(tokenSize == 5) { 
                    int count = incidents.count(tokens.get(1), tokens.get(2));
                    if (count == 0) {
                        incidents.colColColVal(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), countstartDate, countendDate);
                    } else {
                        incidents.sumOfTwoDates(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), countstartDate, countendDate);
                    }
                } else if (tokenSize == 7) {
                    incidents.sumOfTwoDates(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4), tokens.get(5), tokens.get(6), countstartDate, countendDate);
                } else {
                    incidents.sumOfTwoDates(countstartDate, countendDate);
                }
                countstartDate = countendDate;
                Date counterStart = sdf.parse(countstartDate);
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(counterStart);
                countYear = cal1.get(Calendar.YEAR);
                countMonth = cal1.get(Calendar.MONTH) + 1;
            }
        } catch (ParseException e) {
        }
    }
    //Shows Day Of Week That Has Most Occurences
    public void dayOfWeek() {
        HashMap<Integer, Integer> dayCount = new HashMap<Integer, Integer>();
        for (row r: rows) {
            Date counterStart = null;
            try {
                counterStart = sdf.parse(r.columnVal.get(dates));
            } catch (ParseException e) {   
            }
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(counterStart);
            int dayOfWeek = cal1.get(Calendar.DAY_OF_WEEK);
            int count = 0;
            if (dayCount.containsKey(dayOfWeek)) {
                count = dayCount.get(dayOfWeek);
            }
            count += 1;
            dayCount.put(dayOfWeek, count);
        }
        String[] days= {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (int dayOfWeek: dayCount.keySet()) {
            System.out.println(days[dayOfWeek] + " : " + dayCount.get(dayOfWeek));
        }
    }
    public static void main(String[] args) {
        //TODO file names
//        CSVReader vehicles = new CSVReader("/vehicleTypes.csv", ""); 
//        HashMap<String, String> vehicleIdToCategory = new HashMap<String, String>();
//        HashMap<String, String> vehicleIdToDescription = new HashMap<String, String>();
//        for (row r: vehicles.rows) {
//            vehicleIdToCategory.put(r.columnVal.get("UNIT"), r.columnVal.get("category".toUpperCase()));
//            vehicleIdToDescription.put(r.columnVal.get("UNIT"), r.columnVal.get("dbo_templates_description".toUpperCase()));
//        }
//        CSVReader.vehicleIdToCategory = vehicleIdToCategory;
//        CSVReader.vehicleIdToDescription = vehicleIdToDescription;
//        CSVReader incidents = new CSVReader("/observations.csv");
//        CSVReader currentFile = new CSVReader("/birdsAndExtraBirds.csv", "Survey Date (MM/DD/YY)".toUpperCase());
//        CSVReader currentFile = new CSVReader("/incidentswithDriverNameandId.csv", "Incident Date".toUpperCase());
        CSVReader currentFile = null;
        while(true) {
            System.out.print("> ");
            String line = StdIn.readLine();
            ArrayList<String> tokens = new ArrayList<String>();
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
            while (m.find()) {
                tokens.add(m.group(1).replace("\"", "")); 
            }
            try {       
                String command = tokens.get(0);
                switch (command) {
                //what kind of values are there? 1- column, 2-column val or column column, 
                //3- column column column, 4 column val column val
                    case "sort":
                        sort(currentFile, tokens);
                        break;
                    case "load":
//                        if (tokens.size() == 2) {
//                            if (tokens.get(1).equals("callins")) {
//                                currentFile = new CSVReader("/callins10-15.csv", "Incident Date".toUpperCase());
////                                currentFile.multipleOffendersCount("driver name1", "driver name2", "0");
////                                currentFile.vehicleTypeCategoryCount("Vehicle Type", "sport utility vehicle");
//                            } else if (tokens.get(1).equals("incidents")) {
//                                currentFile = new CSVReader("/incidents10-15.csv", "Incident Date".toUpperCase());
//                            } else if (tokens.get(1).equals("birds")) {
//                                currentFile = new CSVReader("/regularcsvbirds.csv", "Survey Date (MM/DD/YY)".toUpperCase());
//                            } else if (tokens.get(1).equals("extrabirds")) { 
//                                currentFile = new CSVReader("/ExtraBirdSheets.csv", "Survey Date (MM/DD/YY)".toUpperCase());
//                            } else if (tokens.get(1).equals("pool")) {
//                                currentFile = new CSVReader("/poolMileage.csv", "");
//                                new CSVReader("Company Vehicle2011.csv","").pool("YYYYMO");
//                                new CSVReader("Company Vehicle2012.csv","").pool("YYYYMO");
//                                new CSVReader("Company Vehicle2013.csv","").pool("YYYYMO");
//                                new CSVReader("Company Vehicle2014.csv","").pool("YYYYMO");
//                                new CSVReader("Company Vehicle2015.csv","").pool("YYYYMO");
//                            } else {
//                               System.out.println("No saved file found with that name");
//                            }
//                        }
                        if (tokens.size() == 3) {
                            currentFile = new CSVReader(tokens.get(1), tokens.get(2));
                        }
                        break;
                    case "dates": {
                        dates(currentFile, tokens);
                        break;
                    }
                    case "columns": {
                        for (String s: currentFile.columns()) {
                            System.out.println(s);
                        }
                        break;
                    }
                    case "sum": {
                        System.out.println(currentFile.sum());
                        break;
                    }
                    case "rows": {
                        if (tokens.size() == 3) {
                            currentFile.rows(tokens.get(1), tokens.get(2));
                        }
                        if (tokens.size() == 5) {
                            currentFile.rows(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
                        }
                        break;
                    }
//                    //observations
//                    case "o": {
//                        if (tokens.size() == 1) {
//                            currentFile.observation();
//                            break;
//                        }
//                        currentFile.observations(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(4));
//                        break;
//                    }
//                    //pool mileage
//                    case "pool":
//                        currentFile.pool("yyyymo");
//                        break;
                    case "month":
                        months(currentFile, tokens);
                        break;
                    default:
                        System.out.println("Invalid command.");
                        break;
                    case "max":
                        System.out.println("minDate = " + currentFile.minDate);
                        System.out.println("maxDate = " + currentFile.maxDate);
                        break;
                    case "day":
                        currentFile.dayOfWeek();
                        break;
                }
            } catch (NullPointerException e) { 
                System.out.println("Null stuff");
            } catch (IllegalArgumentException e) {
                System.out.println("Illegal Argument stuff");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Illegal IndexOutOfBound stuff");
            }
        }
    }
}
