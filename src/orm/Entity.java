package orm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entity {
    //=====================================

    private int id = -1;
    private Field[] fields;
    private dbAdapter dbA;
    //=====================================

    public Entity() {
        //inspect(this.getClass());
    }
    //=====================================

    public int getId() {
        return id;
    }
    //=====================================

    private void setId(int id) {
        this.id = id;
    }
    //=====================================

    <T> void inspect(Class<T> klazz) {

        fields = klazz.getDeclaredFields();

//        System.out.printf("%d fields:%n", fields.length);
//        for (Field field : fields) {
//            System.out.printf("%s %s %s%n",
//                    Modifier.toString(field.getModifiers()),
//                    field.getType().getSimpleName(),
//                    field.getName()
//            );
//        }
    }
    //=====================================

    public void Insert() {
        try {
            dbA = dbAdapter.getInstance();
            //System.out.println("dbAdapter : " + dbA.getConnection().getCatalog());
            inspect(this.getClass());
            DatabaseMetaData meta = dbA.getConnection().getMetaData();
            ResultSet res = meta.getTables(null, null, this.getClass().getName(), new String[]{"TABLE"});
            if (!res.first()) {
                //Create table
                createTable();
            }
            else if(res.first()){
            }
            //check if already exists
            if (this.getId() != -1) {
                //update
                //System.out.println("UPDATE!!");
                update();
            } else {
                //Insert
                InsertRowinDatabase();
            }

            //=====================================
        } catch (SQLException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //=====================================
    public int execQuery(String query, boolean is_insert) {
        try {
            if (is_insert) {
                //System.out.println("exe insert");
                dbA.getStmt().executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = dbA.getStmt().getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } else {
                //System.out.println("exec not insert");
                dbA.getStmt().executeUpdate(query);
            }
        } catch (SQLException e) {
            System.err.println("execQuery : " + e.getMessage());
        }
        return -1;
    }

    //====================================
    public ResultSet execSQL(String query) {
        try {
            return dbA.getStmt().executeQuery(query);
        } catch (SQLException e) {
            System.err.println("execSQL : " + e.getMessage());
        }
        return null;
    }
//====================================

    public void createTable() {
        String query = "CREATE TABLE " + this.getClass().getName() + " ( _id int NOT NULL AUTO_INCREMENT, PRIMARY KEY(_id), ";
        for (int i = 0; i < fields.length; i++) {
            String toBeAdded; //Temp variable to be concatenated to String query
            String name = fields[i].getName(); //Get field name
            String type = turnItIntoSQL(fields[i].getType().getName()); //Get field type
            if (type != null) {
                if (i == fields.length - 1) {
                    toBeAdded = name + " " + type + ");"; //If it's the last field, then close the query brackets
                } else {
                    toBeAdded = name + " " + type + ", "; //If it's not the last field, then wait for another field
                }
                query = query + toBeAdded;
            }
        }
        System.out.println(query);
        execQuery(query, false);
    }
    //=====================================

    public void InsertRowinDatabase() {
        String insertion_Query = "INSERT INTO " + this.getClass().getName() + " ( ";
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if (!fields[i].getName().equals("_id"))//skip id
            {
                //System.out.println(" TYPE : " + fields[i].getName());
                if (isItValidType(fields[i].getType().getName())) {
                    if (i == fields.length - 1) {
                        insertion_Query += fields[i].getName();
                    } else {
                        insertion_Query += fields[i].getName() + ", ";
                    }
                }

            }
        }
        insertion_Query += " ) VALUES ( ";
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if (!fields[i].getName().equals("_id"))//skip id
            {
                try {
                    if (isItValidType(fields[i].getType().getName())) {
                        if (i == fields.length - 1) {
                            insertion_Query += "\'" + fields[i].get(this) + "\'";
                        } else {
                            insertion_Query += "\'" + fields[i].get(this) + "\', ";
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        insertion_Query += " ) ;";

        System.out.println(insertion_Query);
        int id = execQuery(insertion_Query, true);
        this.setId(id);
        //System.out.println("ID : " + id);
    }
    //=====================================

    public String turnItIntoSQL(String notSQL) {
        //Turn a type for Java into a type for SQL
        switch (notSQL) {
            case "boolean":
                return "BOOLEAN";
            case "int":
                return "INTEGER";
            case "float":
                return "REAL";
            case "double":
                return "REAL";
            case "java.lang.String":
                return "VARCHAR(255)";
            case "java.util.ArrayList":
                return "Array";
            case "java.util.List":
                return "ArrayList";
            default:
                return null;
        }
    }

    //========================================
    public boolean isItValidType(String type) {
        if (type != "boolean" && type != "int" && type != "float" && type != "double" && type != "java.lang.String" && type != "java.util.ArrayList" && type != "java.util.List") {
            return false;
        }
        return true;
    }

    //========================================
    private void update() {
        ArrayList<Boolean> fields_to_Update = new ArrayList<Boolean>(Collections.nCopies(fields.length, Boolean.FALSE));
        String select_Query = "SELECT * FROM " + this.getClass().getName() + " WHERE _id = \'" + this.getId() + "\'";
        ResultSet res = execSQL(select_Query);
        if (res != null) {
            try {
                if (res.first()) {
                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);
                        if (!fields[i].getName().equals("_id"))//skip id
                        {
                            if (isItValidType(fields[i].getType().getName())) {
                                if (fields[i].getType().getName() == "java.lang.String") {
                                    String val = res.getString(fields[i].getName());
                                    if (val != fields[i].get(this)) {
                                        fields_to_Update.set(i, Boolean.TRUE);
                                    }
                                } else if (fields[i].getType().getName() == "int") {
                                    int val = res.getInt(fields[i].getName());
                                    if (val != (int) fields[i].get(this)) {
                                        fields_to_Update.set(i, Boolean.TRUE);
                                    }
                                } else if (fields[i].getType().getName() == "float") {
                                    float val = res.getFloat(fields[i].getName());
                                    if (val != (float) fields[i].get(this)) {
                                        fields_to_Update.set(i, Boolean.TRUE);
                                    }
                                } else if (fields[i].getType().getName() == "double") {
                                    double val = res.getDouble(fields[i].getName());
                                    if (val != (double) fields[i].get(this)) {
                                        fields_to_Update.set(i, Boolean.TRUE);
                                    }
                                } else if (fields[i].getType().getName() == "long") {
                                    long val = res.getLong(fields[i].getName());
                                    if (val != (long) fields[i].get(this)) {
                                        fields_to_Update.set(i, Boolean.TRUE);
                                    }
                                } else if (fields[i].getType().getName() == "boolean") {
                                    boolean val = res.getBoolean(fields[i].getName());
                                    if (val != (boolean) fields[i].get(this)) {
                                        fields_to_Update.set(i, Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        String print = "Values : ";
//        for (int i = 0; i < fields_to_Update.size(); i++) {
//            print += " index  : " + i + " value " + fields_to_Update.get(i) + "\n";
//        }
//        System.out.println(print);
        String update_Query = "UPDATE " + this.getClass().getName() + " SET ";
        String values = "";
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if (!fields[i].getName().equals("_id"))//skip id
            {
                if (i == fields.length - 1) {

                    try {
                        if (isItValidType(fields[i].getType().getName())) {
                            if (fields_to_Update.get(i)) {
                                values += fields[i].getName() + " = ";
                                values += "\'" + fields[i].get(this) + "\'";
                            }
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        if (isItValidType(fields[i].getType().getName())) {
                            if (fields_to_Update.get(i)) {
                                values += fields[i].getName() + " = ";
                                values += "\'" + fields[i].get(this) + "\', ";
                            }
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }
        if (!values.isEmpty()) {
            if (values.endsWith(", ")) {
                values = values.replace(", ", "");
            }
            update_Query += values + " WHERE _id = \'" + this.getId() + "\' ;";
            System.out.println(update_Query);
            execQuery(update_Query, false);
        }
    }

}
