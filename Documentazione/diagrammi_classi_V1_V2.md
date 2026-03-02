# Diagramma delle Classi: Versione 1 vs Versione 2

---
## Versione 1 - UML Class Diagram

```plantuml
@startuml
skinparam classAttributeIconSize 0

package "it.unibs.ing.model" {
    class Categoria {
        - nome: String
        - descrizione: String
        - campi: Map<String, Campo>
        + aggiungiCampo(Campo): void
        + rimuoviCampo(String): void
        + getCampiObbligatori(): List<Campo>
    }

    class Campo {
        - nome: String
        - tipo: TipoCampo
        - obbligatorio: boolean
        + isObbligatorio(): boolean
    }

    enum TipoCampo {
        STRINGA
        INTERO
        DECIMALE
        DATA
        ORA
        BOOLEANO
    }

    abstract class Utente {
        - nome: String
        - password: byte[]
        - sale: byte[]
        + verificaPassword(String): boolean
    }

    class Configuratore {
        + Configuratore(String, String)
    }

    Utente <|-- Configuratore
    Categoria "1" *--> "*" Campo : contiene
    Campo -> TipoCampo : usa
}

package "it.unibs.ing.controller" {
    class GestoreCategorie {
        - categorieRadice: List<Categoria>
        - registroCategorie: Map<String, Categoria>
        + creaCategoria(String, String, Categoria): boolean
        + rimuoviCategoria(String): boolean
    }

    class GestoreSessione {
        - utenti: List<Utente>
        - utenteLoggato: Utente
        + login(String, String): Utente
    }
}
@enduml
```

---
## Versione 2 - UML Class Diagram

*(Le nuove classi introdotte in V2, ovvero `Proposta`, `StatoProposta`, `Bacheca` e `GestoreProposte`, sono evidenziate con il colore di riempimento giallo acceso `#Yellow`)*

```plantuml
@startuml
skinparam classAttributeIconSize 0

package "it.unibs.ing.model" {
    class Categoria {
        - nome: String
        - campi: Map<String, Campo>
    }

    class Campo {
        - nome: String
        - obbligatorio: boolean
    }

    class Proposta #Yellow {
        - categoria: Categoria
        - valoriCampi: Map<String, String>
        - stato: StatoProposta
        + impostaValore(String, String): void
        + verificaValidita(): boolean
        + pubblica(): void
    }

    enum StatoProposta #Yellow {
        VALIDA
        APERTA
    }

    class Bacheca #Yellow {
        - bacheca: Map<String, List<Proposta>>
        + aggiungiPropostaAperta(Proposta): void
        + getTutteLeProposte(): Map<String, List<Proposta>>
    }

    Categoria "1" *--> "*" Campo
    Proposta "*" --> "1" Categoria : si basa su
    Proposta -> StatoProposta : ha
    Bacheca "1" o--> "*" Proposta : contiene (Aperte)
}

package "it.unibs.ing.controller" {
    class GestoreCategorie {
        - registroCategorie: Map<String, Categoria>
    }

    class GestoreProposte #Yellow {
        - bacheca: Bacheca
        + validaProposta(Proposta): boolean
        + pubblicaProposta(Proposta): void
    }
    
    GestoreProposte --> Bacheca : gestisce
}
@enduml
```
