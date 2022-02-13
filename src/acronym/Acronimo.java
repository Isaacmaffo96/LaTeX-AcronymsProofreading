package acronym;

/* Ogni acronimo in LaTeX è definito con \acro{acronimo}[nome breve]{nome esteso}
 * Dove:
 * acronimo --> indica l’acronimo stesso (WYSIWYM, per esempio);
 * nome esteso --> indica il nome per esteso dell’acronimo (“What You See Is What You Mean”, per esempio);
 * nome breve --> se l’acronimo richiede dei comandi di LaTeX, il codice necessario si scrive nell’argomento facoltativo ; in questo caso, acronimo rappresenta semplicemente un’etichetta per identificare l’acronimo
 * inoltre l'oggetto Acronimo contiene il campo code --> numero che indica la correttezza dell'acronimo
 * e una lista correction --> lista di tutte le correzioni disponibili per l'acronimo
 * oltre ai consueti getter and setter,
 * getProblem restituisce una Stringa specifacando quale sia il problema riscontrato sulla base del codice associato all'acronimo
 * getCorrectionString() propone una soluzione al problema
 * getOptimalCorrection() restituisce la soluzione più ottimale sulla base delle correzioni disponibili
 * @Override equals() 2 oggetti Acronimo sono uguali se hanno lo stesso acronimo e lo stesso nome esteso, confrontato senza il carattere speciale per LaTeX "\" ,
 *                    senza gli spazi prima e dopo la stringa e senza distinzione tra maiuscolo e minuscolo
 * @Override toString() restituisce una stringa con le informazioni in base a come è costituito l'oggetto
 */

import exceptions.AcronimoNonValidoException;

import java.util.List;

public class Acronimo {

    private final String acronym;
    private final String shortName;
    private String extendedName;
    private int code;						// numero che indica se l'acronimo è corretto: 0=non valutato, 1=corretto, 2=non trovato, 3=trovato ma nome esteso non corretto, 4=errore ignorato, 5=Aggiunto manualmente al dizionario
    private List<String> correction;	    // lista contenente tutte le eventuali correzione al nome dell'acronimo

    public Acronimo(String acro, String brev, String ests, int cod, List<String> correction){
        this.acronym =acro;
        this.shortName =brev;
        this.extendedName =ests;
        this.code =cod;
        this.correction = correction;
    }

    public Acronimo(String acronym,String shortName,String extendedName){this(acronym,shortName,extendedName,0,null); }

    public Acronimo(String acronym,String extendedName){
        this(acronym,null,extendedName,0,null);
    }

    public String getAcronym() {
        return acronym;
    }

    public String getShortName() { return shortName; }

    public String getExtendedName() {
        return extendedName;
    }

    public void setExtendedName(String extendedName) { this.extendedName = extendedName; }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<String> getCorrection() {
        return correction;
    }

    public void setCorrection(List<String> correction) {
        this.correction = correction;
    }

    public String getProblem() throws AcronimoNonValidoException {
        if(code ==1)
            return "Corretto";
        else if(code ==2)
            return "Non trovato nel dizionario";
        else if(code ==3)
            return "Nome esteso non corretto";
        else if(code ==4)
            return "Errore ignorato";
        else if(code ==5)
            return "Aggiunto manualmente al dizionario";
        else
            throw new AcronimoNonValidoException("Codice di errore non riconosciuto");
    }

    public String getCorrectionString() throws AcronimoNonValidoException {
        if(code ==1)
            return "";
        else if(code ==2)
            return "Premi per aggiungerlo al dizionario";
        else if(code ==3)
            return correction.toString();
        else if(code ==4)
            return "";
        else if(code ==5)
            return "Premi per rimuoverlo dal dizionario";
        else
            throw new AcronimoNonValidoException("Codice di errore non riconosciuto");
    }

    public String getOptimalCorrection(){
        if(correction.isEmpty())    return null;
        else if (correction.size()==1)  return  correction.get(0);
        else{
            int j=0; //j = posizione array con la correzione ottimale
            int k=levenshteinDistance(extendedName,correction.get(0)); // k = numero di modifiche minore
            for(int i=1;i<correction.size();i++){ // i = posizione corrente dell'array
                int h=levenshteinDistance(extendedName,correction.get(i)); // h= numero di modifiche necessarie posizone i-esima
                if(h<k) {   // se la correzione i-esima ha una correzione migliore come numero di modifiche necessarie
                    k = h;  // aggiorno il nuovo numero di modifiche più basso
                    j = i;  // salvo la posizione i-esima come la posizione migliore
                }
            }
            return correction.get(j); // restituisco la posizione dell'array con il numero più basso di modifiche necessarie
        }
    }

    @Override
    public boolean equals(Object o) {           // 2 oggetti Acronimo sono uguali se hanno lo stesso acronimo e lo stesso nome esteso, confrontato senza il carattere speciale per LaTeX "\" , senza gli spazi prima e dopo la stringa e senza distinzione tra maiuscolo e minuscolo
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Acronimo acronimo = (Acronimo) o;
        return acronym.equals(acronimo.acronym) &&
                extendedName.trim().toLowerCase().replaceAll("\\\\", "").equals(acronimo.extendedName.trim().toLowerCase().replaceAll("\\\\", ""));
        }

    @Override
    public String toString() {
        if(shortName ==null){
            if(code ==1)
                return "[Acronimo: " + acronym + ", nomeEsteso: " + extendedName + ", corretto] ";
            else if(code ==2)
                return "[Acronimo: " + acronym + ", nomeEsteso: " + extendedName + ", non trovato nel dizionario] ";
            else if(code ==3)
                return "[Acronimo: " + acronym + ", nomeEsteso: " + extendedName + ", nome esteso non corretto, correzione proposta: " + correction.toString() + "] ";
            else if(code ==4)
                return "[Acronimo: " + acronym + ", nomeEsteso: " + extendedName + ", Errore ignorato]";
            else if(code ==5)
                return "[Acronimo: " + acronym + ", nomeEsteso: " + extendedName + ", Aggiunto manualmente al dizionario]";
            else
                return  "[Acronimo: " + acronym + ", nomeEsteso: " + extendedName + "] ";
        }
        else{
            if(code ==1)
                return "[Acronimo: " + acronym + ", nomeBreve: " + shortName + ", nomeEsteso: " + extendedName + ", corretto] ";
            else if(code ==2)
                return "[Acronimo: " + acronym + ", nomeBreve: " + shortName + ", nomeEsteso: " + extendedName + ", non trovato nel dizionario] ";
            else if(code ==3)
                return "[Acronimo: " + acronym + ", nomeBreve: " + shortName + ", nomeEsteso: " + extendedName + ", nome esteso non corretto, correzione proposta: " + correction.toString() + "] ";
            else if(code ==4)
                return "[Acronimo: " + acronym + ", nomeBreve: " + shortName + ", nomeEsteso: " + extendedName + ", Errore ignorato]";
            else if(code ==5)
                return "[Acronimo: " + acronym + ", nomeBreve: " + shortName + ", nomeEsteso: " + extendedName + ", Aggiunto manualmente al dizionario]";
            else
                return "[Acronimo: " + acronym + ", nomeBreve: " + shortName + ", nomeEsteso: " + extendedName + "] ";
            }
        }

    //La distanza di Levenshtein tra due stringhe A e B è il numero minimo di modifiche elementari che consentono di trasformare la A nella B
    private int levenshteinDistance(String a,String b){
        int i, j;
        final int n = a.length(), m = b.length();
        int L[][] = new int[n+1][m+1];
        for ( i=0; i<n+1; i++ ) {
            for ( j=0; j<m+1; j++ ) {
                if ( i==0 || j==0 ) {
                    L[i][j] = max(i, j);
                } else {
                    L[i][j] = min3(L[i-1][j] + 1, L[i][j-1] + 1,
                            L[i-1][j-1] + (a.charAt(i-1) != b.charAt(j-1) ? 1 : 0) );
                }
            }
        }
        return L[n][m];
    }

    private int max( int i, int j ) {
        if(i>j) return i;
        else return j;
    }

    private int min3( int i, int j, int k ) {
        int result = i;
        if (j < result) result = j;
        if (k < result) result = k;
        return result;
    }
}
