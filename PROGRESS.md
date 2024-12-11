# Horus

## Status

- [x] Zaimplementowano podstawową funkcjonalność zbierania logów
- [x] Zaimplementowano podstawową funkcjonalność analizy logów
- [x] Zaimplementowano podstawowego clienta do wyświetlania logów na wykresie
- [x] Zaimplementowano podstawowego clienta do wyświetlania anomalii na wykresie
- [x] Generowanie sztucznych logów do testów z losowymi anomaliami
- [ ] Zbieranie logów z aplikacji w czasie rzeczywistym
- [ ] Bardziej zaawansowana analiza logów
- [ ] Możliwość konfiguracji progów analizy logów
- [ ] Testy
- [ ] Dokumentacja

## Klasy

### Log:
    
```kotlin
@Entity
@Table(name = "logs")
open class LogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @Size(max = 15)
    @NotNull
    @Column(name = "ip", nullable = false, length = 15)
    open var ip: String? = null

    @Size(max = 255)
    @Column(name = "\"user\"")
    open var user: String? = null

    @NotNull
    @Column(name = "\"timestamp\"", nullable = false)
    open var timestamp: Instant? = null

    @Size(max = 10)
    @NotNull
    @Column(name = "method", nullable = false, length = 10)
    open var method: String? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "endpoint", nullable = false)
    open var endpoint: String? = null

    @Size(max = 10)
    @NotNull
    @Column(name = "protocol", nullable = false, length = 10)
    open var protocol: String? = null

    @NotNull
    @Column(name = "status_code", nullable = false)
    open var statusCode: Int? = null

    @NotNull
    @Column(name = "body_bytes_sent", nullable = false)
    open var bodyBytesSent: Int? = null

    @Size(max = 255)
    @Column(name = "http_referer")
    open var httpReferer: String? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "user_agent", nullable = false)
    open var userAgent: String? = null

    @NotNull
    @Column(name = "request_length", nullable = false)
    open var requestLength: Int? = null
}
```

### Anomaly:

```kotlin
@Entity
@Table(name = "logs_anomalies")
open class LogAnomalyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    open var id: UUID? = null

    @NotNull
    @Column(name = "timestamp_from", nullable = false)
    open var timestampFrom: Instant? = null

    @NotNull
    @Column(name = "timestamp_to", nullable = false)
    open var timestampTo: Instant? = null

    @Size(max = 20)
    @NotNull
    @Column(name = "level", nullable = false, length = 20)
    open var level: String? = null

    @Size(max = 255)
    @NotNull
    @Column(name = "endpoint", nullable = false)
    open var endpoint: String? = null

    @NotNull
    @Column(name = "number_of_hits", nullable = false)
    open var numberOfHits: Int? = null

    @Size(max = 255)
    @Column(name = "anomaly_type")
    open var anomalyType: String? = null

    @NotNull
    @Column(name = "body_bytes_sent", nullable = false)
    open var bodyBytesSent: Int? = null

    @NotNull
    @Column(name = "request_length", nullable = false)
    open var requestLength: Int? = null
}
```


## Podstawowa analiza logów:

System wykorzystuje znaczniki czasu (timestamp), aby pobierać jedynie nowe logi od ostatniej analizy.

Wykrywanie anomalii:

- Brute-force: Identyfikowane są wielokrotne nieudane próby logowania w krótkim czasie na endpoint /login. Jeśli liczba prób przekracza ustalony próg (np. 10) w zadanym oknie czasowym, atak jest oznaczany jako wykryty.


- DDoS: Analizowane są żądania o dużym natężeniu i wielkości w krótkim czasie. Jeśli liczba żądań i suma przesyłanych danych przekroczy próg, uznaje się to za potencjalny atak DDoS.

- Aktywność botów: Wykrywana jest na podstawie powtarzalnych odstępów czasowych między żądaniami oraz charakterystycznych wartości w nagłówku User-Agent.


- Zapisywanie anomalii: Wykryte anomalie są zapisywane do bazy danych jako encje z informacjami o typie anomalii, czasie wystąpienia, endpointach i poziomie zagrożenia.


- Harmonogram analizy: Analiza jest uruchamiana cyklicznie co 30 sekund przy użyciu adnotacji @Scheduled, zapewniając bieżące monitorowanie aktywności w logach.


- Proces ten łączy proste heurystyki, takie jak liczba prób w danym czasie, z bardziej specyficznymi wzorcami, np. powtarzalność odstępów czasowych czy rozpoznawanie cech charakterystycznych dla ruchu botów.


## Przykładowy log z nginx

```
167.156.242.235 - - [2024-12-10 14:05:12] "PUT /admin HTTP/1.1" 200 184 "https://example.com/data" "curl/7.68.0" 629
```

Co zonaczają poszczególne pola:

```kotlin
"${log.ip} - ${log.user} [${log.timestamp}] \"${log.method} ${log.endpoint} ${log.protocol}\" ${log.statusCode} ${log.bodyBytesSent} \"${log.httpReferer}\" \"${log.userAgent}\" ${log.requestLength}\n"
```