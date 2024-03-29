import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Decoration of a city to add a cross.
 * 
 * @author Gianni Consiglio
 */
public class CrossCity extends CityDecorator {
    private Rectangle[] squares;

    /**
     * constructor of the cross city and sets the four new squares needed to be
     * painted to the screen.
     * 
     * @param x     -position
     * @param y     -position
     * @param name  - name of the city
     * @param color - color of the squares
     * @param size  - size of the squares
     */
    public CrossCity(int x, int y, String name, Color color, String size) {
        super(x, y, name, color, size);
        int shapeSize;
        try {
            shapeSize = Integer.parseInt(size);
        } catch (Exception e) {
            shapeSize = 16;
        }
        squares = new Rectangle[4];
        squares[0] = new Rectangle(x, y + shapeSize + 4, shapeSize, shapeSize);
        squares[1] = new Rectangle(x, y - shapeSize - 4, shapeSize, shapeSize);
        squares[2] = new Rectangle(x + shapeSize + 4, y, shapeSize, shapeSize);
        squares[3] = new Rectangle(x - shapeSize - 4, y, shapeSize, shapeSize);
    }

    /**
     * draw method needed to be implemented to draw the four new squares.
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawRect(squares[0].x, squares[0].y, squares[0].height, squares[0].width);
        g.setColor(this.color);
        g.fillRect(squares[0].x + 1, squares[0].y + 1, squares[0].height - 1, squares[0].width - 1);
        g.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        g.drawRect(squares[1].x, squares[1].y, squares[1].height, squares[1].width);
        g.setColor(this.color);
        g.fillRect(squares[1].x + 1, squares[1].y + 1, squares[1].height - 1, squares[1].width - 1);
        g.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        g.drawRect(squares[2].x, squares[2].y, squares[2].height, squares[2].width);
        g.setColor(this.color);
        g.fillRect(squares[2].x + 1, squares[2].y + 1, squares[2].height - 1, squares[2].width - 1);
        g.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        g.drawRect(squares[3].x, squares[3].y, squares[3].height, squares[3].width);
        g.setColor(this.color);
        g.fillRect(squares[3].x + 1, squares[3].y + 1, squares[3].height - 1, squares[3].width - 1);
        g.setColor(Color.BLACK);
        super.draw(g);
    }

    /**
     * move - moves the decorated city as well as the four new squares.
     */
    @Override
    public void move(int x, int y) {
        squares[0].x = x;
        squares[0].y = y + squares[0].height + 4;
        squares[1].x = x;
        squares[1].y = y - squares[0].height - 4;
        squares[2].x = x + squares[0].height + 4;
        squares[2].y = y;
        squares[3].x = x - squares[0].height - 4;
        squares[3].y = y;
        super.move(x, y);
    }

    /**
     * contains checks the bounds of the four new squares as well as the decorated
     * city.
     */
    @Override
    public boolean contains(int x, int y) {
        boolean one, two, three, four, center;
        one = squares[0].contains(x, y);
        two = squares[1].contains(x, y);
        three = squares[2].contains(x, y);
        four = squares[3].contains(x, y);
        center = super.contains(x, y);
        if (one || two || three || four || center) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * gets x location of the base city.
     * 
     * @return x -positon
     */
    @Override
    public int getX() {
        return super.getX();
    }

    /**
     * gets the y location of the base city.
     * 
     * @return y - position
     */
    @Override
    public int getY() {
        return super.getY();
    }

    /**
     * gets the center of the base city.
     */
    @Override
    public Point center() {
        return super.center();
    }
}
