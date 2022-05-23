package club.icegames.towerwars.core.exeptions;

public class PlayerIsAlreadyInGameException extends Exception {
    public PlayerIsAlreadyInGameException(String message) {
        super(message);
    }
}
