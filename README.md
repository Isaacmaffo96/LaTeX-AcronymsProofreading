# **Progetto di Tesi 2019/20**
## Rapida visione della relazione di progetto

![Facciata](https://user-images.githubusercontent.com/28917454/153767078-dfab790c-9d9a-410b-b426-4a5aeb2480d4.jpg)

## **Sommario**

Nella frenesia della vita quotidiana abbiamo contratto la cattiva abitudine di scrivere nella
maniera più veloce possibile, questo vizio è stato accentuato dagli smartphone, per mezzo
dei quali componiamo ogni giorno decine, se non centinaia, di messaggi informali con
lo scopo di inviarli senza farci distrarre troppo su quello a cui eravamo concentrati, così
il rischio di compiere un refuso è dietro l’angolo. Qualsiasi tastiera di ogni smartphone
offre la funzione di correzione, così da correggere queste sviste, di conseguenza ci siamo
abituati a questo modo frenetico di scrivere davanti a uno schermo. Se quindi per i
messaggi informali composti da smartphone un refuso non costituisce un problema, lo
può invece rappresentare in ambito professionale su PC, dove non è sempre possibile
disporre di strumenti di correzione automatica.
Lo scopo di questa tesi è quello di fornire uno strumento di correzione di bozze
(proofreading) per documenti professionali scritti in linguaggio LATEX, nello specifico
tratta la realizzazione di un plugin per individuare e correggere gli acronimi del documento
in questione.

## **Breve Introduzione**
Questo utile strumento nasce con lo scopo di aiutare l’autore di un testo ad effettuare
il processo di proofreading, controllando automaticamente tutti gli acronimi presenti nel
documento e mostrando all’utente quelli ritenuti errati, per questi il tool espone la tipologia
di errore e offre una o più possibili correzioni. La scelta finale spetta comunque all’autore
che può decidere di correggere l’acronimo con la soluzione proposta, sfogliare le varie
correzioni, aggiungere l’acronimo nel dizionario oppure ignorare l’errore. In aggiunta
a queste funzioni è presente l’operazione di correzione automatica, che si incarica di
correggere automaticamente il testo con le correzioni ritenute più opportune.
LATEX Acronyms Proofreading mira a ridurre le lacune degli editor WYSIWYM,
offrendo funzionalità comuni a molti editor WYSIWYG.

## **Funzionamento**
Il funzionamento è descritto dalla seguente rete di petri:

![Funzionamento](https://user-images.githubusercontent.com/28917454/153767676-82f1c704-f445-43cd-b61a-d02569f90087.jpg)

## **Esempio funzionale**

Prendiamo il caso dell’acronimo Particulate Matter (PM), nella versione italiana Particolato. Nel documento è stato definito erroneamente come Particolare, per questo è finito  nella lista degli errori

![esempio1](https://user-images.githubusercontent.com/28917454/153767954-8a490fad-8099-46fb-a9a3-4a8d824fbf7e.jpg)

possiamo vedere nel campo Acronimo il nome esteso errato dell’acronimo (Particolare) e nel campo Problema Riscontrato appunto la conferma che è stato inserito un nome esteso
non corretto. Nel campo Correzioni proposte ci è suggerita la correzione Particolato, aprendo la lista drop-down possiamo vedere tutte le correzioni disponibili per questo acronimo

![esempio2](https://user-images.githubusercontent.com/28917454/153768017-fb150db7-571a-47c0-a5c0-edac83772dba.jpg)

Infatti il plugin ci propone come correzione i nomi estesi di Pubblico Ministero, Particulate Matter, Particolato (selezionato di default), Post Meridiem e Dopo Mezzogiorno. Lasciamo selezionato Particolato, il programma ci offre le funzionalità di correggere, ignorare o aggiungere nel dizionario l’acronimo, premiamo sul pulsante Correggi:



Il messaggio pop-up in figura 5.3 ci comunica che l’operazione di
correzione è andata a buon fine.
