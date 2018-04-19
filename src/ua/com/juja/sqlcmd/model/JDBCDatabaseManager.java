package ua.com.juja.sqlcmd.model;

import org.postgresql.util.PSQLException;
import ua.com.juja.sqlcmd.view.View;

import java.sql.*;
import java.util.Arrays;


public class JDBCDatabaseManager implements DatabaseManager {

    private Connection connection;
    private View view;

    public JDBCDatabaseManager(View view) {
        this.view = view;
    }

    public void create(DataSet column, String tableName) {
        try (Statement stmt = connection.createStatement()) {
            String columnNames = getColumnFormated(column, "%s text NOT NULL, ");
            String sql = "CREATE TABLE IF NOT EXISTS public." + tableName + "(" + columnNames + ")";
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            view.write(String.format("Table '%s' already exists", tableName));
        }
    }

    public boolean tableExist(String tableName) {
        try {
            boolean tExists = false;
            try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
                while (rs.next()) {
                    String tName = rs.getString("TABLE_NAME");
                    if (tName != null && tName.equals(tableName)) {
                        tExists = true;
                        break;
                    }
                }
                return tExists;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void deleteTable(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE public." + tableName);

        } catch (SQLException e) {
            view.write(String.format("The table '%s' does not exist. Please enter only existing", tableName));

        }
    }

    public String[] getColumnsNames(String tableName) {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet resultSet = metadata.getColumns(null, null, tableName, null);
            String[] columns = new String[100];//todo magic number
            int index = 0;
            while (resultSet.next()) {
                columns[index++] = resultSet.getString("COLUMN_NAME");
            }
            columns = Arrays.copyOf(columns, index, String[].class);
            return columns;

        }

        catch (SQLException e) {
            view.write("The entered table does not exist. Please enter existing");

            view.write("List of existing tables: ");
            getTableNames();
            return new String[0];

        }
    }
    @Override
    public DataSet[] getTableRows(String tableName) {
        int size = getSize(tableName);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM public." + tableName))
        {
            ResultSetMetaData rsmd = rs.getMetaData();
            DataSet[] result = new DataSet[size];
            int index = 0;
            while (rs.next()) {
                DataSet dataSet = new DataSet();
                result[index++] = dataSet;
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    dataSet.put(rsmd.getColumnName(i), rs.getObject(i));
                }
            }
            return result;
        } catch (SQLException e) {
            view.write("The entered table does not exist. Please enter existing");
            return new DataSet[0];
        }
    }

    private int getSize(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) FROM public." + tableName)) {
            rsCount.next();
            int size = rsCount.getInt(1);
            return size;
        } catch (SQLException e) {
            view.write("The entered table does not exist. Please enter existing");
            return 0;
        }
    }

    public void clear(String tableName) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM public." + tableName);

        } catch (SQLException e) {
            view.write(String.format("The table '%s' does not exist. Please enter only existing", tableName));
        }
    }

    public String[] getTableNames() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")) {
            String[] tables = new String[100]; //TODO magic number
            int index = 0;
            while (rs.next()) {
                tables[index++] = rs.getString("table_name");
            }
            tables = Arrays.copyOf(tables, index, String[].class);
            return tables;

        } catch (SQLException e) {
            view.write(String.format("The entered table does not exist. Please enter only existing"));
            return new String[0];
        }
    }

    public void connect(String database, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            view.write("Please add jdbc jar to project.");
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }

            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/" + database, user,
                    password);
        } catch (SQLException e) {
            view.write(String.format("Cant get connection for database:%s user:%s", database, user));
            e.printStackTrace();
            connection = null;
        }
    }

    public boolean isConnected() {
        return connection != null;

    }

    public void insert(String tableName, DataSet set, String constraint) {
        try (Statement stmt = connection.createStatement()) {
            String columns = getNameFormated(set, "%s,");
            String values = getValuesFormated(set, "'%s',");
            stmt.executeUpdate("INSERT INTO public." + tableName + "(" + columns + ")" +
                    "VALUES (" + values + ")" + " ON CONFLICT " + "(" + constraint + ")" + " DO NOTHING");
            view.write(String.format("Statement are added into the table '%s'", tableName));
        }
         catch (SQLException e) {
           view.write("The number of entered parameters does not correspond to the number of columns in the table or you have entered wrong names of parameters");

        }


    }

    public void deleteRows(String tableName, String columnName, String rowName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM public." + tableName);
             PreparedStatement ps = connection.prepareStatement("DELETE FROM public." + tableName + " WHERE " + columnName + " = ?")) {
            ResultSetMetaData rm = rs.getMetaData();

            for (int i = 1; i <= rm.getColumnCount(); i++) {

                if (columnName.equals(rm.getColumnName(i))) {
                    ps.setString(1, rowName);
                    int countUpdatedRows = ps.executeUpdate();
                    String a = countUpdatedRows > 0 ? "The row has been deleted" : "The row has been not deleted. Please enter correct parameters";
                    System.out.println(a);
                }

            }
        } catch (SQLException e) {
            view.write("You have entered not existing parameters. Please enter only existing");

        }
    }

    public void update(String tableName,DataSet set, String id) {

        String columns = getNameFormated(set, "%s = ?,");
        try (PreparedStatement ps = connection.prepareStatement("UPDATE public." + tableName + " SET " + columns + " WHERE id = ?")) {
            int index = 1;
            for (Object value : set.getValues()) {
                ps.setObject(index, value);
                index++;
            }
            ps.setString(index, id);
            int countUpdatedRows = ps.executeUpdate();
            String a = countUpdatedRows > 0 ? "The row has been updated" : "The row has been not updated. Please enter correct parameters";
            System.out.println(a);
        } catch (SQLException e) {
            view.write("You have entered not existing parameters. Please enter only existing");
        }
    }


    private String getNameFormated(DataSet name, String format) {
        String names = "";
        for (String newName : name.getNames()) {
            names += String.format(format, newName);
        }
        names = names.substring(0, names.length() - 1);
        return names;
    }

    private String getValuesFormated(DataSet input, String format) {
        String values = "";
        for (Object value : input.getValues()) {
            values += String.format(format, value);
        }
        values = values.substring(0, values.length() - 1);
        return values;
    }

    private String getColumnFormated(DataSet newValue, String format) {
        String names = "";
        for (String name : newValue.getNames()) {
            names += String.format(format, name);
        }
        names = names.substring(0, names.length() - 2);
        return names;

    }


}