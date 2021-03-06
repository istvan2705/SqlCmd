package ua.com.juja.sqlcmd.controller.command;

import org.junit.Before;
import org.junit.Test;
import ua.com.juja.sqlcmd.controller.DatabaseManager;
import ua.com.juja.sqlcmd.view.View;
import java.sql.SQLException;
import static org.mockito.Mockito.*;

public class ConnectTest  {

    public View view;
    public DatabaseManager manager;
    public Command command;

    @Before
    public void init() {
        manager = mock(DatabaseManager.class);
        view = mock(View.class);
        command = new Connect(manager, view);
    }

    @Test

    public void testConnectSuccessful() throws SQLException {
        String databaseName = "Academy";
        String userName = "postgres";
        String password = "1401198n";
        command.execute("connect|"+ databaseName+"|"+ userName +"|"+password);
        verify(manager).connect(databaseName, userName, password);
        view.write(String.format("You have login to database '%s' successfully!", databaseName));
            }

    @Test(expected = SQLException.class)
    public void testConnectFailed() throws SQLException {
        String databaseName = "Academy";
        String userName = "postgres";
        String password = "14011981";
        doThrow(new SQLException()).when(manager).connect(databaseName, userName ,password);
        manager.connect(databaseName, userName, password);
        verify(manager).connect(databaseName, userName, password);

    }
}
