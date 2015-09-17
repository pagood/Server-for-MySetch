package util;

public class DistanceUtil {
	private final static double EARTH_RADIUS = 6378.137;
	public static double getDistance(double lat1,double lon1,double lat2,double lon2){
		double radLat1 = lat1 * Math.PI/180.0;
		double radLat2 = lat2 * Math.PI/180.0;
		double a = radLat1 - radLat2;
		double b = lon1 * Math.PI/180.0 - lon2 * Math.PI/180.0;
		
		
		double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
			    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
		
		distance = distance * EARTH_RADIUS;
		distance = Math.round(distance * 10000) / 10000;
		
		return distance;
	}
//	public static void main(String[] args){
//		System.out.println(getDistance(39.036032,117.668345,39.035495,117.668783));
//	}
}
