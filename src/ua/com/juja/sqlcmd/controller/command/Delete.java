package ua.com.juja.sqlcmd.controller.command;

import ua.com.juja.sqlcmd.model.DataSet;
import ua.com.juja.sqlcmd.model.DatabaseManager;
import ua.com.juja.sqlcmd.view.View;

public class Delete implements Command {
   private DatabaseManager manager;
   private View view;

    public Delete(DatabaseManager manager, View view) {
        this.manager = manager;
        this.view = view;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith("delete|");
    }

    @Override
    public void process(String command) {
        String[] data = command.split("\\|");
        try {
            String tableName = data[1];
            String columnName = data[2];
            String rowName = data[3];

            if (manager.tableExist(tableName)&& data.length == 4) {
                manager.deleteRows(tableName, columnName, rowName);
                String[] columns = manager.getColumnsNames(tableName);
                printColumnsNames(columns);

                DataSet[] rows = manager.getTableRows(tableName);
                printTable(rows);


            }
            else if(data.length > 4){
                view.write("You have entered more parameters than needed. Please enter only existing");
            }
            else{
                view.write("The table " + tableName + " does not exist. Please enter existing");

            }

        }catch (IndexOutOfBoundsException e) {
            view.write("You have not entered all needed parameters or names of parameters are not correct.");
        }




    }
    private void printColumnsNames(String[] columns) {
        String result ="|";
        for (String column : columns) {
            result += column + "|";
        }
        view.write("--------------------------");
        view.write(result);
        view.write("--------------------------");
    }
    private void printTable(DataSet[] tableData) {
        for (DataSet row : tableData) {
            printRow(row);
        }
        view.write("--------------------");
    }

    private void printRow(DataSet row) {
        Object[] values = row.getValues();
        String result = "|";
        for (Object value : values) {
            result += value + "|";
        }
        view.write(result);
    }

}