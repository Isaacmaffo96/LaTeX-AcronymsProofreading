package exceptions;
/* Viene lanciata quando riscontro problemi a lavorare sul documento
 * ad esempio quando manca \begin{document}
 * oppure apro un ambiente commento e non lo chiudo ecc..
 *  */
public class DocumentoNonValidoException extends Exception {

    public DocumentoNonValidoException(String msg) {
        super("Documento non valido, " + msg);

    }

}
