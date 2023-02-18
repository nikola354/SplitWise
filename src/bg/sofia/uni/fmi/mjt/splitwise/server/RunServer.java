package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.communication.Server;
import bg.sofia.uni.fmi.mjt.splitwise.server.communication.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.database.Database;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.database.FileDatabase;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise.SplitWise;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise.SplitWiseAPI;

public class RunServer {
    private static final int SERVER_PORT = 7777;
    private static final String MAIN_DATABASE_FILE = "database";

    protected static final String SPLIT = "You owe %s %.2f BGN [%s]";

    public static void main(String[] args) {
        Database database = new FileDatabase(MAIN_DATABASE_FILE);

        SplitWiseAPI splitWise = new SplitWise(database);

        CommandExecutor commandExecutor = new CommandExecutor(splitWise);
        Server server = new Server(SERVER_PORT, commandExecutor);
        server.start();
    }
}