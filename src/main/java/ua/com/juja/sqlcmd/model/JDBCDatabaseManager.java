package ua.com.juja.sqlcmd.model;

import java.sql.*;
import java.util.*;


public class JDBCDatabaseManager implements DatabaseManager {

    private Connection connection;


   @Override
    public boolean create(String tableName, DataSet columns) throws SQLException {
                String columnNames = getNameFormatted(columns, "%s text NOT NULL,");
                String sql = "CREATE TABLE IF NOT EXISTS public." + tableName + "(" + columnNames + ")";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
               return isUpdateTable(ps);

            }
         }

    @Override
    public void deleteTable(String tableName) throws SQLException {
            try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE public." + tableName);
            }
        }

    @Override
    public Set<String> getColumnsNames(String tableName) throws SQLException {
       Set<String> columns = new LinkedHashSet<>();
            DatabaseMetaData metadata = connection.getMetaData();
        try (
            ResultSet resultSet = metadata.getColumns(null, null, tableName, null)){
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME"));
            }
            return columns;
        }
    }

    @Override
    public List<DataSet> getTableRows(String tableName) throws SQLException {
        List<DataSet> result = new LinkedList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM public." + tableName)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                DataSet dataSet = new DataSet();
                result.add(dataSet);
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                  dataSet.put(rsmd.getColumnName(i), rs.getObject(i));
                }
            }

                return result;
            }
    }
    @Override
    public boolean clear(String tableName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM public." + tableName)) {
            return isUpdateTable(ps);

        }
    }

    @Override
    public Set<String> getTableNames() throws SQLException{
       Set<String> columns = new LinkedHashSet<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")) {
            while (rs.next()) {
                columns.add(rs.getString("table_name"));
            }
            return columns;
        }
    }

    @Override
    public void connect(String database, String user, String password) throws SQLException  {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
           throw new SQLException("Please add jdbc jar to project.", e);

        }
         connection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/" + database, user,
           password);
        }

    @Override
    public boolean isConnected() {
        return connection != null;

    }

    @Override
    public boolean insert(String tableName,DataSet set, String primaryKey) throws SQLException  {
         String columns = getNameFormatted(set, "%s,");
            String values = getValuesFormatted(set, "'%s',");
           String insertData = "INSERT INTO public." + tableName + "(" + columns + ")" +
                   "VALUES (" + values + ")" + " ON CONFLICT " + "(" + primaryKey + ")" + " DO NOTHING";
          try(PreparedStatement ps = connection.prepareStatement(insertData)){
                return isUpdateTable(ps);
               }
        }

    @Override
    public boolean deleteRows(String tableName,String columnName, String rowName) throws SQLException  {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM public." + tableName + " WHERE " + columnName + " = ?")){
             ps.setString(1, rowName);
          return isUpdateTable(ps);
                    }
                }

    @Override
    public boolean update(String tableName, String id, DataSet set) throws SQLException  {

       String columns = getNameFormatted(set, "%s = ?,");
        try (PreparedStatement ps = connection.prepareStatement("UPDATE public." + tableName + " SET " + columns + " WHERE id = ?")) {
            int index = 1;
            for (Object value : set.getValues()) {
                ps.setObject(index, value);
                index++;
            }
            ps.setString(index, id);
          return isUpdateTable(ps);
        }
   }

    @Override
    public boolean isUpdateTable(PreparedStatement ps) throws SQLException {
      return ps.executeUpdate() > 0;

    }

    @Override
    public String getNameFormatted(DataSet name, String format) {
        String names = "";
        for (String newName : name.getNames()) {
            names += String.format(format, newName);
        }
        names = names.substring(0, names.length() - 1);
        return names;
    }

    @Override
    public String getValuesFormatted(DataSet input, String format) {
        String values = "";
        for (Object value : input.getValues()) {
            values += String.format(format, value);
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

}