# Casi d'Uso: Versione 1 vs Versione 2

---
## Versione 1

### Attori
- **Configuratore**: Utente responsabile dell'impostazione del sistema.

### Casi d'Uso Testuali (V1)
1. **Effettuare Login/Registrazione**: Il configuratore inserisce le credenziali predefinite per accedere per la prima volta e imposta le proprie credenziali personali, oppure esegue il login con credenziali già impostate.
2. **Impostare Campi Base**: Al primissimo avvio, il configuratore definisce i campi obbligatori di sistema.
3. **Gestire Campi Comuni**: Il configuratore può aggiungere, modificare (cambiarne l'obbligatorietà) o rimuovere campi condivisi da tutte le categorie.
4. **Gestire Categorie**:
   - *Creare Categoria*: Il configuratore crea una nuova categoria, specificandone nome e descrizione, e definendone eventuali campi specifici.
   - *Modificare Categoria*: Il configuratore aggiunge o rimuove campi specifici a una categoria esistente.
   - *Rimuovere Categoria*: Il configuratore elimina una categoria e i suoi campi dal sistema.
5. **Visualizzare Categorie**: Il configuratore vede l'albero delle categorie e i rispettivi campi (base, comuni, specifici).

### Diagramma UML Casi d'Uso (PlantUML) - V1
```plantuml
@startuml
left to right direction
actor Configuratore as Admin

rectangle "Gestione Struttura Iniziative (V1)" {
  usecase "Login / Cambio Password" as UC1
  usecase "Gestione Campi Base (1° avvio)" as UC2
  usecase "Gestione Campi Comuni" as UC3
  usecase "Visualizzare Categorie" as UC4
  usecase "Creare Categoria" as UC5
  usecase "Modificare Categoria" as UC6
  usecase "Rimuovere Categoria" as UC7
}

Admin --> UC1
Admin --> UC2
Admin --> UC3
Admin --> UC4
Admin --> UC5
Admin --> UC6
Admin --> UC7
@enduml
```

---
## Versione 2

### Attori
- **Configuratore**

### Casi d'Uso Testuali (V2)
*(I casi d'uso da 1 a 5 rimangono invariati rispetto alla V1).*

**NUOVI CASI D'USO IN V2:**
6. **<mark>Creare Proposta</mark>**: Il configuratore seleziona una categoria e compila un modulo per proporre un nuovo evento. Il sistema verifica la congruenza logica delle date e la compilazione dei campi obbligatori, assegnando lo stato `VALIDA`.
7. **<mark>Pubblicare Proposta in Bacheca</mark>**: Il configuratore sceglie di rendere pubblica una proposta valida, facendola passare allo stato `APERTA` nella bacheca di sistema.
8. **<mark>Visualizzare Bacheca</mark>**: Il configuratore può vedere tutte le proposte attualmente aperte, raggruppate per categoria d'appartenenza.

### Diagramma UML Casi d'Uso (PlantUML) - V2
*(I nuovi casi d'uso sono evidenziati col colore giallo per risaltare)*

```plantuml
@startuml
left to right direction
actor Configuratore as Admin

rectangle "Gestione Struttura Iniziative (V1 - Invariati)" {
  usecase "Login / Gestione Campi" as UC_V1
  usecase "Gestione Categorie" as UC_CAT
}

rectangle "Gestione Proposte e Bacheca (NUOVI in V2)" {
  usecase "Creare Proposta (Valida)" as UC8 #Yellow
  usecase "Pubblicare Proposta (Aperta)" as UC9 #Yellow
  usecase "Visualizzare Bacheca" as UC10 #Yellow
}

Admin --> UC_V1
Admin --> UC_CAT

Admin --> UC8
Admin --> UC9
Admin --> UC10

UC9 .> UC8 : <<includes>>\n(Richiede Proposta Valida)
@enduml
```
