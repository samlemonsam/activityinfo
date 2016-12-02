package org.activityinfo.geoadmin;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.geoadmin.model.Country;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Swing Application that provides a user interface for managing ActivityInfo's
 * geographic reference databases.
 *
 */
public class GeoAdmin extends JFrame {
    
    static {
        LocaleProxy.initialize();
    }

    static {
        LocaleProxy.initialize();
    }

    private static final String OPEN_TABS = "open_tabs";
    private static final String ACTIVE_TAB = "active_tab";

    private ActivityInfoClient client;
    private JTabbedPane tabPane;

    private List<Country> countries;

    private List<CountryTab> countryTabs = Lists.newArrayList();
    private Map<Integer, CountryTab> countryMap = Maps.newHashMap();

    private Preferences prefs = Preferences.userNodeForPackage(GeoAdmin.class);

    public GeoAdmin(ActivityInfoClient client) {
        this.client = client;

        setTitle("ActivityInfo Geo Administrator");
        setSize(540, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabPane = new JTabbedPane();

        getContentPane().add(tabPane, BorderLayout.CENTER);

        this.countries = client.getCountries();

        createTree();

        loadOpenTabs();
        loadActiveTab();

        tabPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                saveActiveTab();
            }
        });
    }

    private void saveActiveTab() {
        prefs.put(ACTIVE_TAB, tabPane.getTitleAt(tabPane.getSelectedIndex()));
        flushPrefs();
    }

    private void loadActiveTab() {
        String activeTab = prefs.get(ACTIVE_TAB, "");
        for (int i = 0; i != tabPane.getTabCount(); ++i) {
            if (tabPane.getTitleAt(i).equals(activeTab)) {
                tabPane.setSelectedComponent(tabPane.getComponentAt(i));
                return;
            }
        }
    }

    private void loadOpenTabs() {
        try {
            String[] openIds = prefs.get(OPEN_TABS, "").split(",");
            for (String openId : openIds) {
                int countryId = Integer.parseInt(openId);
                for (Country country : countries) {
                    if (country.getId() == countryId) {
                        showCountryWindow(country);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void createTree() {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Countries");

        for (Country country : countries) {
            node.add(new DefaultMutableTreeNode(country));
        }

        final JTree tree = new JTree(node);
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if (selPath != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        Country country = (Country) node.getUserObject();
                        showCountryWindow(country);
                    }
                }
            }
        });

        tabPane.addTab("Countries", new JScrollPane(tree));
    }

    protected void showCountryWindow(Country country) {
        if (countryMap.containsKey(country)) {
            tabPane.setSelectedComponent(countryMap.get(country.getId()));
        } else {
            CountryTab tab = new CountryTab(client, country);
            tabPane.addTab(country.getName(), tab);
            tabPane.setSelectedComponent(tab);
            countryMap.put(country.getId(), tab);
            countryTabs.add(tab);
            saveOpenTabs();
        }
    }

    private void saveOpenTabs() {
        prefs.put(OPEN_TABS, Joiner.on(",").join(countryMap.keySet()));
        flushPrefs();
    }

    private void flushPrefs() {
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        final String endpoint;
        if(args.length == 1) {
            endpoint = args[0];
        } else {
            endpoint = "https://www.activityinfo.org/resources";
        }

        if(!tryLoadCredentialsFromHomeDir(endpoint)) {

            new PasswordForm().show(new PasswordForm.Callback() {
                @Override
                public void ok(String username, String password) {

                    onAuthenticated(username, password, endpoint);
                }
            });
        }
    }

    private static boolean tryLoadCredentialsFromHomeDir(String endpoint) {
        File homeDir = new File(System.getProperty("user.home"));
        File credentialsFile = new File(homeDir, ".geoadmin.credentials");

        if(credentialsFile.exists()) {
            Properties credentials = new Properties();
            try {
                try( FileInputStream in = new FileInputStream(credentialsFile)) {
                    credentials.load(in);
                    String accountEmail = credentials.getProperty("accountEmail");
                    String password = credentials.getProperty("password");
                    if(!Strings.isNullOrEmpty(accountEmail) &&
                       !Strings.isNullOrEmpty(password)) {
                        onAuthenticated(accountEmail, password, endpoint);
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void onAuthenticated(String username, String password, String endpoint) {
        final ActivityInfoClient client = new ActivityInfoClient(endpoint, username, password);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GeoAdmin ex = new GeoAdmin(client);
                ex.setVisible(true);
            }
        });
    }

}