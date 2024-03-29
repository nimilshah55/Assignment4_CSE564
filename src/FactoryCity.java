import java.util.ArrayList;
import java.awt.Color;

/**
 * Factory city decides which class to instantiate.
 * @author Nimil Shah
 */
public class FactoryCity implements Factory {
	private static FactoryCity fc;

	private FactoryCity() {}
	
/**
 * Get City Instance.
 * @return city instance
 */	
	public static FactoryCity getFC() {
		if (fc == null) {
			fc = new FactoryCity();
		}
		return fc;
	}
	
	/**
 * Creates city.
 * @param Name of city.
 * @param City x.
 * @param City y.
 * @param Colour of city.
 * @param Type of city.
 * @return city instance
 */	
	@Override
	public City createCity(String name, int cityX, int cityY, String size, Color colour,String type) {
 		if (type.equals("Square")) {
 			return new BaseCity(cityX, cityY, name, colour, size);
 		} else if(type.equals("Circle")) {
 			return new CircleCity(cityX, cityY, name, colour, size);
 		}else if(type.equals("Cross")) {
 			return new CrossCity(cityX, cityY, name, colour, size);
 		}
		 return null;
	}
}
