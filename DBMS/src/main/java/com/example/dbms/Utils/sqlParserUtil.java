package com.example.dbms.Utils;

import com.example.dbms.Pojo.Result;
import org.apache.ibatis.jdbc.Null;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sqlParserUtil {

    private XMLUtil xmlUtil;
    private examineUtil examineUtil=new examineUtil();
    public String databasename="";

    public sqlParserUtil(XMLUtil xmlUtil) {
        this.xmlUtil = xmlUtil;
    }
    public sqlParserUtil() {


    }

    public Result processSql(String sql) {

        String lowerSql = sql.toLowerCase(); // 转换为小写
        System.out.println(lowerSql);
        try {
            if (lowerSql.matches("(?s)^\\s*insert\\s+into.*")) {
                try {
                    String ret = parseInsert(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
                // 解析 INSERT 语句并执行插入操作
            }else if(lowerSql.matches("(?s)^\\s*update.*")) {
                try {
                    String ret = parseUpdate(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*delete\\s+from.*")) {
                try {
                    String ret = parseDelete(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*select.*")) {
                try {
                    List<Map<String, String>> ret = parseSelect(lowerSql);
                    return Result.success(ret);
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?is)^\\s*create\\s+table\\s+\\w+\\s*\\(.*\\)\\s*;?\\s*$")) {
                try {
                    String ret = parseCreate(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*create database\\s+\\w+\\s*;?\\s*$")) {
                try {
                    String ret = parseCreateDatabase(lowerSql);
                    return Result.success(ret, "");

                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*use database\\s+\\w+\\s*;?\\s*$")) {
                try {
                    String ret = parseUseDatabase(lowerSql);
                    System.out.println(ret);
                    return Result.success(ret,"");

                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if(lowerSql.matches("(?s)^\\s*show\\s+tables\\s*;\\s*$")){

                try {
                    if(this.databasename.equals("")){
                        return Result.error("no database selected");
                    }
                    List<Map<String,String>> ret = FileUtil.showTables(this.databasename);
                    for(Map<String,String> m:ret){
                        //去掉后缀.xml
                        m.put(this.databasename,m.get(this.databasename).substring(0,m.get(this.databasename).length()-4));
                    }
                    return Result.success(ret);
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            }else if(lowerSql.matches("(?s)^\\s*show\\s+databases\\s*;\\s*$")){
                try{
                    List<Map<String,String>> ret = FileUtil.showDatabases();
                    return Result.success(ret);
                }catch (Exception e){
                    return Result.error(e.getMessage());
                }
            }else if(lowerSql.matches("(?s)^\\s*drop\\s+database\\s+\\w+\\s*;?\\s*$")){
                try{
                    String ret = parseDropDatabase(lowerSql);
                    return Result.success(ret,"");
                }catch (Exception e){
                    return Result.error(e.getMessage());
                }

            }else if(lowerSql.matches("(?s)^\\s*drop\\s+table\\s+\\w+\\s*;?\\s*$")){
                try{
                    String ret = parseDropTable(lowerSql);
                    return Result.success(ret,"");
                }catch (Exception e){
                    return Result.error(e.getMessage());
                }
            }else if(lowerSql.matches("(?i)^\\s*alter\\s+table\\s+(\\w+)\\s+(add\\s+column|modify\\s+column)\\s+(.+)\\s*;?\\s*$")) {
                try {
                    String ret = parseAlterTable(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            }else if(lowerSql.matches("(?i)^\\s*alter\\s+table\\s+(\\w+)\\s+drop\\s+column\\s+(\\w+)\\s*;?\\s*$")){
                try {
                    String ret = parseAlterTable(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }


            }else if (lowerSql.matches("(?i)^\\s*revoke\\s+.*$")) {
                try {
                    // 解析并执行 REVOKE 语句的代码
                    // 例如：revoke select on dbname.tablename from 'username'@'hostname';
                    // 根据具体情况编写代码
                    String ret=parseRevoke(lowerSql);


                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?i)^\\s*grant\\s+.*$")) {
                try {
                    // 解析并执行 GRANT 语句的代码
                    // 例如：grant select on dbname.tablename to 'username'@'hostname';
                    // 根据具体情况编写代码
                    String ret=parseGrant(lowerSql);

                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            }
            else {
                return Result.error("Unsupported SQL statement: " + lowerSql);

            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }


    }
    private String parseRevoke(String sql) throws Exception {
        // 使用正则表达式解析 REVOKE 语句
        String regex = "(?i)^\\s*revoke\\s+(\\w+)\\s+on\\s+(\\w+)\\s+from\\s+'(\\w+)'\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String permission = matcher.group(1); // 权限
            if(!(permission.equals("select")
                    ||permission.equals("insert")
                    ||permission.equals("update")
                    ||permission.equals("delete")
                    ||permission.equals("create")
                    ||permission.equals("drop")
                    ||permission.equals("all")
                    ||permission.equals("alter"))){
                throw new Exception("Invalid permission: " + permission);
            }
            String database = matcher.group(2); // 数据库名
            String username = matcher.group(3); // 用户名
            if(!FileUtil.findDatabases(database)){
                throw new Exception("Database " + database + " does not exist");
            }
            if(!FileUtil.findUser(username)){
                throw new Exception("User " + username + " does not exist");
            }
            this.xmlUtil=new XMLUtil("./resources/static/users/"+username);

            xmlUtil.deleteData(username,database,permission);

            return "Revoke successful\nPermission: " + permission + "\nDatabase: " + database +
                    "\nUsername: " + username;
        } else {
            throw new Exception("Invalid revoke statement: " + sql);
        }
    }
    private String parseGrant(String sql) throws Exception {
        // 使用正则表达式解析 REVOKE 语句
        String regex = "(?i)^\\s*grant\\s+(\\w+)\\s+on\\s+(\\w+)\\s+from\\s+'(\\w+)'\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String permission = matcher.group(1); // 权限
            if(!(permission.equals("select")
                    ||permission.equals("insert")
                    ||permission.equals("update")
                    ||permission.equals("delete")
                    ||permission.equals("create")
                    ||permission.equals("drop")
                    ||permission.equals("all")
                    ||permission.equals("alter"))){
                throw new Exception("Invalid permission: " + permission);
            }
            String database = matcher.group(2); // 数据库名
            String username = matcher.group(3); // 用户名
            if(!FileUtil.findDatabases(database)){
                throw new Exception("Database " + database + " does not exist");
            }
            if(!FileUtil.findUser(username)){
                throw new Exception("User " + username + " does not exist");
            }
            this.xmlUtil=new XMLUtil("./resources/static/users/"+username);
            String[] columns=new String[]{database};
            String[] actions=new String[]{permission};
            if(new examineUtil().examinePermission(database,permission).equals("Examine Permission Passed")){
                throw new Exception("User " + username + " already has permission " + permission + " on database " + database);
            }
            xmlUtil.insertData(username,columns,actions);

            return "Grant successful\nPermission: " + permission + "\nDatabase: " + database +
                    "\nUsername: " + username;
        } else {
            throw new Exception("Invalid grant statement: " + sql);
        }
    }

    private String parseAlterTable(String sql) throws Exception {
        // 使用正则表达式解析 ALTER TABLE 语句
        String regex = "(?i)^\\s*alter\\s+table\\s+(\\w+)\\s+(add\\s+column|drop\\s+column)\\s+(.+)\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);


        if (matcher.find()) {
            String tableName = matcher.group(1);
            String action = matcher.group(2);
            String columnsInfo = matcher.group(3);
            String[] columnDefinitions = columnsInfo.split("\\s*,\\s*");

            // 提取列名和列类型
            String[] columnNames = new String[columnDefinitions.length];
            String[] columnTypes = new String[columnDefinitions.length];
            String databaseFolderPath = "resources/static/databases/" + databasename;

            String tableFolderPath = databaseFolderPath + "/" + tableName+".xml";
            if(!FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " does not exist");
            }


            this.xmlUtil = new XMLUtil(tableFolderPath);
            this.examineUtil=new examineUtil(xmlUtil);

            if(action.equals("add column")){

                for (int i = 0; i < columnDefinitions.length; i++) {
                    String[] parts = columnDefinitions[i].split("\\s+");
                    if (parts.length != 2) {
                        throw new Exception("Invalid column definition: " + columnDefinitions[i]);
                    }
                    columnNames[i] = parts[0];
                    columnTypes[i] = parts[1];

                }
                columnTypes[columnDefinitions.length-1] = columnTypes[columnDefinitions.length-1].substring(0,columnTypes[columnDefinitions.length-1].length()-1);

                String examineMessage=examineUtil.examineAlter(columnNames,"add column");
                if(!examineMessage.equals("Examine Alter Passed")){
                    throw new Exception(examineMessage);
                }
            }
            else if(action.equals("drop column")) {

                for (int i = 0; i < columnDefinitions.length; i++) {
                    String[] parts = columnDefinitions[i].split("\\s+");
                    columnNames[i] = parts[0];
                }

                columnNames[columnNames.length-1] = columnNames[columnNames.length-1].substring(0,columnNames[columnNames.length-1].length()-1);

                String examineMessage=examineUtil.examineAlter(columnNames,"drop column");
                if(!examineMessage.equals("Examine Alter Passed")){
                    throw new Exception(examineMessage);
                }

            }
            String permissionMessage=this.examineUtil.examinePermission(databasename,"alter");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }


            xmlUtil.alterTable(tableName, columnNames, columnTypes,action);

            return "Successfully execute " + sql;
        } else {
            return "Invalid ALTER TABLE statement";
        }
    }



    private String parseDropTable(String sql) throws Exception{
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }

        // 使用正则表达式解析 DROP TABLE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*drop\\s+table\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            if(!FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " does not exist");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"drop");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            FileUtil.deleteTable(databasename,tableName);
            return "Success";
        } else {
            throw new Exception("Invalid DROP TABLE statement: " + sql);
        }
    }
    private String parseDropDatabase(String sql) throws Exception{

        // 使用正则表达式解析 DROP DATABASE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*drop\\s+database\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            if(!FileUtil.findDatabases(dbName)){
                throw new Exception("Database " + dbName + " does not exist");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"drop");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            FileUtil.deleteDatabaseFolder(dbName);
            return "Success";
        } else {
            throw new Exception("Invalid DROP DATABASE statement: " + sql);
        }
    }
    private String parseCreateDatabase(String sql) throws  Exception{

        // 使用正则表达式解析 CREATE 语句
        FileUtil fileUtil = new FileUtil();

        Pattern pattern = Pattern.compile("(?i)^\\s*create\\s+database\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");

        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            if(FileUtil.findDatabases(dbName)){
                throw new Exception("Database " + dbName + " already exists");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"create");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            fileUtil.createDatabaseFolder(dbName);
            return "Success";
        } else {
            throw new Exception("Invalid CREATE DATABASE statement: " + sql);
        }

    }

    private String parseUseDatabase(String sql) throws Exception {
        // 使用正则表达式解析 USE DATABASE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*use\\s+database\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            if(FileUtil.findDatabases(dbName)){
                this.databasename = dbName;
                return "Successfully use database " + dbName;
            }else{
                throw new Exception("No DataBase Found " + sql);
            }
        } else {
            throw new Exception("Invalid USE DATABASE statement: " + sql);
        }
    }


    private String parseCreate(String sql) throws Exception {
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        // 使用正则表达式解析 CREATE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*create\\s+table\\s+(\\w+)\\s*\\((.*)\\)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String[] columnDefinitions = matcher.group(2).split("\\s*,\\s*");
            String[] columnNames = new String[columnDefinitions.length];
            String[] columnTypes = new String[columnDefinitions.length];

            for (int i = 0; i < columnDefinitions.length; i++) {
                String[] parts = columnDefinitions[i].trim().split("\\s+");
                columnNames[i] = parts[0];
                columnTypes[i] = parts[1];
            }

            String databaseFolderPath = "resources/static/databases/" + databasename;

            String tableFolderPath = databaseFolderPath + "/" + tableName;
            if(FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " already exists");
            }
            this.xmlUtil = new XMLUtil(tableFolderPath);
            String permissionMessage=new examineUtil().examinePermission(databasename,"create");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            // 调用 XMLUtil 创建表方法
            xmlUtil.createTable(tableName, columnNames, columnTypes);
            return "Successfully execute " + sql;
        } else {
            return "Invalid Create statement";
        }
    }






    private String parseInsert(String sql) throws Exception {
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        String regex = "^\\s*INSERT\\s+INTO\\s+(\\w+)\\s*\\((.*?)\\)\\s*VALUES\\s*(.*?)\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // 使用正则表达式解析 INSERT 语句
        Matcher matcher = pattern.matcher(sql);
        try {
            if (matcher.find()) {
                String tableName = matcher.group(1);
                String[] columnNames = matcher.group(2).split("\\s*,\\s*");
                String values = matcher.group(3);

                String databaseFolderPath = "resources/static/databases/" + databasename;

                String tableFolderPath = databaseFolderPath + "/" + tableName;
                if(!FileUtil.findTable(databasename,tableName)){
                    throw new Exception("Table " + tableName + " does not exist");
                }

                this.xmlUtil = new XMLUtil(tableFolderPath);
                this.examineUtil=new examineUtil(xmlUtil);
                // 处理多行值
                List<String[]> allValues = new ArrayList<>();
                Pattern valuesPattern = Pattern.compile("\\((.*?)\\)");
                Matcher valuesMatcher = valuesPattern.matcher(values);

                while (valuesMatcher.find()) {
                    String[] singleValues = valuesMatcher.group(1).split("\\s*,\\s*");

                     //去除每个字符串值的单引号或双引号
                    for (int i = 0; i < singleValues.length; i++) {
                        singleValues[i] = singleValues[i].replaceAll("[\"']", "");
                    }

                    allValues.add(singleValues);
                }
                String message= examineUtil.examineInsert(columnNames,allValues);
                if(!message.equals("Examine Insert Passed")){
                    return message;
                }
                String permissionMessage=new examineUtil().examinePermission(databasename,"insert");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }

                // 调用 XMLUtil 插入数据方法
                xmlUtil.insertMultipleData(tableName, columnNames, allValues);
                return "Successfully execute " + sql;

            } else {
                return "Invalid INSERT statement: " + sql;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private String parseUpdate(String sql) throws Exception {
        if (this.databasename == null) {
            throw new Exception("No Database Selected");
        }

        // 使用正则表达式解析 UPDATE 语句
        Pattern pattern = Pattern.compile("^\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)\\s+WHERE\\s+(.*?)\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        try {
            if (matcher.find()) {
                String tableName = matcher.group(1);
                String setClause = matcher.group(2);
                String whereClause = matcher.group(3);

                // 解析 SET 子句，获取列名和对应的值
                String[] setValues = setClause.split("\\s*,\\s*");
                Map<String, String> columnValues = new HashMap<>();
                for (String setValue : setValues) {
                    String[] parts = setValue.split("\\s*=\\s*");
                    String columnName = parts[0];
                    String columnValue = parts[1].replaceAll("[\"']", ""); // 去除单引号或双引号
                    columnValues.put(columnName, columnValue);
                }

                // 解析 WHERE 子句
                Map<String, String> conditions = parseWhereClause(whereClause);

                // 检查权限
                String permissionMessage = new examineUtil().examinePermission(databasename, "update");
                if (!permissionMessage.equals("Examine Permission Passed")) {
                    throw new Exception(permissionMessage);
                }
                String databaseFolderPath = "resources/static/databases/" + databasename;

                String tableFolderPath = databaseFolderPath + "/" + tableName;
                if(!FileUtil.findTable(databasename,tableName)){
                    throw new Exception("Table " + tableName + " does not exist");
                }
                this.xmlUtil= new XMLUtil(tableFolderPath);

                // 调用 XMLUtil 的更新数据方法
                xmlUtil.updateData(tableName, columnValues, conditions);
                return "Successfully execute " + sql;
            } else {
                throw new Exception("Invalid UPDATE statement: " + sql);
            }
        } catch (Exception e) {
            throw e;
        }
    }







    private String parseDelete(String sql) throws  Exception{
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        // 使用正则表达式解析 DELETE 语句
        Pattern pattern = Pattern.compile("^\\s*DELETE\\s+FROM\\s+(\\w+)\\s*(?:WHERE\\s+(\\w+)\\s*=\\s*['\"]?(.*?)['\"]?)?\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columnName = matcher.group(2);
            String value = matcher.group(3);
            String databaseFolderPath = "resources/static/databases/" + databasename;

            String tableFolderPath = databaseFolderPath + "/" + tableName;
            if(!FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " does not exist");
            }

            this.xmlUtil = new XMLUtil(tableFolderPath);
            this.examineUtil=new examineUtil(xmlUtil);
            String examineMessage= this.examineUtil.examineDelete(columnName);
            if(!examineMessage.equals("Examine Delete Passed")){
                return examineMessage;
            }



            // 检查是否提供了条件
            if (columnName != null && value != null) {
                // 调用 XMLUtil 删除数据方法
                String permissionMessage=new examineUtil().examinePermission(databasename,"delete");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }
                xmlUtil.deleteData(tableName, columnName, value);
                return "Successfully execute "+sql;
            } else {
                // 没有提供条件，则删除全部记录
                String permissionMessage=new examineUtil().examinePermission(databasename,"alter");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }
                xmlUtil.deleteData(tableName, null, null);
                return "Successfully execute "+sql;
            }
        } else {
            return "Invalid Delete statement: " + sql;
        }
    }


    private List<Map<String, String>> parseSelect(String sql) throws  Exception{
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        // 使用正则表达式解析 SELECT 语句
        Pattern pattern = Pattern.compile("^\\s*SELECT\\s+(.*?)\\s+FROM\\s+(\\w+)\\s*(?:WHERE\\s+(.*?))?\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        List<Map<String, String>> ret = new ArrayList<>();
        try {

            if (matcher.find()) {
                String columnsString = matcher.group(1);
                String tableName = matcher.group(2);
                String whereClause = matcher.group(3);

                // 解析列名
                List<String> columnNames;
                if (columnsString.equals("*")) {
                    // 如果选择所有列，则传递一个空列表
                    columnNames = Collections.emptyList();
                } else {
                    // 否则，将逗号分隔的列名字符串拆分为列表
                    columnNames = Arrays.asList(columnsString.split("\\s*,\\s*"));
                }

                // 处理 WHERE 子句中的条件表达式
                Map<String, String> conditions = parseWhereClause(whereClause);

                // 调用 XMLUtil 来处理查询
                String databaseFolderPath = "resources/static/databases/" + databasename;

                String tableFolderPath = databaseFolderPath + "/" + tableName;
                if(!FileUtil.findTable(databasename,tableName)){
                    throw new Exception("Table " + tableName + " does not exist");
                }

                this.xmlUtil = new XMLUtil(tableFolderPath);
                this.examineUtil=new examineUtil(xmlUtil);
                String examineMessage= this.examineUtil.examineSelect(columnNames);
                if(!examineMessage.equals("Examine Select Passed")){
                    throw new Exception(examineMessage);
                }
                String permissionMessage=new examineUtil().examinePermission(databasename,"select");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }
                ret = xmlUtil.selectData(tableName, columnNames, conditions);

            } else {
                throw new Exception("Invalid SELECT statement: " + sql);
            }
        }catch(Exception e){
            throw  e;
        }

        return ret;
    }


    // 解析 WHERE 子句中的条件表达式
    // 处理 WHERE 子句中的条件表达式
    private Map<String, String> parseWhereClause(String whereClause) {

        Map<String, String> conditions = new HashMap<>();
        if (whereClause != null && !whereClause.isEmpty()) {
            // 这里可以根据具体的 WHERE 子句格式进行解析
            // 这里假设 WHERE 子句中只有一个条件，并且条件格式为 "columnName = value"
            String[] parts = whereClause.split("\\s*=\\s*");
            if (parts.length == 2) {
                String value = parts[1].replaceAll("[\"']", ""); // 去掉值中的单引号或双引号
                conditions.put(parts[0], value);
            }
        }
        return conditions;
    }


}

