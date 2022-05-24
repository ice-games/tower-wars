package club.icegames.towerwars.core.exeptions;

/**
 * @author Seailz
 */
public class PlayerIsAlreadyInGameException extends Exception {
    public PlayerIsAlreadyInGameException(String message) {
        super(message);
    }
}
