package acronym;
/* Classe che mi permette di creare la mappa degli acronimi (dizionario) e di:
 * MappaAcronimi(): tramite il costruttore creare una hash map con gli acronimi di un dizionario su file, la chiave della hash map è l'acronimo e per ogni chiave è presente una lista di acronimi con la stessa chiave
 * addAcronymToMap(Acronimo ac): aggiungire un acronimo sia alla mappa che nel dizionario su file
 * removeFromMap(Acronimo acronym): rimuovere un acronimo dalla mappa e dal file dizionario
 * getMap(): restituire la mappa
 * toString(): Override, restituire una stringa contenente tutta la mappa
 */
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.AcronimoNonValidoException;
import test.MainPlugIn;

public class MappaAcronimi  {

    private final Map<String, List<Acronimo>> map;				// La chiave della mappa è l'acronimo, ogni chiave ha una lista di acronimi con la stessa chiave
    private final String DictionaryFile;

    public MappaAcronimi() throws IOException {
        this.DictionaryFile = MainPlugIn.getFileDizionario();
        this.map = new HashMap<>();
        FileReader fr = new FileReader(DictionaryFile);								// Gli acronimi nel dizionario devono essere disposti riga per riga con il formato seguente: Acronimo (spazio) Nome esteso dell'acronimo
        BufferedReader bf = new BufferedReader(fr);
        String line = null;
        Pattern pattern = Pattern.compile("(\\w+)(\\s+)(.+)");				// creo il pattern con 3 gruppi --> gruppo1:(caratteri alfanumerici) gruppo2:(spaziatura) gruppo3:(qualsiasi carattere)
        Matcher matcher;
        while((line=bf.readLine())!=null) {
            matcher = pattern.matcher(line);
            if(matcher.matches()) {
                String acronymKey = matcher.group(1);
                Acronimo acronimo = new Acronimo(acronymKey,matcher.group(3));
                if(!map.containsKey(acronymKey)) {                          // chiave vuota
                    List<Acronimo> list = new ArrayList<>();
                    list.add(acronimo);
                    map.put(acronymKey, list);
                }
                else {                                                      // chiave già presente
                    List<Acronimo> list = map.get(acronymKey);
                    list.add(acronimo);
                    map.put(acronymKey,list);
                }
            }
        }
        bf.close();
    }

    public void addAcronymToMap(Acronimo ac) throws IOException, AcronimoNonValidoException{
        if(map.containsKey(ac.getAcronym())){                         // chiave già presente nel dizionario
            if(map.get(ac.getAcronym()).contains(ac))                 // acronimo già presente nel dizionario
                throw new AcronimoNonValidoException("Acronimo già presente nel dizionario.");
            else{                                                     // chiave presente, ma non l'acronimo
                List<Acronimo> list = map.get(ac.getAcronym());
                list.add(ac);                                         // aggiungo l'acronimo alla lista già presente
                map.put(ac.getAcronym(),list);
            }
        }
        else {                                                        // chiave non presente
            List<Acronimo> list = new ArrayList<>();                  // creo una nuova lista
            list.add(ac);                                             // aggiungo l'acronimo alla lista appena creata
            map.put(ac.getAcronym(), list);
        }
        PrintWriter out = new PrintWriter(new FileWriter(DictionaryFile, true));
        out.append("\n" + ac.getAcronym()  + " " + ac.getExtendedName());
        out.close();
    }

    public Map<String, List<Acronimo>> getMap() {
        return map;
    }

    public void removeFromMap(Acronimo acronym) throws IOException {
        String key = acronym.getAcronym();              // chiave della mappa
        List<Acronimo> listAcronym = map.get(key);      // lista di acronimi per una data chiave
        listAcronym.remove(acronym);                    // rimuovo l'acronimo da questa lista
        if (listAcronym.isEmpty())                      // se lista diventa vuota
            map.remove(key);                            // rimuovo la chiave dalla mappa
        else                                            // se la lista contiene ancora elementi
            map.put(key,listAcronym);                   // inserisco per la determinata chiave questa lista senza l'acronimo in questione
        String acroToRemove = acronym.getAcronym() + " " + acronym.getExtendedName();   // creo la riga del dizionario da rimuovere --> Acronimo + spazio + nome esteso acronimo
        BufferedReader reader = new BufferedReader(new FileReader(DictionaryFile));     // rileggo il dizionario
        ArrayList<String> dictionary = new ArrayList<>();   // array dove inserisco il documento
        String line = null;
        while((line=reader.readLine())!=null) {             // scorro tutte le righe del documento
            if(line.equals(acroToRemove)) continue;         // ignoro la riga dove è presente l'acronimo da rimuovere
            else
                dictionary.add(line);                       // aggiungo all'array tutte le altre righe
        }
        reader.close();
        PrintWriter writer = new PrintWriter(new FileWriter(DictionaryFile, false)); // sovrascrivo il file dizionario con l'array appena creato
        for(String s:dictionary)
            writer.append(s+ "\n");    // vado a capo dopo ogni acronimo
        writer.close();
    }

    @Override
    public String toString() {
        String string = null;
        for (String key: map.keySet()) {
            if(string==null)
                string = key + " " + map.get(key).toString();
            else
                string = string + "- " + key + " " + map.get(key).toString();
        }
        return "{" + string + "}";
    }

}