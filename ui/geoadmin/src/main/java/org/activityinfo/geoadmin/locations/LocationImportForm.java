package org.activityinfo.geoadmin.locations;

import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.activityinfo.geoadmin.ColumnGuesser;
import org.activityinfo.geoadmin.ImportSource;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.AdminEntity;
import org.activityinfo.geoadmin.model.AdminLevel;

import com.google.common.collect.Maps;

/**
 * Form that allows the user to change the columns used to detect the 
 * columns
 */
public class LocationImportForm extends JPanel {

    private ImportSource source;
    private JComboBox nameCombo;
    private JComboBox codeCombo;

    private Map<AdminLevel, JComboBox> levelCombos = Maps.newHashMap();

    public LocationImportForm(ImportSource source, List<AdminLevel> levels) {
        super(new MigLayout());

        this.source = source;
        
        nameCombo = new JComboBox(source.getAttributeNames());
        nameCombo.setSelectedIndex(guessNameColumn());

        codeCombo = new JComboBox(source.getAttributeNames());

        add(new JLabel("Name Attribute"));
        add(nameCombo, "width 160!, wrap");


        add(new JLabel("Code Attribute"));
        add(codeCombo, "width 160!, wrap");

        for(AdminLevel level : levels) {
        	JComboBox levelCombo = createAdminColumnCombo();
        	
        	add(new JLabel(level.getName() + " Attribute"));
        	add(levelCombo, "width 160!, wrap");
        	
        	levelCombos.put(level, levelCombo);
        }  
    }

	private JComboBox createAdminColumnCombo() {
		String[] columns = new String[source.getAttributeCount() + 1];
		columns[0] = "--NONE--";
		for(int i=0;i!=source.getAttributeCount();++i) {
			columns[i+1] = source.getAttributeNames()[i];
		}
		return new JComboBox(columns);
	}


    private int guessNameColumn() {
        return new ColumnGuesser()
            .forPattern("[A-Za-z-' ]+")
            .favoringUniqueValues()
            .findBest(source);
    }
    
    public int getNameAttributeIndex() {
        return nameCombo.getSelectedIndex();
    }

    public int getCodeAttributeIndex() { return codeCombo.getSelectedIndex(); }

    public void guessLevelColumns(ActivityInfoClient client) {
    	for(AdminLevel level : levelCombos.keySet()) {
    		List<AdminEntity> entities = client.getAdminEntities(level);
    		setLevelAttribute(level,
    			new ColumnGuesser().forEntities(entities)
    				.findBest(source));
    		
    	}
    }
    
    public void setLevelAttribute(AdminLevel level, int attributeIndex) {
    	levelCombos.get(level).setSelectedIndex(attributeIndex+1);
    }
    
    public int getLevelAttributeIndex(AdminLevel level) {
    	return levelCombos.get(level).getSelectedIndex()-1;
    }
}
