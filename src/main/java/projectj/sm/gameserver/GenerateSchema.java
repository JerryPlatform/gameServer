package projectj.sm.gameserver;

import com.github.fluent.hibernate.cfg.scanner.EntityScanner;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Action;
import org.hibernate.tool.schema.TargetType;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateSchema {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        Map<String, String> settings = new HashMap<>();
        settings.put(AvailableSettings.DRIVER, "org.postgresql.Driver");
        settings.put(AvailableSettings.DIALECT, PostgreSQL10Dialect.class.getName());
        settings.put(AvailableSettings.URL, "jdbc:postgresql://localhost:5432/gameserver");
        settings.put(AvailableSettings.USER, "postgres");
        settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        settings.put(AvailableSettings.SHOW_SQL, "true");
        settings.put(AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, "true");
        settings.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY, "projectj.sm.gameserver.strategy.ImplicitNamingStrategyImpl");
        MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(settings)
                .build());
        List<Class<?>> classes = EntityScanner.scanPackages("projectj.sm.gameserver.domain").result();
        classes.stream().forEach(metadata::addAnnotatedClass);
        metadata.addResource("META-INF/orm.xml");
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setHaltOnError(true);
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        String filename = "target" + File.separator + "db-schema.sql";
        File file = new File(filename);
        if (file.exists()) file.delete();
        schemaExport.setOutputFile(filename);
        schemaExport.execute(EnumSet.of(TargetType.SCRIPT), Action.BOTH, metadata.buildMetadata());
    }
}
