package bg.sofia.uni.fmi.mjt.splitwise.client;

import bg.sofia.uni.fmi.mjt.splitwise.client.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.response.Response;
import bg.sofia.uni.fmi.mjt.splitwise.client.exception.ServerNotWorkingException;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private Gson gson;

    private ByteBuffer buffer;

    private SocketChannel socketChannel;

    public Client() {
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        gson = new Gson();
    }

    public void run() throws ServerNotWorkingException {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        } catch (IOException e) {
            throw new ServerNotWorkingException("The server is not working", e);
        }
    }

    public void stop() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException("The connection could not be closed", e);
        }
    }

    public String sendRequest(Command command) {
        try {
            buffer.clear();
            buffer.put(gson.toJson(command).getBytes());
            buffer.flip(); // switch to reading mode
            socketChannel.write(buffer); // buffer drain

            buffer.clear(); // switch to writing mode
            socketChannel.read(buffer); // buffer fill
            buffer.flip(); // switch to reading mode

            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);

            return new String(byteArray, StandardCharsets.UTF_8); // buffer drain
        } catch (Exception e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}