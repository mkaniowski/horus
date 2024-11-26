# Plan aplikacji do analizy logów

### Cel:

Aplikacja analizuje logi backendowe w celu wykrywania potencjalnych zagrożeń, takich jak:

- Ataki brute-force,
- Ataki DDoS,
- Nieautoryzowane próby dostępu,
- Aktywność botów.

### Technologia:

- **Backend**: Kotlin
- **Logi i monitoring**: Grafana Loki
- **Wizualizacja i dashboard**: Grafana
- **Frontend**: React

## 1. Architektura Systemu

### Komponenty:

- **Loki**: Przechowywanie i indeksowanie logów backendowych.
- **Grafana**: Wizualizacja logów oraz dashboard do monitorowania i konfiguracji alertów.
- **Backend (Kotlin)**: Analiza logów, wykrywanie podejrzanych wzorców, integracja z Loki, logika automatycznych
  reakcji.
- **Frontend (React)**: Interfejs użytkownika do przeglądania logów, alertów oraz konfiguracji zasad bezpieczeństwa.

### Przepływ Danych:

1. Backend zapisuje logi aplikacji do Loki, uwzględniając szczegóły takie jak adres IP, typ żądania, kod odpowiedzi,
   czas trwania żądania.
2. Loki indeksuje logi i umożliwia ich analizę oraz przeszukiwanie.
3. Grafana pobiera dane z Loki i wyświetla je na dashboardzie.
4. Backend analizuje logi na żywo, wykrywając wzorce potencjalnych ataków, i generuje alerty do interfejsu React oraz
   Grafana.

---

## 2. Implementacja Kluczowych Funkcjonalności

### a. Zbieranie i Przesyłanie Logów

- **Konfiguracja logów w backendzie**: Zadbaj, aby logi zawierały:
    - Adres IP (`X-Forwarded-For` lub `RemoteAddr`),
    - Nagłówek User-Agent,
    - Typ i czas operacji,
    - Status odpowiedzi HTTP.
- **Wysyłanie logów do Loki**: Użyj loki-loggera do automatycznego przesyłania logów do serwera Loki.

### b. Detekcja Ataków

1. **Brute-force**: Wykrywanie wielokrotnych nieudanych prób logowania z jednego IP w krótkim czasie.
    - Analiza żądań przychodzących przez Loki lub Kotlin, zliczając błędy logowania z tego samego IP.
    - Po przekroczeniu określonego progu, wygenerowanie alertu i ewentualna blokada.

2. **DDoS**: Analiza liczby żądań z tego samego IP lub regionu w krótkim czasie.
    - Filtrowanie żądań przez Loki, aby sprawdzić, czy liczba żądań przekracza ustalone limity.
    - Dynamiczna blokada IP lub modyfikacja firewall w przypadku ataku.

3. **Unauthorized Requests**: Detekcja prób dostępu do zasobów bez autoryzacji.
    - Monitorowanie statusów odpowiedzi 403 lub 401.
    - Generowanie alertów z możliwością przeglądu szczegółowych logów.

4. **Bot Detection**: Wykrywanie ruchu botów poprzez analizę:
    - **Powtarzalność requestów**: Równe odstępy czasowe między żądaniami z jednego adresu IP lub User-Agent mogą
      sugerować boty.
    - **Szybkość wysyłania requestów**: Znacznie szybsze niż przeciętny użytkownik odstępy między żądaniami mogą
      wskazywać na aktywność botów.
    - **Nagłówek User-Agent**: Filtry mogą wykrywać nietypowe lub niestandardowe wzorce User-Agent.

   Jeśli bot zostanie wykryty, możesz automatycznie blokować jego adres IP lub obniżyć jego priorytet.

---

## 3. Tworzenie Systemu Alertów

- **Alerty Grafana**: Ustaw zapytania w Grafana, które uruchamiają alerty w przypadku wykrycia podejrzanych działań,
  takich jak nagły wzrost prób logowania lub nieautoryzowane żądania.
- **Alerty w aplikacji**: Backend w Kotlinie może generować alerty na podstawie analizy logów i wysyłać je do interfejsu
  React.

---

## 4. Frontend w React

- **Panel administracyjny**: Interfejs do przeglądania logów, alertów i statystyk.
- **Ustawienia polityk bezpieczeństwa**: Możliwość konfiguracji progów, przy których system wykrywa atak.
- **Integracja z Grafana**: Osadzenie paneli Grafana lub korzystanie z API do wyświetlania wykresów na żywo.

---

## 5. Przetestowanie i Optymalizacja

- **Testy obciążeniowe**: Symulacja różnych ataków, aby sprawdzić, czy system odpowiednio reaguje.
- **Optymalizacja alertów i progów wykrywania**: Redukcja fałszywych alarmów poprzez kalibrację ustawień.

---

## Podsumowanie

Dzięki Loki i Grafana uzyskasz centralne miejsce do monitorowania logów i wykrywania podejrzanych działań. Kotlin
pozwala na zaawansowaną analizę danych na backendzie, a React ułatwi zarządzanie alertami i konfiguracją polityk
bezpieczeństwa.
