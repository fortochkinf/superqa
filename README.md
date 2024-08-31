### Инструкция по запуску тестов
1. Выбрать необходимую(postgres/mysql) БД. Для этого в файле artifacts/application.properties указать spring.datasource.url, а в файле build.gradle настроить переменную dbUrl 
2. Открыть файл docker/docker-compose.yml в idea и нажать зеленый треугольник слева от слова services или перейти в каталог docker и выполнить в терминале команду docker-compose up
3. Открыть терминал. Перейти в каталог artifacts. Выполнить команду java -jar aqa-shop.jar
4. Запустить тесты через idea - нажать зеленый треугольник на строке "class AqaShopTest" в классе AqaShopTest или выполнить команду ./gradlew test
5. Сгенерировать отчет Allure - нажать в idea на панели gradle verification -> allureReport или выполнить в терминале команду ./gradlew allureReport

Сгенерированные отчеты должны находиться в файлах:
* gradle - reports/tests/test/index.html
* allure - reports/allure-report/index.html