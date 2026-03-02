# Report Versione 2: Gestione Proposte e Bacheca

Questo documento riassume l'implementazione delle funzionalità richieste per la **Versione 2** dell'applicazione, che si focalizza sulla creazione e pubblicazione di iniziative da parte del Configuratore.

## 1. Funzionalità Implementate

### Modello Dati
- **`Proposta.java`**: Rappresenta un'iniziativa concreta basata su una `Categoria`. Gestisce una mappa dinamica (`Map<String, String>`) per immagazzinare i valori inseriti dall'utente per ciascun campo (Titolo, Data, Luogo, ecc.). Contiene la logica per validare se stessa, controllando la presenza dei campi segnalati come obbligatori e rispettando i vincoli temporali previsti.
- **`StatoProposta.java`**: Enumerazione degli stati che la proposta può assumere in questa fase (solo `VALIDA` e `APERTA`).
- **`Bacheca.java`**: Architettura in memoria che mappa e raggruppa le proposte pubblicate (`APERTE`) dividendole per nome della Categoria, in preparazione alle ricerche future.

### Controller
- **`GestoreProposte.java`**: Gestisce il ciclo di vita della proposta (attualmente validazione e transizione allo stato `APERTA` nella bacheca). Espone la bacheca per l'accesso e la visualizzazione.

### Persistenza (Storage custom JSON)
- Modifica manuale (senza librerie esterne, per requisiti accademici) alla classe **`JsonUtil.java`**, con i nuovi metodi `scriviProposte` e `leggiProposte` in grado di estrarre mappe complesse di stringhe e ricollegare la proposta deserializzata alla sua referenza `Categoria` esatta in memoria (sfruttando `GestoreCategorie`).
- Aggiornamento in **`GestoreFile.java`** dei metodi I/O di base per leggere e scrivere su `data/proposte.json`. Le proposte non ancora pubblicate non vengono salvate, come da specifiche.

### Interfaccia Utente (CLI)
- Al menu del Configuratore (`MainCLI`) è stato aggiunto il sottomenu `Gestisci Proposte`:
  1. **Crea Nuova Proposta**: Itera dinamicamente su tutti i campi della Categoria scelta. Mostra il tipo atteso (es. STRINGA, DATA, ORA) e avvisa l'utente della loro obbligatorietà o meno tramite il simbolo `*`. La proposta viene scartata avvisando l'utente se i dati sono errati. Offre la scorciatoia per pubblicare immediatamente la proposta se i check passano positivamente.
  2. **Pubblica una Proposta**: Rimanda alla bacheca.
  3. **Visualizza Bacheca**: Mostra l'elenco stilizzato delle proposte già nello stato `APERTA`, raggruppate per macro-categoria.

---

## 2. Test Eseguiti ed Esito

Le funzionalità sono state testate seguendo il flusso:
1. Re-inizializzazione del database caricando i dati fittizi `admin/admin`.
2. Creazione della radice fittizia "Partita di Calcetto" testando il form e le ereditarietà.
3. Chiamata al sottomenu proposte passando dati temporali appositamente errati (es. Data Evento minore o uguale di 2 giorni rispetto alla Data Iscrizione). *L'algoritmo di validazione ha scartato correttamente l'inserimento.*
4. Chiamata al sottomenu compilando tutti i campi base di default rispettando i tempi futuri e pubblicazione in Bacheca.
5. Spegnimento (opzione `8. Salva & Esci`) del programma. È stato verificato visivamente la corretta persistenza su filesystem del file `data/proposte.json`.
6. Ri-esecuzione del programma: The entry-point ha ricaricato correttamente il JSON, convertendolo un array Java con referenze integre (le mappe campi corrispondono all'oggetto Categoria), bypassando il rischio del reset-in-memory.
 
## 3. Considerazioni in vista della Versione 3

Dal punto di vista architetturale per la **Versione 3**, dove i `Fruitori` inizieranno a interagire con la Bacheca, ritengo i componenti già pronti. L'interfaccia `Bacheca.getProposteApertePerCategoria(String)` si presterà ottimamente per permettere ai `Fruitori` di vedere gli eventi cui iscriversi, aggiungendo semplicemente un nuovo `StatoProposta.CHIUSA` o `FALLITA` quando verrà implementato il time-to-live degli eventi.

Tutti gli obiettivi per il modulo V2 sono stati raggiunti.
