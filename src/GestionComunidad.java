import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GestionComunidad {

    // Constantes de configuración de la comunidad
    private static final int CUOTA_MENSUAL = 5;
    private static final YearMonth MES_ACTUAL = YearMonth.of(2026, 9); 
    private static final String FILE_ENTRADA = "propietarios.csv";
    private static final String FILE_SALIDA = "README.md";
    
    // Configuración de GitHub
    private static final String USUARIO_GITHUB = "JokeeRpg"; 
    private static final String REPO_URL = "https://github.com/" + USUARIO_GITHUB + "/gestion-comunidad-dolores-ibarruri.git";

    // Clase interna para representar a cada propiedad
    public static class Propiedad {
        private String piso;
        private String propietario;
        private String estadoPago;
        private double deuda;

        public Propiedad(String piso, String propietario, String estadoPago) {
            this.piso = piso;
            this.propietario = propietario;
            this.estadoPago = estadoPago;
            this.deuda = 0.0;
        }

        public String getPiso() { return piso; }
        public String getPropietario() { return propietario; }
        public String getEstadoPago() { return estadoPago; }
        public double getDeuda() { return deuda; }
        public void setDeuda(double deuda) { this.deuda = deuda; }
    }

    public static void main(String[] args) {
        List<Propiedad> listadoViviendas = cargarPropietarios();
        
        if (!listadoViviendas.isEmpty()) {
            calculateDeudasComunidad(listadoViviendas);
            generarInformeMarkdown(listadoViviendas);
            System.out.println("✅ Informe local 'README.md' generado correctamente.");
            
            // Aquí ejecutamos de forma automática la subida a GitHub
            subirAGitHubAutomatizado();
        } else {
            System.out.println("❌ No se pudieron cargar los datos de los propietarios. Verifica que propietarios.csv esté en la carpeta del proyecto.");
        }
    }

    private static List<Propiedad> cargarPropietarios() {
        List<Propiedad> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_ENTRADA))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 3) {
                    lista.add(new Propiedad(datos[0].trim(), datos[1].trim(), datos[2].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer propietarios.csv: " + e.getMessage());
        }
        return lista;
    }

    private static void calculateDeudasComunidad(List<Propiedad> propiedades) {
        for (Propiedad prop : propiedades) {
            if (!prop.getEstadoPago().equalsIgnoreCase("Gestionado")) {
                YearMonth pagadoHasta = YearMonth.parse(prop.getEstadoPago());
                if (pagadoHasta.isBefore(MES_ACTUAL)) {
                    long mesesDeDiferencia = ChronoUnit.MONTHS.between(pagadoHasta, MES_ACTUAL);
                    prop.setDeuda(mesesDeDiferencia * CUOTA_MENSUAL);
                }
            }
        }
    }

    private static void generarInformeMarkdown(List<Propiedad> propiedades) {
        try (PrintWriter writer = new PrintWriter(FILE_SALIDA)) {
            writer.println("# 🏢 Estado de Cuentas Consolidado");
            writer.println("Fecha de corte de la aplicación: **" + MES_ACTUAL.getMonth() + " de " + MES_ACTUAL.getYear() + "**");
            writer.println("Cuota fija mensual: **" + CUOTA_MENSUAL + " €**\n");
            writer.println("---");

            String[] plantas = {"Bajo", "1º", "2º", "3º"};
            double totalDeudaComunidad = 0.0;

            for (String planta : plantas) {
                writer.println("\n## 🔹 Planta: " + planta);
                writer.println("| Piso | Propietario / Titular | Estado de Pago | Cantidad Adeudada |");
                writer.println("| :--- | :--- | :--- | :--- |");

                for (Propiedad prop : propiedades) {
                    if (prop.getPiso().startsWith(planta)) {
                        String deudaStr = (prop.getDeuda() > 0) ? "**" + prop.getDeuda() + "0 €** 🔴" : "0,00 € 🟢";
                        writer.printf("| %s | %s | %s | %s |\n", 
                                prop.getPiso(), prop.getPropietario(), prop.getEstadoPago(), deudaStr);
                        totalDeudaComunidad += prop.getDeuda();
                    }
                }
            }
            writer.println("\n---\n## 📊 Resumen Ejecutivo Financiero");
            writer.println("* **Fondo Reclamable Pendiente de Cobro:** " + totalDeudaComunidad + "0 €");
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo README.md: " + e.getMessage());
        }
    }

    private static void subirAGitHubAutomatizado() {
        System.out.println("🚀 Sincronizando con GitHub de forma automática...");
        
        // 📂 Usamos un directorio de trabajo local directo sin alterar rutas del sistema padre
        java.io.File carpetaProyecto = new java.io.File(".").getAbsoluteFile().getParentFile();
        
        // Buscamos Git de Windows
        String comandoGit = "git";
        java.io.File gitRutaEstandar = new java.io.File("C:\\Program Files\\Git\\cmd\\git.exe");
        if (gitRutaEstandar.exists()) {
            comandoGit = gitRutaEstandar.getAbsolutePath();
        }

        List<String[]> comandos = new ArrayList<>();
        comandos.add(new String[]{comandoGit, "init"});
        // Añadimos una copia forzada del propietarios.csv al directorio de envío por si acaso
        comandos.add(new String[]{comandoGit, "add", "README.md"});
        comandos.add(new String[]{comandoGit, "add", "propietarios.csv"});
        comandos.add(new String[]{comandoGit, "add", "."});
        comandos.add(new String[]{comandoGit, "commit", "-m", "Actualizacion de informe de cuentas de la comunidad"});
        comandos.add(new String[]{comandoGit, "branch", "-M", "main"});
        comandos.add(new String[]{comandoGit, "remote", "add", "origin", REPO_URL});
        comandos.add(new String[]{comandoGit, "push", "-u", "origin", "main"});

        for (int i = 0; i < comandos.size(); i++) {
            String[] cmd = comandos.get(i);
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(new java.io.File(".")); // 👈 Forzamos la carpeta de trabajo por defecto de NetBeans
                Process proceso = pb.start();
                proceso.waitFor(); 
            } catch (IOException | InterruptedException e) {
                System.err.println("Excepción en comando Git: " + e.getMessage());
            }
        }

        System.out.println("🚀 ¡Proceso de subida completado! Revisa la página de GitHub o tu aplicación móvil.");
    }
}