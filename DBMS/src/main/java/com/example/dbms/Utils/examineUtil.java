package com.example.dbms.Utils;

import com.example.dbms.Pojo.SharedDataStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Types;
public class examineUtil {
    public examineUtil(){};
    private XMLUtil xmlUtil;
    private List<Map<String ,String>> limits;
    public examineUtil(XMLUtil xmlUtil) throws Exception{

        this.xmlUtil = xmlUtil;
    }
    public String examineInsert(String[] columnNames, List<String[]> allValues) throws Exception {
        Map<String,String> tableInfo=xmlUtil.getTableInfo();



        for(String[] values:allValues){
            if(columnNames.length!=values.length){
                return "The number of columns does not match the number of values.";
            }
           for(int i=0;i<values.length;i++){
               if(!tableInfo.containsKey(columnNames[i])){
                   return "Column "+columnNames[i]+" does not exist.";
               }
               if(!examineValue(values[i],tableInfo.get(columnNames[i]))){
                   return "Invalid value for column "+columnNames[i]+".";
               }
           }
        }
        return "Examine Insert Passed";

    }
    public String examineDelete(String columnName) throws Exception {
        if(columnName==null)return "Examine Delete Passed";
        Map<String,String> tableInfo=xmlUtil.getTableInfo();
        if(!tableInfo.containsKey(columnName)){
            return "Column "+columnName+" does not exist.";
        }
        return "Examine Delete Passed";
    }

    public String examinePermission(String databasename,String action) throws Exception {
        if(SharedDataStorage.getInstance().getThreadLocalData().equals("root")){
            return "Examine Permission Passed";
        }
        List<String> columnName=new ArrayList<>();
        Map<String,String> conditions=new HashMap<>();
        columnName.add(databasename);
        this.xmlUtil=new XMLUtil("resources/static/users/"+SharedDataStorage.getInstance().getThreadLocalData());

        List<Map<String,String>> result=this.xmlUtil.selectData("resources/static/users/"+SharedDataStorage.getInstance().getThreadLocalData()+".xml",columnName,conditions);
        for(Map<String,String> map:result){
            if(map.get(databasename).equals(action)){
                return "Examine Permission Passed";
            }
        }

        return "Examine Permission Rejected";
    }
    public String examineAlter(String[] columnName,String action)throws  Exception{
        Map<String,String> tableInfo=xmlUtil.getTableInfo();
        if(action.equals("add column")){
            for(String name:columnName){
                if(tableInfo.containsKey(name)){
                    return "Column "+name+" already exist.";
                }
            }
        }else if(action.equals("drop column")){
            for (String name:columnName){
                if(!tableInfo.containsKey(name)){
                    return "Column "+name+" does not exist.";
                }
            }
        }

        return "Examine Alter Passed";
    }
    public String examineSelect(List<String> columnName) throws Exception {
        Map<String,String> tableInfo=xmlUtil.getTableInfo();
        for(String name:columnName){
            if(!tableInfo.containsKey(name)){
                return "Column "+name+" does not exist.";
            }
        }
        return "Examine Select Passed";
    }


    public boolean examineValue(String value, String type) {
        if (value == null || value.isEmpty()) {
            return true; // 如果值为空，则认为校验通过
        }

        switch (type.toUpperCase()) {
            case "INTEGER":
                try {
                    int intValue = Integer.parseInt(value);
                    // 这里可以根据实际情况设置整数值的范围
                    return intValue >= Integer.MIN_VALUE && intValue <= Integer.MAX_VALUE;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "FLOAT":
            case "REAL":
            case "DOUBLE":
                try {
                    double doubleValue = Double.parseDouble(value);
                    // 这里可以根据实际情况设置浮点数值的范围
                    return doubleValue >= -Double.MAX_VALUE && doubleValue <= Double.MAX_VALUE;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "VARCHAR":
                // 假设 VARCHAR 类型的长度不能超过 255
                return value.length() <= 255;
            case "BOOLEAN":
                return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
            default:
                return true; // 对于未知的类型，认为校验通过
        }
    }



}
