package exceptions;

public class AcronimoNonValidoException extends Exception {

    public AcronimoNonValidoException(String msg) {
        super("Acronimo non valido, " + msg);

    }

}
