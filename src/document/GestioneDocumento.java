package document;
/* Classe che crea 2 liste e che mi permette di:
 * readFile(String file): leggere un documento da file e inserirlo in due array identici, uno la copia dell'altro, così da modificarne solo uno e tenere il documento originale sull'altro
 * deleteComment(): cancellare dall'array tutti i commenti
 * takeAcronymBlock(): ritagliare dal documento solo le parti dove vengono inizializzati gli acronimi trmite i blocchi \begin{acronym} e \end{acronym}
 * overwrite(): sovrascrivere il documento con l'array con gli acronimi o l'acronimo corretto
 * getArray() e getArraymk2(): restituire gli array
 * clearArray() e clearArraymk2(): resettare gli array
 * toString(): Override, restituisce una stringa contenente tutte le righe dell'arry
 */
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.DocumentoNonValidoException;

public class GestioneDocumento {

    private final ArrayList<String> array;                                                       // array dove risiede il documento riga per riga
    private final ArrayList<String> arraymk2;                                                    // array dove risiede il documento riga per riga modificato
    private String file;

    public GestioneDocumento() {
        this.array = new ArrayList<>();
        this.arraymk2 = new ArrayList<>();
    }

    public void readFile(String file) throws IOException {
        this.file=file;
        FileReader fr = new FileReader(file);
        BufferedReader bf = new BufferedReader(fr);
        String line = null;
        while((line=bf.readLine())!=null)
            array.add(line);															// popolo l'array con il documeto riga per riga
        bf.close();
        // creo un array identico per non andare a sovrascrivere
        arraymk2.addAll(array);
    }

    public void deleteComment() throws DocumentoNonValidoException {					// cancello i commenti così da evitare di cercare acronimi solo commentati, ma non implementati ed evitare bug
        for(int i = 0; i< arraymk2.size(); i++) {										// COMMENTI CON %
            String line = arraymk2.get(i);														// riga i-esima
            if(line.trim().startsWith("%"))												// caso con % al primo posto in una riga, sicuramente � un commento, trim rimuove spazio all'inizio (e fine) per coprire anche il caso di una riga formata da spazio e %
                arraymk2.remove(i--);														// cancello la riga i-esima e decremento il valore poich� le posizioni dell'array si sono scalate di 1
            else if(line.contains("%")){												// se genericamente la riga i-esima contiene il simbolo %
                int k=0;																	// numero di % nella riga
                int j= line.indexOf('%');
                while(j>0) {															// controllo se ci sono altri simboli % // -1 se non c'�, 0 primo posto, quindi non ci interessano questi casi (non ci pu� essere \%)
                    k++;																// incremento il numero di % trovati
                    if(line.charAt(j-1)!='\\') {										// se il simbolo trovato non � l'escape \% che fa apparire il simbolo % invece che commentare
                        String [] dato1=line.split("%");
                        String line1=dato1[0];
                        for(int l=1;l<k;l++)										// vado a inserire nella stringa tutte le parti prima del simbolo di % di commento
                            line1=line1+"%"+dato1[l];								// prendo la parte prima del simbolo % di commento e rinserisco il simbolo tolto dallo split
                        arraymk2.set(i, line1);										// sostituisco con la stringa creata
                        break;														// esco dal ciclo while perch� ho trovato il simbolo di commento e non mi interessa cosa c'� dopo
                    }
                    j = line.indexOf('%', j+1);											// prendo la posizione del prossimo simbolo %
                }
            }
        }

        for(int i = 0; i< arraymk2.size(); i++) {												// creo un secondo ciclo for necessario per gestire il caso \begin{comment} seguito da un %
            String line = arraymk2.get(i);														// prendo la riga i-esima
            Pattern pattern1 = Pattern.compile("(.*)(\\\\begin\\s*\\{comment})(.*)");	// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \begin{comment} con una eventuale spaziatura indefinita tra \begin e {comment}) gruppo3:(eventualmente qualsiasi carattere)
            Matcher matcher1 = pattern1.matcher(line);
            if (matcher1.find()) {														// se nella riga i-esima � presente l'istruzione \begin{comment}
                if(matcher1.group(1).trim().isEmpty())								// caso dell'istruzione \begin{comment} senza nessun codice che la precede, lo spazio non viene considerato
                    arraymk2.remove(i);												// rimuovo qeusta riga
                else 																// caso con del codice che precede l'istruzione \begin{comment}
                    arraymk2.set(i++, matcher1.group(1));								// lascio in quella riga dell'array solo il codice (gruppo 1) e passo alla riga successiva incrementando la i
                Pattern pattern2 = Pattern.compile("(.*)(\\\\end\\s*\\{comment})(.*)");	// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \end{comment} con una eventuale spaziatura indefinita tra \end e {comment}) gruppo3:(eventualmente qualsiasi carattere)
                Matcher matcher2 = pattern2.matcher(line);								// line e non get(i) cos� da partire dalla riga dove ho trovato \begin{comment}
                if(!(matcher1.matches() && matcher2.matches())) {						// non deve essere il caso dell'istruzione \begin{comment} e \end{comment} sulla stessa riga, poich� gi� trattato
                    matcher2 = pattern2.matcher(arraymk2.get(i));							// aggiorno il matcher con la riga a cui voglio puntare
                    while(!matcher2.matches()) {										// fino a quando non trovo \end{comment}
                        arraymk2.remove(i);
                        if(arraymk2.size()==i)												// se rimuovo tutte le righe perch� non trovo \end{comment}, eccezione che si verifica quando manca l'istruzione \end{comment} per terminare il blocco dedicato ai commenti
                            throw new DocumentoNonValidoException("omessa istruzione \\end{comment} dopo aver cominciato un commento con l'istruzione \\begin{comment}");
                        matcher2 = pattern2.matcher(arraymk2.get(i));						// e aggiorno il matcher
                    }
                    matcher2 = pattern2.matcher(arraymk2.get(i));
                    if(matcher2.find())													// quando trovo l'istruzione \end{comment}
                        arraymk2.remove(i--);												// rimuovo completamente la riga, perch� istruzioni dopo questa istruzione vengono ignorate da LaTeX
                }
            }
        }
    }

    public void takeAcronymBlock() throws DocumentoNonValidoException {					    // il blocco dedicato agli acronimi è definito con \begin{acronym}
        if(arraymk2.size()==0) throw new DocumentoNonValidoException("Documento vuoto") ;	// Eccezione se il documento è vuoto
        Pattern patternBeginA = Pattern.compile("(.*)(\\\\begin\\s*\\{acronym})(.*)");	// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \begin{acronym} con una eventuale spaziatura indefinita tra \begin e {acronym}) gruppo3:(eventualmente qualsiasi carattere)
        Pattern patternEndA = Pattern.compile("(.*)(\\\\end\\s*\\{acronym})(.*)");		// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \begin{acronym} con una eventuale spaziatura indefinita tra \begin e {acronym}) gruppo3:(eventualmente qualsiasi carattere)
        int i=0;                                                                        // posizione nell'array
        while(arraymk2.get(i)!=null) {
            Matcher matcherBeginA = patternBeginA.matcher(arraymk2.get(i));
            while (!matcherBeginA.matches()) {											// finchè nella riga i-esima  non è presente l'istruzione \begin{acronym}
                arraymk2.remove(i);
                if(arraymk2.size()!=i)
                    matcherBeginA = patternBeginA.matcher(arraymk2.get(i));
                else return;
            }
            Matcher matcherEndA = patternEndA.matcher(arraymk2.get(i));
            while (!matcherEndA.find()) {												// finchè nella riga i-esima non è presente l'istruzione \end{acronym}
                i++;
                if(arraymk2.size()==i)
                    throw new DocumentoNonValidoException("Manca l'istruzione /end{acronym}") ;
                matcherEndA = patternEndA.matcher(arraymk2.get(i));
            }
            i++;
            if(i== arraymk2.size()) return;													// ho ormai scansionato tutto il documento
        }
    }

    public void overwrite() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(file, false));
        for(String s : array)
            out.append(s + "\n");
        out.close();
    }

    public ArrayList<String> getArray() { return array; }

    public void clearArray() { array.clear(); }

    public ArrayList<String> getArraymk2() { return arraymk2; }

    public void clearArraymk2() { arraymk2.clear(); }

    @Override
    public String toString() {
        String string = null;
        for (String s: arraymk2) {
            if(string==null)
                string = s.toString();
            else
                string = string + "\n " + s.toString();
        }
        return string;
    }

}