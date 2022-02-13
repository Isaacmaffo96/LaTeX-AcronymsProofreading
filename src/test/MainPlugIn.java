package test;
/* Main dell'applicazione con l'interfaccia grafica,
 * bisogna specificare il nome dei file di cocumento e dizionario
 */

import acronym.GestioneAcronimi;
import acronym.MappaAcronimi;
import document.GestioneDocumento;
import exceptions.AcronimoNonValidoException;
import exceptions.DocumentoNonValidoException;
import swing.Screen;

import javax.swing.*;
import java.io.IOException;

public class MainPlugIn extends JFrame {

    private static final String file = "LaTeX-AcronymsProofreading/dispensa/document.tex";
    private static final String fileDizionario = "LaTeX-AcronymsProofreading/dispensa/DizionarioAcronimi.txt";

    public static void main(String[] args) throws IOException {

        GestioneAcronimi a = new GestioneAcronimi();
        GestioneDocumento b = new GestioneDocumento();
        MappaAcronimi m = new MappaAcronimi();
        Screen screen = new Screen(a,b,m);

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
            try {
            a.findAcronym(b.getArraymk2());	//cerco gli acronimi nel documento e gli inserisco in una lista
            }	catch (AcronimoNonValidoException e) {
            e.printStackTrace();
        }
            a.acronymCheck(m.getMap());	//verifico la correttezza degli acronimi
            screen.setAcronym(a.getErrorList());
            screen.setLocation(500,150);
            screen.setVisible(true);
    }

    public static String getFile() {
        return file;
    }

    public static String getFileDizionario() {
        return fileDizionario;
    }
}
