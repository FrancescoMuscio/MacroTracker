# MacroTracker 

MacroTracker è un'applicazione Android progettata per monitorare l'apporto nutrizionale giornaliero, gestire un database personale di alimenti e tenere traccia dei progressi fisici attraverso misurazioni antropometriche.

## Funzionalità Principali

### Monitoraggio Nutrizionale
- **Dashboard Giornaliera**: Visualizza in tempo reale le calorie assunte e i macronutrienti (Proteine, Carboidrati, Grassi) rispetto agli obiettivi impostati.
- **Grafici Dinamici**: Indicatori visivi e grafici a torta per analizzare la distribuzione dei macro nel pasto attuale o nell'intera giornata.
- **Gestione Pasti**: Organizza le tue entrate in Colazione, Pranzo, Cena e Spuntini.

### Database Alimenti Flessibile
- **Inserimento Intelligente**: Aggiungi nuovi alimenti specificando i macro per **100g** o per **porzione singola**.
- **Logging Rapido**: Quando aggiungi un alimento salvato come "porzione", l'app ti permette di loggarlo semplicemente inserendo il numero di porzioni (es. 1, 0.5, 2), automatizzando il calcolo dei grammi.
- **Ricerca Rapida**: Filtra istantaneamente il tuo database personale per trovare ciò che ti serve.

### Progressi e Body Tracking
- **Misurazioni Corporee**: Registra peso e oltre 15 diverse misure (collo, spalle, torace, vita, fianchi, bicipiti, ecc.).
- **Storico e Trend**: Visualizza grafici dell'andamento del peso e delle misure nel tempo per monitorare la tua trasformazione fisica.

### Personalizzazione e UI
- **Temi e Palette**: Scegli tra diversi temi estetici (es. Midnight, Amber, ecc.) per adattare l'app ai tuoi gusti.
- **Multilingua**: Supporto completo per Italiano e Inglese.
- **Esportazione Dati**: Esporta il tuo storico alimentare in formato CSV per analisi esterne.

## Specifiche Tecniche

- **Linguaggio**: Kotlin
- **UI Framework**: Native Android (programmatic UI senza XML layout, per massima fluidità e controllo).
- **Storage**: SharedPreferences con serializzazione JSON per una persistenza dei dati leggera e veloce.
- **Architettura**: Single Activity con gestione dello stato reattiva.

## Requisiti
- Android 8.0+ (API 26+)
