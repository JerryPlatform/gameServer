package projectj.sm.gameserver.strategy;

import org.hibernate.boot.model.naming.*;

public class ImplicitNamingStrategyImpl extends ImplicitNamingStrategyJpaCompliantImpl {
    @Override
    public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
        String property = source.getAttributePath().getProperty();
        return toIdentifier(addUnderscores(property), source.getBuildingContext());
    }

    @Override
    public Identifier determinePrimaryTableName(ImplicitEntityNameSource source) {
        String entityName = source.getEntityNaming().getJpaEntityName();
        return toIdentifier(addUnderscores(entityName), source.getBuildingContext());
    }

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        // 컴포지션 PK에 대해서는 기본 따름
        if (source.getColumnNames().size() > 1)
            super.determineForeignKeyName(source);

        return toIdentifier(String.format("fk_%s_%s",
                source.getTableName().toString().replaceAll("`", ""),
                source.getColumnNames().get(0).toString().replaceAll("`", "")), source.getBuildingContext());
    }

    protected static String addUnderscores(String name) {
        final StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i=1; i<buf.length()-1; i++) {
            if (
                    Character.isLowerCase( buf.charAt(i-1) ) &&
                            Character.isUpperCase( buf.charAt(i) ) &&
                            Character.isLowerCase( buf.charAt(i+1) )
                    ) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase();
    }
}
