package bg.sofia.uni.fmi.mjt.splitwise.client.io;

public interface UserInterface {
    public abstract String read();

    public abstract void write(String text);

    public abstract void writeError(String error);
}
