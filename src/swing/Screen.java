package swing;

/* Classe che mi permette di curare l'interfaccia grafica e il suo funzionamento
 * Richede gli oggetti delle classi di GestioneAcronimi, GestioneDocumento e MappaAcronimi
 * L'interfaccia semplice mostra di default la lista degli acronimi errati nel documento
 * e per ognuno mostra le informazioni di: - Nome esteso - Problema riscontrato - Correzione Proposta
 * In base al tipo di errore offre la possibilità di Correggerlo, Ignorarlo oppure Aggiungerlo al dizionario
 * Vengono proposte più correzioni quando l'acronimo ha più nomi estesi diversi, offrendo la possibilità di scegliere quella più adatta
 * Inoltre viene offerta la possibilità della correzione automatica, che corregge automaticamente solo gli acronimi con nome esteso errato, scegliendo per ogniuno la prima correzione disponibile
 * E' anche possibile visualizzare la lista completa degli acronimi, avendo comunque a disposizione gli strumenti di correzione
 * messaggi pop-up quando necessario comunicano l'esito delle varie operazioni
 */

import acronym.Acronimo;
import acronym.GestioneAcronimi;
import acronym.MappaAcronimi;
import document.GestioneDocumento;
import exceptions.AcronimoNonValidoException;
import exceptions.DocumentoNonValidoException;
import test.MainPlugIn;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Screen extends JFrame {
    private JPanel pannelMain;
    private JList acronymList;
    private JTextField textAcronym;
    private JTextField textProblem;
    private JButton aggiungiAlDizionarioButton;
    private JLabel labelErrorList;
    private JComboBox correctionsComboBox;
    private JLabel labelAcronimo;
    private JLabel labelProblem;
    private JLabel labelCorrection;
    private JButton automaticCorrectionButton;
    private JButton removeErrorButton;
    private JButton completeListButton;
    private JButton correctionButton;
    private JScrollPane listScrollPane;
    private final DefaultListModel ListAcronymModel;
    private final DefaultComboBoxModel CorrectionsModel;
    private final ArrayList<Acronimo> acronimi;
    private final List<String> correzioni;
    private final GestioneAcronimi gestioneAcronimi;
    private final GestioneDocumento gestioneDocumento;
    private final MappaAcronimi mappaAcronimi;
    private final String textErrorList = "Lista Acronimi errati";
    private final String textErrorListButton = "Visualizza Acronimi errati";
    private final String textAcronymList = "Lista completa Acronimi";
    private final String textAcronymListButton = "Visualizza Lista Completa";


    public Screen(GestioneAcronimi gestioneAcronimi, GestioneDocumento gestioneDocumento, MappaAcronimi mappaAcronimi) {
        super("LaTeX Proofreading Acronym");
        this.setContentPane(this.pannelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.gestioneAcronimi = gestioneAcronimi;
        this.gestioneDocumento =gestioneDocumento;
        this.mappaAcronimi=mappaAcronimi;
        aggiungiAlDizionarioButton.setEnabled(false);   // disattivo il pulsante aggiungi al dizionario
        removeErrorButton.setEnabled(false);            // disattivo il pulsante rimuovi errore
        correctionButton.setEnabled(false);             // disattivo il pulsante Correggi
        correctionsComboBox.setEnabled(false);          // disattivo il ,emu a tendina delle correzioni
        acronimi = new ArrayList<>();                   // lista acronimi
        ListAcronymModel=new DefaultListModel();
        acronymList.setModel(ListAcronymModel);
        correzioni = new ArrayList<>();                  // lista correzioni
        CorrectionsModel=new DefaultComboBoxModel();
        correctionsComboBox.setModel(CorrectionsModel);

        acronymList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListAcronymSelection(e);
            }
        });
        aggiungiAlDizionarioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ButtonDizionarioClick(e);
            }
        });
        automaticCorrectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ButtonAutomaticCorrection(e);
            }
        });
        removeErrorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAcronymError(e);
            }
        });
        completeListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(completeListButton.getText().equals(textAcronymListButton))  // se sto visualizzando la lista completa degli acronimi
                    changeList(e,gestioneAcronimi.getAcronymList(),textAcronymList, textErrorListButton);
                else                                                            // se sto visualizzando la lista degli errori
                    changeList(e,gestioneAcronimi.getErrorList(),textErrorList, textAcronymListButton);
            }
        });
        correctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                singleCorrection(e);
            }
        });
    }

      private void ListAcronymSelection(ListSelectionEvent e) {
        int acronymNumber = acronymList.getSelectedIndex();
        aggiungiAlDizionarioButton.setEnabled(false);                           // disattivo il pulsanti
        removeErrorButton.setEnabled(false);
        correctionButton.setEnabled(false);
        correctionsComboBox.setEnabled(false);
        labelCorrection.setText("Correzione proposta");
        removeAllCorrections();
        if(acronymNumber >=0){
            Acronimo a = acronimi.get(acronymNumber);
            textAcronym.setText(a.getExtendedName());                           // nome esteso acronimo
            try {
                textProblem.setText(a.getProblem());                            // problema riscontrato
            } catch (AcronimoNonValidoException acronimoNonValidoException) {
                acronimoNonValidoException.printStackTrace();
            }
            if(a.getCode()==2 || a.getCode()==3)    removeErrorButton.setEnabled(true);  // attivo il pulsante rimuovi errore solo se l'acronimo è errato
            if(a.getCode()==2 || a.getCode()==3){                                        // acronimo non trovato nel dizionario oppure nome esteso non corretto
                aggiungiAlDizionarioButton.setEnabled(true);                             // attivo il pulsante aggiungi al dizionario solo se l'acronimo non è presente nel dizionario
                aggiungiAlDizionarioButton.setText("Aggiungi al Dizionario");
                correzioni.add("↓ Premi per aggiungerlo al dizionario ↓");               // tenendo comunque il menu a tendina disabilitato, mostro il messaggio
                RefreshCorrectionsList();
                correctionsComboBox.setSelectedIndex(0);
                correzioni.clear();
            }
            if(a.getCode()==3) {
                correctionButton.setEnabled(true);                     // attivo il pulsante Correggi solo se il nome esteso è errato
                correctionsComboBox.setEnabled(true);                  // attivo il menu a tendina
                addCorrections(a);
                if(a.getCorrection().size()>1) {                         // se la lista delle correzioni contiene più di un elemento
                    labelCorrection.setText("Correzioni proposte");    // cambio il nome al plurale
                    int indexOptimal = correzioni.indexOf(a.getOptimalCorrection());
                    correctionsComboBox.setSelectedIndex(indexOptimal);
                }
                else
                    correctionsComboBox.setSelectedIndex(0);
                correzioni.clear();
            }
            if(a.getCode()==5){
                aggiungiAlDizionarioButton.setEnabled(true);                             // attivo il pulsante aggiungi al dizionario solo se l'acronimo non è presente nel dizionario
                aggiungiAlDizionarioButton.setText("Rimuovi dal Dizionario");
                correzioni.add("↓ Premi per rimuoverlo dal dizionario ↓");               // tenendo comunque il menu a tendina disabilitato, mostro il messaggio
                RefreshCorrectionsList();
                correctionsComboBox.setSelectedIndex(0);
                correzioni.clear();
            }
        }
    }

     private void ButtonDizionarioClick(ActionEvent e) {
            int acronymNumber = acronymList.getSelectedIndex();             // index della lista
            if(acronymNumber >=0) {
                Acronimo a = acronimi.get(acronymNumber);
                Component frame = null;                                     // messaggio pop up di avvenuta aggiunta al dizionario
                if (a.getCode() == 5) {                                     // azione richiesta: rimuovere dal dizionario
                    try {
                        gestioneAcronimi.removeAcroFromDictionaryList(mappaAcronimi.getMap(),a,mappaAcronimi);
                        JOptionPane.showMessageDialog(frame, a.getAcronym() + " - " + a.getExtendedName() + "\n Rimosso dal dizionario");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        JOptionPane.showMessageDialog(frame, a.getAcronym() + " - " + a.getExtendedName() +
                                "\n Non è stato possibile rimuoverlo dal dizionario", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                    finally {
                        aggiungiAlDizionarioButton.setEnabled(false);
                        RefreshAcronymList();
                        clearField();
                    }
                }
                else {                                                      // azione richiesta: aggiungere al dizionario
                    try {
                        gestioneAcronimi.addInDictionary(a, mappaAcronimi.getMap());
                        JOptionPane.showMessageDialog(frame, a.getAcronym() + " - " + a.getExtendedName() + "\n Aggiunto al dizionario");
                    } catch (IOException | AcronimoNonValidoException ioException) {
                        ioException.printStackTrace();
                        JOptionPane.showMessageDialog(frame, a.getAcronym() + " - " + a.getExtendedName() +
                                "\n Acronimo già presente nel dizionario", "Errore", JOptionPane.ERROR_MESSAGE);
                    } finally {                       // ad ogni modo rimuovo l'acronimo dalla lista degli errori
                        if(completeListButton.getText().equals(textAcronymListButton))
                            removeAcronym(a);
                        else
                            RefreshAcronymList();
                        gestioneAcronimi.removeAcroError(a);
                        aggiungiAlDizionarioButton.setEnabled(false);
                        clearField();
                    }
                }
            }
    }

    private void ButtonAutomaticCorrection(ActionEvent e) { // correzione automatica con la correzione proposta automaticamente per ogni acronimo
        gestioneAcronimi.autoCorrection(gestioneDocumento.getArray());
        Component frame = null;                             // messaggio pop up di avvenuta correzione
        try {
            gestioneDocumento.overwrite();
            update();
            JOptionPane.showMessageDialog(frame, "Documento Corretto");
        } catch (IOException ioException) {
            ioException.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Errore durante la correzione","Errore",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void singleCorrection(ActionEvent e){       // correggo l'acronimo selezionato con la correzione selezionata
        int acronymNumber = acronymList.getSelectedIndex();
        if(acronymNumber >=0) {
            Acronimo a = acronimi.get(acronymNumber);
            Component frame = null;                     // messaggio pop up di avvenuta correzione
            try {
                gestioneAcronimi.correctAcronym(gestioneDocumento.getArray(), a,correctionsComboBox.getSelectedIndex());
                JOptionPane.showMessageDialog(frame, "Acronimo " + a.getAcronym() + " Corretto in\n" + correctionsComboBox.getItemAt(correctionsComboBox.getSelectedIndex()) );
                clearField();                           // ripristino i campi
                gestioneDocumento.overwrite();          // riscrivo il documento
                update();                               // aggiorno le liste
            } catch (AcronimoNonValidoException | IOException acronimoNonValidoException) {
                acronimoNonValidoException.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Errore durante la correzione\n" +
                        acronimoNonValidoException.getMessage() ,"Errore",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAcronymError(ActionEvent e){     // ignoro l'errore
        int acronymNumber = acronymList.getSelectedIndex();
        if(acronymNumber >=0) {
            Acronimo a = acronimi.get(acronymNumber);
            gestioneAcronimi.ignoreError(a);                    // ignoro l'acronimo errato
            if(labelErrorList.getText().equals(textErrorList))  // se sto visualizzando la lista degli errori
                removeAcronym(a);                               // rimuovo l'elemento dalla lista degli errori
            else
                RefreshAcronymList();                           // aggiorno la lista completa degli acronimi
            clearField();                                       // ripristino i campi
            removeErrorButton.setEnabled(false);                // disabilito il pulsante ignora
        }
    }

    private void clearField(){              // ripristino i campi di testo visibili, dedicati a
        textAcronym.setText("");            // nome esteso acronimo
        textProblem.setText("");            // problema riscontrato
    }

    private void changeList(ActionEvent e,ArrayList<Acronimo> arraylist,String textList,String textButton){ // cambio la lista vosualizzata, dalla lista errori alla lista acronimi completa e viceversa
        acronimi.clear();
        RefreshAcronymList();
        setAcronym(arraylist);
        labelErrorList.setText(textList);
        completeListButton.setText(textButton);
    }

    private void update(){
        gestioneDocumento.clearArray();                                // resetto array e liste usate
        gestioneDocumento.clearArraymk2();
        gestioneAcronimi.clearAcronymList();
        gestioneAcronimi.clearErrorList();
        acronimi.clear();
        RefreshAcronymList();
        try {
            gestioneDocumento.readFile(MainPlugIn.getFile());		    //rileggo il documento .tex e lo copio nell'array
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            gestioneDocumento.deleteComment();		                    // cancello i commenti del documento
            gestioneDocumento.takeAcronymBlock();				        // prendo solo la parte dedicata ad inizializzare gli acronimi
        } catch(DocumentoNonValidoException documentoNonValidoException) {
            documentoNonValidoException.printStackTrace();
        }
        try {
            gestioneAcronimi.findAcronym(gestioneDocumento.getArraymk2());	//cerco gli acronimi nel documento e gli inserisco in una lista
        } catch (AcronimoNonValidoException acronimoNonValidoException) {
            acronimoNonValidoException.printStackTrace();
        }
        gestioneAcronimi.acronymCheck(mappaAcronimi.getMap());              // verifico la correttezza degli acronimi e creo la lista degli errori
        if(completeListButton.getText().equals(textAcronymListButton))      // se sto visualizzando la lista degli errori
            setAcronym(gestioneAcronimi.getErrorList());                    // riempio la lista degli errori
        else                                                                // altrimenti se sto visualizzando la lista completa
            setAcronym(gestioneAcronimi.getAcronymList());                  // riempio la lista completa
    }

    private void RefreshCorrectionsList(){
        CorrectionsModel.removeAllElements();
        for(String s : correzioni)
            CorrectionsModel.addElement(s);
    }

    private void addCorrections(Acronimo a) {
        for(String s : a.getCorrection())
            correzioni.add(s);
        RefreshCorrectionsList();
    }

    private void removeAllCorrections(){
        correzioni.clear();
        RefreshCorrectionsList();
    }

    private void RefreshAcronymList(){
        ListAcronymModel.removeAllElements();
        for(Acronimo a : acronimi) {
            if(a.getShortName()!=null)      // se l'acronimo ha il nome breve
                ListAcronymModel.addElement(a.getAcronym() + " - [" + a.getShortName() + "]");
            else                            // solo nome esteso
            ListAcronymModel.addElement(a.getAcronym());
        }
    }

    private void addAcronym(Acronimo a){
        acronimi.add(a);
        RefreshAcronymList();
    }

    private void removeAcronym(Acronimo a){
        acronimi.remove(a);
        RefreshAcronymList();
    }

    public void setAcronym(ArrayList<Acronimo> acronymArray) {
        this.acronimi.addAll(acronymArray);
        RefreshAcronymList();
    }
}