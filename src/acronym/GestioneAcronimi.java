package acronym;
/* Classe che mi permette di:
 * findAcronym(ArrayList<String> doc): cercare gli acronimi dato un determinato documento e salvarli in una lista
 * isInAcronymList(Acronimo a): cercare se nella lista degli acronimi c'è un acronimo con le medesime caratteristiche
 * acronymCheck(Map<String, List<Acronimo>> map): verificare se gli acronimi trovati nel documento siano corretti, cercando la corrispondenza nel dizionario e inserire quelli scorretti in una lista apposita
 * addInDictionary(Acronimo ac, Map<String, List<Acronimo>> map): aggiungere un acronimo nel dizionario
 * autoCorrection(ArrayList<String> array): correggere automaticamente tutto il documento con la prima correzione disponibile per ogni acronimo
 * correctAcronym(ArrayList<String> array, Acronimo acroToBeCorrected,int c): correggere il singolo acronimo in base alla correzione scelta
 * removeAcroError(Acronimo a): rimuovere un determinato acronimo dalla lista degli errori
 * ignoreError(Acronimo a): ignorare un errore e richiamare removeAcroError(a)
 * getErrorList() e getAcronymList(): restituire la lista degli errori e la lista degli acronimi completa
 * removeAcroFromDictionaryList(Map<String, List<Acronimo>> map,Acronimo acronym,MappaAcronimi mappaAcronimi): rimuovere un acronimo dal dizionario
 * clearAcronymList() e  clearErrorList(): ripristinare le liste
 * @Override toString(), AcronymListToString(), ErrortoString(): generare una stringa con il contenuto delle liste
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.AcronimoNonValidoException;
import test.MainPlugIn;

public class GestioneAcronimi {

    private final ArrayList<Acronimo> acronymList;													// Lista di oggetti Acronimo (String acronimo, String nomeBreve, String nomeEsteso)
    private final ArrayList<Acronimo> errorList;													// Lista contenente i soli acronimi scorretti
    private final ArrayList<Acronimo> ignoreList;                                                   // Lista contenente tutti gli acronimi ignorati
    private final ArrayList<Acronimo> dictionaryList;                                               // Lista contenente tutti gli acronimi aggiunti manualmente nel dizionario

    public GestioneAcronimi(){
        this.acronymList = new ArrayList<>();
        this.errorList = new ArrayList<>();
        this.ignoreList = new ArrayList<>();
        this.dictionaryList = new ArrayList<>();
    }

    /* Ogni acronimo in LaTeX è definito con \acro{acronimo}[nome breve]{nome esteso} ,ma [nome breve] è facoltativo */
    public void findAcronym(ArrayList<String> doc) throws AcronimoNonValidoException {
        Pattern patternAcro = Pattern.compile("(.*\\\\acro\\s*\\{)(\\w*)(})(.*)");		// creo il pattern con 4 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \acro qualsiasi eventuale spaziatura e parentesi { gruppo3:(qualsiasi carattere alfanumerico) gruppo4:(parentesi { ) gruppo5:(qualsiasi carattere)
        Pattern patternShort = Pattern.compile("(\\s*\\[)(.+)(]\\s*\\{)(.+)(}.*)");		// creo il pattern con 5 gruppi --> gruppo1:(eventualmente qualsiasi spaziatura e parentesi [) gruppo2:(qualsiasi carattere) gruppo3:(parentesi ] eventualmente qualsiasi spaziatura e parentesi {) gruppo4:(qualsiasi carattere) gruppo5:(parentesi })
        Pattern patternExt = Pattern.compile("(\\s*\\{)(.+)(}.*)");						// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi spaziatura e parentesi {) gruppo2:(qualsiasi carattere) gruppo3:(parentesi })
        for(String s:doc) {
            Matcher matcherAcro = patternAcro.matcher(s);
            if(matcherAcro.matches()) {													    // inanzitutto cerco se trovo il match con l'istruzione \acro
               String  acronym=matcherAcro.group(2);								    	// e mi salvo il nome dell'acronimo
                Matcher matcherShort = patternShort.matcher(matcherAcro.group(4));
                Matcher matcherExt = patternExt.matcher(matcherAcro.group(4));
                if(matcherShort.matches()) {											    // parto dal caso con anche il nome breve [nome breve]{nome esteso}
                    String shortName=matcherShort.group(2).trim();
                    String extendedName=matcherShort.group(4).trim();
                    Acronimo acronymObject = new Acronimo(acronym,shortName,extendedName);  // creo l'oggetto Acronimo con 3 nomi
                    if(!isInList(acronymObject,acronymList)) {                              // se non è già presente nella lista
                        acronymList.add(acronymObject);
                    }
                }
                else if (matcherExt.find()){											    // cerco il nome esteso {nome esteso}
                    String extendedName=matcherExt.group(2).trim();
                    Acronimo acronymObject = new Acronimo(acronym,extendedName);            // creo l'oggetto Acronimo con 3 nomi
                    if(!isInList(acronymObject,acronymList)) {                              // se non è già presente nella lista
                        acronymList.add(acronymObject);
                    }
                }
                else 																	    // eccezione se non trovo il nome esteso dell'acronimo
                    throw new AcronimoNonValidoException("non è stato dichiarato il nome esteso dell'acronimo: " + acronym);
            }
        }
    }

    public void acronymCheck(Map<String, List<Acronimo>> map) {   // controllo la correttezza degli acronimi verificando dal dizionario
        for(Acronimo a: acronymList) {
            if(isInList(a,ignoreList))                            // se l'acronimo è stato ignorato
                a.setCode(4);                                     // codice 4 = acronimo errato ignorato
            else if(isInList(a,dictionaryList))                   // se è statato aggiunto manualmente al dizionario
                a.setCode(5);                                     // codice 5 = acronimo aggiunto manualmente nel dizionario
            else{                                                 // acronimo non ignorato e non aggiunto al dizionario
                acronymAndListSet(map,a);
            }
        }
    }

    public void addInDictionary(Acronimo ac, Map<String, List<Acronimo>> map) throws AcronimoNonValidoException, IOException {
        if(map.containsKey(ac.getAcronym())){                      // chiave già presente nel dizionario
            if(map.get(ac.getAcronym()).contains(ac))              // acronimo già presente nel dizionario
                throw new AcronimoNonValidoException("Acronimo già presente nel dizionario.");
            else{                                                  // chiave presente, ma non l'acronimo
                List<Acronimo> list = map.get(ac.getAcronym());    // lista già presente per quella chiave
                ac.setCode(5);                                     // codice 5 = acronimo aggiunto manualmente nel dizionario
                list.add(ac);                                      // aggiungo l'acronimo alla lista corrente
                map.put(ac.getAcronym(),list);                     // reinserisco la lista nella mappa
                dictionaryList.add(ac);                            // inserisco l'acronimo nella lista degli acronimi inseriti manualmente nel dizionario
            }
        }
        else {                                                      // chiave non presente
            List<Acronimo> list = new ArrayList<>();                // creo una lista nuova per quella chiave
            ac.setCode(5);                                          // codice 5 = acronimo aggiunto manualmente nel dizionario
            list.add(ac);                                           // aggiungo l'acronimo alla lista appena creata
            map.put(ac.getAcronym(),list);                          // inserisco la lista nella mappa
            dictionaryList.add(ac);                                 // inserisco l'acronimo nella lista degli acronimi inseriti manualmente nel dizionario
        }
        PrintWriter out = new PrintWriter(new FileWriter(MainPlugIn.getFileDizionario(), true));    //in una nuova riga inserisco l'acronimo nel dizionario senza sovrascrivere il contenuto
        out.append("\n" + ac.getAcronym()  + " " + ac.getExtendedName());
        out.close();
        errorList.remove(ac);                                       // rimuovo l'acronimo dalla lista degli errori
    }

    public void removeAcroFromDictionaryList(Map<String, List<Acronimo>> map,Acronimo acronym,MappaAcronimi mappaAcronimi) throws IOException {
        mappaAcronimi.removeFromMap(acronym);   // rimuovo l'acronimo dalla mappa e dal dizionario
        dictionaryList.remove(acronym);         // rimuovo l'acronimo dalla lista di acronimi aggiunti manualmente
        acronymAndListSet(map,acronym);         // valuto nuovamente l'acronimo settandogli il codice di errore e inserendolo nella lista degli errori
    }

    public void autoCorrection(ArrayList<String> array) {
        Pattern patternAcroShort = Pattern.compile("(.*)(\\\\acro\\s*\\{)(\\w*)(}\\s*\\[)(.+)(]\\s*\\{)(.+)(})(.*)");  // creo il pattern con 7 gruppi --> gruppo2 = acronimo, gruppo4 = nome breve, gruppo6= nome esteso
        Pattern patternAcroExt  = Pattern.compile("(.*)(\\\\acro\\s*\\{)(\\w*)(}\\s*\\{)(.+)(})(.*)");                // creo il pattern con 5 gruppi --> gruppo2 = acronimo, gruppo4 = nome esteso

        Pattern patternBeginA = Pattern.compile("(.*)(\\\\begin\\s*\\{acronym})(.*)");	// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \begin{acronym} con una eventuale spaziatura indefinita tra \begin e {acronym}) gruppo3:(eventualmente qualsiasi carattere)
        Pattern patternEndA = Pattern.compile("(.*)(\\\\end\\s*\\{acronym})(.*)");		// creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \begin{acronym} con una eventuale spaziatura indefinita tra \begin e {acronym}) gruppo3:(eventualmente qualsiasi carattere)

        for (int i =0; i< array.size();i++) {
            String s = array.get(i);                                                            // riga i-esima dell'array
            Matcher matcherAcroShort = patternAcroShort.matcher(s);
            Matcher matcherAcroExt = patternAcroExt.matcher(s);
            if (matcherAcroShort.matches()) {                                                   // parto a cercare la versione con il nome breve
                String acronym = matcherAcroShort.group(3);
                String shortName = matcherAcroShort.group(5);
                String extendedName = matcherAcroShort.group(7);
                Acronimo a = new Acronimo(acronym, shortName, extendedName);                     // creo l'oggetto Acronimo con 3 nomi
                if(searchInErrorList(a)!=null){                                             // se è presente nella lista errori
                    Acronimo acro = searchInErrorList(a);                                   // restituisce l'acronimo presente nella lista degli errori
                    if(acro.getCode()==3) {                                                      // cod=3 --> se il nome esteso non corretto
                        String slash = "";                                                              // Stringa che mi permette di aggiungere il carattere speciale LaTeX "\" senza causare problemi con le regex
                        if(shortName.startsWith("\\"))                                 // nel nome breve "\" è posizionato all'inizio del nome
                            slash="\\\\";
                        String correctName = acro.getOptimalCorrection();                             // correzione proposta dal dizionario
                        String replacement = matcherAcroShort.group(1) + "\\\\acro{" + acronym + "} [" + slash + shortName + "] {" + correctName + "}" + matcherAcroShort.group(9); //correggo l'acronimo, lascio invariato ciò che c'era prima e dopo di esso
                        StringBuilder buffer = new StringBuilder();
                        matcherAcroShort.appendReplacement(buffer, replacement);
                        array.set(i, matcherAcroShort.appendTail(buffer).toString());             // inserisco la nuova riga nell'array
                    }
                }
            } else if (matcherAcroExt.matches()) {                                                    // inanzitutto cerco se trovo il match con l'istruzione \acro
                String acronym = matcherAcroExt.group(3);                                    // e mi salvo il nome dell'acronimo
                String extendedName = matcherAcroExt.group(5);
                Acronimo a = new Acronimo(acronym, extendedName);                // creo l'oggetto Acronimo con 3 nomi
                if(searchInErrorList(a)!=null){
                    Acronimo acro = searchInErrorList(a);
                    if(acro.getCode()==3) {
                        String correctName = acro.getOptimalCorrection();
                        String replacement = matcherAcroExt.group(1) + "\\\\acro{" + acronym + "}  {" + correctName + "}" + matcherAcroExt.group(7); //correggo l'acronimo, lascio invariato ciò che c'era prima e dopo di esso
                        StringBuilder buffer = new StringBuilder();
                        matcherAcroExt.appendReplacement(buffer, replacement);
                        array.set(i, matcherAcroExt.appendTail(buffer).toString());
                    }
                }
            }
        }
    }

    public void correctAcronym(ArrayList<String> array, Acronimo acroToBeCorrected,int c) throws AcronimoNonValidoException {
        if (acroToBeCorrected.getCode() != 3)
            throw new AcronimoNonValidoException("L'acronimo non ha correzione");
        String acroToBeCorrectedAcro = acroToBeCorrected.getAcronym();
        String acroToBeCorrectedShortName = acroToBeCorrected.getShortName();
        String acroToBeCorrectedExtendedName = acroToBeCorrected.getExtendedName();
        String acroToBeCorrectedCorrection = acroToBeCorrected.getCorrection().get(c);
        Pattern patternAcro = Pattern.compile("(.*)(\\\\acro\\s*\\{)(.*)");        // creo il pattern con 3 gruppi --> gruppo1:(eventualmente qualsiasi carattere) gruppo2:(istruzione \acro qualsiasi eventuale spaziatura e parentesi { gruppo3:(qualsiasi carattere)
        if (acroToBeCorrectedShortName != null) {
            Pattern patternAcroShort = Pattern.compile("(\\s*" + acroToBeCorrectedAcro + "\\s*)(}\\s*\\[)(\\s*" + acroToBeCorrectedShortName.replaceAll("\\\\", "").trim() + "\\s*)(]\\s*\\{)(\\s*" + acroToBeCorrectedExtendedName.replaceAll("\\\\", "").trim() + "\\s*)(})(.*)");  // creo il pattern con 7 gruppi --> gruppo1 = acronimo, gruppo3 = nome breve, gruppo5= nome esteso
            for (int i = 0; i < array.size(); i++) {
                String s = array.get(i);                                                            // riga i-esima dell'array
                Matcher matcherAcro = patternAcro.matcher(s);
                if (matcherAcro.matches()) {
                    Matcher matcherAcroShort = patternAcroShort.matcher(matcherAcro.group(3).replaceAll("\\\\", ""));
                    if (matcherAcroShort.matches()) {                                                   // parto a cercare la versione con il nome breve
                        String slash = "";                                                              // Stringa che mi permette di aggiungere il carattere speciale LaTeX "\" senza causare problemi con le regex
                        if(acroToBeCorrectedShortName.startsWith("\\"))                                 // nel nome breve "\" è posizionato all'inizio del nome
                                slash="\\\\";
                        String replacement = matcherAcro.group(1) + "\\\\acro{" + acroToBeCorrectedAcro + "} [" + slash + acroToBeCorrectedShortName + "] {" + acroToBeCorrectedCorrection + "}" + matcherAcroShort.group(7); //correggo l'acronimo, lascio invariato ciò che c'era prima e dopo di esso
                        StringBuilder buffer = new StringBuilder();
                        matcherAcroShort.appendReplacement(buffer, replacement);
                        array.set(i, matcherAcroShort.appendTail(buffer).toString());             // inserisco la nuova riga nell'array
                    }
                }
            }
        } else {
            Pattern patternAcroExt = Pattern.compile("(\\s*" + acroToBeCorrectedAcro + ")(}\\s*\\{)(\\s*" + acroToBeCorrectedExtendedName.replaceAll("\\\\", "") + "\\s*)(})(.*)");                // creo il pattern con 5 gruppi --> gruppo2 = acronimo, gruppo4 = nome esteso
            for (int i = 0; i < array.size(); i++) {
                String s = array.get(i);                                                            // riga i-esima dell'array
                Matcher matcherAcro = patternAcro.matcher(s);
                if (matcherAcro.matches()) {
                    Matcher matcherAcroExt = patternAcroExt.matcher(matcherAcro.group(3).replaceAll("\\\\", ""));
                    if (matcherAcroExt.matches()) {
                        String replacement = matcherAcro.group(1) + "\\\\acro{" + acroToBeCorrectedAcro + "} {" + acroToBeCorrectedCorrection + "}" + matcherAcroExt.group(5); //correggo l'acronimo, lascio invariato ciò che c'era prima e dopo di esso
                        StringBuilder buffer = new StringBuilder();
                        matcherAcroExt.appendReplacement(buffer, replacement);
                        array.set(i, matcherAcroExt.appendTail(buffer).toString());             // inserisco la nuova riga nell'array
                    }
                }
            }
        }
    }

    public void removeAcroError(Acronimo a){ errorList.remove(a); }  // rimuove l'acronimo dalla lista degli errori

    public void ignoreError(Acronimo a){
        removeAcroError(a);
        a.setCode(4);   // codice di errore 4 = errore ignorato
        ignoreList.add(a);
    }

    public ArrayList<Acronimo> getErrorList() { return errorList; }

    public ArrayList<Acronimo> getAcronymList() { return acronymList; }

    public ArrayList<Acronimo> getIgnoreList() { return ignoreList; }

    public ArrayList<Acronimo> getDictionaryList(){ return dictionaryList; }

    public void clearAcronymList() { acronymList.clear(); }         // rimuove tutti gli elementi della lista degli acronimi

    public void clearErrorList() { errorList.clear(); }             // rimuove tutti gli elementi della lista degli errori

    @Override
    public String toString() {
        String string = null;
        for (Acronimo acro: acronymList) {
            if(string==null)
                string = "{Lista Acronimi:(" + acro.toString();
            else
                string = string + "- " + acro.toString();
        }
        string = string + ") Lista Errori:(";
        for (Acronimo error: errorList) {
            string = string + error.toString() + "- ";
        }
        return string + ")}";
    }

    public String AcronymListToString() {
        String string = null;
        for (Acronimo acro: acronymList) {
            if(string==null)
                string = acro.toString();
            else
                string = string + "- " + acro.toString();
        }
        return string;
    }

    public String ErrorListToString() {
        String string = null;
        for (Acronimo error: errorList) {
            if(string==null)
                string = error.toString();
            else
                string = string + "- " + error.toString();
        }
        return string;
    }

    private void acronymAndListSet(Map<String, List<Acronimo>> map,Acronimo a){
        String key = a.getAcronym();
        if (!map.containsKey(key)) {                      // l'acronimo non è presente nel dizionario
            a.setCode(2);
            errorList.add(a);
        } else {                                          // chiave acronimo presente nel dizionario
            List<Acronimo> list = map.get(key);
            boolean findIt = false;
            List<String> corrections = new ArrayList<>(); // lista delle correzioni possibili
            for (Acronimo acro : list) {
                if (a.equals(acro)) {
                    findIt = true;
                    break;
                }
                corrections.add(acro.getExtendedName());  // tutti i nomi estesi degli acronimi con la stessa chiave
            }
            if (findIt)                                   // acronimo presente nel dizionario
                a.setCode(1);                             // codice 1 = corretto
            else {
                a.setCode(3);                             // chiave presente ma non nome esteso, codice 3 = nome esteso non corretto
                a.setCorrection(corrections);             // correzione proposta presente nel dizionario
                errorList.add(a);
            }
        }
    }

    private Acronimo searchInErrorList(Acronimo acro){
        for(Acronimo a: errorList) {                                                // scorro la lista degli errori
            if(acro.getShortName()!=null){                                          // nome Breve != null
                if(acro.getAcronym().equals(a.getAcronym()) &&                      // coincidono acronimo, nome breve e nome esteso
                        acro.getShortName().equals(a.getShortName()) &&
                        acro.getExtendedName().equals(a.getExtendedName()))
                    return a;                                                       // ritorno l'acronimo che coincide
            }
            else{                                                                   // nome Breve == null
                if(acro.getAcronym().equals(a.getAcronym()) &&                      // coincidono acronimo e nome esteso
                        acro.getExtendedName().equals(a.getExtendedName()))
                    return a;                                                       // ritorno l'acronimo che coincide
            }
        }
        return null;                                                                // non coincide con nessun acronimo presente nella lista degli errori
    }

    private boolean isInList(Acronimo a, ArrayList<Acronimo> list){    // cerco se nella lista c'è un acronimo con le medisime caratteristiche
        if(a.getShortName()!=null){
            for(Acronimo acro: list){
                if(acro.getShortName()!=null){
                    if(a.getShortName().equals(acro.getShortName()) && a.equals(acro))
                        return true;
                }
            }
        }
        else {
            for (Acronimo acro : list)
                if (acro.equals(a)) return true;
        }
        return false;
    }
}
