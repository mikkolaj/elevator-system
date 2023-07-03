# System wind

Na początku trzeba się zastanowić na jakiej podstawie będziemy obsługiwać zlecenia dla wind.
Możliwości jest co najmniej kika:

- FCFS - najprostszy, często bardzo nieoptymalny, złośliwy użytkownik, może wyklikać kombinację pięter, która sprawi,
  że winda będzie jeździć na zmianę między najdalej oddalonymi od siebie piętrami
- Optymalizacja długości trasy - wybieramy optymalną trasę dla windy, ale przez to korzystanie z windy może być
  nieintuicyjne. Pozostaje pytanie, czy powinniśmy optymalizować trasę kosztem czasu oczekiwania użytkownika, który wsiadł
  wcześniej.
- Każda winda ma przypisany kierunek, w którym jedzie i najpierw obsługuje zlecenia z tego kierunku.

Wybieram opcję 3 ponieważ wydaje mi się najbardziej intuicyjna dla użytkownika.

## Założenia co do systemu:

- na każdym piętrze jest panel do przywoływania wind z przyciskiem w górę i w dół
- użytkownik windy przed wejściem może zaobserwować na wyświetlaczu, w którą stronę jedzie
- gdy użytkownik wyklika w windzie piętro zgodne z kierunkiem jazdy windy, to ma gwarancję, że dotrze tam bez zmian kierunku
- gdy użytkownik wyklika w windzie piętro przeciwne do kierunku jazdy windy, to będzie czekał aż winda nie będzie miała
żadnych pięter do odwiedzenia jadąc zgodnie z kierunkiem jazdy

## Jak działa winda:
- winda jest niezależnym bytem w systemie i przyjmuje zlecenia od systemu, jak i od osób w windzie
- winda w jednym kroku porusza się o dyskretną wartość pięter (domyślnie 1)
- winda zmienia kierunek tylko wtedy, gdy na liście zleceń nie ma postoju, do którego można dotrzeć zgodnie z kierunkiem
  jazdy windy
- windę można interpretować jako FSM z trzema stanami (Idle, TakingPassengers, Moving)
- w jednej turze (komenda Step) może nastąpić jedno przejście między stanami windy
- pozostałe komendy do wind są przetwarzane pomiędzy turami
- ElevatorSystem bierze pod uwagę wszystkie powyższe założenia i dla każdego zlecenia z piętra wybiera windę, która
dotrze do niego najszybciej

## Jak uruchomić
Zbudować jarkę i uruchomić ze skryptu:
- `sbt assembly`
- `./run.sh`

## Jak sterować
Sterujemy za pomocą requestów HTTP, dostępnych do zaimportowania z postmanowej kolekcji `elevator-system.json`.

## Opis API
 - `GET /system/states` - pobiera stan wszystkich wind
 - `POST /system/pickup` - przyjmuje zlecenie z jednego z zewnętrznych paneli do sterowania
 - `POST /system/step` - wykonuje jedną turę w symulacji
 - `POST /elevator/<id>/stopAt` - przyjmuje zlecenie z wewnętrznego panelu windy

## Co można rozwinąć/poprawić
- Testy nie pokrywają każdego edge-case'u, ale mimo to pomogły mi weryfikować zmiany w kodzie
- Wypadałoby dorobić walidację wejść użytkownika i zwracać odpowiednie kody błędu w API HTTP. Obecnie użytkownik może
zawołać windę na nieistniejące piętro
- Można by dorobić persystencję kolejki zleceń w każdej windzie, żeby nie tracić jej po restarcie systemu
- Konfiguracja aplikacji jest obecnie zahardkodowana w obiekcie Config, lepiej byłoby wykorzystać plik z propertiesami,
co umożliwiłaby m.in. libka typesafe/config.
- Gdyby to był backend to aplikacji webowej, a nie apka konsolowa, to dobrze byłoby dodać logowanie zdarzeń w systemie
- Niektóre klasy mają za dużo odpowiedzialności np. ElevatorState mógłby wyodrębnić funkcjonalność liczenia dystansu do
innej klasy