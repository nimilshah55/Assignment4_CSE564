import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * GUI panel that handles mouse events and interactions with the cities.
 * @author Nate Robinson, Dustin Howarth, Gianni Consiglio
 */
public class Workspace extends JPanel implements MouseListener, 
        MouseMotionListener, IObserver {

    int preX, preY;
    boolean isAddingCity = false;
    private City selected = null;
    final NewCityHandler newCityHandler;
    private Strategy strategy = new GreedyTSP();
    private Thread thread = new Thread(strategy);
    
    public enum ActionMode {
        CREATE, MOVE, CONNECT
    }
    
    public enum ConnectionMode {
        TSP_GREEDY, TSP_PRO, CLUSTERS, USER_CONNECT
    }
    
    ActionMode actionModeState = ActionMode.CREATE;
    ConnectionMode connectionModeState = ConnectionMode.TSP_GREEDY;

    /**
     * Instantiates Workspace.
     */
    public Workspace() {
        this.newCityHandler = new NewCityHandler();
        // add to observables/listeners
        CityDatabase.getInstance().addObserver(this);
        addMouseMotionListener(this);
        addMouseListener(this);
    }
    
    /**
     * Set the action mode state that decides how to handle mouse events.
     * @param mode ActionMode to set state to.
     */
    public void setActionState(ActionMode mode) {
        selected = null;
        actionModeState = mode;
        StatusBar.getInstance().setStatus("Action Mode changed to: " + mode.name());
    }
    
    /**
     * Set the connection mode state that decides how to connect cities.
     * @param mode ConnectionMode to set state to.
     */
    public void setConnectionState(ConnectionMode mode) throws InterruptedException {
        selected = null;
        connectionModeState = mode;
        StatusBar.getInstance().setStatus("Connection Mode changed to: " + mode.name());
        checkForPath();
        repaint();
    }

    private void checkForPath() throws InterruptedException {
        thread.interrupt();
        if(connectionModeState == ConnectionMode.TSP_GREEDY) {
            strategy = new GreedyTSP();
            thread = new Thread(strategy);
            thread.start();
        } else if(connectionModeState == ConnectionMode.TSP_PRO) {
            strategy = new BruteForcePath();
            thread = new Thread(strategy);
            thread.start();
        } else if(connectionModeState == ConnectionMode.CLUSTERS) {
            strategy = new Cluster();
            thread = new Thread(strategy);
            thread.start();
        }
    }
    
    /**
     * Clear collection of cities and repaint.
     */
    public void reset() {
        selected = null;
        thread.interrupt();
        CityDatabase.getInstance().clear();
        StatusBar.getInstance().setStatus("Cities cleared.");
        repaint();
    }
    
    /**
     * Clear collection of cities and load new cities.
     * @param newCities Cities to load.
     */
    public void loadCities(City[] newCities) throws InterruptedException {
        thread.interrupt();
        CityDatabase.getInstance().addCities(newCities);
        StatusBar.getInstance().setStatus("New cities loaded.");
        checkForPath();
        repaint();
    }
    
    /**
     * Prompts city model to draw itself.
     * @param g Graphics to draw on.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        List<City> cities = CityDatabase.getInstance().cities;
        Map<City,City> paths = CityDatabase.getInstance().paths;
        
        Color prevColor = g.getColor();
        paintCities(g, cities);
        paintPaths(g2, paths);
        g.setColor(prevColor);
    }
    
    private void paintCities(Graphics g, List<City> cities) {
        for (City city : cities) {
            city.draw(g);
        }
    }
    
    private void paintPaths(Graphics2D g, Map<City, City> paths) {
        if (paths != null) {
            g.setColor(Color.RED);
            for (City thisCity : paths.keySet()) {
                City otherCity = paths.get(thisCity);
                g.drawLine(thisCity.center().x, thisCity.center().y, 
                        otherCity.center().x, otherCity.center().y);
            }
        }
    }

    /**
     * when double clicked it enables editing for the city.
     * @param e ActionEvent - click that occurs
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
            City select = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
            new EditCityHandler(select, this);
        }
    }

    /**
     * Create a city if empty spot, otherwise select and move city.
     * @param e Used to get the location of the mouse
     */
    @Override
    public void mousePressed(MouseEvent e) {
        switch (actionModeState) {
            case CONNECT:
                if (selected != null) {
                    // if a city is already selected and another is sequentially
                    // selected, create a path between them.
                    City newSelected = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
                    if (newSelected != null) {
                        preX = (int)(selected.getX() - e.getX());
                        preY = (int)(selected.getY() - e.getY());
                        CityDatabase.getInstance().addConnections(
                                Collections.singletonMap(selected, newSelected));
                        StatusBar.getInstance().setStatus("Connection created between City " 
                                + selected.name + " and " + newSelected.name + ".");
                    } else if (selected != null) {
                        StatusBar.getInstance().setStatus("Empty space clicked." 
                                + "City was deselected.");
                    } else {
                        StatusBar.getInstance().setStatus("No city selected. " 
                                + "Click a city to start a connection.");
                    }
                    // reset store of first city, successful or not (e.g. 
                    // empty-space click)
                    selected = null;
                } else {
                    // else select initial city to link next city to
                    selected = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
                    if (selected != null) {
                        preX = (int)(selected.getX() - e.getX());
                        preY = (int)(selected.getY() - e.getY());
                        CityDatabase.getInstance().moveCity(selected, 
                                preX + e.getX(), preY + e.getY());
                        StatusBar.getInstance().setStatus("City link started with City " 
                                + selected.name + ".");
                    }
                }
                break;
            case CREATE:
                selected = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
                if(selected == null) {
                   if (!isAddingCity) {
                       isAddingCity = true;
                       NewCityHandler handler = new NewCityHandler();
                       handler.promptAt(e.getX(),e.getY());
                   }
                }
                break;
            case MOVE:
                selected = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
                if (selected != null) {
                    preX = (int)(selected.getX() - e.getX());
                    preY = (int)(selected.getY() - e.getY());
                    CityDatabase.getInstance().moveCity(selected, 
                            preX + e.getX(), preY + e.getY());
                }
                break;
        }
    }

    /**
     * Move the city to location of mouse release.
     * @param e Used to get the location of the mouse
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (actionModeState == ActionMode.MOVE) {
            selected = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());

            if (selected != null) {
                CityDatabase.getInstance().moveCity(selected, preX + e.getX(), preY + e.getY());
                StatusBar.getInstance().setStatus("Placed city at new location: " 
                        + (preX + e.getX()) + ", " + (preY + e.getY()));
                try {
                    checkForPath();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                repaint();
            }
        }
    }

    /**
     * Unused.
     * @param e ActionEvent, unused
     */
    @Override
    public void mouseEntered(MouseEvent e) {}

    /**
     * Unused.
     * @param e ActionEvent, unused
     */
    @Override
    public void mouseExited(MouseEvent e) {}

    /**
     * If mouse is pressed down, move the selected city to new location and 
     * repaint.
     * @param e Unused
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if(actionModeState == ActionMode.MOVE && selected != null) {
            CityDatabase.getInstance().moveCity(selected, preX + e.getX(), preY + e.getY());
            try {
                checkForPath();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            repaint();
        }
    }

    /**
     * Unused.
     * @param e ActionEvent, unused
     */
    @Override
    public void mouseMoved(MouseEvent e) {}
    
    /**
     * Sets the city paths and redraw the map.
     * @param ob Paths as a map with key and value as cities
     */
    @Override
    public void update(Object ob) {
        repaint();
    }
    
    private class NewCityHandler implements ActionListener {
        int x, y;
        final JTextField pendingNameField;
        
        private NewCityHandler() {
            this.pendingNameField = new JTextField();
            pendingNameField.setFont(new Font("Courier", Font.PLAIN, 16));
            pendingNameField.setVisible(false);
            pendingNameField.addActionListener(this);
            Workspace.this.add(pendingNameField);
        }
        
        private void promptAt(int x, int y) {
            this.x = x;
            this.y = y;
            isAddingCity = true;
            pendingNameField.setText("");
            pendingNameField.setBounds(x, y, 60, 25);
            pendingNameField.setVisible(true);
            pendingNameField.requestFocus();
            
        }

        /**
         * Finish city creation and send data to generate city.
         * @param e ActionEvent, used to get entered name
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            pendingNameField.setVisible(false);
            String name = e.getActionCommand();
            CityDatabase.getInstance().createCity(x, y, name, Color.BLACK, "");
            StatusBar.getInstance().setStatus("New city " + name + " created.");
            isAddingCity = false;
            try {
                checkForPath();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            repaint();
        }
    }
}
