package test;
/* Classe di Test che mi permette di testare tutte la maggior parte delle funzioni dell'applicazione senza usare l'interfaccia grafica,
 * quindi solamente usando la Console, è stata utile per la prima versione dell'applicazione, ormai superata da MainPlugIn
 */
import acronym.Acronimo;
import acronym.GestioneAcronimi;
import acronym.MappaAcronimi;
import document.GestioneDocumento;
import exceptions.AcronimoNonValidoException;
import exceptions.DocumentoNonValidoException;

import java.io.IOException;

public class MainTest {

    private static final String file = "document.tex";
    private static final String fileDizionario = "DizionarioAcronimi.txt";

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        GestioneDocumento b = new GestioneDocumento();
        GestioneAcronimi a = new GestioneAcronimi();
        MappaAcronimi m = new MappaAcronimi();

        try {
            b.readFile(file);			//leggo il documento .tex e lo copio nell'array
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            b.deleteComment();		// cancello i commenti del documento
            b.takeAcronymBlock();				// prendo solo la parte dedicata ad inizializzare gli acronimi
        } catch(DocumentoNonValidoException d) {
            d.printStackTrace();
        }
        System.out.println(b.toString());	// visualizzazione sulla Console dell'Array tramite la stringa
        try {
            a.findAcronym(b.getArraymk2());	//cerco gli acronimi nel documento e gli inserisco in una lista
        }	catch (AcronimoNonValidoException e) {
            e.printStackTrace();
        }
        a.acronymCheck(m.getMap());	//verifico la correttezza degli acronimi

        try {
            m.addAcronymToMap(new Acronimo("DOC","Denominazione di Origine Controllata"));  // aggiungo un nuovo acronimo al dizionario
        } catch (IOException | AcronimoNonValidoException e) {
            e.printStackTrace();
        }
        System.out.println(a.toString());				// stampa su Console la lista degli acronimi e la lista degli acronimi errati
        System.out.println(a.AcronymListToString());		// stampa su Console la lista degli acronimi
        System.out.println(a.ErrorListToString());			// stampa su Console la lista degli acronimi errati
        System.out.println(m.toString());				// Stampo su Console la stringa della mappa degli acronimi
        a.autoCorrection(b.getArray());
        try {
            b.overwrite();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}