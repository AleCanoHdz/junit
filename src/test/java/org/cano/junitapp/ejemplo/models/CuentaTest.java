package org.cano.junitapp.ejemplo.models;

import org.cano.junitapp.ejemplo.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS) //Esto no es una buena practica ya que crearia una sola instancia 2
class CuentaTest {

    Cuenta cuenta; //Esta variable no mantiene el estado de los metodos
    private TestInfo testInfo;
    private TestReporter testReporter;
    @BeforeEach //Esta anotacion indica que se ejecutara antes de cada metodo test
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) { //Inyeccion de dependencias testInfo y testReporter, de esta manera se inyectarian en todos los metodos
        System.out.println("Iniciando el metodo");

        cuenta = new Cuenta("Andres", new BigDecimal("1000.1234"));

        this.testInfo = testInfo;
        this.testReporter = testReporter;

        //System.out.println("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName()
        //      + " con las etiquetas: " + testInfo.getTags()); //Mostrara la info indicada de cada test con el TestInfo

        //En lugar de imprimirlo con sout es recomendable hacerlo con la salida de junit usando testReporter y publishEntry

        testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName()
                + " con las etiquetas: " + testInfo.getTags()); //Mostrara la info indicada de cada test con el TestInfo

    }

    @AfterEach //Esta anotacion indica que se ejecutara despues de cada metodo test
    void tearDown() {
        System.out.println("Finalizando el metodo de prueba.");
    }

    @BeforeAll
    static void beforeAll() { //Esta anotacion permite que se ejecute antes de la creacion del test y solo se ejecuta una vez, al ser static puede ser ejecutado
        System.out.println("Inicializando el test");
    }

    @AfterAll
    static void afterAll() { //Esta anotacion permite que se ejecute el metodo al finalizar el test
        System.out.println("Finalizando el test");
    }

    @Tag("cuenta")
    @Nested
    @DisplayName("probando atributos de cuenta")
    class CuentaTestNombreSaldo{
        @Test
        @DisplayName("Probando nombre de la cuenta corriente") //Esta anotación nos permite especificar un nombre al test que se mostrara
        void testNombreCuenta() { //Inyeccion de depencencias TestInfo y TestReporter

            testReporter.publishEntry(testInfo.getTags().toString());
            if(testInfo.getTags().contains("cuenta")){
                testReporter.publishEntry("Hacer algo con la etiqueta cuenta");
            }


            cuenta = new Cuenta("Andres", new BigDecimal("1000.1234"));
            //cuenta.setPersona("Andres");
            String esperado = "Andres";
            String real = cuenta.getPersona();

            //Agregar mensajes de error personalizados de manera literal
            assertNotNull(real, "El nombre de la cuenta no puede ser nula");
            //De esta manera se esta creando el objeto mensaje aun cuando no exista error
            assertEquals(esperado, real, "El nombre de la cuenta no es el que se esperaba, se esperaba: "+esperado
                    + " sin embargo fue el " + real);
            //Para evitar que se cree el objeto mensaje, se crea una funcion lambda, que ejecutara un metodo solo si envia un error
            assertTrue(real.equals("Andres"), () -> "nombre esperado debe ser igual al real"); //Deevuelve una expresion boleana
        }

        @Test
        @DisplayName("Probando el saldo de la cuenta que sea null, mayor que 0, valor esperado")
        void testSaldoCuenta() {
            assertEquals(1000.1234, cuenta.getSaldo().doubleValue());
            //Regla de negocio: Afirmar que el saldo nunca sea 0 o negativo
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

        }

        @Test
        @DisplayName("Probar referencias que sean iguales con el metodo equials")
        void testReferenciaCuenta() {
            Cuenta cuenta1 = new Cuenta("Jhon", new BigDecimal("8990.1234"));
            Cuenta cuenta2 = new Cuenta("Jhon", new BigDecimal("8990.1234"));

            //assertNotEquals(cuenta1, cuenta2); //Compara por referencia
            assertEquals(cuenta1, cuenta2); //Compara por valor

        }
    }

    @Nested
    class CuentaOperacionesTest{
        @Tag("cuenta") //Las etiquetas pueden estar dispersas entre las diferentes clases
        @Test
        void testDebitoCuenta() {
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.1234", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Test
        void testCreditoCuenta() {
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.1234", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Tag("banco") //Pueden tener mas de una etiqueta
        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("Juan", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Pedro", new BigDecimal("1500.123"));

            Banco banco = new Banco();
            banco.setNombre("Banco del estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
            assertEquals("1000.123", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }


    @Tag("cuenta")
    @Tag("error")
    @Test
    void testDineroinsuficienteException() {
        //Si se coloca un monto mayor deberia lanzar una excepcion
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito(new BigDecimal(1500));
        });
        String actual = exception.getMessage();
        String esperado = "Dinero insuficiente";
        assertEquals(esperado, actual);
    }


    @Tag("cuenta")
    @Tag("banco")
    @Test
    //@Disabled //Esta anotacion permite deshabilitar un test, de esta manera podemos continuar con otras implementaciones
    @DisplayName("Probando relaciones emtre las cuentas y el banco con assertAll.")
    void testRelacionBancoCuentas() {
        //fail(); //Este metodo obliga el metodo a fallar
        Cuenta cuenta1 = new Cuenta("Juan", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Pedro", new BigDecimal("1500.123"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);
        banco.setNombre("Banco del estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

        //assertAll funciona con expresiones lambda ()->{}, separadas por coma en cada asert
        assertAll(
                () -> {
                    assertEquals("1000.123", cuenta2.getSaldo().toPlainString(),
                            ()-> "El valor de saldo de la cuenta 2 no es el esperado");
                },
                () -> {
                    assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                            ()-> "El valor de saldo de la cuenta 1 no es el esperado");
                },
                () -> {//Validar que el banco cuente con 2 cuentas
                    assertEquals(2, banco.getCuentas().size(),
                            ()-> "El numero de cuentas en el banco no es el esperado");
                },
                () -> {//Validar que la cuente sea de un banco especifico
                    assertEquals("Banco del estado", cuenta1.getBanco().getNombre(),
                            ()-> "La cuenta no pertenece al banco esperado");
                },
                () -> {//Validar que una cuenta pertenezca a un cierto nombre o persona partiendo desde el banco
                    assertEquals("Juan", banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Juan"))
                            .findFirst()
                            .get().getPersona(),
                            ()->"No se encuentra ninguna cuenta afiliada al valor esperado"
                    );
                },
                () -> {//mediante assertTrue lo que devuelve un boolean si se cumple
                    assertTrue(banco.getCuentas().stream()
                            .anyMatch(c -> c.getPersona().equals("Pedro")),
                                    ()->"No se encuentra ninguna cuenta igual a lo especificado"
                    );
                }
        );


    }

    //Anidar tests usando @Nested

    //Se crea una clase en la que colocaremos todos los metodos que queramos ejecutar en ese grupo

    @Nested
    class SistemasOperativos{
        @Test
        @EnabledOnOs(OS.WINDOWS) //Esta anotación permite que solo se ejecute en el sistema operativo windows
        void testSoloWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX,OS.MAC}) //En caso de que sean varios sistemas operativos se colocan dentro de {} y se separan por comas
        void testSoloLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS) //Esta anotación al contrario de Enable, no se ejecuta en el sistema operativo especificado
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersion{
        @Test
        @EnabledOnJre(JRE.JAVA_8) //Esta anotacion indica que el test solo sera ejecutado en la version 8 de java
        void soloJDK8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_19) //Version actual del proyecto
        void soloJdk19() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_19) //Version actual del proyecto
        void testNoJdk19() {
        }
    }

    @Nested
    class SystemPropertiesTests{
        //Ejecutar test si contiene una propiedad de sistema especifico

        //Para conocer las propiedades del sistema
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k,v)-> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named="java.version", matches = "19.0.2") //Habilitar si existe la pripiedad llamada java.version y el valor es 19, si se quiere colocar una aproximacion se coloca ".*19.*"
        void testJavaVersion() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*") //Esta deshabilitara el test en arquitecturas de 32bits
        void testSolo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*") //Esta deshabilitara el test en arquitecturas de 64bits
        void testNo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name",matches = "comoc")
        void testUsername() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev") //Configurar nuestras propias propiedades
        void testDev() {
        }
    }

    @Nested
    class VariableAmbienteTest{
        //Ejecutar dependiendo de las variables de ambiente del sistema

        @Test
        void imprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k,v)-> System.out.println(k + " : " + v));

        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-11.0.18.10.*")
        void testJavaHome() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "4")
        void testProcesadores() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
        void testEnvProdDisabled() {
        }
    }





    //Para que solo este test solo se ejecute si se cumple una condicion, se usa assumeTrue/False, de esta manera se seguira ejecutando el metodo
    @Test
    @DisplayName("testSaldoCuentaDev")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV")); //Verifica que el ambiente de desarrollo sea dev y lo guarda en una variable booleana
        assumeTrue(esDev); //Si el ambiente de desarrollo es dev o sea true, se ejecutara el resto del metodo
        assertEquals(1000.1234, cuenta.getSaldo().doubleValue());
        //Regla de negocio: Afirmar que el saldo nunca sea 0 o negativo
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

    }

    //Si se quiere ejecutar solo una parte del metodo utilizamos assumingThat(boolean, ()->{Lo que se ejecutara si es verdadero}

    @Test
    @DisplayName("testSaldoCuentaDev 2")
    void testSaldoCuentaDev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV")); //Verifica que el ambiente de desarrollo sea dev y lo guarda en una variable booleana
        assumingThat(esDev, ()->{//Si el ambiente de desarrollo es dev o sea true, se ejecutara lo que contenga la expresion lambda
            assertEquals(1000.1234, cuenta.getSaldo().doubleValue());
            //Regla de negocio: Afirmar que el saldo nunca sea 0 o negativo
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        });
    }

    //Repetir una prueba con @RepeatedTest, se utiliza cuando una variable o parametro dentro de nuestro metodo cambia por ejemplo un valor ramdom
    @RepeatedTest(value = 5, name = "Repeticion numero {currentRepetition} de {totalRepetitions}") //Se coloca la cantidad de veces que se quiera repetir
    @DisplayName("Repeated")
    void testDebitoCuentaRepeated(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3){
            System.out.println("Estas en la repeticion 3");
        }
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.1234", cuenta.getSaldo().toPlainString());
    }

    @Tag("param") //Esta anotacion nos permite etiquetar a todos los metodos que se encuentren dentro de la clase, de esta manera podriamos ejecutar los metodos que sean param si asi se desea
    @Nested
    public class ParametrizedTest{
        //Pruebas parametrizadas, esta tipo de prueba es similar a repeated pero colocamos datos de entrada en lugar de que se generen los datos aleatorios o cambiantes
        @ParameterizedTest(name = "numero {index} ejecutando con el valor: {0}") //Anotación para un test parametrizado, para personalizar lo que muestra el test como nombre, se coloca name, el indice y el parametro (0)
        @DisplayName("ParametrizedTest")
        @ValueSource(strings = {"100","200","300","500","700","1000"}) //En esta anotación se van a especificar los valores que se asignaran al parametro
        void testDebitoCuentaValueSource(String monto) {//Dentro de los parametros del metodo se crea una variable que va recibir de ValueSource, esta debe ser del mismo tipo de dato
            cuenta.debito(new BigDecimal(monto)); //Hacer que el valor ingresado para el metodo debito sea parametrizado y podamos colocar diferentes valores
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); //Esto verifica que el saldo sea positivo
        }

        //Otra forma de prueba parametrizada Csv (indice y valor)
        @ParameterizedTest(name = "numero {index} ejecutando con el valor: {0}") //Anotación para un test parametrizado, para personalizar lo que muestra el test como nombre, se coloca name, el indice y el parametro (0)
        @DisplayName("ParametrizedTestCSV")
        @CsvSource({"1,100","2,200","3,300","4,500","5,700","6,1000.123"}) //Con Csv se coloca el indice seguido de una coma y su valor
        void testDebitoCuentaCSV(String index, String monto) { //Dentro de los parametros se tiene que crear la variable de entrada indice
            System.out.println(index + "-> " + monto);
            cuenta.debito(new BigDecimal(monto)); //Hacer que el valor ingresado para el metodo debito sea parametrizado y podamos colocar diferentes valores
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); //Esto verifica que el saldo sea positivo
        }

        //CsvSource con dos diferentes argumentos

        @ParameterizedTest(name = "numero {index} ejecutando con el valor: {0}") //Anotación para un test parametrizado, para personalizar lo que muestra el test como nombre, se coloca name, el indice y el parametro (0)
        @DisplayName("ParametrizedTestCSVModificado")
        @CsvSource({"200,100,Juan,Pedro","250,200,Pepe,Pepe","350,300,Maria,Maria","600,500,Jose,Josh","750,700,Manuel,Manuel","1000.1234,1000.123,Jorch,Jorge"}) //Con Csv se coloca el indice seguido de una coma y su valor
        void testDebitoCuentaCSVModificado(String saldo, String monto, String esperado, String actual) { //Dentro de los parametros se tiene que crear la variable de entrada indice
            System.out.println(saldo + "-> " + monto);
            cuenta.setPersona(actual);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto)); //Hacer que el valor ingresado para el metodo debito sea parametrizado y podamos colocar diferentes valores
            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado,actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); //Esto verifica que el saldo sea positivo
        }

        //CsvFileSource (archivo.csv)
        @ParameterizedTest(name = "numero {index} ejecutando con el valor: {0}") //Anotación para un test parametrizado, para personalizar lo que muestra el test como nombre, se coloca name, el indice y el parametro (0)
        @DisplayName("ParametrizedTestCSVFile")
        @CsvFileSource(resources = "/data.csv") //Con CsvFile se coloca la ubicacion del archivo donde se encuentran los argumentos
        void testDebitoCuentaCSVFile(String monto) { //Dentro de los parametros solo se coloca la variable que tomara del archivo
            cuenta.debito(new BigDecimal(monto)); //Hacer que el valor ingresado para el metodo debito sea parametrizado y podamos colocar diferentes valores
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); //Esto verifica que el saldo sea positivo
        }

        //CvsFileSource con mas argumentos
        @ParameterizedTest(name = "numero {index} ejecutando con el valor: {0}") //Anotación para un test parametrizado, para personalizar lo que muestra el test como nombre, se coloca name, el indice y el parametro (0)
        @DisplayName("ParametrizedTestCSVFileModificado")
        @CsvFileSource(resources = "/data2.csv") //Con CsvFile se coloca la ubicacion del archivo donde se encuentran los argumentos
        void testDebitoCuentaCSVFileModificado(String saldo, String monto, String esperado, String actual) { //Dentro de los parametros solo se coloca la variable que tomara del archivo
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto)); //Hacer que el valor ingresado para el metodo debito sea parametrizado y podamos colocar diferentes valores
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo()); //Verificamos que no tenga valores nullos
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado,actual); //Comparamos el valor esperado con el actual

            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); //Esto verifica que el saldo sea positivo
        }

    }

    //MethodSource (Metodo estatico)

    @Tag("param") //Tambien puede ser usado en cada metodo individual
    @ParameterizedTest(name = "numero {index} ejecutando con el valor: {0}") //Anotación para un test parametrizado, para personalizar lo que muestra el test como nombre, se coloca name, el indice y el parametro (0)
    @DisplayName("ParametrizedTestMethodSource")
    @MethodSource("montoList") //MethodSource ejecuta un metodo estatico ya antes creado especificando el nombre
    void testDebitoCuentaMethodSource(String monto) { //Dentro de los parametros solo se coloca la variable que tomara del archivo
        cuenta.debito(new BigDecimal(monto)); //Hacer que el valor ingresado para el metodo debito sea parametrizado y podamos colocar diferentes valores
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0); //Esto verifica que el saldo sea positivo
    }

    static List<String> montoList(){

        return Arrays.asList("100","200","300","500","700","1000"); //Este es un atajo de la creacion de una lista

    }

    @Nested
    @Tag("timeout")
    public class EjemploTimeoutTest{
        //Timeout para marcar como error un metodo que demora cierta cantidad de tiempo definida

        @Test
        @Timeout(5) //Timeout indica que si la prueba demora mas de 5 segundos en ejecutarse va fallar la prueba
        void testTimeOut() throws InterruptedException {
            TimeUnit.SECONDS.sleep(4); //La tarea va durar 6 segundos usando TimeUnit, por lo tanto el test va fallar
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS) //Timeout indica que si la prueba demora mas de 1000 milisegundos en ejecutarse va fallar la prueba
        void testTimeOut2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900); //La tarea va durar 20000 milisegundos usando TimeUnit, por lo tanto el test va fallar
        }

        @Test
        void pruebaTimeOutAssertions() {
            assertTimeout(Duration.ofSeconds(5),()->{ //assertTimeout realiza lo mismo que la anotacion @Timeout, dentro de ella solo se define la duracion y la accion que quiere realizar dentro de una expresion lambda
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }
    }

}