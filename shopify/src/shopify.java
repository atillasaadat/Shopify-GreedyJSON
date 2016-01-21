/**
 * @(#)shopify.java
 *
 * shopify application
 *
 * @Atilla Saadat
 * @version 1.00 2016/1/19
 *
 * Library imported from JSON jar found here: http://central.maven.org/maven2/org/json/json/20151123/json-20151123.jar
 * Knapsack algorithm based on: https://cs.uwaterloo.ca/~mgrzes/code/mg/Knapsack.java
 */
 
import java.util.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.*;

public class shopify {

  //---------------------------------------------------------------------------------------------------
  // JSON Parse and read funtions
  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }
  //---------------------------------------------------------------------------------------------------
  //Converts ArrayList<Integer> to int[] Array
  public static int[] convertIntegers(ArrayList<Integer> myList)
	{
    int[] arr = new int[myList.size()];
		for(int i = 0; i < myList.size(); i++) {
    		if (myList.get(i) != null) {
        		arr[i] = myList.get(i);
    		}
		}
		return arr;
	}
  //---------------------------------------------------------------------------------------------------
  // 1/0 Greedy (knapsack) algorithm for determining which items to take or leave behind
  // One per item as based on intern email
  public static boolean[] weightsToKeep(int maxWeight, ArrayList<Double> prices, ArrayList<Integer> weights){
  	
  	int[]wi = convertIntegers(weights);
  	int[]vi = new int[weights.size()];
  	boolean [] ifBuy = new boolean[weights.size()];
	Arrays.fill(ifBuy, Boolean.FALSE);
  	//Set all values as one because price is not a deciding factor in choice
    Arrays.fill(vi,1);
  	
  	int n = vi.length;
  	int W = maxWeight;
  	int[][] V = new int[n][W+1];
	boolean[][] keep = new boolean[n][W+1];
	for ( int i = 0; i < n; i++ ) {
		for ( int w=0; w <= W; w++ ) {
			keep[i][w] = false;
		}
	}
	for ( int w = 0; w <= W; w++ ) {
		if ( wi[0] <= w) {
			V[0][w] = wi[0];
			keep[0][w] = true;
		} else {
			V[0][w] = 0;
		}
	}
	for ( int i = 1; i < n; i++ ) {
		for ( int w = 0; w <= W; w++) {
			if ( wi[i] <= w && vi[i] + V[i-1][w-wi[i]] > V[i-1][w] ) {
				V[i][w] = vi[i] + V[i-1][w-wi[i]];
				keep[i][w] = true;
			} else {
				V[i][w] = V[i-1][w];
			}
		}
	}
	int K = W;
	int wsel = 0;
	int vsel = 0;

	for ( int i = n - 1 ; i >= 0; i-- ) {
		if ( keep[i][K] == true) {
			ifBuy[i]=true;
		}else{
			ifBuy[i]=false;
		}
	}
  	return ifBuy;
  }
  
  
  //---------------------------------------------------------------------------------------------------
  // Finds total price based on which weights to take or not (price indexes correspond bool array from weightsToKeep)
  public static double finalPrice (int maxWeight, ArrayList<Double> prices, ArrayList<Integer> weights){
  	boolean[] ifBuy = weightsToKeep(maxWeight,prices,weights);
    
    double totalPrice = 0;
    for (int index = 0; index < weights.size(); index++) {
    	if(ifBuy[index]){
    		totalPrice+=prices.get(index);
    	}
    }
    return totalPrice;
  }
  //---------------------------------------------------------------------------------------------------
  // Gets Price and Weights of Keyboard and Computer Variants 
  public static double findKeyCompTotalPrice(int maxWeight, String jsonLink) throws IOException, JSONException{
    JSONArray products = (readJsonFromUrl(jsonLink)).getJSONArray("products");
    ArrayList<Double> priceList = new ArrayList<Double>();
    ArrayList<Integer> weightList = new ArrayList<Integer>();
    for(int index = 0; index < products.length(); index++){
      String currentItem = products.getJSONObject(index).getString("product_type");
      if(currentItem.equals("Keyboard")||currentItem.equals("Computer")){ // Search for Keyboard or Computer
        int numOfVariants = products.getJSONObject(index).getJSONArray("variants").length();
        for(int variant = 0; variant < numOfVariants; variant++){
          priceList.add(Double.parseDouble(products.getJSONObject(index).getJSONArray("variants").getJSONObject(variant).getString("price")));
          weightList.add(products.getJSONObject(index).getJSONArray("variants").getJSONObject(variant).getInt("grams"));
        }
      }
    }
    // Return Price of all Keyboard and Computer Variants based of 1/0 Greedy Algorithm
    double x = 0;
    int y = 0;    
    for(int i = 0; i < weightList.size();i++){
    	x+=priceList.get(i);
    	y+=weightList.get(i);;
    }
    return finalPrice(maxWeight,priceList,weightList);
  }
  //---------------------------------------------------------------------------------------------------
  public static void main(String[] args) throws IOException {
    int maxWeight = 100000;
    String jsonLink = "http://shopicruit.myshopify.com/products.json";
    
    //Based on above link, all items should be taken because: all Keyboard and Computer weights < 100kg
    System.out.println(findKeyCompTotalPrice(maxWeight,jsonLink));
  }
}