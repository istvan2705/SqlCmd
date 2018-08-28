package ua.com.juja.sqlcmd.controller.command;

import ua.com.juja.sqlcmd.model.DataSet;
import ua.com.juja.sqlcmd.model.DataSetImpl;
import ua.com.juja.sqlcmd.model.DatabaseManager;
import ua.com.juja.sqlcmd.view.View;

import java.sql.SQLException;
import java.util.List;

public class Drop  implements Command {
    private DatabaseManager manager;
    private View view;
    private DataSet data = new DataSetImpl();
    public Drop(DatabaseManager manager, View view) {
        this.manager = manager;
        this.view = view;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith("drop|");
    }

    @Override
    public void process(String command) {
        List<String> parameters = data.getParameters(command);
        if (parameters.size() != 2) {
            view.write(String.format(ERROR_ENTERING_MESSAGE + "'drop|tableName'", command));
            return;
        }
        String tableName = data.getTableName(command);

        try {
            manager.deleteTable(tableName);
            view.write(String.format("The table '%s' has been deleted", tableName));
        } catch (SQLException e) {
            view.write(String.format(SQL_EXCEPTION_MESSAGE, e.getMessage()));
        }
    }
}
