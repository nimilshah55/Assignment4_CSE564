import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.awt.Label;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * GUI panel that handles mouse events and interactions with the cities.
 * @author Nate Robinson
 */
public class Workspace extends JPanel implements MouseListener, 
        MouseMotionListener, IObserver {

    int preX, preY;
    boolean isAddingCity = false;
    private City selected = null;
    final NewCityHandler newCityHandler;
    
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
    public void setConnectionState(ConnectionMode mode) {
        selected = null;
        connectionModeState = mode;
        StatusBar.getInstance().setStatus("Connection Mode changed to: " + mode.name());
    }
    
    /**
     * Clear collection of cities and repaint.
     */
    public void reset() {
        selected = null;
        CityDatabase.getInstance().clear();
        StatusBar.getInstance().setStatus("Cities cleared.");
        repaint();
    }
    
    /**
     * Clear collection of cities and load new cities.
     * @param newCities Cities to load.
     */
    public void loadCities(City[] newCities) {
        CityDatabase.getInstance().addCities(newCities);
        StatusBar.getInstance().setStatus("New cities loaded.");
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
     * Unused.
     * @param e ActionEvent, unused
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
            City select = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
            new EditCityHandler(select);
        }
    }

    /**
     * CREATE a city if empty spot, otherwise select and move city.
     * @param e Used to get the location of the mouse
     */
    @Override
    public void mousePressed(MouseEvent e) {
        switch (actionModeState) {
            case CONNECT:
                if (selected != null) {
                    // if a city is already selected and another is sequentially
                    // selected, create a path between them.
                    City selected2 = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());
                    if (selected2 != null) {
                        preX = (int)(selected.getX() - e.getX());
                        preY = (int)(selected.getY() - e.getY());
                        CityDatabase.getInstance().addConnections(
                                Collections.singletonMap(selected, selected2));
                        StatusBar.getInstance().setStatus("Connection created between City " 
                                + selected.name + " and " + selected2.name + ".");
                        repaint();
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
                        repaint();
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
                    StatusBar.getInstance().setStatus("City " + selected.name + " selected to move.");
                    repaint();
                }
                break;
        }
    }

    /**
     * MOVE the city to location of mouse release.
     * @param e Used to get the location of the mouse
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (actionModeState == ActionMode.MOVE) {
            selected = CityDatabase.getInstance().findCityAt(e.getX(), e.getY());

            if (selected != null) {
                CityDatabase.getInstance().moveCity(selected, preX + e.getX(), preY + e.getY());
                repaint();
            }
            StatusBar.getInstance().setStatus("[MOVE] Placed city at new location: " 
                    + (preX + e.getX()) + ", " + (preY + e.getY()));
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
    
    private class EditCityHandler implements ActionListener {
        int x, y;
        final JTextField name;
        final JTextField size;
        Color squareColor;
        Color circleColor;
        JComboBox<String> type;
        JFrame popup;
        JFrame squareFrame;
        JFrame circleFrame;
        City change;
        private EditCityHandler(City change) {
            x = change.getX();
            y = change.getY();
            this.change = change;
            this.squareColor = new Color(0);
            this.circleColor = new Color(0);
            squareFrame = new JFrame();
            circleFrame = new JFrame();
            String[] selection = {
                    "cross",
                    "circle",
                    "square",
                    "cross and circle"
            };
            type = new JComboBox<String>(selection);
            this.name = new JTextField();
            this.size = new JTextField();
            name.setFont(new Font("Courier", Font.PLAIN, 16));
            size.setFont(new Font("Courier", Font.PLAIN, 16));
            popup = new JFrame();
            popup.setLayout(new GridLayout(6, 2));
            popup.setSize(400, 300);
            popup.setVisible(false);
            JColorChooser chooseSquare = new JColorChooser();
            chooseSquare.getSelectionModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    squareColor = chooseSquare.getColor();
                    squareFrame.setVisible(false);
                }
            });
            JColorChooser chooseCircle = new JColorChooser();
            chooseCircle.getSelectionModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    circleColor = chooseCircle.getColor();
                    circleFrame.setVisible(false);
                }
            });
            Button square = new Button("Color");
            square.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    squareFrame.setSize(400, 400);
                    squareFrame.add(chooseSquare);
                    squareFrame.setVisible(true);
                    squareFrame.requestFocus();
                }
            });
            Button circle = new Button("Color");
            circle.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    circleFrame.setSize(400, 400);
                    circleFrame.add(chooseCircle);
                    circleFrame.setVisible(true);
                    circleFrame.requestFocus();
                }
            });
            Button ok = new Button("ok");
            ok.addActionListener(this);
            popup.add(new Label("Name: "));
            popup.add(name);
            popup.add(new Label("Size: "));
            popup.add(size);
            popup.add(new Label("Type: "));
            popup.add(type);
            popup.add(new Label("Square Color: "));
            popup.add(square);
            popup.add(new Label("Circle Color: "));
            popup.add(circle);
            popup.add(ok);
            name.setText("");
            popup.setVisible(true);
            popup.requestFocus();
        }
        /**
         * Edit the city double clicked on
         * @param e ActionEvent event on the ok button
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            popup.setVisible(false);
            City created = new BaseCity(x, y, name.getText(), squareColor, size.getText());
            switch((String)type.getSelectedItem()) {
              case "cross":
                  CrossCity cross = new CrossCity(x, y, name.getText(), squareColor, size.getText());
                  cross.setCity(created);
                  CityDatabase.getInstance().swapInstance(change, cross);
                  break;
              case "circle":
                  CircleCity circle = new CircleCity(x, y, name.getText(), circleColor, size.getText());
                  circle.setCity(created);
                  CityDatabase.getInstance().swapInstance(change, circle);
                  break;
              case "cross and circle":
                  CrossCity cross2 = new CrossCity(x, y, name.getText(), squareColor, size.getText());
                  cross2.setCity(created);
                  CircleCity circle2 = new CircleCity(x, y, name.getText(), circleColor, size.getText());
                  circle2.setCity(cross2);
                  CityDatabase.getInstance().swapInstance(change, circle2);
                  break;
              default:
                  CityDatabase.getInstance().swapInstance(change, created);
                  break;
            }
            CityDatabase.getInstance().swapInstance(change, created);
            StatusBar.getInstance().setStatus("City " + name + " edited.");
            repaint();
        }
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
            repaint();
        }
    }
}
